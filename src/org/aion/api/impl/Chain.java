package org.aion.api.impl;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.aion.api.IChain;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.impl.internal.Message;
import org.aion.api.impl.internal.Message.Funcs;
import org.aion.api.impl.internal.Message.Retcode;
import org.aion.api.log.AionLoggerFactory;
import org.aion.api.log.LogEnum;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.ApiMsg.cast;
import org.aion.api.type.Block;
import org.aion.base.type.AionAddress;
import org.aion.base.type.Hash256;
import org.aion.base.util.ByteArrayWrapper;
import org.aion.base.util.ByteUtil;
import org.aion.vm.api.interfaces.Address;
import org.slf4j.Logger;

/** Created by Jay Tseng on 14/11/16. */
public final class Chain implements IChain {

    private static final Logger LOGGER = AionLoggerFactory.getLogger(LogEnum.CHN.name());
    AionAPIImpl apiInst;

    Chain(AionAPIImpl inst) {
        this.apiInst = inst;
    }

    public ApiMsg blockNumber() {

        if (!apiInst.isInitialized.get()) {
            return new ApiMsg(-1003);
        }

        byte[] reqHdr =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER, Message.Servs.s_chain, Message.Funcs.f_blockNumber);
        byte[] rsp = this.apiInst.nbProcess(reqHdr);

        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            return new ApiMsg(
                    Message.rsp_blockNumber
                            .parseFrom(ApiUtils.parseBody(rsp).getData())
                            .getBlocknumber(),
                    cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(
                        "[blockNumber] {} exception: [{}]",
                        ErrId.getErrString(-104L),
                        e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), cast.OTHERS);
        }
    }

    public ApiMsg getBalance(Address address) {
        return getBalance(address, blockNumber().getObject());
    }

    public ApiMsg getBalance(Address address, long blockNumber) {

        if (!apiInst.isInitialized.get()) {
            return new ApiMsg(-1003);
        }

        if (blockNumber < -1L) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getBalance] {}", ErrId.getErrString(-129L));
            }
            return new ApiMsg(-129);
        }

        Message.req_getBalance reqBody =
                Message.req_getBalance
                        .newBuilder()
                        .setAddress(ByteString.copyFrom(address.toBytes()))
                        .setBlockNumber(blockNumber)
                        .build();

        byte[] reqHead =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER, Message.Servs.s_chain, Message.Funcs.f_getBalance);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            Message.rsp_getBalance resp =
                    Message.rsp_getBalance.parseFrom(ApiUtils.parseBody(rsp).getData());

            byte[] balance = resp.getBalance().toByteArray();
            if (balance == null) {
                return new ApiMsg(
                        Retcode.r_fail_null_rsp_VALUE,
                        "null balance for address " + address + " and blockNumber " + blockNumber,
                        cast.OTHERS);
            } else {
                return new ApiMsg(new BigInteger(balance), cast.OTHERS);
            }

        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getBalance] {}", ErrId.getErrString(-104L) + e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), cast.OTHERS);
        }
    }

    public ApiMsg getBlockByNumber(long blockNumber) {
        if (!apiInst.isInitialized.get()) {
            return new ApiMsg(-1003);
        }

        if (blockNumber < -1L) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getBlockByNumber] {}", ErrId.getErrString(-129L));
            }
            return new ApiMsg(-129);
        }

        Message.req_getBlockByNumber body =
                Message.req_getBlockByNumber.newBuilder().setBlockNumber(blockNumber).build();

        byte[] header =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER,
                        Message.Servs.s_chain,
                        Message.Funcs.f_getBlockByNumber);
        byte[] reqMsg = ByteUtil.merge(header, body.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);
        int code = this.apiInst.validRspHeader(rsp);

        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            return new ApiMsg(
                    toBlock(Message.rsp_getBlock.parseFrom(ApiUtils.parseBody(rsp).getData())),
                    cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getBlockByNumber] {}", ErrId.getErrString(-104L) + e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), cast.OTHERS);
        }
    }

    public ApiMsg getTransactionByBlockHashAndIndex(Hash256 blockHash, int index) {
        if (!apiInst.isInitialized.get()) {
            return new ApiMsg(-1003);
        }

        if (index < -1) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getTxByBlkHash&TxIdx] {}", ErrId.getErrString(-311L));
            }
            return new ApiMsg(-311);
        }

        Message.req_getTransactionByBlockHashAndIndex reqBody =
                Message.req_getTransactionByBlockHashAndIndex
                        .newBuilder()
                        .setBlockHash(ByteString.copyFrom(blockHash.toBytes()))
                        .setTxIndex(index)
                        .build();

        byte[] reqHead =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER,
                        Message.Servs.s_chain,
                        Message.Funcs.f_getTransactionByBlockHashAndIndex);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);
        int code = this.apiInst.validRspHeader(rsp);

        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            return new ApiMsg(
                    ApiUtils.toTransaction(
                            Message.rsp_getTransaction.parseFrom(
                                    ApiUtils.parseBody(rsp).getData())),
                    cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(
                        "[getTxByBlkHash&TxIdx] {} exception: [{}]",
                        ErrId.getErrString(-104L),
                        e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), cast.OTHERS);
        }
    }

    public ApiMsg getTransactionByBlockNumberAndIndex(long blockNumber, int index) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003, null);
        }

        if (blockNumber < -1L) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getTxByBlkNbr&TxIdx] {}", ErrId.getErrString(-310L));
            }
            return new ApiMsg(-310, null);
        }

        if (index < -1) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getTxByBlkNbr&TxIdx] {}", ErrId.getErrString(-311L));
            }
            return new ApiMsg(-311, null);
        }

        Message.req_getTransactionByBlockNumberAndIndex reqBody =
                Message.req_getTransactionByBlockNumberAndIndex
                        .newBuilder()
                        .setBlockNumber(blockNumber)
                        .setTxIndex(index)
                        .build();

        byte[] reqHead =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER,
                        Message.Servs.s_chain,
                        Message.Funcs.f_getTransactionByBlockNumberAndIndex);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            return new ApiMsg(
                    ApiUtils.toTransaction(
                            Message.rsp_getTransaction.parseFrom(
                                    ApiUtils.parseBody(rsp).getData())),
                    cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(
                        "[getTxByBlkNbr&TxIdx] {} exception: [{}]",
                        ErrId.getErrString(-104L),
                        e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), cast.OTHERS);
        }
    }

    public ApiMsg getTransactionByHash(Hash256 transactionHash) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        Message.req_getTransactionByHash reqBody =
                Message.req_getTransactionByHash
                        .newBuilder()
                        .setTxHash(ByteString.copyFrom(transactionHash.toBytes()))
                        .build();

        byte[] reqHead =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER,
                        Message.Servs.s_chain,
                        Message.Funcs.f_getTransactionByHash);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());
        byte[] rsp = this.apiInst.nbProcess(reqMsg);

        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            return new ApiMsg(
                    ApiUtils.toTransaction(
                            Message.rsp_getTransaction.parseFrom(
                                    ApiUtils.parseBody(rsp).getData())),
                    cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(
                        "[getTxByHash] {} exception: [{}]",
                        ErrId.getErrString(-104L),
                        e.getMessage());
            }
            return new ApiMsg(-104);
        }
    }

    public ApiMsg getBlockByHash(Hash256 blockHash) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        Message.req_getBlockByHash reqBody =
                Message.req_getBlockByHash
                        .newBuilder()
                        .setBlockHash(ByteString.copyFrom(blockHash.toBytes()))
                        .build();

        byte[] reqHead =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER,
                        Message.Servs.s_chain,
                        Message.Funcs.f_getBlockByHash);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);

        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            return new ApiMsg(
                    toBlock(Message.rsp_getBlock.parseFrom(ApiUtils.parseBody(rsp).getData())),
                    cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(
                        "[getBlkByHash] {} exception: [{}]",
                        ErrId.getErrString(-104L),
                        e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), cast.OTHERS);
        }
    }

    public ApiMsg getTransactionCount(Address address, long blockNumber) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        if (blockNumber < -1L) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getTxCount] {}", ErrId.getErrString(-310L));
            }
            return new ApiMsg(-310);
        }

        Message.req_getTransactionCount reqBody =
                Message.req_getTransactionCount
                        .newBuilder()
                        .setAddress(ByteString.copyFrom(address.toBytes()))
                        .setBlocknumber(blockNumber)
                        .build();

        byte[] reqHead =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER,
                        Message.Servs.s_chain,
                        Message.Funcs.f_getTransactionCount);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);

        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            return new ApiMsg(
                    Message.rsp_getTransactionCount
                            .parseFrom(ApiUtils.parseBody(rsp).getData())
                            .getTxCount(),
                    cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(
                        "[getTxCount] {} exception: [{}]",
                        ErrId.getErrString(-104L),
                        e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), cast.OTHERS);
        }
    }

    public ApiMsg getBlockTransactionCountByHash(Hash256 blockHash) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        Message.req_getBlockTransactionCountByHash reqBody =
                Message.req_getBlockTransactionCountByHash
                        .newBuilder()
                        .setBlockHash(ByteString.copyFrom(blockHash.toBytes()))
                        .build();

        byte[] reqHead =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER,
                        Message.Servs.s_chain,
                        Message.Funcs.f_getBlockTransactionCountByHash);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            return new ApiMsg(
                    Retcode.r_success_VALUE,
                    Message.rsp_getBlockTransactionCount
                            .parseFrom(ApiUtils.parseBody(rsp).getData())
                            .getTxCount(),
                    cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(
                        "[getBlkTxCntByHash] {} exception: [{}]",
                        ErrId.getErrString(-104L),
                        e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), cast.OTHERS);
        }
    }

    public ApiMsg getBlockTransactionCountByNumber(long blockNumber) {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        if (blockNumber < -1L) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getBlkTxCntByNumber] {}", ErrId.getErrString(-310L));
            }
            return new ApiMsg(-310);
        }

        Message.req_getBlockTransactionCountByNumber reqBody =
                Message.req_getBlockTransactionCountByNumber
                        .newBuilder()
                        .setBlockNumber(blockNumber)
                        .build();

        byte[] reqHead =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER,
                        Message.Servs.s_chain,
                        Message.Funcs.f_getBlockTransactionCountByNumber);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            return new ApiMsg(
                    Retcode.r_success_VALUE,
                    Message.rsp_getBlockTransactionCount
                            .parseFrom(ApiUtils.parseBody(rsp).getData())
                            .getTxCount(),
                    cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(
                        "[getBlkTxCntByNumber] {} exception: [{}]",
                        ErrId.getErrString(-104L),
                        e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), cast.OTHERS);
        }
    }

    // public long hashRate() {
    //    if (!this.apiInst.isConnected()) {
    //        LOGGER.isError(new Throwable().getStackTrace()[0].getMethodName() +
    // ErrId.getErrString(-1003L));
    //        return -1L;
    //    }
    //
    //    byte[] reqHdr = ApiUtils.toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_chain,
    // Message.Funcs.f_hashrate);
    //    byte[] rsp = this.apiInst.nbProcess(reqHdr);
    //
    //    if (this.apiInst.validRspHeader(rsp)) {
    //        return -1L;
    //    }
    //
    //    try {
    //        return Message.rsp_hashrate.parseFrom(ApiUtils.parseBody(rsp)).getHashrate();
    //    } catch (InvalidProtocolBufferException e) {
    //        LOGGER.isError(new Throwable().getStackTrace()[0].getMethodName() +
    // ErrId.getErrString(-104L) + e.getMessage());
    //        return -1L;
    //    }
    // }

    /**
     * A helper function intended to provide easy copying from response type to {@link
     * org.aion.api.type.Block}, intended for internal usage
     */
    private static Block toBlock(Message.rsp_getBlock rsp) {

        Block.BlockBuilder builder = new Block.BlockBuilder();

        List<Hash256> txs = new ArrayList<>();
        for (ByteString bs : rsp.getTxHashList()) {
            txs.add(Hash256.wrap(bs.toByteArray()));
        }

        return builder.bloom(ByteArrayWrapper.wrap(rsp.getLogsBloom().toByteArray()))
                .difficulty(new BigInteger(rsp.getDifficulty().toByteArray()))
                .extraData(ByteArrayWrapper.wrap(rsp.getExtraData().toByteArray()))
                .nonce(new BigInteger(rsp.getNonce().toByteArray()))
                .miner(AionAddress.wrap(rsp.getMinerAddress().toByteArray()))
                .nrgConsumed(rsp.getNrgConsumed())
                .nrgLimit(rsp.getNrgLimit())
                .txTrieRoot(Hash256.wrap(rsp.getTxTrieRoot().toByteArray()))
                .stateRoot(Hash256.wrap(rsp.getStateRoot().toByteArray()))
                .timestamp(rsp.getTimestamp())
                .receiptTxRoot(Hash256.wrap(rsp.getReceiptTrieRoot().toByteArray()))
                .number(rsp.getBlockNumber())
                .txHash(txs)
                .hash(Hash256.wrap(rsp.getHash().toByteArray()))
                .parentHash(Hash256.wrap(rsp.getParentHash().toByteArray()))
                .solution(ByteArrayWrapper.wrap(rsp.getSolution().toByteArray()))
                .size(rsp.getSize())
                .totalDifficulty(new BigInteger(rsp.getTotalDifficulty().toByteArray()))
                .createBlock();
    }

    /**
     * GetNonce returns a BigInteger representing the nonce of the account address at the latest
     * block number.
     *
     * @param address the class {@link Address Address} of the desired account to get the nonce of.
     * @return the account's nonce.
     */
    public ApiMsg getNonce(Address address) {
        if (!apiInst.isInitialized.get()) {
            return new ApiMsg(-1003);
        }

        Message.req_getNonce reqBody =
                Message.req_getNonce
                        .newBuilder()
                        .setAddress(ByteString.copyFrom(address.toBytes()))
                        .build();

        byte[] reqHead =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER, Message.Servs.s_chain, Message.Funcs.f_getNonce);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            Message.rsp_getNonce resp =
                    Message.rsp_getNonce.parseFrom(ApiUtils.parseBody(rsp).getData());

            byte[] nonce = resp.getNonce().toByteArray();
            if (nonce == null) {
                return new ApiMsg(
                        Retcode.r_fail_null_rsp_VALUE,
                        "null nonce for address " + address,
                        cast.OTHERS);
            } else {
                return new ApiMsg(new BigInteger(nonce), cast.OTHERS);
            }

        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getNonce] {}", ErrId.getErrString(-104L) + e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), cast.OTHERS);
        }
    }

    public ApiMsg getStorageAt(Address address, int position) {
        return getStorageAt(address, position, -1L);
    }

    public ApiMsg getStorageAt(Address address, int position, long blockNumber) {
        if (!apiInst.isInitialized.get()) {
            return new ApiMsg(-1003);
        }

        Message.req_getStorageAt reqBody =
                Message.req_getStorageAt
                        .newBuilder()
                        .setAddress(ByteString.copyFrom(address.toBytes()))
                        .setBlocknumber(blockNumber)
                        .setPosition(position)
                        .build();

        byte[] reqHead =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER, Message.Servs.s_chain, Funcs.f_getStorageAt);
        byte[] reqMsg = ByteUtil.merge(reqHead, reqBody.toByteArray());

        byte[] rsp = this.apiInst.nbProcess(reqMsg);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            Message.rsp_getStorageAt resp =
                    Message.rsp_getStorageAt.parseFrom(ApiUtils.parseBody(rsp).getData());

            String data = resp.getStorage();
            if (data == null) {
                return new ApiMsg(
                        Retcode.r_fail_null_rsp_VALUE,
                        "no storage on this addres " + address,
                        cast.OTHERS);
            } else {
                return new ApiMsg(data, cast.OTHERS);
            }

        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[getStorageAt] {}", ErrId.getErrString(-104L) + e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), cast.OTHERS);
        }
    }
}
