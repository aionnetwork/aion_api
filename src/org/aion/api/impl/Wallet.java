package org.aion.api.impl;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.List;
import org.aion.api.IWallet;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.impl.internal.Message;
import org.aion.api.log.AionLoggerFactory;
import org.aion.api.log.LogEnum;
import org.aion.api.type.ApiMsg;
import org.aion.base.type.AionAddress;
import org.aion.base.util.ByteUtil;
import org.slf4j.Logger;

/** Created by Jay Tseng on 14/11/16. */
public class Wallet implements IWallet {
    private static final Logger LOGGER = AionLoggerFactory.getLogger(LogEnum.WLT.name());
    private AionAPIImpl apiInst;

    protected Wallet(AionAPIImpl inst) {
        this.apiInst = inst;
    }

    public ApiMsg getAccounts() {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        byte[] reqHdr =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER, Message.Servs.s_wallet, Message.Funcs.f_accounts);

        byte[] rsp = this.apiInst.nbProcess(reqHdr);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            Message.rsp_accounts msgRsp =
                    Message.rsp_accounts.parseFrom(ApiUtils.parseBody(rsp).getData());
            List<ByteString> accBs = msgRsp.getAccoutList();

            List<AionAddress> account = new ArrayList<>();
            for (ByteString bs : accBs) {
                account.add(AionAddress.wrap(bs.toByteArray()));
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[getAccounts] {}", account.stream().toString());
            }
            return new ApiMsg(account, ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(
                        "[getAccounts] {} exception: [{}]",
                        ErrId.getErrString(-104L),
                        e.toString());
            }
            return new ApiMsg(-104);
        }
    }

    public ApiMsg unlockAccount(AionAddress acc, String passphrase) {
        return unlockAccount(acc, passphrase, 60);
    }

    public ApiMsg unlockAccount(AionAddress acc, String passphrase, int duration) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        if (passphrase == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[unlockAccount] {}", ErrId.getErrString(-301L));
            }
            return new ApiMsg(-301);
        }

        if (duration < 0) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[unlockAccount] {}", ErrId.getErrString(-302L));
            }
            return new ApiMsg(-302);
        }

        Message.req_unlockAccount reqBody =
                Message.req_unlockAccount
                        .newBuilder()
                        .setAccount(ByteString.copyFrom(acc.toBytes()))
                        .setDuration(duration)
                        .setPassword(passphrase)
                        .build();

        byte[] reqHdr =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER,
                        Message.Servs.s_wallet,
                        Message.Funcs.f_unlockAccount);
        byte[] reqMsg = ByteUtil.merge(reqHdr, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        byte[] body = ApiUtils.parseBody(rsp).getData();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[unlockAccount] rspmsg: [{}]", body[0]);
        }
        return new ApiMsg(ApiUtils.isTypeBoolean(body[0]), ApiMsg.cast.BOOLEAN);
    }

    public ApiMsg getMinerAccount() {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        byte[] reqHdr =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER,
                        Message.Servs.s_wallet,
                        Message.Funcs.f_minerAddress);
        byte[] rsp = this.apiInst.nbProcess(reqHdr);

        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            Message.rsp_minerAddress msgRsp =
                    Message.rsp_minerAddress.parseFrom(ApiUtils.parseBody(rsp).getData());
            this.apiInst.minerAddress = AionAddress.wrap(msgRsp.getMinerAddr().toByteArray());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[getMinerAccount] minerAddress: [{}]", this.apiInst.minerAddress);
            }

            return new ApiMsg(this.apiInst.minerAddress, ApiMsg.cast.OTHERS);
        } catch (Exception e) {
            e.printStackTrace();
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(
                        "[getMinerAccount] {} exception: [{}]",
                        ErrId.getErrString(-104L),
                        e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), ApiMsg.cast.OTHERS);
        }
    }

    public ApiMsg setDefaultAccount(AionAddress addr) {
        this.apiInst.defaultAccount = addr;
        return new ApiMsg(true, ApiMsg.cast.BOOLEAN);
    }

    public ApiMsg getDefaultAccount() {
        return new ApiMsg(this.apiInst.defaultAccount, ApiMsg.cast.OTHERS);
    }

    public ApiMsg lockAccount(AionAddress acc, String passphrase) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        if (passphrase == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[lockAccount] {}", ErrId.getErrString(-301L));
            }
            return new ApiMsg(-301);
        }

        Message.req_accountlock reqBody =
                Message.req_accountlock
                        .newBuilder()
                        .setAccount(ByteString.copyFrom(acc.toBytes()))
                        .setPassword(passphrase)
                        .build();

        byte[] reqHdr =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER, Message.Servs.s_wallet, Message.Funcs.f_accountLock);
        byte[] reqMsg = ByteUtil.merge(reqHdr, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        byte[] body = ApiUtils.parseBody(rsp).getData();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[lockAccount] rspmsg: [{}]", body[0]);
        }
        return new ApiMsg(ApiUtils.isTypeBoolean(body[0]), ApiMsg.cast.BOOLEAN);
    }
}
