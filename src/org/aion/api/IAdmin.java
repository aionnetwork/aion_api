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

package org.aion.api;

import org.aion.api.type.ApiMsg;
import org.aion.base.type.Address;

import java.util.List;

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
     * separated by comma [,] &/or dash [-]. eg. 600-650 eg. 700,701,750 eg. 700,800-900 eg.
     * 500-200
     * @return the interface {@link java.util.List List} of the class {@link
     * org.aion.api.type.BlockDetails BlockDetails} wrapped into ApiMsg. You can retrieve through
     * the method {@link ApiMsg#getObject() getObject}.
     * <p>
     * The kernel will only return the first 1000 block details fit the query interval if the user
     * query the block over the 1000 blocks
     * </p>
     */
    ApiMsg getBlockDetailsByNumber(String blkNum);

    /**
     * Get detailed block information include all transactions and the transaction logs by given the
     * List of block numbers.
     *
     * @param blkNum the list of block numbers, it can be discontinuous.
     * @return the interface {@link java.util.List List} of the class {@link
     * org.aion.api.type.BlockDetails BlockDetails} wrapped into ApiMsg. You can retrieve through
     * the method {@link ApiMsg#getObject() getObject}.
     * <p>
     * The kernel will only return the first 1000 block details fit the query interval if the user
     * query the block over the 1000 blocks
     * </p>
     */
    ApiMsg getBlockDetailsByNumber(List<Long> blkNum);

    /**
     * Get detailed block information as SQL like structure include all transactions and the transaction logs by given the
     * starting/end block number.
     *
     * @param blkStart the start block number.
     * @param blkEnd the end of block number.
     * @return the interface {@link java.util.List List} of the class {@link
     * org.aion.api.type.BlockDetails BlockDetails} wrapped into ApiMsg. You can retrieve through
     * the method {@link ApiMsg#getObject() getObject}.
     * <p>
     * The kernel will only return the first 1000 block details fit the query interval if the user
     * query the block over the 1000 blocks
     * </p>
     */
    ApiMsg getBlockDetailsByRange(Long blkStart, Long blkEnd);

    /**
     * Get detailed block information, for all blocks in range (latest - n, latest]
     *
     * @param count number of blocks from latest for which to retrieve block details
     * @return the interface {@link java.util.List List} of the class {@link
     * org.aion.api.type.BlockDetails BlockDetails} wrapped into ApiMsg. You can retrieve through
     * the method {@link ApiMsg#getObject() getObject}.
     * <p>
     * The kernel will only return the first 1000 block details fit the query interval if the user
     * query the block over the 1000 blocks
     * </p>
     */
    ApiMsg getBlockDetailsByLatest(Long count);

    /**
     * Get detailed block information as SQL like structure include all transactions and the transaction logs by given the
     * starting/end block number.
     *
     * @param blkStart the start block number.
     * @param blkEnd the end of block number.
     * @return the interface {@link java.util.List List} of the class {@link
     * org.aion.api.type.BlockSql BlockSql} wrapped into ApiMsg. You can retrieve through
     * the method {@link ApiMsg#getObject() getObject}.
     * <p>
     * The kernel will only return the first 1000 block details fit the query interval if the user
     * query the block over the 1000 blocks
     * </p>
     */
    ApiMsg getBlockSqlByRange(Long blkStart, Long blkEnd);

    /**
     * Get block information, for all blocks in range (latest - n, latest]
     *
     * @param count number of blocks from latest for which to retrieve block details
     * @return the interface {@link java.util.List List} of the class {@link org.aion.api.type.Block
     * Block} wrapped into ApiMsg. You can retrieve through the method {@link ApiMsg#getObject()
     * getObject}.
     * <p>
     * The kernel will only return the first 1000 block details fit the query interval if the user
     * query the block over the 1000 blocks
     * </p>
     */
    ApiMsg getBlocksByLatest(Long count);

    /**
     * Get detailed account information, given a string of addresses
     *
     * @param addresses the class {@link java.lang.String String} represent by the list of accounts
     * separated by comma [,]. eg. 0xA,0xB,0xC ...
     * @return the interface {@link java.util.List List} of the class {@link
     * org.aion.api.type.AccountDetails AccountDetails} wrapped into ApiMsg. You can retrieve
     * through the method {@link ApiMsg#getObject() getObject}.
     *
     * <p>
     * The kernel will only return the first 1000 account details that fit the query interval.
     * </p>
     */
    ApiMsg getAccountDetailsByAddressList(String addresses);

    /**
     * Get detailed account information, given a string of addresses.
     *
     * @param addresses list of addresses
     * @return the interface {@link java.util.List List} of the class {@link
     * org.aion.base.type.Address Address} wrapped into ApiMsg. You can retrieve through the method
     * {@link ApiMsg#getObject() getObject}.
     * <p>
     * The kernel will only return the first 1000 account details that fit the query interval.
     * </p>
     */
    ApiMsg getAccountDetailsByAddressList(List<Address> addresses);

    ///**
    // * Retrieves system information including DB size. CPU & Memory usage
    // *
    // * @return the {@link Types.SystemInfo SystemInfo} format wrapped into ApiMsg.
    // * You can retrieve through {@link ApiMsg#getObject() getObject}.
    // */
    //ApiMsg getSystemInfo();
}
