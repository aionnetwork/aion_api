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

package org.aion.api.impl.internal;

import com.google.protobuf.ByteString;
import org.aion.api.impl.Account;
import org.aion.api.impl.Utils;
import org.aion.api.keccak.Keccak;
import org.aion.api.keccak.Keccak256;
import org.aion.api.type.*;
import org.aion.base.type.Address;
import org.aion.base.type.Hash256;
import org.aion.base.util.ByteArrayWrapper;
import org.aion.base.util.ByteUtil;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.aion.api.IUtils.bytes2Hex;

public class ApiUtils {
    public static final int PROTOCOL_VER = 2;
    public static final int MSG_HASH_LEN = 8;
    public static final int RSP_HEADER_NOHASH_LEN = 3;
    public static final int REQ_HEADER_NOHASH_LEN = 4;
    public static final int RSP_HEADER_LEN = RSP_HEADER_NOHASH_LEN + MSG_HASH_LEN;
    public static final int REQ_HEADER_LEN = REQ_HEADER_NOHASH_LEN + MSG_HASH_LEN;
    public static final byte[] EMPTY_MSG_HASH = new byte[MSG_HASH_LEN];
    public static final Keccak256 KC_256 = new Keccak256();
    private static Random rm = new SecureRandom();

    private ApiUtils() {}

    /**
     * Assembles a byte array, intended for internal usage
     *
     * @param vers
     * @param serv
     * @param func
     * @param hash
     * @return
     */
    public static byte[] toReqHeader(int vers, Message.Servs serv, Message.Funcs func, ByteArrayWrapper hash) {
        if (hash.getData().length != MSG_HASH_LEN) {
            return ByteArrayWrapper.NULL_BYTE;
        }

        return ByteUtil.merge(toReqHeader(vers, serv, func, true), hash.getData());
    }

    /**
     * Assembles a byte array, intended for internal usage
     *
     * @param vers
     * @param serv
     * @param func
     * @return
     */
    public static byte[] toReqHeader(int vers, Message.Servs serv, Message.Funcs func) {
        return toReqHeader(vers, serv, func, false);
    }

    public static byte[] toReqHeader(int vers, Message.Servs serv, Message.Funcs func, boolean hasHash) {
        byte[] result = new byte[4];
        result[0] = (byte) vers;
        result[1] = (byte) serv.ordinal();
        result[2] = (byte) func.ordinal();
        result[3] = (byte) (hasHash ? 1 : 0);
        return result;
    }


    /**
     * A helper function intended to provide easy copying from response type to
     * {@link org.aion.api.type.Transaction}, intended for internal usage
     *
     * @param rsp
     * @return
     */
    public static Transaction toTransaction(Message.rsp_getTransaction rsp) {

        return new Transaction.TransactionBuilder()
                .blockHash(Hash256.wrap(rsp.getBlockhash().toByteArray()))
                .blockNumber(rsp.getBlocknumber())
                .data(ByteArrayWrapper.wrap(rsp.getData().toByteArray()))
                .from(Address.wrap(rsp.getFrom().toByteArray()))
                .to(Address.wrap(rsp.getTo().toByteArray()))
                .timeStamp(rsp.getTimeStamp())
                .nonce(new BigInteger(rsp.getNonce().toByteArray()))
                .value(new BigInteger(rsp.getValue().toByteArray()))
                .txHash(Hash256.wrap(rsp.getTxHash().toByteArray()))
                .nrgPrice(rsp.getNrgPrice())
                .nrgConsumed(rsp.getNrgConsume())
                .createTransaction();
    }

