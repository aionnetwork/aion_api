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

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.math.BigInteger;
import org.aion.api.ITx;
import org.aion.api.IUtils;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.impl.internal.Message;
import org.aion.api.impl.internal.Message.Funcs;
import org.aion.api.impl.internal.Message.Servs;
import org.aion.api.log.AionLoggerFactory;
import org.aion.api.log.LogEnum;
import org.aion.api.type.*;
import org.aion.api.type.ApiMsg.cast;
import org.aion.api.type.TxArgs.TxArgsBuilder;
import org.aion.api.type.core.tx.AionTransaction;
import org.aion.base.type.Address;
import org.aion.base.type.Hash256;
import org.aion.base.util.ByteArrayWrapper;
import org.aion.base.util.ByteUtil;
import org.aion.base.util.Hex;
import org.aion.crypto.ECKey;
import org.aion.crypto.ECKeyFac;
import org.slf4j.Logger;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jay Tseng on 15/11/16.
 */
public final class Tx implements ITx {

    private final Logger LOGGER = AionLoggerFactory.getLogger(LogEnum.TRX.name());
    final AionAPIImpl apiInst;

    private ByteArrayWrapper fmsg;
    private boolean fastbuild = false;
    private static final String ASCII = "ascii";

    Tx(AionAPIImpl inst) {
        this.apiInst = inst;
    }

