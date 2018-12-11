package org.aion.api;

import java.util.List;
import org.aion.api.impl.Contract;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.CompileResponse;
import org.aion.api.type.ContractDeploy;
import org.aion.api.type.ContractEventFilter;
import org.aion.api.type.DeployResponse;
import org.aion.api.type.MsgRsp;
import org.aion.api.type.TxArgs;
import org.aion.api.type.TxReceipt;
import org.aion.base.type.Address;
import org.aion.base.type.Hash256;
import org.aion.base.util.ByteArrayWrapper;

/**
 * ITx is an interface of the class Transaction. ITx assists in providing methods for transaction
 * related calls.
 *
 * @author Jay Tseng
 */
public interface ITx {

    /** Transaction price and limit is following by the current Aion-0 kernel definition. */
    long NRG_PRICE_MIN = 10_000_000_000L; // 10 PLAT  (10 * 10 ^ -9 AION)

    long NRG_PRICE_MAX = 9_000_000_000_000_000_000L; //  9 AION
    long NRG_LIMIT_CONTRACT_CREATE_MAX = 5_000_000L;
    long NRG_LIMIT_TX_MAX = 2_000_000L;
    long NRG_LIMIT_TX_MIN = 21_000L;

    /**
     * Deploys a new contract onto the Aion blockchain. Note that contract response via successful
     * execution does <b>not</b> indicate that the contract has been deployed if you use async mode.
     *
     * @param cd the class {@link ContractDeploy ContractDeploy} represent the contract deploy
     *     arguments. Use the class builder ContractDeployBuilder to create the class object.
     * @return the class {@link DeployResponse DeployResponse} information wrapped into ApiMsg. You
     *     can retrieve through {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg contractDeploy(ContractDeploy cd);

    /**
     * Initiates a call to retrieve the result from a contract.
     *
     * @param args given a class {@link TxArgs TxArgs} created by the builder class {@link
     *     org.aion.api.type.TxArgs.TxArgsBuilder TxArgsBuilder}.
     * @return the bytes array represent the function defined by the contract wrapped into ApiMsg.
     *     You can retrieve through {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg call(TxArgs args);

    /**
     * Estimates the Nrg required to execute transaction.
     *
     * @param args given a class {@link TxArgs TxArgs} created by the builder class {@link
     *     org.aion.api.type.TxArgs.TxArgsBuilder TxArgsBuilder}.
     * @return amount of energy by long value required to execute the transaction.
     */
    ApiMsg estimateNrg(TxArgs args);

    /**
     * Estimates the Nrg required to execute contract deploy.
     *
     * @param code the class {@link java.lang.String String} of the source code to be compiled.
     * @return amount of energy by long value required to execute the transaction.
     */
    ApiMsg estimateNrg(String code);

    /**
     * Retrieves the transaction receipt given a transaction hash in the format of {@link TxReceipt
     * TxReceipt}.
     *
     * @param transactionHash the 32 bytes hash represent by the class {@link Hash256 Hash256}.
     * @return the class {@link TxReceipt TxReceipt} wrapped into ApiMsg. You can retrieve through
     *     {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg getTxReceipt(Hash256 transactionHash);

    /**
     * Initiates a transaction given a class of type {@link TxArgs TxArgs}. This function provides
     * no functionality to assist with encoding or decoding transaction data. Users may choose to
     * either use the more convenient {@link Contract} class for contract transactions.
     *
     * @param args given a class {@link TxArgs TxArgs} created by the builder class {@link
     *     org.aion.api.type.TxArgs.TxArgsBuilder TxArgsBuilder}.
     * @return the class {@link MsgRsp MessageResponse} returned from backend and the current
     *     progress in the backend wrapped into ApiMsg. You can retrieve through {@link
     *     ApiMsg#getObject() getObject}.
     * @see Contract
     * @see MsgRsp
     */
    ApiMsg sendTransaction(TxArgs args);

    /**
     * Initiates a transaction given the class {@link TxArgs TxArgs}, the class {@link
     * ByteArrayWrapper ByteArrayWrapper} represent the private key of the transaction sender. Then
     * send a transaction including the raw transaction data and signed with the sender's key.
     *
     * @param args given a class {@link TxArgs TxArgs} created by the builder class {@link
     *     org.aion.api.type.TxArgs.TxArgsBuilder TxArgsBuilder}.
     * @param key the class {@link ByteArrayWrapper ByteArrayWrapper} represent the sender's private
     *     key.
     * @return the class {@link MsgRsp MessageResponse} returned from backend and the current
     *     progress in the backend wrapped into ApiMsg. You can retrieve through {@link
     *     ApiMsg#getObject() getObject}.
     * @see MsgRsp
     */
    ApiMsg sendSignedTransaction(TxArgs args, ByteArrayWrapper key);

    /**
     * Initiates a transaction given the class {@link ByteArrayWrapper ByteArrayWrapper} represent
     * the encoded transaction byte array with the sender's signature. This function provides no
     * functionality to assist with encoding or decoding transaction data. Users may choose to
     * either use the more convenient {@link Contract Contract} class for contract transactions.
     *
     * @param tx given a class {@link ByteArrayWrapper ByteArrayWrapper} represent the encoded
     *     transaction byte array with the sender's signature.
     * @return the class {@link MsgRsp MsgRsp} returned from backend and the current progress in the
     *     backend wrapped into ApiMsg. You can retrieve through {@link ApiMsg#getObject()
     *     getObject}.
     * @see Contract
     * @see MsgRsp
     */
    ApiMsg sendRawTransaction(ByteArrayWrapper tx);