    /**
     * A helper function intended to provide easy copying from response type to
     * {@link TxReceipt}, intended for internal usage
     *
     * @param rsp
     * @return
     */
    public static TxReceipt toTransactionReceipt(Message.rsp_getTransactionReceipt rsp) {

        TxReceipt.TxReceiptBuilder builder = new TxReceipt.TxReceiptBuilder();

        List<TxLog> txLogList = new ArrayList<>();
        for (Message.t_LgEle tl : rsp.getLogsList()) {

            List<String> topics = new ArrayList<>();
            for (String bs : tl.getTopicsList()) {
                topics.add(bs);
            }

            txLogList.add(new TxLog(Address.wrap(tl.getAddress().toByteArray()),
                    ByteArrayWrapper.wrap(tl.getData().toByteArray()),
                    topics));
        }

        return  builder.blockHash(Hash256.wrap(rsp.getBlockHash().toByteArray()))
                .blockNumber(rsp.getBlockNumber())
                .contractAddress(Address.wrap(rsp.getContractAddress().toByteArray()))
                .cumulativeNrgUsed(rsp.getCumulativeNrgUsed())
                .from(Address.wrap(rsp.getFrom().toByteArray()))
                .to(Address.wrap(rsp.getTo().toByteArray()))
                .nrgConsumed(rsp.getNrgConsumed())
                .txHash(Hash256.wrap(rsp.getTxHash().toByteArray()))
                .txIndex(rsp.getTxIndex())
                .txLogs(txLogList)
                .createTxReceipt();
    }


    /**
     * Returns a 32 byte left padded hex string with character '0'
     *
     * @param bytes
     * @return 64 character (32 byte) left padded hex string
     */
    public static String toHexPadded(byte[] bytes) {
        String zeroes = "0000000000000000" + "0000000000000000" + "0000000000000000" + "0000000000000000";

        String data = bytes2Hex(bytes);

        String rtn = data.length() <= 64 ? zeroes.substring(data.length()) + data : data;
        return rtn;
    }

    /**
     * Returns a 16 byte left padded hex string with character '0'
     *
     * @param bytes
     * @return 32 character (16 byte) left padded hex string
     */
    public static String toHexPadded16(byte[] bytes) {
        String zeroes = "0000000000000000" + "0000000000000000";

        String data = bytes2Hex(bytes);

        String rtn = data.length() <= 32 ? zeroes.substring(data.length()) + data : data;
        return rtn;
    }

    /**
     * Returns a 32 byte left padded hex string with character 'f'
     *
     * @param bytes
     * @return 64 character (32 byte) left padded hex string
     */
    public static String toHexPaddedNegative(byte[] bytes) {

        String zeroes = "ffffffffffffffff" + "ffffffffffffffff" + "ffffffffffffffff" + "ffffffffffffffff";

        String data = bytes2Hex(bytes);

        return data.length() <= 64 ? zeroes.substring(data.length()) + data : data;
    }

    /**
     * Returns a 16 byte left padded hex string with character 'f'
     *
     * @param bytes
     * @return 32 character (16 byte) left padded hex string
     */
    public static String toHexPaddedNegative16(byte[] bytes) {

        String zeroes = "ffffffffffffffff" + "ffffffffffffffff";

        String data = bytes2Hex(bytes);

        return data.length() <= 32 ? zeroes.substring(data.length()) + data : data;
    }

    /**
     * Returns a right padded '0' string, given an input string and padding
     * length
     *
     * @param in
     * @param padding
     * @return
     */
    public static String padRight(String in, int padding) {
        int padLength = padding - in.length();
        StringBuilder rtn = new StringBuilder(in);

        //TODO: Find a more elegant way
        for (int i = 0; i < padLength; i++) {
            rtn.append("0");
        }

        return rtn.toString();
    }

    /**
     * Returns a 4 byte array given an integer input
     *
     * @param i
     *         integer input
     * @return 4 byte array
     */
    public static byte[] toBytes(int i) {
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i /*>> 0*/);

