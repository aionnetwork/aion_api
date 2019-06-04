package org.aion.api;

import java.io.File;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import org.aion.aion_types.NewAddress;
import org.aion.api.sol.ISolidityArg;
import org.aion.api.type.ApiMsg;

/**
 * This interface provides methods for deploy the contract to the Aion blockchain network, store the
 * contract into the contract container, retrieve the contract and remove the contract.
 *
 * @author Jay Tseng
 */
public interface IContractController {

    /**
     * Multiple contract create methods for deploy contracts on the Aion network.
     *
     * @param source the class {@link String String} represent the contract source code. It could be
     *     multiple contracts.
     * @param from the class {@link NewAddress Address} represent the sender or the contract owner whom
     *     deploy the contract.
     * @param nrgLimit the long value represent the maximum energy consume during this contract
     *     deploy been allowed. if the contract consume energy more than this number. the deploy
     *     will fail, Also the contract deployer will been charged all of the energy for trying to
     *     deploy this contract.
     * @param nrgPrice the long value represent the unit price of the energy the contract deployer
     *     want to pay. It effect the contract deploy response time by the current network
     *     environment.
     * @return the the deploy status wrapped into {@link ApiMsg ApiMsg}. The deployed contract will
     *     been store inside the Controller and user can retrieve it by given the contract address.
     * @see IContractController#getContract
     *     <p>A multi-contract source, set key value pair when you want to deploy the contract "key"
     *     with initial parameters "value"
     */
    ApiMsg createFromSource(String source, NewAddress from, long nrgLimit, long nrgPrice);

    /**
     * Multiple contract create methods for deploy contracts on the Aion network.
     *
     * @param source the class {@link String String} represent the contract source code. It could be
     *     multiple contracts.
     * @param from the class {@link NewAddress Address} represent the sender or the contract owner whom
     *     deploy the contract.
     * @param nrgLimit the long value represent the maximum energy consume during this contract
     *     deploy been allowed. if the contract consume energy more than this number. the deploy
     *     will fail, Also the contract deployer will been charged all of the energy for trying to
     *     deploy this contract.
     * @param nrgPrice the long value represent the unit price of the energy the contract deployer
     *     want to pay. It effect the contract deploy response time by the current network
     *     environment.
     * @param value the class {@link BigInteger BigInteger} represent how many balance of the the
     *     sender's account want to send during this deploy.
     * @return the the deploy status wrapped into {@link ApiMsg ApiMsg}. The deployed contract will
     *     been store inside the Controller and user can retrieve it by given the contract address.
     * @see IContractController#getContract
     *     <p>A multi-contract source, set key value pair when you want to deploy the contract "key"
     *     with initial parameters "value"
     */
    ApiMsg createFromSource(
            String source, NewAddress from, long nrgLimit, long nrgPrice, BigInteger value);

    /**
     * contract create method for deploy a contract with initial arguments on the Aion network.
     *
     * @param source the class {@link String String} represent the contract source code. It could be
     *     multiple contracts.
     * @param from the class {@link NewAddress Address} represent the sender or the contract owner whom
     *     deploy the contract.
     * @param nrgLimit the long value represent the maximum energy consume during this contract
     *     deploy been allowed. if the contract consume energy more than this number. the deploy
     *     will fail, Also the contract deployer will been charged all of the energy for trying to
     *     deploy this contract.
     * @param nrgPrice the long value represent the unit price of the energy the contract deployer
     *     want to pay. It effect the contract deploy response time by the current network
     *     environment.
     * @param params the interface {@link List List} of the interface {@link ISolidityArg
     *     ISolidityArg} represent the contract constructor arguments when the contract has the
     *     constructor.
     * @return the the deploy status wrapped into {@link ApiMsg ApiMsg}. The deployed contract will
     *     been store inside the Controller and user can retrieve it by given the contract address.
     * @see IContractController#getContract
     *     <p>A multi-contract source, set key value pair when you want to deploy the contract "key"
     *     with initial parameters "value"
     */
    ApiMsg createFromSource(
            String source, NewAddress from, long nrgLimit, long nrgPrice, List<ISolidityArg> params);

