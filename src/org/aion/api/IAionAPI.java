package org.aion.api;

import org.aion.api.impl.AionAPIImpl;
import org.aion.api.type.ApiMsg;

/**
 * IAionAPI contains the majority of methods to interact with the Aion Kernel. To declare a instance
 * use the code shown:
 *
 * <p>IAionAPI api = IAionAPI.init(); All API functionality requires the user to connect to the Aion
 * Kernel utilizing {@link IAionAPI#connect(String)}.
 *
 * @author Jay Tseng
 */
public interface IAionAPI {

    /** Default LOCALHOST_URL "tcp://127.0.0.1:8547" */
    String LOCALHOST_URL = "tcp://127.0.0.1:8547";

    String VERSION = "0.1.17";

    static IAionAPI init() {
        return AionAPIImpl.inst();
    }

    /**
     * Destroys the socket connection with backend, utilize when operations between frontend and
     * backend client are done.
     *
     * @return the boolean value indicating the successful disconnect from backend wrapped into
     *     ApiMsg. You can retrieve through the method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg destroyApi();

    /**
     * Establishes connection between Aion Java API and Aion Kernel.
     *
     * @param url the class {@link java.lang.String} including the connection address and port of
     *     the desired backend Aion client.
     * @return the boolean value indicating the success of the connection wrapped into ApiMsg. You
     *     can retrieve through the method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg connect(String url);

    /**
     * Establishes connection between Aion Java API and Aion Kernel.
     *
     * @param url the {@link java.lang.String String} including the connection address and port of
     *     the desired backend Aion client.
     * @param reconnect the boolean value set the client api will try to reconnect when the
     *     connection broken. If retry reach 10 times fail, will return a false message.
     * @return the boolean value indicating the success of the connection wrapped into ApiMsg. You
     *     can retrieve through the method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg connect(String url, boolean reconnect);

    /**
     * Establishes connection between Aion Java API and Aion Kernel.
     *
     * @param url the {@link java.lang.String String} including the connection address and port of
     *     the desired backend Aion client.
     * @param reconnect the boolean value set the client api will try to reconnect when the
     *     connection broken. If retry reach 10 times fail, will return a false message.
     * @param pubkey the string value represent the public key of the connecting server. The zmq
     *     socket will setup a secure connect to the server.
     * @return the boolean value indicating the success of the connection wrapped into ApiMsg. You
     *     can retrieve through the method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg connect(String url, boolean reconnect, String pubkey);

    /**
     * Establishes connection between Aion Java API and ion Kernel.
     *
     * @param url the class {@link java.lang.String String} including the connection address and
     *     port of the desired backend Aion client.
     * @param worker set the thread number for handle the transaction API. The default is one
     *     worker.
     * @param pubkey the string value represent the public key of the connecting server. The zmq
     *     socket will setup a secure connect to the server.
     * @return the boolean value indicating the success of the connection wrapped into ApiMsg. You
     *     can retrieve through the method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg connect(String url, int worker, String pubkey);

    /**
     * Establishes connection between Aion Java API and Aion Kernel.
     *
     * @param url the class {@link java.lang.String String} including the connection address and
     *     port of the desired backend Aion client.
     * @param reconnect the boolean value set the client api will try to reconnect when the
     *     connection broken. If retry reach 10 times fail, will return a false message.
     * @param worker set the thread number for handle the transaction API. The default is one
     *     worker.
     * @param pubkey the string value represent the public key of the connecting server. The zmq
     *     socket will setup a secure connect to the server.
     * @return the boolean value indicating the success of the connection wrapped into ApiMsg. You
     *     can retrieve through the method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg connect(String url, boolean reconnect, int worker, String pubkey);

    /**
     * Establishes connection between Aion Java API and Aion Kernel.
     *
     * @param url the class {@link java.lang.String String} including the connection address and
     *     port of the desired backend Aion client.
     * @param reconnect the boolean value set the client api will try to reconnect when the
     *     connection broken. If retry reach 10 times fail, will return a false message.
     * @param worker set the thread number for handle the transaction API.
     * @param timeout set the timeout for the api message does not have the response from server
     * @param pubkey the string value represent the public key of the connecting server. The zmq
     *     socket will setup a secure connect to the server.
     * @return the boolean value indicating the success of the connection wrapped into ApiMsg. You
     *     can retrieve through the method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg connect(String url, boolean reconnect, int worker, int timeout, String pubkey);

    /**
     * Check the connection status between Aion Java API and Aion Kernel.
     *
     * @return the boolean value indicating the success of the connection.
     */
    boolean isConnected();

    /**
     * Get Net Module for call network relative APIs.
     *
     * @return the class {@link INet INet}.
     */
    INet getNet();

    /**
     * Get Chain Module for query the blockchain information relative APIs.
     *
     * @return the class {@link IChain IChain}.
     */
    IChain getChain();

    /**
     * Get Mine Module for call miner relative APIs.
     *
     * @return the class {@link IMine IMine}.
     * @see IMine
     */
    IMine getMine();

    /**
     * Get Transaction Module for deploy contract and transaction relative APIs.
     *
     * @return the class {@link ITx ITx}.
     */
    ITx getTx();

    /**
     * Get Wallet Module for account operation relative APIs.
     *
     * @return the class {@link IWallet IWallet}.
     * @see IWallet
     */
    IWallet getWallet();

    /**
     * Get Utils Module for cast data format.
     *
     * @return the class {@link IUtils IUtils}.
     */
    IUtils getUtils();

    /**
     * Get Account Module for account generate import/export relative APIs.
     *
     * @return the class {@link IAccount IAccount}.
     */
    IAccount getAccount();

    /**
     * Get ContractController Module for contract deploy/transaction operation relative APIs.
     *
     * @return the class {@link IContractController IContractController}.
     */
    IContractController getContractController();

    /**
     * Get Admin Module for dedicate to get Aion kernel information relative APIs.
     *
     * @return the class {@link IAccount IAccount}.
     * @see IAccount
     */
    IAdmin getAdmin();

    /**
     * Get Aion Api Version.
     *
     * @return {@link java.lang.String String}.
     */
    static String API_VERSION() {
        return VERSION;
    }
}