    public ApiMsg contractDeploy(ContractDeploy cd) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        if (cd == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[contractDeploy] {}", ErrId.getErrString(-306L));
            }
            return new ApiMsg(-306);
        }

        byte[] code = cd.isConstructor()
            ? ByteUtil.merge(cd.getCompileResponse().getCode().getBytes(), cd.getData().getData())
            : cd.getCompileResponse().getCode().getBytes();

        Message.req_contractDeploy reqBody = Message.req_contractDeploy.newBuilder()
            .setFrom(ByteString.copyFrom(
                cd.getFrom() == null ? apiInst.defaultAccount.toBytes() : cd.getFrom().toBytes()))
            .setNrgLimit(cd.getNrgLimit())
            .setNrgPrice(cd.getNrgPrice())
            .setData(ByteString.copyFrom(code))
            .setValue(ByteString.copyFrom(cd.getValue().toByteArray()))
            .build();

        ByteArrayWrapper hash = ByteArrayWrapper.wrap(ApiUtils.genHash(ApiUtils.MSG_HASH_LEN));
        byte[] reqHead = ApiUtils
            .toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_tx, Message.Funcs.f_contractDeploy,
                hash);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        MsgRsp rsp = this.apiInst.blockTx(hash.getData(), reqMsg);

        if (rsp == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[contractDeploy] {}", ErrId.getErrString(-103L));
            }
            return new ApiMsg(-103);
        }

        try {
            Message.rsp_contractDeploy msgRsp = Message.rsp_contractDeploy
                .parseFrom(rsp.getTxDeploy().toBytes());
            return new ApiMsg(
                new DeployResponse(Address.wrap(msgRsp.getContractAddress().toByteArray()),
                    Hash256.wrap(msgRsp.getTxHash().toByteArray())),
                ApiMsg.cast.OTHERS);

        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[contractDeploy] {} exception: [{}]", ErrId.getErrString(-104L),
                    e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), ApiMsg.cast.OTHERS);
        }
    }

    public ApiMsg call(TxArgs args) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        Message.req_call reqBody = Message.req_call.newBuilder()
            .setData(ByteString.copyFrom(args.getData().toBytes()))
            .setFrom(ByteString.copyFrom(args.getFrom() == null ? apiInst.defaultAccount.toBytes()
                : args.getFrom().toBytes()))
            .setTo(ByteString.copyFrom(args.getTo().toBytes()))
            .setNrg(args.getNrgLimit())
            .setNrgPrice(args.getNrgPrice())
            .setValue(ByteString.copyFrom(args.getValue().toByteArray()))
            .build();

        byte[] reqHead = ApiUtils
            .toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_tx, Message.Funcs.f_call);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());
        byte[] rsp = this.apiInst.nbProcess(reqMsg);

        if (rsp == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[call] {}", ErrId.getErrString(-103L));
            }
            return new ApiMsg(-103);
        }

        try {
            return new ApiMsg(
                Message.rsp_call.parseFrom(ApiUtils.parseBody(rsp).getData()).getResult()
                    .toByteArray(),
                ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER
                    .error("[call] {} exception: [{}]", ErrId.getErrString(-104L), e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), ApiMsg.cast.OTHERS);
        }
    }

    public ApiMsg getTxReceipt(Hash256 txHash) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        Message.req_getTransactionReceipt reqBody = Message.req_getTransactionReceipt.newBuilder()
            .setTxHash(ByteString.copyFrom(txHash.toBytes())).build();

        byte[] reqHead = ApiUtils
            .toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_tx,
                Message.Funcs.f_getTransactionReceipt);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            Message.rsp_getTransactionReceipt mrsp = Message.rsp_getTransactionReceipt
                .parseFrom(ApiUtils.parseBody(rsp).getData());
            return new ApiMsg(ApiUtils.toTransactionReceipt(mrsp), ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            //Todo : kernel return message change
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getTxReceipt] {} exception: [{}]", ErrId.getErrString(-104L),
                    e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), ApiMsg.cast.OTHERS);
        }
    }

    @Override
    // when using stream,   this method need better sync.
    //synchronized public byte[] sendTransaction(Types.TxArgs args) {
    public ApiMsg sendTransaction(TxArgs args) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        byte[] reqMsg;
        byte[] hash = ApiUtils.genHash(ApiUtils.MSG_HASH_LEN);

        if (this.fastbuild) {
            if (args == null) {
                if (fmsg == null) {
                    throw new IllegalArgumentException("Null fmsg");
                }

                int msglen = fmsg.getData().length;
                reqMsg = new byte[msglen];

                System.arraycopy(fmsg.getData(), 0, reqMsg, 0, msglen);
                System.arraycopy(hash, 0, reqMsg, ApiUtils.REQ_HEADER_NOHASH_LEN,
                    ApiUtils.MSG_HASH_LEN);
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            if (args == null) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[sendTransaction] {}", ErrId.getErrString(-303L));
                }
                return new ApiMsg(-303);
            }

            byte[] reqHead = ApiUtils
                .toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_tx,
                    Message.Funcs.f_sendTransaction, ByteArrayWrapper.wrap(hash));

            Message.req_sendTransaction reqBody = Message.req_sendTransaction.newBuilder()
                .setFrom(ByteString.copyFrom(
                    args.getFrom() == null ? apiInst.defaultAccount.toBytes()
                        : args.getFrom().toBytes()))
                .setTo(ByteString.copyFrom(args.getTo().toBytes()))
                .setData(ByteString.copyFrom(args.getData().toBytes()))
                .setNonce(ByteString.copyFrom(args.getNonce().toByteArray()))
                .setValue(ByteString.copyFrom(args.getValue().toByteArray()))
                .setNrg(args.getNrgLimit())
                .setNrgPrice(args.getNrgPrice())
                .build();

            reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());
        }

        MsgRsp msgRsp;
        if (this.apiInst.nb) {
            msgRsp = this.apiInst.Process(hash, reqMsg);
            this.apiInst.nb = false;
        } else {
            msgRsp = this.apiInst.blockTx(hash, reqMsg);
        }

        if (msgRsp == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[sendTransaction] {}", ErrId.getErrString(-103L));
            }
            return new ApiMsg(-103);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[sendTransaction] TxRsp.status [{}]", msgRsp.getStatus());
        }

        return new ApiMsg((int) msgRsp.getStatus(), msgRsp, ApiMsg.cast.OTHERS);
    }

    @Override
    public ApiMsg sendSignedTransaction(TxArgs args, ByteArrayWrapper key) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        byte[] reqMsg;
        byte[] hash = ApiUtils.genHash(ApiUtils.MSG_HASH_LEN);

        if (args == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[sendTransaction] {}", ErrId.getErrString(-303L));
            }
            return new ApiMsg(-303);
        }

        if (key == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[sendTransaction] {}", ErrId.getErrString(-315L));
            }
            return new ApiMsg(-315);
        }

        byte[] reqHead = ApiUtils
            .toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_tx,
                Message.Funcs.f_signedTransaction, ByteArrayWrapper.wrap(hash));

        ECKey ecKey = ECKeyFac.inst().create().fromPrivate(key.toBytes());
        if (ecKey == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[sendTransaction] {}", ErrId.getErrString(-21L));
            }
            return new ApiMsg(-21);
        }

        AionTransaction tx = new AionTransaction(args.getNonce().toByteArray()
            , args.getTo()
            , args.getValue().toByteArray()
            , args.getData().getData()
            , args.getNrgLimit()
            , args.getNrgPrice());
        tx.sign(ecKey);

        Message.req_rawTransaction reqBody = Message.req_rawTransaction.newBuilder()
            .setEncodedTx(ByteString.copyFrom(tx.getEncoded()))
            .build();

        reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        MsgRsp msgRsp;
        if (this.apiInst.nb) {
            msgRsp = this.apiInst.Process(hash, reqMsg);
            this.apiInst.nb = false;
        } else {
            msgRsp = this.apiInst.blockTx(hash, reqMsg);
        }

        if (msgRsp == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[sendTransaction] {}", ErrId.getErrString(-103L));
            }
            return new ApiMsg(-103);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[sendTransaction] TxRsp.status [{}]", msgRsp.getStatus());
        }

        return new ApiMsg((int) msgRsp.getStatus(), msgRsp, ApiMsg.cast.OTHERS);
    }

    @Override
    public ApiMsg sendRawTransaction(ByteArrayWrapper tx) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        byte[] reqMsg;
        byte[] hash = ApiUtils.genHash(ApiUtils.MSG_HASH_LEN);

        if (tx == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[sendTransaction] {}", ErrId.getErrString(-315L));
            }
            return new ApiMsg(-315);
        }

        byte[] reqHead = ApiUtils
            .toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_tx,
                Message.Funcs.f_signedTransaction, ByteArrayWrapper.wrap(hash));

        Message.req_rawTransaction reqBody = Message.req_rawTransaction.newBuilder()
            .setEncodedTx(ByteString.copyFrom(tx.toBytes()))
            .build();

        reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        MsgRsp msgRsp;
        if (this.apiInst.nb) {
            msgRsp = this.apiInst.Process(hash, reqMsg);
            this.apiInst.nb = false;
        } else {
            msgRsp = this.apiInst.blockTx(hash, reqMsg);
        }

        if (msgRsp == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[sendTransaction] {}", ErrId.getErrString(-103L));
            }
            return new ApiMsg(-103);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[sendTransaction] TxRsp.status [{}]", msgRsp.getStatus());
        }

        return new ApiMsg((int) msgRsp.getStatus(), msgRsp, ApiMsg.cast.OTHERS);
    }

    public ApiMsg compile(String code) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        if (code == null) {
            throw new NullPointerException();
        }

        byte[] reqHead = ApiUtils
            .toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_tx, Message.Funcs.f_compile);
        Message.req_compile reqBody = Message.req_compile.newBuilder().setCode(code).build();

        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());
        byte[] rsp = this.apiInst.nbProcess(reqMsg);

        int val = this.apiInst.validRspHeader(rsp);

        if (val != 1) {
            return new ApiMsg(val);
        }

        Map<String, Message.t_Contract> ctMap;
        try {
            ctMap = Message.rsp_compile.parseFrom(ApiUtils.parseBody(rsp).getData())
                .getConstractsMap();

        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[compile] {} exception: [{}]", ErrId.getErrString(-104L),
                    e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), ApiMsg.cast.OTHERS);
        }

        if (ctMap.isEmpty()) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[compile] {}", ErrId.getErrString(-126L));
            }
            return new ApiMsg(-126);
        }

        // generate type for Gson parsing
        Type abiType = new TypeToken<ArrayList<ContractAbiEntry>>() {
        }.getType();

        // create contracts output hashmap
        Map<String, CompileResponse> rtn = new HashMap<>();

        for (Map.Entry<String, Message.t_Contract> ct : ctMap.entrySet()) {

            CompileResponse.CompileResponseBuilder builder = new CompileResponse.CompileResponseBuilder();

            builder.code(ct.getValue().getCode())
                .compilerOptions(ct.getValue().getCompilerOptions())
                .compilerVersion(ct.getValue().getCompilerVersion())
                .source(ct.getValue().getSource())
                //**@Jay TODO :
                //** figure out these four features from the solidity compiler
                .language("")
                .languageVersion("")
                .developerDoc(JsonFmt.JsonFmtBuilder.emptyJsonFmt())
                .userDoc(JsonFmt.JsonFmtBuilder.emptyJsonFmt());
            //**

            // parsing ABI file
            try {
                builder.abiDefString(ct.getValue().getAbiDef().toString(ASCII))
                    .abiDefinition(
                        new Gson().fromJson(ct.getValue().getAbiDef().toString(ASCII), abiType));

                rtn.put(ct.getKey(), builder.createCompileResponse());
            } catch (JsonSyntaxException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[compile] {} exception: [{}]", ErrId.getErrString(-105L),
                        e.toString());
                }
                return new ApiMsg(-105, e.getMessage(), ApiMsg.cast.OTHERS);
            } catch (JsonParseException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[compile] {} exception: [{}]", ErrId.getErrString(-106L),
                        e.toString());
                }
                return new ApiMsg(-106, e.getMessage(), ApiMsg.cast.OTHERS);
            } catch (UnsupportedEncodingException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[compile] {} exception: [{}]", ErrId.getErrString(-200L),
                        e.toString());
                }
                return new ApiMsg(-200, e.getMessage(), ApiMsg.cast.OTHERS);
            }
        }

        return new ApiMsg(rtn, ApiMsg.cast.OTHERS);
    }

    public ApiMsg getSolcVersion() {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        byte[] reqHead = ApiUtils
            .toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_tx, Message.Funcs.f_getSolcVersion);

        byte[] rsp = this.apiInst.nbProcess(reqHead);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            return new ApiMsg(
                Message.rsp_getSolcVersion.parseFrom(ApiUtils.parseBody(rsp).getData()).getVer(),
                ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getSolcVersion] {} exception: [{}]", ErrId.getErrString(-104L),
                    e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), ApiMsg.cast.OTHERS);
        }
    }

    public ApiMsg getCode(Address address, long blockNumber) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        if (blockNumber < -1L) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getCode] {}", ErrId.getErrString(-310L));
            }
            return new ApiMsg(-310);
        }

        Message.req_getCode reqBody = Message.req_getCode.newBuilder()
            .setAddress(ByteString.copyFrom(address.toBytes()))
            .setBlocknumber(blockNumber).build();

        byte[] reqHead = ApiUtils
            .toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_tx, Message.Funcs.f_getCode);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            return new ApiMsg(
                Message.rsp_getCode.parseFrom(ApiUtils.parseBody(rsp).getData()).getCode()
                    .toByteArray(),
                ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getCode] {} exception: [{}]", ErrId.getErrString(-104L),
                    e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), ApiMsg.cast.OTHERS);
        }
    }

    public void fastTxbuild(TxArgs args) {
        fastTxbuild(args, false);
    }

    public void fastTxbuild(TxArgs args, boolean call) {
        if (args == null) {
            throw new NullPointerException();
        }

        byte[] reqHead = call ?
            ApiUtils.toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_tx,
                Message.Funcs.f_sendTransaction) :
            ApiUtils.toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_tx,
                Message.Funcs.f_sendTransaction,
                ByteArrayWrapper.wrap(ApiUtils.EMPTY_MSG_HASH));

        Message.req_sendTransaction reqBody = Message.req_sendTransaction.newBuilder()
            .setFrom(ByteString.copyFrom(args.getFrom() == null ? apiInst.defaultAccount.toBytes()
                : args.getFrom().toBytes()))
            .setTo(ByteString.copyFrom(args.getTo().toBytes()))
            .setData(ByteString.copyFrom(args.getData().toBytes()))
            .setNonce(ByteString.copyFrom(args.getNonce().toByteArray()))
            .setValue(ByteString.copyFrom(args.getValue().toByteArray()))
            .setNrg(args.getNrgLimit())
            .setNrgPrice(args.getNrgPrice())
            .build();

        this.fmsg = ByteArrayWrapper.wrap(ByteUtil.merge(reqHead, reqBody.toByteArray()));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[fastTxbuild] msg: {}", fmsg.toString());
        }

        fastbuild = true;
    }

    public Tx nonBlock() {
        this.apiInst.nb = true;
        return this;
    }

    public Tx timeout(int t) {
        this.apiInst.timeout = (t < 300_000 ? 300_000 : t);
        return this;
    }

    public ApiMsg estimateNrg(String code) {

        if (code.contains("0x") || code.contains("0X")) {
            code = code.substring(2);
        }

        byte[] byteCode = Hex.decode(code);
        if (byteCode == null) {
            return new ApiMsg(-17);
        }

        TxArgs txArgs = new TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(byteCode))
            .from(apiInst.defaultAccount.equals(Address.EMPTY_ADDRESS()) ? Address
                .wrap("0xa000000000000000000000000000000000000000000000000000000000000000")
                : apiInst.defaultAccount)
            .createTxArgs();

        return estimateNrg(txArgs);
    }

    public ApiMsg estimateNrg(TxArgs args) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        byte[] reqMsg;
        byte[] hash = ApiUtils.genHash(ApiUtils.MSG_HASH_LEN);
        if (this.fastbuild) {
            if (args == null) {
                if (fmsg == null) {
                    throw new IllegalArgumentException("Null fmsg");
                }

                int msglen = fmsg.getData().length;
                reqMsg = new byte[msglen];

                System.arraycopy(fmsg.getData(), 0, reqMsg, 0, msglen);
                System.arraycopy(hash, 0, reqMsg, ApiUtils.REQ_HEADER_NOHASH_LEN,
                    ApiUtils.MSG_HASH_LEN);
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            if (args == null) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[sendTransaction] {}", ErrId.getErrString(-303L));
                }
                return new ApiMsg(-303);
            }

            byte[] reqHead = ApiUtils
                .toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_tx,
                    Message.Funcs.f_estimateNrg);

            Message.req_estimateNrg reqBody = Message.req_estimateNrg.newBuilder()
                .setFrom(
                    ByteString.copyFrom(args.getFrom() == null ? apiInst.defaultAccount.toBytes()
                        : args.getFrom().toBytes()))
                .setTo(ByteString.copyFrom(args.getTo().toBytes()))
                .setData(ByteString.copyFrom(args.getData().toBytes()))
                .setValue(ByteString.copyFrom(args.getValue().toByteArray()))
                .setNrg(args.getNrgLimit())
                .setNrgPrice(args.getNrgPrice())
                .build();

            reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());
        }

        byte[] rsp = this.apiInst.nbProcess(reqMsg);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            return new ApiMsg(
                Message.rsp_estimateNrg.parseFrom(ApiUtils.parseBody(rsp).getData()).getNrg(),
                ApiMsg.cast.LONG);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getCode] {} exception: [{}]", ErrId.getErrString(-104L),
                    e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), ApiMsg.cast.OTHERS);
        }
    }

    public ApiMsg getMsgStatus(ByteArrayWrapper b) {
        return new ApiMsg(this.apiInst.msgExecutor.getStatus(b), ApiMsg.cast.OTHERS);
    }

    public ApiMsg eventRegister(List<String> evt, ContractEventFilter ef, Address address) {

        if (evt == null || ef == null || address == null) {
            throw new NullPointerException("evt#" + String.valueOf(evt)
                + " ContractEventFilter#" + String.valueOf(ef)
                + " Address#" + String.valueOf(address));
        }

        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        List<ByteString> addrList = new ArrayList<>();

        for (Address ad : ef.getAddresses()) {
            addrList.add(ByteString.copyFrom(ad.toBytes()));
        }

        List<String> topics = new ArrayList<>(ef.getTopics());

        Message.t_FilterCt filter = Message.t_FilterCt.newBuilder()
            .addAllAddresses(addrList)
            .setFrom((ef.getFromBlock()))
            .setTo(ef.getToBlock())
            .setContractAddr(ByteString.copyFrom(address.toBytes()))
            .setExpireTime(ef.getExpireTime())
            .addAllTopics(topics)
            .build();

        Message.req_eventRegister reqBody = Message.req_eventRegister.newBuilder()
            .addAllEvents(evt)
            .setFilter(filter)
            .build();

        byte[] reqHead = ApiUtils
            .toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_tx, Message.Funcs.f_eventRegister);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            return new ApiMsg(
                Message.rsp_eventDeregister.parseFrom(ApiUtils.parseBody(rsp).getData())
                    .getResult(),
                ApiMsg.cast.BOOLEAN);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[eventRegister] {} exception: [{}]", ErrId.getErrString(-104L),
                    e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), ApiMsg.cast.OTHERS);
        }
    }

    public ApiMsg eventDeregister(List<String> evt, Address address) {

        if (evt == null || address == null) {
            throw new NullPointerException("evt#" + String.valueOf(evt)
                + " Address#" + String.valueOf(address));
        }

        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        Message.req_eventDeregister reqBody = Message.req_eventDeregister.newBuilder()
            .addAllEvents(evt)
            .setContractAddr(ByteString.copyFrom(address.toBytes())).build();

        byte[] reqHead = ApiUtils
            .toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_tx,
                Message.Funcs.f_eventDeregister);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            return new ApiMsg(
                Message.rsp_eventDeregister.parseFrom(ApiUtils.parseBody(rsp).getData())
                    .getResult(),
                ApiMsg.cast.BOOLEAN);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[eventDeregister] {} exception: [{}]", ErrId.getErrString(-104L),
                    e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), ApiMsg.cast.OTHERS);
        }
    }

    public ApiMsg getNrgPrice() {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        byte[] reqHead = ApiUtils
            .toReqHeader(ApiUtils.PROTOCOL_VER, Servs.s_tx, Funcs.f_getNrgPrice);

        byte[] rsp = this.apiInst.nbProcess(reqHead);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            return new ApiMsg(
                Message.rsp_getNrgPrice.parseFrom(ApiUtils.parseBody(rsp).getData()).getNrgPrice(),
                cast.LONG);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getNrgPrice] {}", ErrId.getErrString(-104L) + e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), cast.OTHERS);
        }
    }

    protected void reset() {
        this.fastbuild = false;
    }

    //public ApiMsg queryEvents(ContractEventFilter ef, Address address) {
    //
    //    if ( ef == null || address == null) {
    //        throw new NullPointerException("ContractEventFilter#" + String.valueOf(ef)
    //                + " Address#" + String.valueOf(address));
    //    }
    //
    //    if (!this.apiInst.isConnected()) {
    //        return apiMsg.set(-1003);
    //    }
    //
    //    List<ByteString> addrList = new ArrayList<>();
    //    List<String> topics = new ArrayList<>();
    //
    //    for (Address ad : ef.getAddresses()) {
    //        addrList.add(ByteString.copyFrom(ad.toBytes()));
    //    }
    //
    //    for (String s : ef.getTopics()) {
    //        topics.add(s);
    //    }
    //
    //    Message.t_FilterCt filter = Message.t_FilterCt.newBuilder()
    //            .addAllAddresses(addrList)
    //            .setFrom(ef.getFromBlock())
    //            .setTo(ef.getToBlock())
    //            .setContractAddr(ByteString.copyFrom(address.toBytes()))
    //            .setExpireTime(ef.getExpireTime())
    //            .addAllTopics(topics)
    //            .build();
    //
    //    Message.req_queryCtEvents reqBody = Message.req_queryCtEvents.newBuilder().setFilter(filter).build();
    //
    //    byte[] reqHead = ApiUtils.toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_tx, Message.Funcs.f_eventQuery);
    //    byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());
    //
    //    byte[] rsp = this.apiInst.nbProcess(reqMsg);
    //    int code = this.apiInst.validRspHeader(rsp);
    //    if (code != 1) {
    //        return apiMsg.set(code);
    //    }
    //
    //    try {
    //        List<Message.t_EventCt> catcher = Message.rsp_queryCtEvents.parseFrom(ApiUtils.parseBody(rsp).getData()).getEcList();
    //        List<ContractEvent> rtn = new ArrayList<>();
    //
    //        for (Message.t_EventCt ev : catcher) {
    //            ContractEvent.ContractEventBuilder builder = new ContractEvent.ContractEventBuilder();
    //            builder.address(Address.wrap(ev.getAddress().toByteArray()))
    //                    .blockHash(Hash256.wrap(ev.getBlockHash().toByteArray()))
    //                    .blockNumber(ev.getBlockNumber())
    //                    .data(ByteArrayWrapper.wrap(ev.getData().toByteArray()))
    //                    .eventName(ev.getEventName())
    //                    .logIndex(ev.getLogIndex())
    //                    .removed(ev.getRemoved())
    //                    .txHash(Hash256.wrap(ev.getTxHash().toByteArray()))
    //                    .txIndex(ev.getTxIndex());
    //
    //            rtn.add(builder.createContractEvent());
    //        }
    //
    //        return apiMsg.set(rtn, org.aion.api.type.ApiMsg.cast.OTHERS);
    //    } catch (InvalidProtocolBufferException e) {
    //        if (LOGGER.isErrorEnabled()) {
    //            LOGGER.error("[queryEvents] {} exception: [{}]", ErrId.getErrString(-104L), e.getMessage());
    //        }
    //        return apiMsg.set(-104, e.getMessage(), org.aion.api.type.ApiMsg.cast.OTHERS);
    //    }
    //}