    /**
     * contract create method for deploy contracts with initial arguments on the Aion network.
     *
     * @param source the class {@link String String} represent the contract source code. It could be
     *     multiple contracts.
     * @param from the class {@link NewAddress Address} represent the sender or the contract owner whom
     *     deploy the contract.
     * @param nrgLimit the long value represent the maximum energy consume during this contract
     *     deploy been allowed. if the contract consume energy more than this number. the deploy
     *     will fail, Also the contract deployer will been charged all of the energy for trying to
     *     deploy this contract.
     * @param nrgPrice the long value represent the unit price of the energy the contract deployer
     *     want to pay. It effect the contract deploy response time by the current network
     *     environment.
     * @param params the interface {@link Map Map} of the pair of the class {@link String String}
     *     and the interface {@link ISolidityArg ISolidityArg} represent the contract name and the
     *     constructor arguments when the contract has the constructor.
     * @return the the deploy status wrapped into {@link ApiMsg ApiMsg}. The deployed contract will
     *     been store inside the Controller and user can retrieve it by given the contract address.
     * @see IContractController#getContract
     */
    ApiMsg createFromSource(
            String source,
            NewAddress from,
            long nrgLimit,
            long nrgPrice,
            Map<String, List<ISolidityArg>> params);

    /**
     * Multiple contract create methods for deploy a contract to the Aion blockchain network.
     *
     * @param source the class {@link String String} represent the contract source code. It could be
     *     multiple contracts.
     * @param from the class {@link NewAddress Address} represent the sender or the contract owner whom
     *     deploy the contract.
     * @param nrgLimit the long value represent the maximum energy consume during this contract
     *     deploy been allowed. if the contract consume energy more than this number. the deploy
     *     will fail, Also the contract deployer will been charged all of the energy for trying to
     *     deploy this contract.
     * @param nrgPrice the long value represent the unit price of the energy the contract deployer
     *     want to pay. It effect the contract deploy response time by the current network
     *     environment.
     * @param value the class {@link BigInteger BigInteger} represent how many blanceof the the
     *     sender's account want to send during this deploy.
     * @param params the interface {@link List List} of the interface {@link ISolidityArg
     *     ISolidityArg} represent the contract constructor arguments when the contract has the
     *     constructor.
     * @return the the deploy status wrapped into {@link ApiMsg ApiMsg}. The deployed contract will
     *     been store inside the Controller and user can retrieve it by given the contract address.
     * @see IContractController#getContract
     *     <p>A multi-contract source, set key value pair when you want to deploy the contract "key"
     *     with initial parameters "value"
     */
    ApiMsg createFromSource(
            String source,
            NewAddress from,
            long nrgLimit,
            long nrgPrice,
            BigInteger value,
            List<ISolidityArg> params);

    /**
     * Multiple contract create methods for deploy a contract to the Aion blockchain network.
     *
     * @param source the class {@link String String} represent the contract source code. It could be
     *     multiple contracts.
     * @param from the class {@link NewAddress Address} represent the sender or the contract owner whom
     *     deploy the contract.
     * @param nrgLimit the long value represent the maximum energy consume during this contract
     *     deploy been allowed. if the contract consume energy more than this number. the deploy
     *     will fail, Also the contract deployer will been charged all of the energy for trying to
     *     deploy this contract.
     * @param nrgPrice the long value represent the unit price of the energy the contract deployer
     *     want to pay. It effect the contract deploy response time by the current network
     *     environment.
     * @param value the class {@link BigInteger BigInteger} represent how many blanceof the the
     *     sender's account want to send during this deploy.
     * @param params the interface {@link Map Map} of the pair of the class {@link String String}
     *     and the interface {@link ISolidityArg ISolidityArg} represent the contract name and the
     *     constructor arguments when the contract has the constructor.
     * @return the the deploy status wrapped into {@link ApiMsg ApiMsg}. The deployed contract will
     *     been store inside the Controller and user can retrieve it by given the contract address
     * @see IContractController#getContract
     *     <p>A multi-contract source, set key value pair when you want to deploy the contract "key"
     *     with initial parameters "value"
     */
    ApiMsg createFromSource(
            String source,
            NewAddress from,
            long nrgLimit,
            long nrgPrice,
            BigInteger value,
            Map<String, List<ISolidityArg>> params);

