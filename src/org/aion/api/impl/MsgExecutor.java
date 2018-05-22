/*******************************************************************************
 * Copyright (c) 2017-2018 Aion foundation.
 *
 *     This file is part of the aion network project.
 *
 *     The aion network project is free software: you can redistribute it
 *     and/or modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation, either version 3 of
 *     the License, or any later version.
 *
 *     The aion network project is distributed in the hope that it will
 *     be useful, but WITHOUT ANY WARRANTY; without even the implied
 *     warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *     See the GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the aion network project source files.
 *     If not, see <https://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Aion foundation.
 *
 ******************************************************************************/

package org.aion.api.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import org.aion.api.IUtils;
import org.aion.api.cfg.CfgApi;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.impl.internal.LRUTimeMap;
import org.aion.api.impl.internal.Message;
import org.aion.api.log.AionLoggerFactory;
import org.aion.api.log.LogEnum;
import org.aion.api.type.ContractEvent;
import org.aion.api.type.Event;
import org.aion.api.type.MsgRsp;
import org.aion.base.type.Address;
import org.aion.base.type.Hash256;
import org.aion.base.util.ByteArrayWrapper;
import org.aion.base.util.ByteUtil;
import org.aion.base.util.NativeLoader;
import org.apache.commons.collections4.map.LRUMap;
import org.slf4j.Logger;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Socket;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

/**
 * Created by Jay Tseng on 15/03/17.
 */
public class MsgExecutor implements Runnable {
    private final int qSize = 50_000;
    private final int maxPenddingTx = 10_000;
    private final Logger LOGGER = AionLoggerFactory.getLogger(LogEnum.EXE.name());
    private final String WK_BIND_ADDR = "inproc://apiWkTh";
    private final String CB_BIND_ADDR = "inproc://apicbTh";
    private final String HB_BIND_ADDR = "inproc://apihbTh";

    private final static int HB_TOLERANCE = 3;
    private final static int HB_POLL_MS = 500;
    private final static int RECVTIMEOUT = 3000;
    // = If api is unresponsive for more than ~2 seconds, kill the api.
    // TODO: implement auto-reconnect on kernel failure

    //TODO: update kernel api privilege then remove this flag
    private final boolean PRIVILEGE = true; // temp flag
    private Socket nbSocket;
    AtomicBoolean isInitialized = new AtomicBoolean(false);
    private Map<ByteArrayWrapper, MsgRsp> hashMap;
    private Map<String, BlockingQueue<Event>> eventMap;
    private AtomicInteger penddingTx = new AtomicInteger(0);
    private BlockingQueue<MsgReq> queue = new LinkedBlockingQueue<>(qSize);
    private int ver;
    private String url;
    private volatile boolean running = true;
    private Thread msgThread;
    private int workers = 1;
    private String addrBindNumber;
    private Map<String, Boolean> privilege;

    static {
        NativeLoader.loadLibrary("zmq");
    }

    MsgExecutor(int protocolVer, String url) {
        this.ver = protocolVer;
        this.url = url;
        this.addrBindNumber = Arrays.toString(ApiUtils.genHash(8));
        this.hashMap = Collections.synchronizedMap(new LRUTimeMap<>(maxPenddingTx << 1));
        this.eventMap = Collections.synchronizedMap(new LRUMap<>(100));
        initPriviege();
    }

    MsgExecutor(int protocolVer, String url, int timeout) {
        this.ver = protocolVer;
        this.url = url;
        this.addrBindNumber = Arrays.toString(ApiUtils.genHash(8));
        this.hashMap = Collections.synchronizedMap(new LRUTimeMap<>(maxPenddingTx << 1, timeout));
        this.eventMap = Collections.synchronizedMap(new LRUMap<>(100));
        initPriviege();
    }

    private void initPriviege() {
        this.privilege = Collections.synchronizedMap(new HashMap<>());
        this.privilege.put("Account", PRIVILEGE);
        this.privilege.put("Transaction", PRIVILEGE);
        this.privilege.put("Contract", PRIVILEGE);
        this.privilege.put("Net", PRIVILEGE);
        this.privilege.put("Chain", PRIVILEGE);
        this.privilege.put("Wallet", PRIVILEGE);
        this.privilege.put("Admin", PRIVILEGE);
    }