//    public void removeAllEvents() {
//        this.apiInst.msgExecutor.removeAllEvents();
//    }
//
//    public void removeEvent(String e) {
//        this.apiInst.msgExecutor.removeEvent(e);
//    }
//
//    public List<Event> getEvents(List<String> evtNames) {
//        return this.apiInst.msgExecutor
//                .getEvents(this.eventsIssued.values().stream().collect(Collectors.toList()));
//    }

    /*
    Types.Transaction SignTransaction(Types.Transaction trans) throws Throwable {
		if (apiInst.m_socket == null)
		{
			throw new NullPointerException("apiInst.m_socket is null");
		}

		Message.req_signTransaction reqBody = Message.req_signTransaction.newBuilder()
				.setNonce(trans.nonce)
				.setData(ApiUtils.bytes2Hex(trans.data))
				.setFrom(ByteString.copyFrom(trans.from))
				.setTo(ByteString.copyFrom(trans.to))
				.setNonce(trans.nonce)
				.setNrg(trans.gas)
				.setNrgPrice(trans.gasPrice)
				.setValue(trans.Value)
				.build();

		byte[] reqHead = ApiUtils.toByteArray(1, Message.Servs.s_tx, Message.Funcs.f_signTransaction);

		if (!apiInst.m_socket.send(reqHead, 0)) {
			throw new IOException("apiInst.m_socket failed to send");
		}

		byte[] respB = apiInst.m_socket.recv();

		if (respB == null ||
				(respB.length == 1 && respB[0] == 0))
		{
			throw new NullPointerException("respB is null");
		}
	}
     */

 /*
    public ArrayList<Types.tTransaction> FetchQueuedTransactions() throws Throwable {
		if (apiInst.m_socket == null)
		{
			throw new NullPointerException("apiInst.m_socket is null");
		}

		byte[] reqHead = ApiUtils.toByteArray(1, Message.Servs.s_tx, Message.Funcs.f_fetchQueuedTransactions);

		if (!apiInst.m_socket.send(reqHead, 0)) {
			throw new IOException("apiInst.m_socket failed to send");
		}

		byte[] respB = apiInst.m_socket.recv();

		if (respB == null ||
				(respB.length == 1 && respB[0] == 0))
		{
			throw new NullPointerException("respB is null");
		}

		try {
			Message.rsp_fetchQueuedTransactions resp = Message.rsp_fetchQueuedTransactions.parseFrom(respB);

			ArrayList<Types.tTransaction> outTransList = new ArrayList<>();
			for (Message.t_Transaction trans : resp.getTxsList()) {
				Types.tTransaction tempTrans = ApiUtils.totTransaction(trans);
				outTransList.add(tempTrans);
			}

			return outTransList;
		} catch (InvalidProtocolBufferException e) {
			throw e;
		}
	}


    public Types.TxPoolContents InspectTransaction() throws Throwable {
        if (apiInst.m_socket == null)
        {
            throw new NullPointerException("apiInst.m_socket is null");
        }

        byte[] reqHead = ApiUtils.toByteArray(1, Message.Servs.s_tx, Message.Funcs.f_inspectTransaction);
        try {
            apiInst.m_socket.send(reqHead, 0);
            byte[] respB = apiInst.m_socket.recv();

            if (respB == null ||
                    (respB.length == 1 && respB[0] == 0))
            {
                throw new NullPointerException("respB is null");
            }

            Message.rsp_inspectTransaction resp = Message.rsp_inspectTransaction.parseFrom(respB);
            Types.TxPoolContents txPool = new Types.TxPoolContents();

            // pending transactions
            for (Map.Entry<String, Message.t_Dump> dumpmap : resp.getPendingMap().entrySet()) {
                Types.Dump dump = new Types.Dump();

                for (Map.Entry<String, Message.t_Nonce> noncemap : dumpmap.getValue().getDumpMap().entrySet()) {
                    ArrayList<String> txList = new ArrayList<>();
                    txList.addAll(noncemap.getValue().getTxList());
                    dump.dump.put(noncemap.getKey(), txList);
                }

                txPool.pending.put(dumpmap.getKey(), dump);
            }

            return txPool;
        } catch (ZMQException e) {
            e.printStackTrace();
            throw e;
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
     */
}
