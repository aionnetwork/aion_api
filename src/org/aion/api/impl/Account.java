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

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.aion.api.IAccount;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.impl.internal.Message;
import org.aion.api.impl.internal.Message.t_Key;
import org.aion.api.log.AionLoggerFactory;
import org.aion.api.log.LogEnum;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.Key;
import org.aion.api.type.KeyExport;
import org.aion.api.type.core.account.KeystoreFormat;
import org.aion.base.type.Address;
import org.aion.base.util.ByteUtil;
import org.aion.base.util.TypeConverter;
import org.aion.crypto.ECKey;
import org.aion.crypto.ECKeyFac;
import org.slf4j.Logger;

/**
 * Created by Jay Tseng on 19/04/17.
 */
public final class Account implements IAccount {

    private static final Logger LOGGER = AionLoggerFactory.getLogger(LogEnum.ACC.name());
    private final static int ACCOUNT_LIMIT = 1000;
    private static final Pattern HEX_64 = Pattern.compile("^[\\p{XDigit}]{64}$");
    private static final String ADDR_PREFIX = "0x";
    private static final String AION_PREFIX = "a0";
    private static final String KEYSTORE_PATH;
    private static final Path PATH;

    static {
        String storageDir = System.getProperty("local.storage.dir");
        if (storageDir == null || storageDir.equalsIgnoreCase("")) {
            storageDir = System.getProperty("user.dir");
        }
        KEYSTORE_PATH = storageDir + "/keystore";
        PATH = Paths.get(KEYSTORE_PATH);
    }

    private AionAPIImpl apiInst;

    protected Account(AionAPIImpl inst) {
        this.apiInst = inst;
    }

    public static ApiMsg keystoreCreateLocal(List<String> passphrase) {

        if (passphrase == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[keystoreCreateLocal]" + ErrId.getErrString(-17L));
            }
            return new ApiMsg(-17);
        }

        List<String> addr = new ArrayList<>();
        for (String p : passphrase) {
            if (p == null) {
                p = "";
            }
            addr.add(create(p));
        }

