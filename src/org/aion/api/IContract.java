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

import java.util.List;
import org.aion.api.sol.ISolidityArg;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.ContractAbiEntry;
import org.aion.api.type.ContractEvent;
import org.aion.api.type.ContractEventFilter;
import org.aion.api.type.ContractResponse;
import org.aion.api.type.JsonFmt;
import org.aion.api.type.TxArgs;
import org.aion.base.type.AionAddress;
import org.aion.base.type.Hash256;
import org.aion.base.util.ByteArrayWrapper;

/**
 * A Contract class that sits above the Aion Java API layer that provides the user with convenient
 * methods of encoding and decoding contract calls and transactions from the Aion Kernel. The
 * IContract interface has no public constructors, instead the user utilizes the {@link
 * IContractController IContractController} to create the Contract.
 *
 * <pre>{@code
 * IAionAPI api = IAionAPI.init();
 * IContractController cc = new ContractController(api);
 * IContractController.createFromSource(String, Address, long, long) or
 * IContractController.getContractAt(Address, Address, String)
 * }
 *
 * A factory method for creating a contract object from an ABI Definition and contract address.
 * This helper function takes a JSON string of the ABI definition and the Address class represent the contract
 * address of the desired contract.
 *
 * param from
 *      the class Address represent the sender.
 * param contractAddress
 *      the class Address represent the  contract address.
 * param abiDef
 *      JSON string of the ABI definition
 *
 * Several factory methods for creating a contract object from smart contract code.
 *
 * param source smart contract code.
 * param from deploy by whom.
 * param nrglimit the energy maximum consume for deploy the new contract.
 * param nrgprice the unit price for the energy.
 * param value (optional) the balance of the the sender's account
 * param params (optional) assign parameters when Contract has constructor (function name is the same as contract name),
 *
 *
 * for deploy
 * A multi-contract source, set key value pair when you want to deploy the contract "key" with initial parameters "value".
 *
 * for class instantiation.
 * This class requires the user tp provide an active and connected the class {@link IAionAPI IAionAPI}.
 * </pre>
 *
 * @see IContractController IContractController
 */
public interface IContract {

    /**
     * Initiates a new contract function call. Intended usage is to use this function in conjunction
     * with {@link IContract#setParam(ISolidityArg) setParam}, {@link #build() build} and {@link
     * IContract#execute() execute} to execute a contract function call as a chain call. Intended
     * usage is shown as below :
     *
     * <pre>{@code
     * IAionAPI api = IAionAPI.init();
     * IContractController cc = new ContractController(api);
     * cc.createFromSource(contract_source, deployer, energyLimit, energyPrice)
     * IContract c = cc.getContract(contract_address)
     * c.build('contract_function')
     *      .setParam(param1)
     * 	    .setParam(param2)
     * 		.setParam(param3)
     *      .setTxNrgLimit(value)
     *      .setTxNrgPrice(value)
     *      .setTxValue(value)
     * 		.build()
     * 		.execute(); //executes function call
     * }</pre>
     *
     * @param f name of the function call by {@link java.lang.String String} exactly as it appears
     *     in the ABI definition.
     * @return the contract interface {@link IContract IContract}.
     */
    IContract newFunction(String f);

    /**
     * Sets a parameter of the function, the order in which this function is called must be in order
     * of the ABI definition. Type and size checks are done in {@link #build() build} to ensure
     * parameter order and size are correct.
     *
     * @param val the Solidity Type like {@link org.aion.api.sol.IInt IInt}, {@link
     *     org.aion.api.sol.IBool IBool} or {@link org.aion.api.sol.ISString ISString}.
     * @return the contract interface {@link IContract IContract}.
     */
    IContract setParam(ISolidityArg val);

    /**
     * Sets the transaction energy for function be executed.
     *
     * @param val set the max energy consume for the desiring transaction. The transaction will not
     *     been executed when the energy is not enough. Current kernel limitations for one
     *     transaction must be higher than 21K but lower than 2M for the regular transaction and the
     *     contract transaction. For a contract create transaction, it can be maximum 5M energy.
     * @return the contract interface {@link IContract IContract}.
     */
    IContract setTxNrgLimit(long val);

    /**
     * Sets the transaction energy price for function be executed.
     *
     * @param val given the energy price for each energy unit consume. Currently the price can not
     *     lower than 10 amp AION and higher than 9 AION. (10^10 to 9^18)
     * @return the contract interface {@link IContract IContract}.
     */
    IContract setTxNrgPrice(long val);

