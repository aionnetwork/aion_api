/*
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
 */

package org.aion.api.impl;

import org.aion.api.IUtils;
import org.aion.api.cfg.CfgApi;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.impl.internal.Message;
import org.aion.api.log.AionLoggerFactory;
import org.aion.api.log.LogEnum;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.Event;
import org.aion.api.type.MsgRsp;
import org.aion.base.type.Address;
import org.aion.base.util.ByteArrayWrapper;
import org.slf4j.Logger;
import org.zeromq.ZMQ;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Jay Tseng on 14/11/16.
 */
public class ApiBase {

    protected final Logger LOGGER;
    private final int SLEEPTIME = 3000;
    AtomicBoolean isInitialized = new AtomicBoolean(false);
    // END PRIVATE MEMBERS --------------------------------------------
    Address minerAddress;
    Address defaultAccount = Address.EMPTY_ADDRESS();
    String url;
    boolean nb = false;
    int timeout = 300_000;
    MsgExecutor msgExecutor;
    boolean recon = false;
    private CfgApi cfg;

    ApiBase() {
        cfg = CfgApi.inst();
        AionLoggerFactory.init(cfg.getLog().getModules());
        LOGGER = AionLoggerFactory.getLogger(LogEnum.BSE.name());
        Thread.currentThread().setName("api");
    }

    public ApiMsg connect(String url) {
        return connect(url, false, 1, 300_000, null);
    }

    public ApiMsg connect(String url, boolean reconnect) {
        return connect(url, reconnect, 1, 300_000, null);
    }

    public ApiMsg connect(String url, boolean reconnect, String pubkey) {
        return connect(url, reconnect, 1, 300_000, pubkey);
    }

    public ApiMsg connect(String url, int workers, String pubkey) {
        return connect(url, false, workers, 300_000, pubkey);
    }

    public ApiMsg connect(String url, boolean reconnect, int workers, String pubkey) {
        return connect(url, reconnect, workers, 300_000, pubkey);
    }

    public ApiMsg connect(String url, boolean reconnect, int workers, int timeout, String pubkey) {
        if (url == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[connect]" + ErrId.getErrString(-1004L));
            }
            return new ApiMsg(-1004, false, org.aion.api.type.ApiMsg.cast.BOOLEAN);
        }

        // connection check and then reconnect later.
        if (this.isInitialized.get()) {
            ApiMsg msg = destroyApiBase();
            if (msg.isError()) {
                return msg.set(false, org.aion.api.type.ApiMsg.cast.BOOLEAN);
            }

            try {
                Thread.sleep(SLEEPTIME);
            } catch (InterruptedException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[connect]" + ErrId.getErrString(50L));
                }
                return msg.set(50, false, org.aion.api.type.ApiMsg.cast.BOOLEAN);
            }
        }

        this.recon = reconnect;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[connect]" + " Init MsgExecutor.");
        }

        if (timeout < 60_000) {
            timeout = 60_000;
        }

        int numProcs = Math.max(Runtime.getRuntime().availableProcessors() >> 1, 1);

        if (workers > numProcs) {
            workers = numProcs;
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("[connect]" + " Workers adjust to [{}]", numProcs);
            }
        }

        while (!isInitialized.get()) {
            if (this.msgExecutor == null) {
                this.msgExecutor = new MsgExecutor(ApiUtils.PROTOCOL_VER, url, timeout, pubkey);
                this.msgExecutor.start(workers);
            }

            try {
                Thread.sleep(SLEEPTIME);
            } catch (InterruptedException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[connect]" + ErrId.getErrString(50L));
                }
                return new ApiMsg(50, false, org.aion.api.type.ApiMsg.cast.BOOLEAN);
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[connect]" + " Waiting connect.");
            }

            if (!this.msgExecutor.isInitialized.get()) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[connect] Connect failed.");
                }
                this.msgExecutor.terminate();
                this.msgExecutor = null;

                if (!this.recon) {
                    return new ApiMsg(-1009, false, org.aion.api.type.ApiMsg.cast.BOOLEAN);
                }
            } else {
                this.isInitialized.set(true);
            }
        }

        this.url = url;

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("[connect]" + " Api connected");
        }
        return new ApiMsg(true, org.aion.api.type.ApiMsg.cast.BOOLEAN);
    }

    protected ApiMsg destroyApiBase() {
        if (!this.isInitialized.get()) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[destroyApi]" + ErrId.getErrString(-1003L));
            }
            return new ApiMsg(-1003, false, org.aion.api.type.ApiMsg.cast.BOOLEAN);
        }

        this.msgExecutor.terminate();
        this.msgExecutor = null;

        this.isInitialized.set(false);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("[destroyApi] Api destroyed");
        }
        return new ApiMsg(true, org.aion.api.type.ApiMsg.cast.BOOLEAN);
    }

    MsgRsp blockTx(byte[] hash, byte[] req) {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("[blockTx] MsgHash: [{}], reqmsg: [{}]", IUtils.bytes2Hex(hash),
                IUtils.bytes2Hex(req));
        }

        Future<MsgRsp> future = this.msgExecutor.aSyncSend(hash, req);
        try {
            MsgRsp msgRsp = future.get();

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("[blockTx] TxRsp: [{}], status: [{}]", msgRsp.getTxHash().toString(),
                    msgRsp.getStatus());
            }

            return msgRsp;
        } catch (Exception e) {

            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[blockTx]" + ErrId.getErrString(52L) + e.getMessage());
            }
            return new MsgRsp(52, null);
        }
    }

    byte[] nbProcess(byte[] reqHdr) {
        this.nb = false;
        this.msgExecutor.getNbSocket().send(reqHdr, ZMQ.DONTWAIT);

        while (this.msgExecutor.isInitialized.get()) {
            byte[] data = this.msgExecutor.getNbSocket().recv(0);

            if (data != null) {
                return data;
            }
        }

        return null;
    }

    MsgRsp Process(byte[] hash, byte[] req) {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("[Process] MsgHash: [{}], reqmsg: [{}]", IUtils.bytes2Hex(hash),
                IUtils.bytes2Hex(req));
        }

        MsgRsp rsp = this.msgExecutor.send(hash, req);

        if (rsp == null) {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[Process]" + ErrId.getErrString(55L));
            }
            return new MsgRsp(55, null);
        }

        long time = System.currentTimeMillis();
        while ((rsp == null || rsp.getStatus() == 100) && (System.currentTimeMillis() - time
            < 10000)) {
            rsp = this.msgExecutor.getStatus(ByteArrayWrapper.wrap(hash));

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return rsp;
    }

    int validRspHeader(byte[] rsp) {
        if (rsp == null) {

            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[validRspHeader]" + ErrId.getErrString(-103L));
            }
            return -103;
        }

        if (rsp.length < ApiUtils.RSP_HEADER_NOHASH_LEN) {

            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[validRspHeader]" + ErrId.getErrString(-328L));
            }
            return -328;
        }

        if (rsp[0] != ApiUtils.PROTOCOL_VER) {

            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[validRspHeader]" + ErrId.getErrString(-318L));
            }
            return -318;
        }

        if (rsp[1] != Message.Retcode.r_success_VALUE) {

            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[validRspHeader] Failed return code: [{}]", rsp[1]);
            }
            return rsp[1];
        }

        return 1;
    }

    public List<Event> getContractEvent(List<String> e) {
        return this.msgExecutor.getEvents(e);
    }

    Map<String, Boolean> getPrivilege() {
        return this.msgExecutor.getPrivilege();
    }

    public class CheckStatusTask implements Runnable {

        public void run() {
            // add your code here
        }
    }
}