    /**
     * Multiple contract create methods for deploy a contract to the Aion blockchain network.
     *
     * @param sourceDir a zip of the directory {@link java.io.File File} that contains the Solidity
     *     contracts to be compiled.
     * @param entryPoint the name {@link String String} of the entry point of these Solidity
     *     contracts
     * @param from the class {@link NewAddress Address} represent the sender or the contract owner whom
     *     deploy the contract.
     * @param nrgLimit the long value represent the maximum energy consume during this contract
     *     deploy been allowed. if the contract consume energy more than this number. the deploy
     *     will fail, Also the contract deployer will been charged all of the energy for trying to
     *     deploy this contract.
     * @param nrgPrice the long value represent the unit price of the energy the contract deployer
     *     want to pay. It effect the contract deploy response time by the current network
     *     environment.
     * @return the the deploy status wrapped into {@link ApiMsg ApiMsg}. The deployed contract will
     *     been store inside the Controller and user can retrieve it by given the contract address
     * @see IContractController#getContract
     *     <p>A multi-contract source, set key value pair when you want to deploy the contract "key"
     *     with initial parameters "value"
     */
    ApiMsg createFromDirectory(
            File sourceDir, String entryPoint, NewAddress from, long nrgLimit, long nrgPrice);

    /**
     * Multiple contract create methods for deploy a contract to the Aion blockchain network.
     *
     * @param sourceDir a zip of the directory {@link java.io.File File} that contains the Solidity
     *     contracts to be compiled.
     * @param entryPoint the name {@link String String} of the entry point of these Solidity
     *     contracts
     * @param from the class {@link NewAddress Address} represent the sender or the contract owner whom
     *     deploy the contract.
     * @param nrgLimit the long value represent the maximum energy consume during this contract
     *     deploy been allowed. if the contract consume energy more than this number. the deploy
     *     will fail, Also the contract deployer will been charged all of the energy for trying to
     *     deploy this contract.
     * @param nrgPrice the long value represent the unit price of the energy the contract deployer
     *     want to pay. It effect the contract deploy response time by the current network
     *     environment.
     * @param value the class {@link BigInteger BigInteger} represent how many blanceof the the
     *     sender's account want to send during this deploy.
     * @return the the deploy status wrapped into {@link ApiMsg ApiMsg}. The deployed contract will
     *     been store inside the Controller and user can retrieve it by given the contract address
     * @see IContractController#getContract
     *     <p>A multi-contract source, set key value pair when you want to deploy the contract "key"
     *     with initial parameters "value"
     */
    ApiMsg createFromDirectory(
            File sourceDir,
            String entryPoint,
            NewAddress from,
            long nrgLimit,
            long nrgPrice,
            BigInteger value);

    /**
     * Multiple contract create methods for deploy a contract to the Aion blockchain network.
     *
     * @param sourceDir a zip of the directory {@link java.io.File File} that contains the Solidity
     *     contracts to be compiled.
     * @param entryPoint the name {@link String String} of the entry point of these Solidity
     *     contracts
     * @param from the class {@link NewAddress Address} represent the sender or the contract owner whom
     *     deploy the contract.
     * @param nrgLimit the long value represent the maximum energy consume during this contract
     *     deploy been allowed. if the contract consume energy more than this number. the deploy
     *     will fail, Also the contract deployer will been charged all of the energy for trying to
     *     deploy this contract.
     * @param nrgPrice the long value represent the unit price of the energy the contract deployer
     *     want to pay. It effect the contract deploy response time by the current network
     *     environment.
     * @param params the interface {@link List List} of the interface {@link ISolidityArg
     *     ISolidityArg} represent the contract constructor arguments when the contract has the
     *     constructor.
     * @return the the deploy status wrapped into {@link ApiMsg ApiMsg}. The deployed contract will
     *     been store inside the Controller and user can retrieve it by given the contract address
     * @see IContractController#getContract
     *     <p>A multi-contract source, set key value pair when you want to deploy the contract "key"
     *     with initial parameters "value"
     */
    ApiMsg createFromDirectory(
            File sourceDir,
            String entryPoint,
            NewAddress from,
            long nrgLimit,
            long nrgPrice,
            List<ISolidityArg> params);

