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

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.aion.api.IAccount;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.impl.internal.Message;
import org.aion.api.log.AionLoggerFactory;
import org.aion.api.log.LogEnum;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.Key;
import org.aion.api.type.KeyExport;
import org.aion.base.type.Address;
import org.aion.base.util.ByteUtil;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Jay Tseng on 19/04/17.
 */
public final class Account implements IAccount {
    private static final Logger LOGGER = AionLoggerFactory.getLogger(LogEnum.ACC.name());
    private final ApiMsg apiMsg = new ApiMsg();
    private AionAPIImpl apiInst;
    private final static int ACCOUNT_LIMIT = 1000;

    protected Account(AionAPIImpl inst) {
        this.apiInst = inst;
    }

    public ApiMsg accountCreate(List<String> pw, boolean pk) {
        if (!this.apiInst.isConnected()) {
            return apiMsg.set(-1003);
        }

        if (pw == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountCreate]" + ErrId.getErrString(-17L));
            }
            return apiMsg.set(-17);
        }

        if (pw.size() > ACCOUNT_LIMIT) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountCreate]" + ErrId.getErrString(-19L));
            }
            return apiMsg.set(-19);
        }


        Message.req_accountCreate reqBody = Message.req_accountCreate.newBuilder()
                .addAllPassword(pw)
                .setPrivateKey(pk)
                .build();

        byte[] reqHead = ApiUtils
                .toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_account, Message.Funcs.f_accountCreate);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);

        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return apiMsg.set(code);
        }

        try {
            List<Key> k = ApiUtils.toKey(Message.rsp_accountCreate.parseFrom(ApiUtils.parseBody(rsp).getData()), pk);

            return apiMsg.set(k, org.aion.api.type.ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountCreate]" + ErrId.getErrString(-104L) + e.getMessage());
            }
            return apiMsg.set(-104, e.getMessage(), org.aion.api.type.ApiMsg.cast.OTHERS);
        }
    }

    public ApiMsg accountBackup(List<Key> keys) {
        if (!this.apiInst.isConnected()) {
            return apiMsg.set(-1003);
        }

        if (keys == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountBackup]" + ErrId.getErrString(-17L));
            }
            return apiMsg.set(-17);
        }

        if (keys.size() > ACCOUNT_LIMIT) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountBackup]" + ErrId.getErrString(-19L));
            }
            return apiMsg.set(-19);
        }

        Set<Message.t_Key> mkeys = new HashSet<>();
        Set<Address> invalidAddress = new HashSet<>();
        for (int i=0 ; i< keys.size() ; i++) {
            try {
                Message.t_Key key = Message.t_Key.newBuilder()
                        .setAddress(ByteString.copyFrom(keys.get(i).getPubKey().toBytes()))
                        .setPassword(keys.get(i).getPassPhrase()).build();
                mkeys.add(key);
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[accountBackup]" + ErrId.getErrString(-20L));
                }
                invalidAddress.add(keys.get(i).getPubKey());
                continue;
            }
        }

        Message.req_exportAccounts reqBody = Message.req_exportAccounts.newBuilder()
                .addAllKeyFile(mkeys)
                .build();

        byte[] reqHead = ApiUtils
                .toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_account, Message.Funcs.f_backupAccounts);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);

        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return apiMsg.set(code);
        }

        try {
            KeyExport ke = ApiUtils.toKeyExport(Message.rsp_exportAccounts.parseFrom(ApiUtils.parseBody(rsp).getData()));
            return apiMsg.set(ke, org.aion.api.type.ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountBackup]" + ErrId.getErrString(-104L) + e.getMessage());
            }
            return apiMsg.set(-104, e.getMessage(), org.aion.api.type.ApiMsg.cast.OTHERS);
        }
    }

    public ApiMsg accountExport(List<Key> keys) {
        if (!this.apiInst.isConnected()) {
            return apiMsg.set(-1003);
        }

        if (keys == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountExport]" + ErrId.getErrString(-17L));
            }
            return apiMsg.set(-17);
        }

        if (keys.size() > ACCOUNT_LIMIT) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountExport]" + ErrId.getErrString(-19L));
            }
            return apiMsg.set(-19);
        }

        Set<Message.t_Key> mkeys = new HashSet<>();
        Set<Address> invalidAddress = new HashSet<>();
        for (int i=0 ; i< keys.size() ; i++) {
            try {
                Message.t_Key key = Message.t_Key.newBuilder()
                        .setAddress(ByteString.copyFrom(keys.get(i).getPubKey().toBytes()))
                        .setPassword(keys.get(i).getPassPhrase()).build();
                mkeys.add(key);
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[accountExport]" + ErrId.getErrString(-20L));
                }
                invalidAddress.add(keys.get(i).getPubKey());
                continue;
            }
        }

        Message.req_exportAccounts reqBody = Message.req_exportAccounts.newBuilder()
                .addAllKeyFile(mkeys)
                .build();

        byte[] reqHead = ApiUtils
                .toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_account, Message.Funcs.f_exportAccounts);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);

        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return apiMsg.set(code);
        }

        try {
            KeyExport ke = ApiUtils.toKeyExport(Message.rsp_exportAccounts.parseFrom(ApiUtils.parseBody(rsp).getData()));
            return apiMsg.set(ke, org.aion.api.type.ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountExport]" + ErrId.getErrString(-104L) + e.getMessage());
            }
            return apiMsg.set(-104, e.getMessage(), org.aion.api.type.ApiMsg.cast.OTHERS);
        }
    }

    public ApiMsg accountImport(Map<String,String> keys) {
        if (!this.apiInst.isConnected()) {
            return apiMsg.set(-1003);
        }

        if (keys == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountImport]" + ErrId.getErrString(-17L));
            }
            return apiMsg.set(-17);
        }

        if (keys.size() > ACCOUNT_LIMIT) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountImport]" + ErrId.getErrString(-19L));
            }
            return apiMsg.set(-19);
        }


        Set<Message.t_PrivateKey> pKey = keys.entrySet().parallelStream().map(
                k -> Message.t_PrivateKey.newBuilder()
                                        .setPrivateKey(k.getKey())
                                        .setPassword(k.getValue())
                                        .build()
        ).collect(Collectors.toSet());

        Message.req_importAccounts reqBody = Message.req_importAccounts.newBuilder()
                .addAllPrivateKey(pKey)
                .build();

        byte[] reqHead = ApiUtils
                .toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_account, Message.Funcs.f_importAccounts);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);

        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return apiMsg.set(code);
        }

        try {
            List<String> invalidKey = Message.rsp_importAccounts.parseFrom(ApiUtils.parseBody(rsp).getData())
                    .getInvalidKeyList();

            return apiMsg.set(invalidKey, org.aion.api.type.ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountImport]" + ErrId.getErrString(-104L) + e.getMessage());
            }
            return apiMsg.set(-104, e.getMessage(), org.aion.api.type.ApiMsg.cast.OTHERS);
        }
    }
}