    Map<String, Boolean> getPrivilege() {
        return this.privilege;
    }

    private void update(ByteArrayWrapper msgHash, ByteArrayWrapper rsp, int status) throws CloneNotSupportedException {

        if (msgHash == null || rsp == null) {
            throw new NullPointerException("msgHash#" + String.valueOf(msgHash) + " rsp#" + String.valueOf(rsp));
        }

        MsgRsp newRsp = this.hashMap.get(msgHash);
        if (newRsp == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[update] Can not find the msgHash in the msgMap.");
            }
            return;
        }

        MsgRsp msgRsp = MsgRsp.copy(newRsp);
        boolean update = false;

        if (rsp.getData().length > 0) {
            try {
                if (status != 101) {
                    if (rsp.getData()[0] > 0 && rsp.getData().length > rsp.getData()[0]) {
                        msgRsp.setError(new String(rsp.getData(), 1, rsp.getData()[0]));
                    }

                    if (status == 105) {
                        msgRsp.setTxResult(ByteArrayWrapper.wrap(ByteBuffer.wrap(rsp.getData(),rsp.getData()[0]+1, rsp.getData().length-(rsp.getData()[0]+1)).array()));
                    }
                } else {
                    // if response message = 68, that is a contract deploy
                    if (rsp.getData().length == 68) {
                        Message.rsp_contractDeploy result = Message.rsp_contractDeploy.parseFrom(rsp.getData());
                        msgRsp.setTxHash(Hash256.wrap(result.getTxHash().toByteArray()));
                        msgRsp.setTxDeploy(ByteArrayWrapper.wrap(rsp.getData()));
                    } else {
                        msgRsp.setTxHash(Hash256.wrap(Message.rsp_sendTransaction.parseFrom(rsp.getData()).getTxHash().toByteArray()));
                    }
                }

                update = true;
            } catch (InvalidProtocolBufferException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[update] ProtocolBuffer exception. [{}]", e.getMessage());
                }
            }
        }

        if ((byte) status > msgRsp.getStatus() || status < 1) {
            msgRsp.setPreStatus(msgRsp.getStatus());
            msgRsp.setStatus((byte) status);
            this.hashMap.replace(msgHash, msgRsp);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[update] Msg: [{}] Msgbody: [{}]", msgHash.toString(),
                        msgRsp.getTxHash().toString());
            }
        } else if (update) {
            this.hashMap.replace(msgHash, msgRsp);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[update] late update Msg: [{}] Msgbody: [{}]", msgHash.toString(),
                        msgRsp.getTxHash().toString());
            }
        }

    }

    private synchronized void process(final byte[] rsp) {
        if (rsp == null) {
            return;
        }

        if (rsp.length < ApiUtils.RSP_HEADER_LEN) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[process] {}", ErrId.getErrString(-317L));
            }
            return;
        }

        if ((int) rsp[0] < this.ver) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[process] {}", ErrId.getErrString(-318L));
            }
            return;
        }

        if ((short) rsp[1] >= Message.Retcode.r_NA_VALUE || (short) rsp[1] <= Message.Retcode.r_fail_unknown_VALUE) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[process] {}", ErrId.getErrString(-319L));
            }
            return;
        }

        if ((short) rsp[1] == Message.Retcode.r_tx_eventCb_VALUE) {
            updateEvent(ApiUtils.parseBody(rsp));
        } else if ((short) rsp[1] == Message.Retcode.r_privilegeReturn_VALUE) {
            updatePrivilege(ApiUtils.parseBody(rsp));
        } else {
            try {
                update(ApiUtils.parseHash(rsp), ApiUtils.parseBody(rsp), (int) rsp[1]);
            } catch (CloneNotSupportedException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[process] update data get a CloneNotSupportedException exception");
                }
            }
        }
    }

    private void updatePrivilege(ByteArrayWrapper _p) {
        try {
            Message.rsp_userPrivilege up = Message.rsp_userPrivilege.parseFrom(_p.getData());

            List<String> privileges = up.getPrivilegeList();
            if (Optional.ofNullable(privileges).isPresent()) {
                privileges.forEach(p -> {
                    if (Optional.ofNullable(this.privilege.get(p)).isPresent()) {
                        this.privilege.put(p, true);
                    }
                });
            }
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[updatePrivilege] {}", ErrId.getErrString(-104L));
            }
        }
    }

    private void updateEvent(ByteArrayWrapper data) {
        if (Optional.ofNullable(data).isPresent()) {

            Message.rsp_EventCtCallback evt;
            try {
                evt = Message.rsp_EventCtCallback.parseFrom(data.getData());
            } catch (InvalidProtocolBufferException exception) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[updateEvent] {}", ErrId.getErrString(-104L));
                }
                return;
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[updateEvent] {}", IUtils.bytes2Hex(data.getData()));
            }

            for (Message.t_EventCt cte : evt.getEcList()) {
                ContractEvent.ContractEventBuilder builder = new ContractEvent.ContractEventBuilder()
                        .address(Address.wrap(cte.getAddress().toByteArray()))
                        .blockHash(Hash256.wrap(cte.getBlockHash().toByteArray()))
                        .blockNumber(cte.getBlockNumber())
                        .data(ByteArrayWrapper.wrap(cte.getData().toByteArray()))
                        .eventName(cte.getEventName())
                        .logIndex(cte.getLogIndex())
                        .removed(cte.getRemoved())
                        .txHash(Hash256.wrap(cte.getTxHash().toByteArray()))
                        .txIndex(cte.getTxIndex());

                ContractEvent d = builder.createContractEvent();

                if (this.eventMap.get(d.getEventName()) != null) {
                    BlockingQueue<Event> em = this.eventMap.get(d.getEventName());
                    if (em != null) {
                        try {
                            em.put(d);
                        } catch (InterruptedException e) {
                            if (LOGGER.isErrorEnabled()) {
                                LOGGER.error("[updateEvent] {}", ErrId.getErrString(-137L));
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void run() {

        Socket feSocket = null;
        try {
            Context ctx = ZMQ.context(1);
            feSocket = ctx.socket(ZMQ.DEALER);

            feSocket.connect(this.url);

            byte[] req = ApiUtils.toReqHeader(this.ver, Message.Servs.s_hb, Message.Funcs.f_NA);

            if (!feSocket.send(req, ZMQ.DONTWAIT)) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[run] Send hb msg failed: [{}]", IUtils.bytes2Hex(req));
                }
                return;
            }

            byte[] rsp = feSocket.recv(ZMQ.PAIR);

            if (rsp == null) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("[run] Connection failed.");
                }
                return;
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[run] Hb msg: [{}]", IUtils.bytes2Hex(rsp));
            }

            if (checkNotHbRspMsg(rsp)) {
                return;
            }

            // Check user api privilege
            if (!PRIVILEGE) {
                byte[] header = ApiUtils
                        .toReqHeader(this.ver, Message.Servs.s_privilege, Message.Funcs.f_userPrivilege);

                String user = CfgApi.inst().getConnect().getUser();
                String pw = CfgApi.inst().getConnect().getPassword();

                Message.req_userPrivilege reqBody = Message.req_userPrivilege.newBuilder().setUsername(user)
                        .setPassword(pw).build();

                byte[] body = reqBody.toByteArray();

                req = ByteUtil.merge(header, body);

                if (!feSocket.send(req, ZMQ.DONTWAIT)) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("[run] check api privilege failed: [{}]", IUtils.bytes2Hex(req));
                    }
                    return;
                }

                rsp = feSocket.recv(ZMQ.PAIR);
                process(rsp);
            }

            Socket beSocket = ctx.socket(ZMQ.DEALER);
            beSocket.bind(WK_BIND_ADDR + addrBindNumber);

            Socket cbSocket = ctx.socket(ZMQ.DEALER);
            cbSocket.bind(CB_BIND_ADDR + addrBindNumber);

            Socket nbDealer = ctx.socket(ZMQ.DEALER);
            String NB_BIND_ADDR = "inproc://apinbTh";
            nbDealer.bind(NB_BIND_ADDR + addrBindNumber);

            Socket hbDealer = ctx.socket(ZMQ.DEALER);
            hbDealer.bind(HB_BIND_ADDR + addrBindNumber);

            nbSocket = ctx.socket(ZMQ.DEALER);
            nbSocket.setReceiveTimeOut(RECVTIMEOUT);
            nbSocket.connect(NB_BIND_ADDR + addrBindNumber);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[run] Worker connected!");
            }

            int thNum = workers + 2;
            if (thNum < 3) {
                thNum = 3;
            }

            ExecutorService es = Executors.newFixedThreadPool(thNum);
            for (int i = 0 ; i<thNum ; i++) {
                if (i == 0) {
                    es.execute(() -> heartBeatRun(ctx));
                } else if (i == 1) {
                    es.execute(() -> callbackRun(ctx));
                } else {
                    es.execute(() -> workerRun(ctx));
                }
            }

            proxy(feSocket, beSocket, cbSocket, nbDealer, hbDealer);

            // Shutdown ZmqSocket
            hbDealer.close();
            nbDealer.close();
            cbSocket.close();
            beSocket.close();
            feSocket.close();
            nbSocket.close();

            es.shutdown();

        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[run] Exception [{}].", e.getMessage());
            }
            if (feSocket != null) {
                try {
                    feSocket.disconnect(this.url);
                } catch (Exception f) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("[run] feSocket.disconnect failed, probably due to forceful context close [{}].", f.getMessage());
                    }
                }

            }
        } finally {
            this.isInitialized.set(false);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("[run] Socket disconnected!");
            }
        }


    }

    private void heartBeatRun(Context ctx) {
        Socket hbWorker = ctx.socket(ZMQ.DEALER);
        hbWorker.connect(HB_BIND_ADDR + addrBindNumber);
        hbWorker.setReceiveTimeOut(1000);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[heartBeatRun] hbWorker connected!");
        }

        int hbTolerance = HB_TOLERANCE;
        byte[] hbMsg = ApiUtils.toReqHeader(this.ver, Message.Servs.s_hb, Message.Funcs.f_NA);

        while ( this.running && hbTolerance > 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[heartBeatRun] hbWorker sent!");
            }
            if (!hbWorker.send(hbMsg, ZMQ.DONTWAIT)) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[heartBeatRun] Worker send heartbeat msg failed.");
                }
                continue;
            }

            byte[] rsp = hbWorker.recv(ZMQ.PAIR);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[heartBeatRun] hbWorker recv msg: [{}]", (rsp != null ? IUtils.bytes2Hex(rsp) : "null"));
            }

            if (checkNotHbRspMsg(rsp)) {
                hbTolerance--;
            } else {
                hbTolerance = HB_TOLERANCE;
            }

            try {
                Thread.sleep(HB_POLL_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (this.running) {
            LOGGER.warn("Heartbeat Timeout, disconnect the connection!");
            terminate();
        } else {
            LOGGER.info("Heartbeat worker closing!");
        }

        hbWorker.close();
        LOGGER.info("Heartbeat worker closed!");
    }

    private void callbackRun(Context ctx) {
        Socket cbWorker = ctx.socket(ZMQ.DEALER);
        cbWorker.setReceiveTimeOut(RECVTIMEOUT);
        cbWorker.connect(CB_BIND_ADDR + addrBindNumber);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[callbackRun] CbWorker connected!");
        }

        while (true) {
            byte[] rsp = cbWorker.recv(ZMQ.PAIR);
            if (this.running) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("[callbackRun] CbWorker recv msg: [{}]", (rsp != null ? IUtils.bytes2Hex(rsp) : "= null"));
                }
                process(rsp);
            } else {
                break;
            }
        }

        LOGGER.info("Callback worker closing!");
        cbWorker.close();
        LOGGER.info("Callback worker closed!");
    }

    private void workerRun(Context ctx) {
        Socket worker = ctx.socket(ZMQ.DEALER);
        worker.connect(WK_BIND_ADDR + addrBindNumber);
        worker.setReceiveTimeOut(RECVTIMEOUT);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[workerRun] Worker connected!");
        }

        while (true) {
            MsgReq msg = null;
            try {
                msg = queue.poll(RECVTIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!this.running) {
                break;
            }

            if (msg != null && msg.req != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[workerRun] Poll q: [{}]", IUtils.bytes2Hex(msg.hash));
                }

                if (!worker.send(msg.req, ZMQ.PAIR)) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("[workerRun] Worker send msg failed. Msg: [{}]", IUtils.bytes2Hex(msg.req));
                    }
                    continue;
                }

                byte[] rsp = worker.recv(ZMQ.PAIR);
                if (this.running) {
                    if (rsp == null) {
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error("[workerRun] Worker recv msg: [null]");
                        }
                        return;
                    }

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("[workerRun] Worker recv msg: [{}]", IUtils.bytes2Hex(rsp));
                    }
                    process(rsp);
                } else {
                    break;
                }
            }
        }

        LOGGER.info("worker closing!");
        worker.close();
        LOGGER.info("worker closed!");
    }

    private void proxy(Socket feSocket, Socket beSocket, Socket cbSocket, Socket nbDealer, Socket hbDealer) {
        PollItem[] items = new PollItem[4];
        items[0] = new PollItem(feSocket, ZMQ.Poller.POLLIN);
        items[1] = new PollItem(beSocket, ZMQ.Poller.POLLIN);
        items[2] = new PollItem(nbDealer, ZMQ.Poller.POLLIN);
        items[3] = new PollItem(hbDealer, ZMQ.Poller.POLLIN);

        try {
            this.isInitialized.set(true);

            while (this.running) {
                //  Wait while there are either requests or replies to process.
                int rc = ZMQ.poll(items, 3000);
                if (rc < 1) {
                    if ( this.running && LOGGER.isDebugEnabled()) {
                        LOGGER.debug("ZMQ.poll error rc:{}", rc);
                    }
                    continue;
                }

                //  Process a reply.
                if (items[0].isReadable()) {
                    while (true) {
                        if (!msgHandle(feSocket, beSocket, cbSocket, nbDealer, hbDealer)) {
                            throw new Exception("ZMQ items[0] handle abnormal!");
                        }
                        break;
                    }
                }
                //  Process a request.
                if (items[1].isReadable()) {
                    while (true) {
                        if (invalidMsgHandle(beSocket, feSocket)) {
                            throw new Exception("ZMQ items[1] handle abnormal!");
                        }
                        break;
                    }
                }

                //  Process a request.
                if (items[2].isReadable()) {
                    while (true) {
                        if (invalidMsgHandle(nbDealer, feSocket)) {
                            throw new Exception("ZMQ items[2] handle abnormal!");
                        }
                        break;
                    }
                }

                if (items[3].isReadable()) {
                    while (true) {
                        if (invalidMsgHandle(hbDealer, feSocket)) {
                            throw new Exception("ZMQ items[3] handle abnormal!");
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[proxy] Exception: [{}]", e.getMessage());
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[proxy] Socket proxy exit!");
        }
    }

    private boolean msgHandle(Socket receiver, Socket sender, Socket sender2, Socket sender3, Socket sender4) {

        byte[] msg = receiver.recv(ZMQ.PAIR);
        if (msg == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[msgHandle] {}", ErrId.getErrString(-322L));
            }
            return false;
        }

        if (msg.length < ApiUtils.RSP_HEADER_NOHASH_LEN) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[msgHandle] {}", ErrId.getErrString(-321L));
            }
            return false;
        }

        if (msg[1] > Message.Retcode.r_tx_Recved_VALUE) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[msgHandle]" + " forward to txSender.");
            }

            if (!sender2.send(msg, ZMQ.PAIR)) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[msgHandle] txSender{}", ErrId.getErrString(-323L));
                }
                return false;
            }

        } else if (msg[1] == Message.Retcode.r_heartbeatReturn_VALUE) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[msgHandle] forward to hbDealer.");
            }
            if (!sender4.send(msg, ZMQ.PAIR)) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[msgHandle] hbDealer{}", ErrId.getErrString(-323L));
                }
                return false;
            }
        } else {
            if (msg[2] == 0) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[msgHandle] forward to nonBlockSender.");
                }
                if (!sender3.send(msg, ZMQ.PAIR)) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("[msgHandle] nonBlockSender{}", ErrId.getErrString(-323L));
                    }
                    return false;
                }
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[msgHandle] forward to normalSender.");
                }
                if (!sender.send(msg, ZMQ.PAIR)) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("[msgHandle] regularSender{}", ErrId.getErrString(-323L));
                    }
                    return false;
                }
            }
        }

        return true;
    }

    private boolean invalidMsgHandle(Socket receiver, Socket sender) {

        byte[] msg = receiver.recv(ZMQ.PAIR);
        if (msg == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[invalidMsgHandle] {}", ErrId.getErrString(-322L));
            }
            return true;
        }

        if (!sender.send(msg, ZMQ.DONTWAIT)) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[invalidMsgHandle] {}", ErrId.getErrString(-323L));
            }
            return true;
        }

        return false;
    }

    private boolean checkNotHbRspMsg(byte[] rsp) {
        if (rsp == null || rsp.length < ApiUtils.RSP_HEADER_NOHASH_LEN) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[checkHbRspMsg] {}", ErrId.getErrString(-321L));
            }
            return true;
        }

        if (rsp[0] != this.ver) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[checkHbRspMsg] {}", ErrId.getErrString(-318L));
            }
            return true;
        }

        if (rsp[1] != Message.Retcode.r_heartbeatReturn_VALUE) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[checkHbRspMsg] {}", ErrId.getErrString(-319L));
            }
            return true;
        }

        if (rsp[2] != 0) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[checkHbRspMsg] {}", ErrId.getErrString(-320L));
            }
            return true;
        }

        return false;
    }

    Future<MsgRsp> aSyncSend(byte[] hash, byte[] req) {
        new CompletableFuture();
        return CompletableFuture.supplyAsync(() -> {
            int code = this.put(hash, req);
            if (1 == code) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[aSyncSend] Req msg put: [{}] req: [{}]", IUtils.bytes2Hex(hash), IUtils.bytes2Hex(req));
                }
                MsgRsp msgRsp = this.getStatus(ByteArrayWrapper.wrap(hash));
                long start = System.currentTimeMillis();
                boolean timeout = false;
                while (msgRsp != null && !this.endState(msgRsp.getStatus())) {

                    if (System.currentTimeMillis() - start > 180_000) {
                        timeout = true;
                        break;
                    }

                    msgRsp = this.getStatus(ByteArrayWrapper.wrap(hash));
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error("[aSyncSend] {} Exception: [{}]", ErrId.getErrString(50L), e.getMessage());
                        }
                        return new MsgRsp(50, null);
                    }
                }

                if (timeout) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("[aSyncSend] Transaction timeout");
                    }
                    return new MsgRsp(51, null);
                }

                return msgRsp;
            } else {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[aSyncSend]" + ErrId.getErrString(53L));
                }
                return new MsgRsp(53, null);
            }
        });
    }

    synchronized MsgRsp getStatus(ByteArrayWrapper msgHash) {
        if (msgHash == null || msgHash.getData().length != ApiUtils.MSG_HASH_LEN) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getStatus] {}", ErrId.getErrString(57L));
            }
            return new MsgRsp(57, null);
        }

        MsgRsp msgStatus = null;
        MsgRsp in = this.hashMap.get(msgHash);

        if (in == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getStatus] {}", ErrId.getErrString(54L));
            }
            return new MsgRsp(54, null);
        } else {
            try {
                msgStatus = MsgRsp.copy(in);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        if (msgStatus != null && !clearTx(msgHash.getData(), msgStatus.getStatus())) {
            return new MsgRsp(56, null);
        }

        return msgStatus;
    }

    private boolean endState(int status) {
        return status == Message.Retcode.r_tx_Included_VALUE || status == Message.Retcode.r_tx_Dropped_VALUE
                || status <= Message.Retcode.r_wallet_nullcb_VALUE;
    }

    public synchronized int put(byte[] hash, byte[] payload) {

        if (this.penddingTx.get() >= this.maxPenddingTx) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[put] The pending tx reached the tx pendding limit!");
            }
            return Message.Retcode.r_fail_hit_pending_tx_limit_VALUE;
        }

        try {
            this.hashMap.put(ByteArrayWrapper.wrap(hash), new MsgRsp(Message.Retcode.r_tx_Init_VALUE, ByteArrayWrapper.wrap(hash)));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[put] Put tx into hashMap [{}]", IUtils.bytes2Hex(hash));
        }

        try {
            this.queue.put(new MsgReq(hash, payload));
            this.penddingTx.incrementAndGet();
        } catch (InterruptedException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[put] Transaction put in queue exception. {}", e.getMessage());
            }
            return Message.Retcode.r_fail_txqueue_exception_VALUE;
        }

        return 1;
    }

    private boolean clearTx(byte[] msgHash, int status) {
        if (msgHash == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[clearTx] Null msgHash.");
            }
            return false;
        }

        if (this.endState(status)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[clearTx] MsgHash: [{}]", IUtils.bytes2Hex(msgHash));
            }
            this.penddingTx.decrementAndGet();
        }

        return true;
    }

    public MsgRsp send(byte[] hash, byte[] req) {
        int code = this.put(hash, req);
        if (code == 1) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[send] Reqmsg hash: [{}] msg: [{}]", IUtils.bytes2Hex(hash), IUtils.bytes2Hex(req));
            }
            return this.getStatus(ByteArrayWrapper.wrap(hash));
        } else {
            if (code != -15)
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[send] {}", ErrId.getErrString(code));
                }

            return new MsgRsp(code, null);
        }
    }

    void terminate() {
        this.running = false;

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (this.msgThread != null) {
            this.msgThread.interrupt();
        }

        if (this.hashMap != null) {
            hashMap.clear();
        }

        this.clear();
    }

    public void clear() {
        this.hashMap.clear();
        this.queue.clear();
        this.penddingTx.set(0);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[clear] Transaction cleared!");
        }
    }

    public void start(int workers) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[start] Run MsgExecutor, start [{}] worker threads", workers);
        }

        this.workers = workers;
        this.msgThread = new Thread(this, "msg-exec");
        this.msgThread.start();
    }

    public List<Event> getEvents(List<String> evtNames) {
        List<Event> result = new ArrayList<>();
        if (Optional.ofNullable(evtNames).isPresent()) {
            evtNames.forEach((ev) -> {
                Optional<BlockingQueue<Event>> evtQ = Optional.ofNullable(this.eventMap.get(ev));
                if (evtQ.isPresent()) {
                    int number = evtQ.get().size();
                    for (int i = 0; i < number; i++) {
                        try {
                            result.add(evtQ.get().take());
                        } catch (InterruptedException e) {
                            if (LOGGER.isErrorEnabled()) {
                                LOGGER.error("[getEvents] Get event exception: " + e.toString());
                            }
                            break;
                        }
                    }
                }
            });
        }

        return result;
    }

    public void setEvent(String e) {

        if (!Optional.ofNullable(this.eventMap.get(e)).isPresent()) {
            LinkedBlockingQueue<Event> q = new LinkedBlockingQueue<>();
            this.eventMap.put(e, q);
        }
    }

    void removeEvent(String e) {
        this.eventMap.remove(e);
    }

    void removeAllEvents() {
        this.eventMap.clear();
    }

    Socket getNbSocket() {
        return nbSocket;
    }

    public static class MsgReq {
        public byte[] hash;
        byte[] req;

        MsgReq(byte[] hash, byte[] payload) {
            this.hash = hash;
            this.req = payload;
        }
    }
}