    /**
     * Multiple contract create methods for deploy a contract to the Aion blockchain network.
     *
     * @param sourceDir a zip of the directory {@link java.io.File File} that contains the Solidity
     *     contracts to be compiled.
     * @param entryPoint the name {@link String String} of the entry point of these Solidity
     *     contracts
     * @param from the class {@link NewAddress Address} represent the sender or the contract owner whom
     *     deploy the contract.
     * @param nrgLimit the long value represent the maximum energy consume during this contract
     *     deploy been allowed. if the contract consume energy more than this number. the deploy
     *     will fail, Also the contract deployer will been charged all of the energy for trying to
     *     deploy this contract.
     * @param nrgPrice the long value represent the unit price of the energy the contract deployer
     *     want to pay. It effect the contract deploy response time by the current network
     *     environment.
     * @param params the interface {@link Map Map} of the pair of the class {@link String String}
     *     and the interface {@link ISolidityArg ISolidityArg} represent the contract name and the
     *     constructor arguments when the contract has the constructor.
     * @return the the deploy status wrapped into {@link ApiMsg ApiMsg}. The deployed contract will
     *     been store inside the Controller and user can retrieve it by given the contract address
     * @see IContractController#getContract
     *     <p>A multi-contract source, set key value pair when you want to deploy the contract "key"
     *     with initial parameters "value"
     */
    ApiMsg createFromDirectory(
            File sourceDir,
            String entryPoint,
            NewAddress from,
            long nrgLimit,
            long nrgPrice,
            Map<String, List<ISolidityArg>> params);

    /**
     * Multiple contract create methods for deploy a contract to the Aion blockchain network.
     *
     * @param sourceDir a zip of the directory {@link java.io.File File} that contains the Solidity
     *     * point of these Solidity * contracts
     * @param entryPoint the name {@link String String} of the entry point of these Solidity
     *     contracts
     * @param from the class {@link NewAddress Address} represent the sender or the contract owner whom
     *     deploy the contract.
     * @param nrgLimit the long value represent the maximum energy consume during this contract
     *     deploy been allowed. if the contract consume energy more than this number. the deploy
     *     will fail, Also the contract deployer will been charged all of the energy for trying to
     *     deploy this contract.
     * @param nrgPrice the long value represent the unit price of the energy the contract deployer
     *     want to pay. It effect the contract deploy response time by the current network
     *     environment.
     * @param value the class {@link BigInteger BigInteger} represent how many blanceof the the
     *     sender's account want to send during this deploy.
     * @param params the interface {@link List List} of the interface {@link ISolidityArg
     *     ISolidityArg} represent the contract constructor arguments when the contract has the
     *     constructor.
     * @return the the deploy status wrapped into {@link ApiMsg ApiMsg}. The deployed contract will
     *     been store inside the Controller and user can retrieve it by given the contract address
     * @see IContractController#getContract
     *     <p>A multi-contract source, set key value pair when you want to deploy the contract "key"
     *     with initial parameters "value"
     */
    ApiMsg createFromDirectory(
            File sourceDir,
            String entryPoint,
            NewAddress from,
            long nrgLimit,
            long nrgPrice,
            BigInteger value,
            List<ISolidityArg> params);