    /**
     * Sets the message sender address for function be executed.
     *
     * @param address the class {@link AionAddress Address} represent the desired sender account.
     * @return the contract interface {@link IContract IContract}.
     */
    IContract setFrom(AionAddress address);

    /**
     * Sets the transaction value for certain functions.
     *
     * @param val the transaction value by long value of the desired transaction.
     * @return the contract interface {@link IContract IContract}.
     */
    IContract setTxValue(long val);

    /**
     * Does checks to ensure the function is correct and assembles the transaction hash. Required
     * for {@link #execute() execute} to operate.
     *
     * @return the contract interface {@link IContract IContract}.
     */
    IContract build();

    /**
     * Executes the built transaction in the VM of the connection kernel for evaluating the
     * execution result. Refer to {@link #newFunction(String)} for function use.
     *
     * @return the class {@link ContractResponse} containing all relevant information wrapped by the
     *     class {@link ApiMsg ApiMsg}.
     */
    ApiMsg call();

    /**
     * Executes the built transaction. Refer to {@link #newFunction(String)} for function use.
     *
     * @return the class {@link ContractResponse} containing all relevant information wrapped by the
     *     class {@link ApiMsg ApiMsg}.
     */
    ApiMsg execute();

    /**
     * GetEncoded gets the input parameters after executed built transaction and then set the return
     * value to TxArgs's data field. {@link TxArgs TxArgs}.
     *
     * @return Bytes array encoded function input parameters.
     */
    ByteArrayWrapper getEncodedData();

    /**
     * Retrieve the current sender's account address of the contract.
     *
     * @return the class {@link AionAddress Address} represent the sender's account address.
     */
    AionAddress getFrom();

    /**
     * Retrieve the address of the deployed contract.
     *
     * @return the class {@link AionAddress Address} represent the contractaddress.
     */
    AionAddress getContractAddress();

    /**
     * Retrieve the transaction hash of the deployed contract.
     *
     * @return the class {@link Hash256 Hash256} represent the 32bytes array wrapper.
     */
    Hash256 getDeployTxId();

    /**
     * Retrieve the abiDefinition of the deployed contract.
     *
     * @return the interface {@link List List} of {@link ContractAbiEntry ContractAbiEntry}.
     */
    List<ContractAbiEntry> getAbiDefinition();

    /**
     * Retrieve the abiDefinition of the current function.
     *
     * @return the class {@link ContractAbiEntry ContractAbiEntry}.
     */
    ContractAbiEntry getAbiFunction();

    /**
     * Retrieve the abiDefinition of the deployed contract by a string.
     *
     * @return the class {@link java.lang.String String}.
     */
    String getAbiDefToString();

    /**
     * Retrieve the compiled contract byte code of the deployed contract to string.
     *
     * @return the class {@link java.lang.String String}.
     */
    String getCode();

    /**
     * Retrieve the contract source contained in the deployed contract.
     *
     * @return the class {@link java.lang.String String}.
     */
    String getSource();

    /**
     * Retrieve the LanguageVersion information in the deployed contract.
     *
     * @return the class {@link java.lang.String String}.
     */
    String getLanguageVersion();

    /**
     * Retrieve the CompilerVersion information in the deployed contract.
     *
     * @return the class {@link java.lang.String String}.
     */
    String getCompilerVersion();

    /**
     * Retrieve the CompilerOptions information in the deployed contract.
     *
     * @return the class {@link java.lang.String String}.
     */
    String getCompilerOptions();

    /**
     * Retrieve the UserDoc information in the deployed contract.
     *
     * @return the class {@link JsonFmt JsonFmt}. The UserDoc field of the deployed contract.
     */
    JsonFmt getUserDoc();

    /**
     * Retrieve the DeveloperDoc information in the deployed contract.
     *
     * @return the class {@link JsonFmt}. The DeveloperDoc field of the deployed contract.
     */
    JsonFmt getDeveloperDoc();

    /**
     * Retrieve the contract name.
     *
     * @return the class {@link String String}.
     */
    String getContractName();

    /**
     * Set contract transaction to be in the non-block node.
     *
     * @return the interface {@link IContract IContract}.
     * @see ITx#nonBlock() nonBlock
     */
    IContract nonBlock();

