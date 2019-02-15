package org.aion.api;

import java.math.BigInteger;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.Block;
import org.aion.api.type.Transaction;
import org.aion.type.api.interfaces.common.Address;
import org.aion.type.api.interfaces.common.Hash;

/**
 * This interface provides methods for fetching blockchain specific details such as blocks and
 * transactions.
 *
 * @author Jay Tseng
 */
public interface IChain {

    /**
     * Returns the current block number of the Aion Kernel connected.
     *
     * @return The current block number by long value wrapped into ApiMsg. You can retrieve through
     *     the method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg blockNumber();

    /**
     * GetBalance returns a BigInteger representing the balance of the account address at latest
     * block number.
     *
     * @param address the class {@link Address Address} of the desired account to get the balance
     *     of.
     * @return balance of the desired account by the class {@link BigInteger BigInteger} wrapped
     *     into ApiMsg. You can retrieve through the method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg getBalance(Address address);

    /**
     * GetBalance returns a Long value representing the balance of the account address at a certain
     * block number.
     *
     * @param address the class {@link Address Address} of the desired account.
     * @param blockNumber the block number by long value at which the balance of the address should
     *     be retrieved from.
     * @return balance of the desired account by the class {@link BigInteger BigInteger} wrapped
     *     into ApiMsg. You can retrieve through the method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg getBalance(Address address, long blockNumber);

    /**
     * GetNonce returns a BigInteger representing the nonce of the account address at the latest
     * block number.
     *
     * @param address the class {@link Address Address} of the desired account to get the nonce of.
     * @return nonce of the desired account by the class {@link BigInteger BigInteger} wrapped into
     *     ApiMsg. You can retrieve through the method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg getNonce(Address address);

    /**
     * Gets the block corresponding to the block number.
     *
     * @param blockNumber the block number by long value of the desired block.
     * @return the class {@link Block block} format wrapped into ApiMsg. You can retrieve through
     *     the method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg getBlockByNumber(long blockNumber);

    /**
     * Gets a transaction given a block hash and transaction index.
     *
     * @param blockHash 32 bytes hash of the desired block wrapped into the class {@link Hash
     *     Hash}.
     * @param index the transaction position by int value of the transaction been stored into the
     *     desired block.
     * @return the class {@link Transaction transaction} information wrapped into ApiMsg. You can
     *     retrieve through the method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg getTransactionByBlockHashAndIndex(Hash blockHash, int index);

    /**
     * Gets a transaction based on the block number and transaction index.
     *
     * @param blockNumber the block number by long value of the desired block.
     * @param index the transaction position by int value of the transaction been stored into the
     *     desired block.
     * @return the class {@link Transaction transaction} wrapped into ApiMsg. You can retrieve
     *     through the method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg getTransactionByBlockNumberAndIndex(long blockNumber, int index);

    /**
     * Retrieves a block given the block hash.
     *
     * @param blockHash 32 bytes hash of the desired block wrapped into the class {@link Hash
     *     Hash}.
     * @return the class {@link Block block} format wrapped into ApiMsg. You can retrieve through
     *     the method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg getBlockByHash(Hash blockHash);

    /**
     * Retrieves the total transactions within a block at a given block hash.
     *
     * @param blockHash 32 bytes hash of the desired block wrapped into the class {@link Hash
     *     Hash}.
     * @return the transaction number of the block by int value wrapped into ApiMsg. You can
     *     retrieve through the method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg getBlockTransactionCountByHash(Hash blockHash);

    /**
     * Retrieves the total transactions within a block at a given block number.
     *
     * @param blockNumber the block number by long value of the desired block.
     * @return the transaction number of the block by int value wrapped into ApiMsg. You can
     *     retrieve through the method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg getBlockTransactionCountByNumber(long blockNumber);

    /**
     * Retrieves the total transactions committed by a certain account address at a given block
     * number.
     *
     * @param address the class {@link Address Address} of the desired account.
     * @param blockNumber the block number by long value of the desired block.
     * @return the transaction number of the block by long value wrapped into ApiMsg. You can
     *     retrieve through the method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg getTransactionCount(Address address, long blockNumber);

    /**
     * Retrieves the transaction given the transaction hash. Function will throw if transaction is
     * not found, or the transaction hash is improperly formatted.
     *
     * @param transactionHash 32 bytes hash of the desired transaction wrapped into the class {@link
     *     Hash Hash}.
     * @return the class {@link Transaction TransactionExtend} containing all relevant information related
     *     to the transaction wrapped into ApiMsg. You can retrieve through the method {@link
     *     ApiMsg#getObject() getObject}.
     */
    ApiMsg getTransactionByHash(Hash transactionHash);

    /**
     * Get the storage at a specific position of an address by current blockchain database status.
     *
     * @param address the class {@link Address Address} of the desired address to get the storage
     *     from.
     * @param position the index position of the storage.
     * @return the class {@link java.lang.String String} represent the hexString of the byte array
     *     wrapped into ApiMsg.
     */
    ApiMsg getStorageAt(Address address, int position);

    /**
     * Get the storage at a specific position of an address by giving blockNumber; Current kernel
     * version doesn't support the query status by giving blockNumber. please use
     * getStorageAt(Address address, int position) or giving the blockNumber = -1L.
     *
     * @param address the class {@link Address Address} of the desired address to get the storage
     *     from.
     * @param position the index position of the storage.
     * @param blockNumber the block number by long value of the desired block.
     * @return the class {@link java.lang.String String} represent the hexString of the byte array
     *     wrapped into ApiMsg.
     */
    ApiMsg getStorageAt(Address address, int position, long blockNumber);

    /// **
    // * Returns the current miner hashrate.
    // * @version 0.9.10
    // * @return The current hashing power measured by hashs per seconds.
    // */
    // long hashRate();

}