    /**
     * Multiple contract create methods for deploy a contract to the Aion blockchain network.
     *
     * @param sourceDir a zip of the directory {@link java.io.File File} that contains the Solidity
     *     contracts to be compiled.
     * @param entryPoint the name {@link String String} of the entry point of these Solidity
     *     contracts
     * @param from the class {@link NewAddress Address} represent the sender or the contract owner whom
     *     deploy the contract.
     * @param nrgLimit the long value represent the maximum energy consume during this contract
     *     deploy been allowed. if the contract consume energy more than this number. the deploy
     *     will fail, Also the contract deployer will been charged all of the energy for trying to
     *     deploy this contract.
     * @param nrgPrice the long value represent the unit price of the energy the contract deployer
     *     want to pay. It effect the contract deploy response time by the current network
     *     environment.
     * @param value the class {@link BigInteger BigInteger} represent how many blanceof the the
     *     sender's account want to send during this deploy.
     * @param params the interface {@link Map Map} of the pair of the class {@link String String}
     *     and the interface {@link ISolidityArg ISolidityArg} represent the contract name and the
     *     constructor arguments when the contract has the constructor.
     * @return the the deploy status wrapped into {@link ApiMsg ApiMsg}. The deployed contract will
     *     been store inside the Controller and user can retrieve it by given the contract address
     * @see IContractController#getContract
     *     <p>A multi-contract source, set key value pair when you want to deploy the contract "key"
     *     with initial parameters "value"
     */
    ApiMsg createFromDirectory(
            File sourceDir,
            String entryPoint,
            NewAddress from,
            long nrgLimit,
            long nrgPrice,
            BigInteger value,
            Map<String, List<ISolidityArg>> params);

    /**
     * create a contract object by given the sender, the deployed contract address on the Aion
     * network, and the solidity abi definition for the contract. And store into the controller.
     *
     * @param from the class {@link NewAddress Address} represent the sender whom want to interact with
     *     the contract.
     * @param contract the class {@link NewAddress Address} represent the contract address.
     * @param abi the class {@link String String} represent the contract Abi definition.
     * @return the class {@link IContract IContract} represent the contract been created in the
     *     contractController. The created contract object will been store inside the Controller and
     *     user can retrieve it by given the contract address.
     * @see IContractController#getContract
     */
    IContract getContractAt(NewAddress from, NewAddress contract, String abi);

    /**
     * retrieve the contract by given the contract address.
     *
     * @param contractAddr the class {@link NewAddress Address} represent the contract address.
     * @return the class {@link IContract IContract} represent the contract been stored inside the
     *     contractController.
     * @see IContractController#getContract
     */
    IContract getContract(NewAddress contractAddr);

    /**
     * retrieve the contractList by given the contract name.
     *
     * @param contractName the class {@link String String} represent the contract address.
     * @return the interface {@link List List} of the class {@link IContract IContract} represent
     *     the contract been stored inside the contractController. the user could deploy same
     *     contract multiple times. Therefore the return will be a list of the IContract.
     * @see IContractController#getContract
     */
    List<IContract> getContract(String contractName);

    /**
     * retrieve the first contract inside the ContractController
     *
     * @return the the contract create status wrapped into {@link ApiMsg ApiMsg}. The created
     *     contract object will been store inside the Controller and user can retrieve it by given
     *     the contract address.
     * @see IContractController#getContract
     */
    IContract getContract();

    /**
     * retrieve all of the pair of the contract address and the contract name inside the
     * ContractController.
     *
     * @return the interface {@link Map Map} of the key of the Class {@link NewAddress Address} and the
     *     value of the class {@link String String} represent the contract address and the contract
     *     name.
     */
    Map<NewAddress, String> getContractMap();

    /**
     * remove contract by given the contract object from ContractController.
     *
     * @param c the interface {@link IContract IContract} represent the contract object.
     * @return the boolean value represent the status of the operation wrapped by the class {@link
     *     ApiMsg}. You can retrieve through {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg remove(IContract c);

    /** remove all contracts from the ContractController. */
    void clear();
}