    /**
     * Represent the isError code of the contract execution result.
     *
     * @return int value.
     */
    int getErrorCode();

    /**
     * Represent the detailed information of the isError code.
     *
     * @return the class {@link java.lang.String String}.
     */
    String getErrString();

    /**
     * Check if any isError happened during the contract creation, deployment or transaction.
     *
     * @return the boolean value if isError happening.
     */
    boolean error();

    /**
     * Retrieve the contract function input parameters.
     *
     * @return the interface {@link List List} of the class {@link ISolidityArg ISolidityArg}, need
     *     to cast to response solidity type by the contract function.
     */
    List<ISolidityArg> getInputParams();

    /**
     * Retrieve the contract function output parameters.
     *
     * @return the interface {@link List List} of the class {@link ISolidityArg ISolidityArg}, need
     *     to cast to response solidity type by contract function.
     */
    List<ISolidityArg> getOutputParams();

    /**
     * This is the builder method to set the event the developer want to listen.
     *
     * @param e the class {@link String String} represent to the event name.
     * @return the interface {@link IContract IContract}
     */
    IContract newEvent(String e);

    /**
     * This is the builder method to set the events the developer want to listen.
     *
     * @param e the interface {@link List List} of the class {@link String String} represent to the
     *     event name.
     * @return the interface {@link IContract IContract}
     */
    IContract newEvents(List<String> e);

    /**
     * This is the builder method to set all events in the contract the developer want to listen.
     *
     * @return the interface {@link IContract IContract}
     */
    IContract allEvents();

    /**
     * register the contract events for listening the events set by the method {@link
     * IContract#newEvent(String)}, {@link IContract#newEvents(List)} and {@link
     * IContract#allEvents} to the Aion network and listening events until deregister it.
     *
     * @return the boolean value represent the status of the register wrapped by the class {@link
     *     ApiMsg}.
     */
    ApiMsg register();

    /**
     * register the contract events for listening the events set by the method {@link
     * IContract#newEvent(String)}, {@link IContract#newEvents(List)} and {@link
     * IContract#allEvents} to the Aion network and listening events until the given block number as
     * a string.
     *
     * @param s the class {@link String String} represent the block number.
     * @return the boolean value represent the status of the register wrapped by the class {@link
     *     ApiMsg}.
     */
    ApiMsg register(String s);

    /**
     * register the contract events for listening the events set by the method {@link
     * IContract#newEvent(String)}, {@link IContract#newEvents(List)} and {@link
     * IContract#allEvents} to the Aion network and listening events until the given block number as
     * a class {@link String String}.
     *
     * @param ef the class {@link ContractEventFilter ContractEventFilter} represent the event
     *     listening condition such as the start block number , the end block number, or the
     *     specific address.
     * @return the boolean value represent the status of the register wrapped by the class {@link
     *     ApiMsg}.
     */
    ApiMsg register(ContractEventFilter ef);

    /**
     * deregister the contract events listening from the Aion network given by the event name.
     *
     * @param e the interface {@link List List} of the class {@link String String} represent the
     *     event name.
     * @return the boolean value represent the status of the register wrapped by the class {@link
     *     ApiMsg}.
     */
    ApiMsg deregister(List<String> e);

    /**
     * deregister all of the contract events listening from the Aion network.
     *
     * @return the boolean value represent the status of the register wrapped by the class {@link
     *     ApiMsg}.
     */
    ApiMsg deregisterAll();

    /**
     * Retrieve the event callbacks to the Aion JavaAPI.
     *
     * @return the interface {@link List List} of the class {@link ContractEvent ContractEvent}
     *     represent the happened events.
     */
    List<ContractEvent> getEvents();

    /**
     * Check which events been registered to the Aion network kernel.
     *
     * @return the interface {@link List List} of the class {@link String String} represent the
     *     event name.
     */
    List<String> issuedEvents();

    /// **
    // * Query the past events in the Aion network kernel by given specified conditions.
    // *
    // * @param ef
    // *      the class {@link ContractEventFilter ContractEventFilter} represent the event query
    // conditions.
    // * @return the interface {@link List List} of the class {@link ContractEvent ContractEvent}
    // represent the event detail
    // * information.
    // */
    // ApiMsg queryEvents(ContractEventFilter ef);

    /**
     * retrieve the event list of the deployed contract
     *
     * @return the interface {@link List List} of the class {@link String String} represent the
     *     event name;
     */
    List<String> getContractEventList();
}