    /**
     * Sends the source code to be compiled in the backend, and returns all relevant information
     * about the compiled code. Will throw if backend compiler is unavailable or code is improperly
     * formatted.
     *
     * @param code the class {@link java.lang.String String} of the source code to be compiled.
     * @return the interface {@link java.util.Map Map} of the class {@link CompileResponse
     *     CompileResponse} containing the contract name as key and as value wrapped into ApiMsg.
     *     You can retrieve through the method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg compile(String code);

    /**
     * Get the kernel contained solidity compiler version.
     *
     * @return the class {@link java.lang.String String} wrapped into ApiMsg. You can retrieve
     *     through the method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg getSolcVersion();

    /**
     * Retrieves the compiled code for a given contract with checking latest block.
     *
     * @param address the class {@link Address Address} represent the deployed contract address.
     * @return the variable bytes array of the compiled code wrapped into ApiMsg. You can retrieve
     *     through {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg getCode(Address address);

    /**
     * Retrieves the compiled code for a given contract.
     *
     * @param address the class {@link Address Address} represent the deployed contract address.
     * @param blockNumber block number of which the contract by long value was committed on.
     *     Indicate -1L to check for the latest block.
     * @return the variable bytes array of the compiled code wrapped into ApiMsg. You can retrieve
     *     through {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg getCode(Address address, long blockNumber);

    /**
     * Pre-build {@link TxArgs TxArgs} for increasing sendTransaction speeds if these transaction
     * must been executed repeatedly. This function provides no functionality to assist with
     * encoding or decoding transaction data. Users may choose to either use the more convenient
     * {@link Contract Contract} class for contract transactions.
     *
     * @param args the class {@link TxArgs TxArgs} represent the Transaction arguments.
     */
    void fastTxbuild(TxArgs args);

    /**
     * Pre-build {@link TxArgs TxArgs} for increasing sendTransaction speeds if these transaction
     * must been executed repeatedly. This function provides no functionality to assist with
     * encoding or decoding transaction data. Users may choose to either use the more convenient
     * Contract object for contract transactions.
     *
     * @param args the class {@link TxArgs TxArgs} represent the Transaction arguments.
     * @param call the boolean value represent the function of the contract is a call function.
     */
    void fastTxbuild(TxArgs args, boolean call);

    /**
     * Mark the transaction send to kernel and return immediately. The developer prefer to check the
     * transaction status later.
     *
     * @return {@link ITx ITx}.
     */
    ITx nonBlock();

    /**
     * Mark the transaction timeout. If the message of this transaction can not been done within
     * timeout, the transaction status will been dropped in the transaction pending pool. The
     * default timeout is 300 seconds. If user set timeout below than 30 seconds, the timeout will
     * been modified to 30 seconds.
     *
     * @param t timeout represent by millisecond.
     * @return Transaction object.
     */
    ITx timeout(int t);

    /**
     * Check the transaction progress given by the api session hash value.
     *
     * @param msgHash the 8 bytes array wrapped by the class {@link ByteArrayWrapper
     *     ByteArrayWrapper} represent the session hash the api client sent.
     * @return the class {@link MsgRsp MessageResponse} including transaction status and hash
     *     wrapped into ApiMsg. You can retrieve through the method {@link ApiMsg#getObject()
     *     getObject}.
     */
    ApiMsg getMsgStatus(ByteArrayWrapper msgHash);

    /// **
    // * Query the transaction events by given the contract event filter and the contract Address.
    // *
    // * @param ef
    // *         the class {@link ContractEventFilter ContractEventFilter} represent the event query
    // conditions.
    // * @param address
    // *         the class {@link Address Address} represent the deployed contract address.
    // * @return the interface {@link List List} of the class {@link ContractEvent ContractEvent}
    // fit the user's query condition.
    // * You can retrieve through the method {@link ApiMsg#getObject() getObject}.
    // */
    // ApiMsg queryEvents(ContractEventFilter ef, Address address);

    /**
     * remove the listening events by given the event name and the contract address.
     *
     * @param evt the interface {@link List List} of the class {@link String String} represent the
     *     events the user want to listen.
     * @param address the class {@link Address Address} represent the deployed contract address.
     * @return the boolean value represent the register success or failed.
     */
    ApiMsg eventDeregister(List<String> evt, Address address);

    /**
     * register the events the user want to listen by given the event name, event filter and the
     * contract address.
     *
     * @param evt the interface {@link List List} of the class {@link String String} represent the
     *     events the user want to listen.
     * @param ef the class {@link ContractEventFilter ContractEventFilter} represent the event query
     *     conditions.
     * @param address the class {@link Address Address} represent the deployed contract address.
     * @return the boolean value represent the register success or failed.
     */
    ApiMsg eventRegister(List<String> evt, ContractEventFilter ef, Address address);

    /**
     * getNrgPrice returns a Long value representing the recommended energy price.
     *
     * @return the recommended energy price as a long value wrapped into ApiMsg. You can retrieve
     *     through {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg getNrgPrice();

    //    void removeAllEvents();
    //
    //    void removeEvent(String e);
    //
    //    void setEvent(String e);
    //
    //    List<Event> getEvents(List<String> evtNames);
}