        return new ApiMsg(1, addr, org.aion.api.type.ApiMsg.cast.OTHERS);
    }

    private static String create(String password) {
        return create(password, ECKeyFac.inst().create());
    }

    private static String create(String password, ECKey key) {

        boolean isWindows = ApiUtils.isWindows();

        FileAttribute<Set<PosixFilePermission>> attr = null;
        if (!Files.exists(PATH)) {
            try {
                if (isWindows) {
                    Files.createDirectory(PATH);
                } else {
                    Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxr-----");
                    attr = PosixFilePermissions.asFileAttribute(perms);
                }
            } catch (IOException e) {
                LOGGER.error("keystore folder create failed!");
                return "";
            }
        }

        String address = ByteUtil.toHexString(key.getAddress());
        if (exist(address)) {
            return ADDR_PREFIX;
        } else {
            byte[] content = new KeystoreFormat().toKeystore(key, password);
            DateFormat df = new SimpleDateFormat(
                isWindows ? "yyyy-MM-dd'T'HH-mm-ss.SSS'Z'" : "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            String iso_date = df.format(new Date(System.currentTimeMillis()));
            String fileName = "UTC--" + iso_date + "--" + address;
            try {
                Path keyFile = PATH.resolve(fileName);
                if (!Files.exists(keyFile)) {
                    if (isWindows) {
                        keyFile = Files.createFile(keyFile);
                    } else {
                        keyFile = Files.createFile(keyFile, attr);
                    }
                }
                String path = keyFile.toString();
                FileOutputStream fos = new FileOutputStream(path);
                fos.write(content);
                fos.close();

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Keystore created! {}", TypeConverter.toJsonHex(address));
                }

                return TypeConverter.toJsonHex(address);
            } catch (IOException e) {
                LOGGER.error("fail to create keystore");
                return ADDR_PREFIX;
            }
        }
    }

    private static boolean exist(String _address) {
        if (_address.startsWith(ADDR_PREFIX)) {
            _address = _address.substring(2);
        }

        boolean flag = false;
        if (_address.startsWith(AION_PREFIX)) {
            List<File> files = getFiles();
            for (File file : files) {
                if (HEX_64.matcher(_address).find() && file.getName().contains(_address)) {
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    private static List<File> getFiles() {
        File[] files = PATH.toFile().listFiles();
        return files != null ? Arrays.asList(files) : Collections.emptyList();
    }

    public ApiMsg accountCreate(List<String> pw, boolean pk) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        if (pw == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountCreate]" + ErrId.getErrString(-17L));
            }
            return new ApiMsg(-17);
        }

        if (pw.size() > ACCOUNT_LIMIT) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountCreate]" + ErrId.getErrString(-19L));
            }
            return new ApiMsg(-19);
        }

        Message.req_accountCreate reqBody = Message.req_accountCreate.newBuilder()
            .addAllPassword(pw)
            .setPrivateKey(pk)
            .build();

        byte[] reqHead = ApiUtils
            .toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_account,
                Message.Funcs.f_accountCreate);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);

        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            List<Key> k = ApiUtils
                .toKey(Message.rsp_accountCreate.parseFrom(ApiUtils.parseBody(rsp).getData()), pk);

            return new ApiMsg(k, org.aion.api.type.ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountCreate]" + ErrId.getErrString(-104L) + e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), org.aion.api.type.ApiMsg.cast.OTHERS);
        }
    }

    public ApiMsg accountBackup(List<Key> keys) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        if (keys == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountBackup]" + ErrId.getErrString(-17L));
            }
            return new ApiMsg(-17);
        }

        if (keys.size() > ACCOUNT_LIMIT) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountBackup]" + ErrId.getErrString(-19L));
            }
            return new ApiMsg(-19);
        }

        Set<Message.t_Key> mkeys = new HashSet<>();
        Set<Address> invalidAddress = new HashSet<>();
        for (Key key1 : keys) {
            try {
                t_Key key = t_Key.newBuilder()
                    .setAddress(ByteString.copyFrom(key1.getPubKey().toBytes()))
                    .setPassword(key1.getPassPhrase()).build();
                mkeys.add(key);
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[accountBackup]" + ErrId.getErrString(-20L));
                }
                invalidAddress.add(key1.getPubKey());
            }
        }

        Message.req_exportAccounts reqBody = Message.req_exportAccounts.newBuilder()
            .addAllKeyFile(mkeys)
            .build();

        byte[] reqHead = ApiUtils
            .toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_account,
                Message.Funcs.f_backupAccounts);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);

        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            KeyExport ke = ApiUtils.toKeyExport(
                Message.rsp_exportAccounts.parseFrom(ApiUtils.parseBody(rsp).getData()));
            return new ApiMsg(ke, org.aion.api.type.ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountBackup]" + ErrId.getErrString(-104L) + e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), org.aion.api.type.ApiMsg.cast.OTHERS);
        }
    }

    public ApiMsg accountExport(List<Key> keys) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        if (keys == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountExport]" + ErrId.getErrString(-17L));
            }
            return new ApiMsg(-17);
        }

        if (keys.size() > ACCOUNT_LIMIT) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountExport]" + ErrId.getErrString(-19L));
            }
            return new ApiMsg(-19);
        }

        Set<Message.t_Key> mkeys = new HashSet<>();
        Set<Address> invalidAddress = new HashSet<>();
        for (Key key1 : keys) {
            try {
                t_Key key = t_Key.newBuilder()
                    .setAddress(ByteString.copyFrom(key1.getPubKey().toBytes()))
                    .setPassword(key1.getPassPhrase()).build();
                mkeys.add(key);
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[accountExport]" + ErrId.getErrString(-20L));
                }
                invalidAddress.add(key1.getPubKey());
            }
        }

        Message.req_exportAccounts reqBody = Message.req_exportAccounts.newBuilder()
            .addAllKeyFile(mkeys)
            .build();

        byte[] reqHead = ApiUtils
            .toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_account,
                Message.Funcs.f_exportAccounts);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);

        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            KeyExport ke = ApiUtils.toKeyExport(
                Message.rsp_exportAccounts.parseFrom(ApiUtils.parseBody(rsp).getData()));
            return new ApiMsg(ke, org.aion.api.type.ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountExport]" + ErrId.getErrString(-104L) + e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), org.aion.api.type.ApiMsg.cast.OTHERS);
        }
    }

    public ApiMsg accountImport(Map<String, String> keys) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        if (keys == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountImport]" + ErrId.getErrString(-17L));
            }
            return new ApiMsg(-17);
        }

        if (keys.size() > ACCOUNT_LIMIT) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountImport]" + ErrId.getErrString(-19L));
            }
            return new ApiMsg(-19);
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
            .toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_account,
                Message.Funcs.f_importAccounts);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);

        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            List<String> invalidKey = Message.rsp_importAccounts
                .parseFrom(ApiUtils.parseBody(rsp).getData())
                .getInvalidKeyList();

            return new ApiMsg(invalidKey, org.aion.api.type.ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[accountImport]" + ErrId.getErrString(-104L) + e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), org.aion.api.type.ApiMsg.cast.OTHERS);
        }
    }
}
