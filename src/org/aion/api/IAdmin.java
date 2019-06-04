package org.aion.api;

import java.util.List;
import org.aion.aion_types.NewAddress;
import org.aion.api.type.ApiMsg;
import org.aion.base.type.Hash256;

/**
 * This interface dedicate to contact with Aion kernel for certain purpose.
 *
 * @author Jay Tseng
 */
public interface IAdmin {

    /**
     * Get detailed block information include all transactions and the transaction logs by given the
     * string of block numbers.*
     *
     * @param blkNum the class {@link java.lang.String String} represent by the list of blocks
     *     separated by comma [,] and/or dash [-]. eg. 600-650 eg. 700,701,750 eg. 700,800-900 eg.
     *     500-200
     * @return the interface {@link java.util.List List} of the class {@link
     *     org.aion.api.type.BlockDetails BlockDetails} wrapped into ApiMsg. You can retrieve
     *     through the method {@link ApiMsg#getObject() getObject}.
     *     <p>The kernel will only return the first 1000 block details fit the query interval if the
     *     user query the block over the 1000 blocks
     */
    ApiMsg getBlockDetailsByNumber(String blkNum);

    /**
     * Get detailed block information include all transactions and the transaction logs by given the
     * block number.
     *
     * @param blkNum long value represent the block number.
     * @return the class {@link org.aion.api.type.BlockDetails BlockDetails} wrapped into ApiMsg.
     *     You can retrieve through the method {@link ApiMsg#getObject() getObject}.
     *     <p>The kernel will only return the first 1000 block details fit the query interval if the
     *     user query the block over the 1000 blocks
     */
    ApiMsg getBlockDetailsByNumber(long blkNum);

    /**
     * Get detailed block information include all transactions and the transaction logs by given the
     * block hash.
     *
     * @param blockHash 32 bytes hash of the desired block wrapped into the class {@link Hash256
     *     Hash256}.
     * @return the class {@link org.aion.api.type.BlockDetails BlockDetails} wrapped into ApiMsg.
     *     You can retrieve through the method {@link ApiMsg#getObject() getObject}.
     *     <p>The kernel will only return the first 1000 block details fit the query interval if the
     *     user query the block over the 1000 blocks
     */
    ApiMsg getBlockDetailsByHash(Hash256 blockHash);

    /**
     * Get detailed block information include all transactions and the transaction logs by given the
     * List of block numbers.
     *
     * @param blkNum the list of block numbers, it can be discontinuous.
     * @return the interface {@link java.util.List List} of the class {@link
     *     org.aion.api.type.BlockDetails BlockDetails} wrapped into ApiMsg. You can retrieve
     *     through the method {@link ApiMsg#getObject() getObject}.
     *     <p>The kernel will only return the first 1000 block details fit the query interval if the
     *     user query the block over the 1000 blocks
     */
    ApiMsg getBlockDetailsByNumber(List<Long> blkNum);

    /**
     * Get detailed block information as SQL like structure include all transactions and the
     * transaction logs by given the starting/end block number.
     *
     * @param blkStart the start block number.
     * @param blkEnd the end of block number.
     * @return the interface {@link java.util.List List} of the class {@link
     *     org.aion.api.type.BlockDetails BlockDetails} wrapped into ApiMsg. You can retrieve
     *     through the method {@link ApiMsg#getObject() getObject}.
     *     <p>The kernel will only return the first 1000 block details fit the query interval if the
     *     user query the block over the 1000 blocks
     */
    ApiMsg getBlockDetailsByRange(Long blkStart, Long blkEnd);

    /**
     * Get detailed block information, for all blocks in range (latest - n, latest]
     *
     * @param count number of blocks from latest for which to retrieve block details
     * @return the interface {@link java.util.List List} of the class {@link
     *     org.aion.api.type.BlockDetails BlockDetails} wrapped into ApiMsg. You can retrieve
     *     through the method {@link ApiMsg#getObject() getObject}.
     *     <p>The kernel will only return the first 1000 block details fit the query interval if the
     *     user query the block over the 1000 blocks
     */
    ApiMsg getBlockDetailsByLatest(Long count);

    /**
     * Get detailed block information as SQL like structure include all transactions and the
     * transaction logs by given the starting/end block number.
     *
     * @param blkStart the start block number.
     * @param blkEnd the end of block number.
     * @return the interface {@link java.util.List List} of the class {@link
     *     org.aion.api.type.BlockSql BlockSql} wrapped into ApiMsg. You can retrieve through the
     *     method {@link ApiMsg#getObject() getObject}.
     *     <p>The kernel will only return the first 1000 block details fit the query interval if the
     *     user query the block over the 1000 blocks
     */
    ApiMsg getBlockSqlByRange(Long blkStart, Long blkEnd);

    /**
     * Get block information, for all blocks in range (latest - n, latest]
     *
     * @param count number of blocks from latest for which to retrieve block details
     * @return the interface {@link java.util.List List} of the class {@link org.aion.api.type.Block
     *     Block} wrapped into ApiMsg. You can retrieve through the method {@link ApiMsg#getObject()
     *     getObject}.
     *     <p>The kernel will only return the first 1000 block details fit the query interval if the
     *     user query the block over the 1000 blocks
     */
    ApiMsg getBlocksByLatest(Long count);

    /**
     * Get detailed account information, given a string of addresses
     *
     * @param addresses the class {@link java.lang.String String} represent by the list of accounts
     *     separated by comma [,]. eg. 0xA,0xB,0xC ...
     * @return the interface {@link java.util.List List} of the class {@link
     *     org.aion.api.type.AccountDetails AccountDetails} wrapped into ApiMsg. You can retrieve
     *     through the method {@link ApiMsg#getObject() getObject}.
     *     <p>The kernel will only return the first 1000 account details that fit the query
     *     interval.
     */
    ApiMsg getAccountDetailsByAddressList(String addresses);

    /**
     * Get detailed account information, given a string of addresses.
     *
     * @param addresses list of addresses
     * @return the interface {@link java.util.List List} of the class {@link NewAddress Address}
     *     wrapped into ApiMsg. You can retrieve through the method {@link ApiMsg#getObject()
     *     getObject}.
     *     <p>The kernel will only return the first 1000 account details that fit the query
     *     interval.
     */
    ApiMsg getAccountDetailsByAddressList(List<NewAddress> addresses);

    /// **
    // * Retrieves system information including DB size. CPU & Memory usage
    // *
    // * @return the {@link Types.SystemInfo SystemInfo} format wrapped into ApiMsg.
    // * You can retrieve through {@link ApiMsg#getObject() getObject}.
    // */
    // ApiMsg getSystemInfo();
}