        return result;
    }

    /**
     * Returns the byte representation of a hex string, note that this function
     * translates the hex into byte, not a direct getBytes() conversion.
     *
     * @param hexstr
     * @return byte array reprsentation of hex string
     */
    public static byte[] hex2Bytes(String hexstr) {
        int len = hexstr.length();
        byte[] out_arr = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            out_arr[i / 2] = (byte) ((Character.digit(hexstr.charAt(i), 16) << 4) + Character
                    .digit(hexstr.charAt(i + 1), 16));
        }
        return out_arr;
    }

    // KeccakHash-256 (~2012 implementation) different from SHA3-256
    @Deprecated
    public static String keccak256(byte[] in) {
        String hexInput = Utils.bytes2Hex(in);
        Keccak k = new Keccak(1600);
        String hashed = k.getHash(hexInput, 1088, 32);
        return hashed;
    }

    /**
     * Returns an integer given a byte array, will always assume big endian and
     * that integer is located in the last 4 bytes of the array.
     *
     * @param in
     *         byte array input
     * @return integer
     */
    public static int toInt(byte[] in) {
        ByteBuffer wrapped = ByteBuffer.wrap(in, in.length - 5, 4);
        return wrapped.getInt();
    }

    // assumes atleast a 4 length byte array

    /**
     * Returns an integer given a byte array, with controls for offset and
     * length, assumes integer data begins in index
     * <pre>(offset + length - 4)</pre>, and is 4 bytes long.
     *
     * @param in
     *         byte array input
     * @return integer
     */
    public static int toInt(byte[] in, int offset, int length) {
        ByteBuffer wrapped = ByteBuffer.wrap(in, offset + length - 4, 4);
        return wrapped.getInt();
    }

    /**
     * Returns an Long given a byte array, will always assume big endian and
     * that integer is located in the last 8 bytes of the array.
     *
     * @param in
     *         byte array input
     * @return integer
     */
    public static long toLong(byte[] in) {
        ByteBuffer wrapped = ByteBuffer.wrap(in, in.length - 9, 8);
        return wrapped.getLong();
    }

    /**
     * Returns an Long given a byte array, with controls for offset and length,
     * assumes integer data begins in index
     * <pre>(offset + length - 8)</pre>, and is 8 bytes long.
     *
     * @param in
     *         byte array input
     * @return integer
     */
    public static long toLong(byte[] in, int offset, int length) {
        ByteBuffer wrapped = ByteBuffer.wrap(in, offset + length - 8, 8);
        return wrapped.getLong();
    }

    /**
     * Returns an unsigned Long given a byte array, will always assume big
     * endian and that integer is located in the last 8 bytes of the array.
     *
     * @param in
     *         byte array input
     * @return integer
     */
    public static long toUnsignedLong(byte[] in) {
        long result = 0L;
        for (int i = 0; i < 8; i++) {
            result |= ((long) in[in.length - 1 - i] & 0xff) << 8 * i;
        }

        return result;
    }

    /**
     * Returns an unsigned Long given a byte array, with controls for offset and
     * length, assumes integer data begins in index
     * <pre>(offset + length - 8)</pre>, and is 8 bytes long.
     *
     * @param in
     *         byte array input
     * @return integer
     */
    public static long toUnsignedLong(byte[] in, int offset, int length) {
        long result = 0L;
        for (int i = 0; i < 8; i++) {
            result |= ((long) in[offset + length - 1 - i] & 0xff) << 8 * i;
        }

        return result;
    }

    public static byte[] toTwosComplement(Integer in) {
        return ByteBuffer.allocate(4).putInt(in).array();
    }

    public static byte[] toTwosComplement(Long in) {
        return ByteBuffer.allocate(8).putLong(in).array();
    }

    // Solidity Type Checkers
    public static boolean isTypeAddress(String in) {
        return Pattern.matches("address((\\[([0-9]*)\\])+)?", in);
    }

    public static boolean isTypeBoolean(String in) {
        return Pattern.matches("^bool(\\[([0-9]*)\\])*$", in);
    }

    public static boolean isTypeBytes(String in) {
        return Pattern.matches("^bytes([0-9]{1,})(\\[([0-9]*)\\])*$", in);
    }

    public static boolean isTypeDynamicBytes(String in) {
        return Pattern.matches("^bytes(\\[([0-9]*)\\])*$", in);
    }

    public static boolean isTypeInt(String in) {
        return Pattern.matches("^int([0-9]*)?(\\[([0-9]*)\\])*$", in);
    }

    public static boolean isTypeReal(String in) {
        return Pattern.matches("real([0-9]*)?(\\[([0-9]*)\\])?", in);
    }

    public static boolean isTypeString(String in) {
        return Pattern.matches("^string(\\[([0-9]*)\\])*$", in);
    }

    public static boolean isTypeUint(String in) {
        return Pattern.matches("^uint([0-9]*)?(\\[([0-9]*)\\])*$", in);
    }

    public static boolean isTypeUreal(String in) {
        return Pattern.matches("ureal([0-9]*)?(\\[([0-9]*)\\])?", in);
    }

    public static byte[] genHash(int hashlen) {
        return ByteBuffer.allocate(hashlen).put(keccak(String.valueOf(rm.nextInt()).getBytes()), 0, hashlen).array();
    }

    /**
     * Keccak 256 hashing
     *
     * @param in
     *         msgHash input
     * @return hashed representation
     */
    public static byte[] keccak(byte[] in) {
        Keccak256 hasher = new Keccak256();
        return hasher.digest(in);
    }

    public static ByteArrayWrapper parseHash(byte[] rsp) {
        return ByteArrayWrapper.wrap(Arrays.copyOfRange(rsp, RSP_HEADER_NOHASH_LEN, RSP_HEADER_NOHASH_LEN + MSG_HASH_LEN));
    }

    public static ByteArrayWrapper parseBody(byte[] rsp) {
        boolean hasHash = (rsp[2] == 1 ? true : false);
        int bodyLen = rsp.length - (hasHash ? RSP_HEADER_LEN : RSP_HEADER_NOHASH_LEN);

        if (hasHash) {
            return ByteArrayWrapper.wrap(Arrays.copyOfRange(rsp, RSP_HEADER_LEN, RSP_HEADER_LEN + bodyLen));
        } else {
            return ByteArrayWrapper.wrap(Arrays.copyOfRange(rsp, RSP_HEADER_NOHASH_LEN, RSP_HEADER_NOHASH_LEN + bodyLen));
        }
    }

    public static boolean isTypeBoolean(byte b) {
        return b == 1 ? true : false;
    }

    public static boolean endTxStatus(int status) {
        return status == Message.Retcode.r_tx_Included_VALUE || status == Message.Retcode.r_tx_Dropped_VALUE
                || status <= Message.Retcode.r_wallet_nullcb_VALUE;
    }

    public static List<Key> toKey(Message.rsp_accountCreate account, boolean pk) {
        if (account == null) {
            throw new NullPointerException();
        }

        if (pk && account.getAddressList().size() != account.getPrivateKeyList().size()) {
            throw new IllegalArgumentException();
        }

        List<Key> keys = new ArrayList<>();
        for (int i=0 ; i < account.getAddressList().size() ; i++) {
            Key key = new Key(Address.wrap(account.getAddress(i).toByteArray()),
                            pk ? ByteArrayWrapper.wrap(account.getPrivateKey(i).toByteArray()) : ByteArrayWrapper.wrap(ByteArrayWrapper.NULL_BYTE));

            keys.add(key);
        }

        return keys;
    }

    public static KeyExport toKeyExport(Message.rsp_exportAccounts rsp_exportAccounts) {
        if (rsp_exportAccounts == null) {
            throw new NullPointerException();
        }

        List<ByteArrayWrapper> keyFiles = rsp_exportAccounts.getKeyFileList()
                .parallelStream()
                .map(bs -> ByteArrayWrapper.wrap(bs.toByteArray()))
                .collect(Collectors.toList());

        List<Address> invalidAddr = rsp_exportAccounts.getFailedKeyList()
                .parallelStream()
                .map(bs -> Address.wrap(bs.toByteArray()))
                .collect(Collectors.toList());


        return new KeyExport(keyFiles, invalidAddr);
    }

    public static List<BlockDetails> toBlockDetails(List<Message.t_BlockDetail> blkDetails) {
        if (blkDetails == null) {
            throw new NullPointerException();
        }

        List<BlockDetails> rtn = new ArrayList<>();
        for (Message.t_BlockDetail bd : blkDetails) {
            BlockDetails.BlockDetailsBuilder bdBuilder = new BlockDetails.BlockDetailsBuilder()
                    .bloom(ByteArrayWrapper.wrap(bd.getLogsBloom().toByteArray()))
                    .difficulty(new BigInteger(1, bd.getDifficulty().toByteArray()))
                    .extraData(ByteArrayWrapper.wrap(bd.getExtraData().toByteArray()))
                    .miner(Address.wrap(bd.getMinerAddress().toByteArray()))
                    .nonce(new BigInteger(1, bd.getNonce().toByteArray()))
                    .nrgConsumed(bd.getNrgConsumed())
                    .nrgLimit(bd.getNrgLimit())
                    .number(bd.getBlockNumber())
                    .parentHash(Hash256.wrap(bd.getParentHash().toByteArray()))
                    .hash(Hash256.wrap(bd.getHash().toByteArray()))
                    .receiptTxRoot(Hash256.wrap(bd.getReceiptTrieRoot().toByteArray()))
                    .size(bd.getSize())
                    .solution(ByteArrayWrapper.wrap(bd.getSolution().toByteArray()))
                    .stateRoot(Hash256.wrap(bd.getStateRoot().toByteArray()))
                    .timestamp(bd.getTimestamp())
                    .txTrieRoot(Hash256.wrap(bd.getTxTrieRoot().toByteArray()))
                    .totalDifficulty(new BigInteger(1, bd.getTotalDifficulty().toByteArray()));

            List<TxDetails> txDetails = new ArrayList<>();
            for (Message.t_TxDetail td : bd.getTxList()) {
                TxDetails.TxDetailsBuilder txBuilder = new TxDetails.TxDetailsBuilder()
                        .data(ByteArrayWrapper.wrap(td.getData().toByteArray()))
                        .from(Address.wrap(td.getFrom().toByteArray()))
                        .to(Address.wrap(td.getTo().toByteArray()))
                        .contract(Address.wrap(td.getContract().toByteArray()))
                        .txHash(Hash256.wrap(td.getTxHash().toByteArray()))
                        .txIndex(td.getTxIndex())
                        .nonce(new BigInteger(1, td.getNonce().toByteArray()))
                        .value(new BigInteger(1, td.getValue().toByteArray()))
                        .nrgConsumed(td.getNrgConsumed())
                        .nrgPrice(td.getNrgPrice());

                List<TxLog> txLogs = new ArrayList<>();
                for (Message.t_LgEle log : td.getLogsList()) {
                    TxLog txlog = new TxLog(Address.wrap(log.getAddress().toByteArray())
                                            , ByteArrayWrapper.wrap(log.getData().toByteArray())
                                            , log.getTopicsList());
                    txLogs.add(txlog);
                }
                txDetails.add(txBuilder.logs(txLogs).createTxDetails());
            }
            rtn.add(bdBuilder.txDetails(txDetails).createBlockDetails());
        }

        return rtn;
    }

    public static List<Block> toBlocks(List<Message.t_Block> blks) {
        if (blks == null) {
            throw new NullPointerException();
        }

        List<Block> rtn = new ArrayList<>();
        for (Message.t_Block b : blks) {
            List<Hash256> txs = new ArrayList<>();
            for (ByteString bs : b.getTxHashList()) {
                txs.add(Hash256.wrap(bs.toByteArray()));
            }
            Block built = new Block.BlockBuilder()
                .bloom(ByteArrayWrapper.wrap(b.getLogsBloom().toByteArray()))
                .difficulty(new BigInteger(b.getDifficulty().toByteArray()))
                .extraData(ByteArrayWrapper.wrap(b.getExtraData().toByteArray()))
                .nonce(new BigInteger(b.getNonce().toByteArray()))
                .miner(Address.wrap(b.getMinerAddress().toByteArray()))
                .nrgConsumed(b.getNrgConsumed())
                .nrgLimit(b.getNrgLimit())
                .txTrieRoot(Hash256.wrap(b.getTxTrieRoot().toByteArray()))
                .stateRoot(Hash256.wrap(b.getStateRoot().toByteArray()))
                .timestamp(b.getTimestamp())
                .receiptTxRoot(Hash256.wrap(b.getReceiptTrieRoot().toByteArray()))
                .number(b.getBlockNumber())
                .txHash(txs)
                .parentHash(Hash256.wrap(b.getParentHash().toByteArray()))
                .solution(ByteArrayWrapper.wrap(b.getSolution().toByteArray()))
                .size(b.getSize())
                .totalDifficulty(new BigInteger(b.getTotalDifficulty().toByteArray()))
                .createBlock();

            rtn.add(built);
        }

        return rtn;
    }

    public static List<AccountDetails> toAccountDetails (List<Message.t_AccountDetail> accs) {
        if (accs == null) {
            throw new NullPointerException();
        }

        List<AccountDetails> rtn = new ArrayList<>();
        for (Message.t_AccountDetail a : accs) {
            AccountDetails built = new AccountDetails.AccountDetailsBuilder()
                    .address(Address.wrap(a.getAddress().toByteArray()))
                    .balance(new BigInteger(a.getBalance().toByteArray()))
                    .createAccountDetails();

            rtn.add(built);
        }

        return rtn;
    }
}
