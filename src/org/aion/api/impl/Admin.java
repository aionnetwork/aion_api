package org.aion.api.impl;

import static java.lang.Long.max;
import static java.lang.Math.min;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.aion.api.IAdmin;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.impl.internal.Message;
import org.aion.api.impl.internal.Message.Funcs;
import org.aion.api.impl.internal.Message.rsp_getBlockDetailsByHash;
import org.aion.api.log.AionLoggerFactory;
import org.aion.api.log.LogEnum;
import org.aion.api.type.AccountDetails;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.ApiMsg.cast;
import org.aion.api.type.Block;
import org.aion.api.type.BlockDetails;
import org.aion.api.type.BlockSql;
import org.aion.base.type.AionAddress;
import org.aion.base.type.Hash256;
import org.aion.base.util.ByteUtil;
import org.aion.vm.api.interfaces.Address;
import org.slf4j.Logger;

public class Admin implements IAdmin {

    private static final Logger LOGGER = AionLoggerFactory.getLogger(LogEnum.ADM.name());
    private AionAPIImpl apiInst;

    Admin(AionAPIImpl inst) {
        this.apiInst = inst;
    }

    @Override
    public ApiMsg getBlockDetailsByNumber(String blks) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        if (blks == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getBlockDetailsByNumber]" + ErrId.getErrString(-17L));
            }
            return new ApiMsg(-17);
        }

        try {
            return getBlockDetailsByNumber(parseBlockList(blks));
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getBlockDetailsByNumber] " + e.getMessage());
            }
            return new ApiMsg(-17);
        }
    }

    @Override
    public ApiMsg getBlockDetailsByNumber(long blkNum) {
        ApiMsg msg = getBlockDetailsByNumber(String.valueOf(blkNum));

        if (msg.isError()) {
            return msg;
        }

        List<BlockDetails> bdl = msg.getObject();

        if (bdl.size() != 1) {
            throw new IllegalArgumentException("wrong return block size! Should not happen");
        }

        return new ApiMsg(1, bdl.get(0), cast.OTHERS);
    }

    @Override
    public ApiMsg getBlockDetailsByHash(Hash256 blockHash) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        if (blockHash == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getBlockDetailsByHash]" + ErrId.getErrString(-17L));
            }
            return new ApiMsg(-17);
        }

        Message.req_getBlockDetailsByHash reqBody =
                Message.req_getBlockDetailsByHash
                        .newBuilder()
                        .setBlockHash(ByteString.copyFrom(blockHash.toBytes()))
                        .build();

        byte[] reqHead =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER,
                        Message.Servs.s_admin,
                        Funcs.f_getBlockDetailsByHash);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            List<BlockDetails> k =
                    ApiUtils.toBlockDetails(
                            Collections.singletonList(
                                    rsp_getBlockDetailsByHash
                                            .parseFrom(ApiUtils.parseBody(rsp).getData())
                                            .getBlkDetails()));
            return new ApiMsg(k.get(0), org.aion.api.type.ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(
                        "[getBlockDetailsByHash]" + ErrId.getErrString(-104L) + e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), org.aion.api.type.ApiMsg.cast.OTHERS);
        }
    }

    private List<Long> parseBlockList(String blks) {
        if (blks == null) {
            throw new NullPointerException();
        }

        String[] parts = blks.split(",");
        Set<Long> numbers = new HashSet<>();
        for (String part : parts) {
            String[] interval = part.split("-");
            if (interval.length == 1) {
                numbers.add(Long.valueOf(interval[0]));
            } else if (interval.length == 2) {
                for (long i = min(Long.valueOf(interval[0]), Long.valueOf(interval[1]));
                        i <= max(Long.valueOf(interval[0]), Long.valueOf(interval[1]));
                        i++) {
                    numbers.add(i);
                }
            } else {
                throw new IllegalArgumentException();
            }
        }

        return numbers.parallelStream().sorted().collect(Collectors.toList());
    }

    private List<Address> parseAddressList(String addresses) {
        if (addresses == null) {
            throw new NullPointerException();
        }

        String[] parts = addresses.split(",");
        List<Address> addressList = new ArrayList<>();
        for (String part : parts) {
            addressList.add(new AionAddress(part));
        }

        return addressList;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public ApiMsg getBlockSqlByRange(Long blkStart, Long blkEnd) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        if (blkStart == null || blkEnd == null || blkStart < 0 || blkEnd < 0 || blkEnd < blkStart) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getBlockSqlByNumber]" + ErrId.getErrString(-17L));
            }
            return new ApiMsg(-17);
        }

        Message.req_getBlockSqlByRange reqBody =
                Message.req_getBlockSqlByRange
                        .newBuilder()
                        .setBlkNumberStart(blkStart)
                        .setBlkNumberEnd(blkEnd)
                        .build();

        byte[] reqHead =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER,
                        Message.Servs.s_admin,
                        Message.Funcs.f_getBlockSqlByRange);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            List<BlockSql> k =
                    ApiUtils.toBlockSql(
                            Message.rsp_getBlockSqlByRange
                                    .parseFrom(ApiUtils.parseBody(rsp).getData())
                                    .getBlkSqlList());
            return new ApiMsg(k, org.aion.api.type.ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getBlockSqlByRange]" + ErrId.getErrString(-104L) + e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), org.aion.api.type.ApiMsg.cast.OTHERS);
        }
    }

    @SuppressWarnings("Duplicates")
    @Override
    public ApiMsg getBlockDetailsByRange(Long blkStart, Long blkEnd) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        if (blkStart == null || blkEnd == null || blkStart < 0 || blkEnd < 0 || blkEnd < blkStart) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getBlockDetailsByRange]" + ErrId.getErrString(-17L));
            }
            return new ApiMsg(-17);
        }

        Message.req_getBlockDetailsByRange reqBody =
                Message.req_getBlockDetailsByRange
                        .newBuilder()
                        .setBlkNumberStart(blkStart)
                        .setBlkNumberEnd(blkEnd)
                        .build();

        byte[] reqHead =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER,
                        Message.Servs.s_admin,
                        Message.Funcs.f_getBlockDetailsByRange);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            List<BlockDetails> k =
                    ApiUtils.toBlockDetails(
                            Message.rsp_getBlockDetailsByRange
                                    .parseFrom(ApiUtils.parseBody(rsp).getData())
                                    .getBlkDetailsList());
            return new ApiMsg(k, org.aion.api.type.ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(
                        "[getBlockDetailsByRange]" + ErrId.getErrString(-104L) + e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), org.aion.api.type.ApiMsg.cast.OTHERS);
        }
    }

    @Override
    public ApiMsg getBlockDetailsByNumber(List<Long> blks) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        if (blks == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getBlockDetailsByNumber]" + ErrId.getErrString(-17L));
            }
            return new ApiMsg(-17);
        }

        Message.req_getBlockDetailsByNumber reqBody =
                Message.req_getBlockDetailsByNumber.newBuilder().addAllBlkNumbers(blks).build();

        byte[] reqHead =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER,
                        Message.Servs.s_admin,
                        Message.Funcs.f_getBlockDetailsByNumber);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            List<BlockDetails> k =
                    ApiUtils.toBlockDetails(
                            Message.rsp_getBlockDetailsByNumber
                                    .parseFrom(ApiUtils.parseBody(rsp).getData())
                                    .getBlkDetailsList());
            return new ApiMsg(k, org.aion.api.type.ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(
                        "[getBlockDetailsByNumber]" + ErrId.getErrString(-104L) + e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), org.aion.api.type.ApiMsg.cast.OTHERS);
        }
    }

    @Override
    public ApiMsg getBlockDetailsByLatest(Long count) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        if (count == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getBlockDetailsByLatest]" + ErrId.getErrString(-17L));
            }
            return new ApiMsg(-17);
        }

        Message.req_getBlockDetailsByLatest reqBody =
                Message.req_getBlockDetailsByLatest.newBuilder().setCount(count).build();

        byte[] reqHead =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER,
                        Message.Servs.s_admin,
                        Message.Funcs.f_getBlockDetailsByLatest);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            List<BlockDetails> k =
                    ApiUtils.toBlockDetails(
                            Message.rsp_getBlockDetailsByLatest
                                    .parseFrom(ApiUtils.parseBody(rsp).getData())
                                    .getBlkDetailsList());
            return new ApiMsg(k, org.aion.api.type.ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(
                        "[getBlockDetailsByLatest]" + ErrId.getErrString(-104L) + e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), org.aion.api.type.ApiMsg.cast.OTHERS);
        }
    }

    @Override
    public ApiMsg getBlocksByLatest(Long count) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        if (count == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getBlocksByLatest]" + ErrId.getErrString(-17L));
            }
            return new ApiMsg(-17);
        }

        Message.req_getBlocksByLatest reqBody =
                Message.req_getBlocksByLatest.newBuilder().setCount(count).build();

        byte[] reqHead =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER,
                        Message.Servs.s_admin,
                        Message.Funcs.f_getBlocksByLatest);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            List<Block> k =
                    ApiUtils.toBlocks(
                            Message.rsp_getBlocksByLatest
                                    .parseFrom(ApiUtils.parseBody(rsp).getData())
                                    .getBlksList());
            return new ApiMsg(k, org.aion.api.type.ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getBlocksByLatest]" + ErrId.getErrString(-104L) + e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), org.aion.api.type.ApiMsg.cast.OTHERS);
        }
    }

    @Override
    public ApiMsg getAccountDetailsByAddressList(String addressList) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        if (addressList == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getAccountDetailsByAddressList]" + ErrId.getErrString(-17L));
            }
            return new ApiMsg(-17);
        }

        try {
            return getAccountDetailsByAddressList(parseAddressList(addressList));
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getAccountDetailsByAddressList] " + e.getMessage());
            }
            return new ApiMsg(-17);
        }
    }

    @Override
    public ApiMsg getAccountDetailsByAddressList(List<Address> addressList) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        if (addressList == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getAccountDetailsByAddressList]" + ErrId.getErrString(-17L));
            }
            return new ApiMsg(-17);
        }

        // convert addresses to protobuf ByteString
        List<ByteString> addressListString =
                addressList
                        .parallelStream()
                        .map(address -> ByteString.copyFrom(address.toBytes()))
                        .collect(Collectors.toList());

        Message.req_getAccountDetailsByAddressList reqBody =
                Message.req_getAccountDetailsByAddressList
                        .newBuilder()
                        .addAllAddresses(addressListString)
                        .build();

        byte[] reqHead =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER,
                        Message.Servs.s_admin,
                        Message.Funcs.f_getAccountDetailsByAddressList);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            List<AccountDetails> k =
                    ApiUtils.toAccountDetails(
                            Message.rsp_getAccountDetailsByAddressList
                                    .parseFrom(ApiUtils.parseBody(rsp).getData())
                                    .getAccountsList());
            return new ApiMsg(k, org.aion.api.type.ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(
                        "[getAccountDetailsByAddressList]"
                                + ErrId.getErrString(-104L)
                                + e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), org.aion.api.type.ApiMsg.cast.OTHERS);
        }
    }
}
