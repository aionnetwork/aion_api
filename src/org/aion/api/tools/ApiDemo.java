package org.aion.api.tools;

import static java.lang.System.exit;
import static org.aion.api.IAionAPI.API_VERSION;
import static org.aion.api.ITx.NRG_LIMIT_CONTRACT_CREATE_MAX;
import static org.aion.api.ITx.NRG_LIMIT_TX_MAX;
import static org.aion.api.ITx.NRG_PRICE_MIN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.aion.aion_types.NewAddress;
import org.aion.api.IAionAPI;
import org.aion.api.IContract;
import org.aion.api.sol.IAddress;
import org.aion.api.sol.ISString;
import org.aion.api.sol.ISolidityArg;
import org.aion.api.sol.IUint;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.ContractResponse;

/**
 * This class shows the basic operation of the Aion Java API.
 *
 * <p>There are four testcases in the ApiDemo. You can check the testcases by
 *
 * <p>"java -jar aionApi.jar -h"
 *
 * <p>Case 1: Get the highest block number in the connected blockchain network.
 *
 * <p>Case 2: Get accounts thar are stored in the connected kernel.
 *
 * <p>Case 3: Unlock the first account stored in the connected kernel.
 *
 * <p>Case 4: Deploy a Hello world contract and print the String.
 *
 * <p>Case 5: Deploy a token contract and execute a transaction and then check the final balance.
 */
public class ApiDemo {

    private static final String tokenSC =
            "contract MyToken{  \n"
                    + "    event Transfer(address  from, address  to, uint128 value); \n"
                    + "    string public name;  \n"
                    + "    string public symbol;  \n"
                    + "    uint8 public decimals; \n"
                    + "    mapping(address=>uint128) public balanceOf; \n"
                    + "    function MyToken(uint128 initialSupply, string tokenName, uint8 decimalUnits, string tokenSymbol){ \n"
                    + "        balanceOf[msg.sender]=initialSupply;    \n"
                    + "        name = tokenName;    \n"
                    + "        symbol = tokenSymbol;    \n"
                    + "        decimals = decimalUnits;  \n"
                    + "    } \n"
                    + "    function transfer(address _to,uint64 _value){    \n"
                    + "        if (balanceOf[msg.sender] < _value || balanceOf[_to] + _value < balanceOf[_to]) throw;    \n"
                    + "            balanceOf[msg.sender] -= _value;    \n"
                    + "            balanceOf[_to] += _value;    \n"
                    + "            Transfer(msg.sender, _to, _value);\n"
                    + "    }}\n";
    private static final String helloworldSC =
            "pragma solidity ^0.4.0;\n"
                    + "contract HelloWorld {\n"
                    + "    function greeting () constant returns (string){\n"
                    + "        return \"Hello, World!\";\n"
                    + "    }\n"
                    + "}";
    private static boolean runTest = false;
    private static String url = IAionAPI.LOCALHOST_URL;

    public static void main(String[] args) {
        int caseNum = -1;
        if (args.length > 0) {
            for (int i = 0; i < args.length; ++i) {
                String arg = args[i];
                switch (arg) {
                    case "--test":
                    case "-t":
                        try {
                            if (++i < args.length) {
                                caseNum = Integer.parseInt(args[i]);
                            } else {
                                System.out.println("No case number input.");
                                return;
                            }
                        } catch (Exception e) {
                            System.err.println("Can not recognize input args");
                            return;
                        }
                        runTest = true;
                        break;
                    case "--help":
                    case "-h":
                        System.out.println(
                                "[OPTION] [NUM|STRING] [OPTION] [NUM|STRING] [OPTION] [NUM|STRING]");
                        System.out.println("  Ex. ./apiDemo.sh -t 1 -l tcp://localhost:8547");
                        System.out.println(
                                "Mandatory arguments to long options are mandatory for short options too.");
                        System.out.println("  -v, --version             get api version.");
                        System.out.println(
                                "  -l, --url                 set the kernel url and port.");
                        System.out.println("  -t, --test [NUM]          demo api tests");
                        System.out.println("              NUM = 1       demo get BlockNumber.");
                        System.out.println("              NUM = 2       demo GetAccounts.");
                        System.out.println("              NUM = 3       demo UnlockAccount.");
                        System.out.println("              NUM = 4       demo HelloWorld Contract.");
                        System.out.println(
                                "              NUM = 5       demo Token Smart Contract.");
                        System.out.println("              NUM = 0       demo all api .");
                        return;
                    case "--version":
                    case "-v":
                        System.out.println(API_VERSION());
                        return;
                    case "--url":
                    case "-l":
                        if (++i < args.length) {
                            url = args[i];
                        } else {
                            System.out.println("No url input.");
                            return;
                        }
                        break;
                }
            }

            if (runTest) {
                switch (caseNum) {
                    case 0:
                        new ApiDemo().DemoBlockNumber();
                        new ApiDemo().DemoTokenContract();
                        new ApiDemo().DemoGetAccounts();
                        new ApiDemo().DemoUnlockAccount();
                        new ApiDemo().DemoHelloWorld();
                        break;
                    case 1:
                        new ApiDemo().DemoBlockNumber();
                        break;
                    case 2:
                        new ApiDemo().DemoGetAccounts();
                        break;
                    case 3:
                        new ApiDemo().DemoUnlockAccount();
                        break;
                    case 4:
                        new ApiDemo().DemoHelloWorld();
                        break;
                    case 5:
                        new ApiDemo().DemoTokenContract();
                        break;
                    default:
                        System.out.println("Wrong input test case number");
                        return;
                }
            }
        } else {
            System.out.println("Must input arg, please use -h or --help to see the details");
            System.out.println("if you are unable to run this jar, try \"./apiDemo.sh -h\" .");
        }

        exit(0);
    }

    public void DemoBlockNumber() {
        System.out.println("===============  Demo BlockNumber ======================");

        IAionAPI api = IAionAPI.init();
        ApiMsg apiMsg = api.connect(url);
        if (apiMsg.isError()) {
            System.out.println("Connect server failed, exit test! " + apiMsg.getErrString());
            return;
        }

        System.out.println("Get server connected!");

        apiMsg.set(api.getChain().blockNumber());
        if (apiMsg.isError()) {
            System.out.println("Get blockNumber isError: " + apiMsg.getErrString());
        }

        long bn = api.getChain().blockNumber().getObject();
        System.out.println("The highest block number is: " + bn);

        api.destroyApi();
        System.out.println("===== Demo BlockNumber finish =====");
        System.out.println();
    }

    public void DemoTokenContract() {

        System.out.println(
                "===============  Demo token contract transaction ======================");

        System.out.println(
                "Create api instance and connect, please press enter key to go next step!");
        Scanner scan = new Scanner(System.in);
        scan.nextLine();

        IAionAPI api = IAionAPI.init();
        ApiMsg apiMsg = api.connect(url);
        if (apiMsg.isError()) {
            System.out.println("Connect server failed, exit test! " + apiMsg.getErrString());
            return;
        }
        System.out.println("Get server connected!");
        System.out.println();

        System.out.println("Get accounts from server, please press enter the key to go next step!");
        scan = new Scanner(System.in);
        scan.nextLine();

        apiMsg.set(api.getWallet().getAccounts());
        if (apiMsg.isError()) {
            System.out.println("GetAccounts failed! " + apiMsg.getErrString());
            return;
        }
        List accs = apiMsg.getObject();

        if (accs.size() < 2) {
            System.out.println(
                    "The number of accounts in the server is lower than 2, please check the server has a least 2 accounts to support the test!");
            return;
        }

        System.out.println("Get " + accs.size() + " accounts!");

        NewAddress acc = (NewAddress) accs.get(0);
        System.out.println("Get the first account: " + acc.toString());
        NewAddress acc2 = (NewAddress) accs.get(1);
        System.out.println("Get the second account: " + acc2.toString());
        System.out.println();

        // unlockAccount before deployContract or send a transaction.
        System.out.println("Unlock account before deploy smart contract or send transactions.");
        System.out.println("Please press the enter key to go next step!");
        scan = new Scanner(System.in);
        scan.nextLine();
        System.out.println("Please input the password of the first account: ");
        String password = scan.nextLine();

        apiMsg.set(api.getWallet().unlockAccount(acc, password, 300));
        if (apiMsg.isError() || !(boolean) apiMsg.getObject()) {
            System.out.println(
                    "Unlock account failed! Please check your password input! "
                            + apiMsg.getErrString());
            return;
        }

        System.out.println("Account unlocked!");
        System.out.println();

        Map<String, List<ISolidityArg>> param = new HashMap<>();

        List<ISolidityArg> solArg = new ArrayList<>();
        solArg.add(IUint.copyFrom(100000));
        solArg.add(ISString.copyFrom("Aion token"));
        solArg.add(IUint.copyFrom(10));
        solArg.add(ISString.copyFrom("AION"));

        param.put("MyToken", solArg);
        System.out.println("Prepare to deploy the token contract.");
        System.out.println("Please press the enter key to go next step!");
        scan = new Scanner(System.in);
        scan.nextLine();

        apiMsg =
                api.getContractController()
                        .createFromSource(
                                tokenSC, acc, NRG_LIMIT_CONTRACT_CREATE_MAX, NRG_PRICE_MIN, param);

        if (apiMsg.isError()) {
            System.out.println("Deploy contract failed!" + apiMsg.getErrString());
            return;
        }

        System.out.println("Contract deployed!");
        System.out.println();

        // Check initial default account balance
        System.out.println(
                "Check the balance of the first account, please press the enter key to go next step!");
        scan = new Scanner(System.in);
        scan.nextLine();

        IContract ct = api.getContractController().getContract();

        apiMsg.set(
                ct.newFunction("balanceOf")
                        .setParam(IAddress.copyFrom(acc.toString()))
                        .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                        .setTxNrgPrice(NRG_PRICE_MIN)
                        .build()
                        .nonBlock()
                        .execute());

        if (apiMsg.isError()) {
            System.out.println("Function exceution isError! " + apiMsg.getErrString());
            return;
        }

        ContractResponse contractResponse = apiMsg.getObject();

        for (Object a : contractResponse.getData()) {
            System.out.println(
                    "The initial balance Of the first account "
                            + acc.toString()
                            + " is "
                            + a.toString());
        }

        IContract tmp =
                ct.newFunction("transfer")
                        .setFrom(acc)
                        .setParam(IAddress.copyFrom(acc2.toString()))
                        .setParam(IUint.copyFrom(1))
                        .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                        .setTxNrgPrice(NRG_PRICE_MIN)
                        .build();

        if (tmp.error()) {
            System.out.println("Function build isError! " + tmp.getErrString());
            return;
        }

        System.out.println(
                "Prepare to send 1 transaction, send 1 unit each transaction from the first account to the second account!");
        System.out.println("Please press the enter key to go next step!");
        scan.nextLine();

        apiMsg.set(tmp.execute());
        if (apiMsg.isError()) {
            System.out.println("Send token failed! " + apiMsg.getErrString());
        } else {
            System.out.println("Token sent!");
        }

        System.out.println();
        System.out.println("Check the blance of the first account!");
        System.out.println("Please press the enter key to go next step!");
        scan.nextLine();

        apiMsg.set(
                ct.newFunction("balanceOf")
                        .setParam(IAddress.copyFrom(acc.toString()))
                        .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                        .setTxNrgPrice(NRG_PRICE_MIN)
                        .build()
                        .nonBlock()
                        .execute());

        if (apiMsg.isError()) {
            System.out.println(
                    "Function balanceOf isError! Account: "
                            + acc.toString()
                            + apiMsg.getErrString());
            return;
        }

        contractResponse = apiMsg.getObject();

        if (contractResponse.isStatusError()) {
            System.out.println("ContractResponse isError! " + contractResponse.statusToString());
        }

        for (Object a : contractResponse.getData()) {
            System.out.println("The balance of " + acc.toString() + ": " + a.toString());
        }

        System.out.println();
        System.out.println("Check the blance of the second account!");
        System.out.println("Please press the enter key to go next step!");
        scan.nextLine();

        apiMsg.set(
                ct.newFunction("balanceOf")
                        .setParam(IAddress.copyFrom(acc2.toString()))
                        .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                        .setTxNrgPrice(NRG_PRICE_MIN)
                        .build()
                        .nonBlock()
                        .execute());

        if (apiMsg.isError()) {
            System.out.println(
                    "Function balanceOf isError! Account: "
                            + acc2.toString()
                            + apiMsg.getErrString());
            return;
        }

        contractResponse = apiMsg.getObject();

        for (Object a : contractResponse.getData()) {
            System.out.println("The balance of " + acc2.toString() + ": " + a.toString());
        }

        System.out.println();
        System.out.println("Disconnect connection between api and node!");
        api.destroyApi();
        System.out.println("===============  Demo token contract finish ======================");
        System.out.println();
    }

    public void DemoGetAccounts() {
        System.out.println("===============  Demo GetAccounts ======================");

        IAionAPI api = IAionAPI.init();
        ApiMsg apiMsg = api.connect(url);
        if (apiMsg.isError()) {
            System.out.println("Connect server failed, exit test! " + apiMsg.getErrString());
            return;
        }

        System.out.println("Get server connected!");

        System.out.println("Get accounts from server, please press enter the key to go next step!");
        Scanner scan = new Scanner(System.in);
        scan.nextLine();

        apiMsg.set(api.getWallet().getAccounts());
        if (apiMsg.isError()) {
            System.out.println("GetAccounts failed! " + apiMsg.getErrString());
            return;
        }
        List accs = apiMsg.getObject();

        System.out.println("Total " + accs.size() + " accounts!");
        for (Object acc : accs) {
            System.out.println("Found account: " + acc.toString());
        }

        api.destroyApi();
        System.out.println("===== Demo GetAccounts finish =====");
        System.out.println();
    }

    public void DemoUnlockAccount() {
        System.out.println("===============  Demo UnlockAccount ======================");

        IAionAPI api = IAionAPI.init();
        ApiMsg apiMsg = api.connect(url);
        if (apiMsg.isError()) {
            System.out.println("Connect server failed, exit test! " + apiMsg.getErrString());
            return;
        }

        System.out.println("Get server connected!");

        System.out.println("Get accounts from server, please press enter the key to go next step!");
        Scanner scan = new Scanner(System.in);
        scan.nextLine();

        apiMsg.set(api.getWallet().getAccounts());
        if (apiMsg.isError()) {
            System.out.println("GetAccounts failed! " + apiMsg.getErrString());
            return;
        }
        List accs = apiMsg.getObject();

        System.out.println("Total " + accs.size() + " accounts!");
        for (Object acc1 : accs) {
            System.out.println("Found account: " + acc1.toString());
        }

        NewAddress acc = (NewAddress) accs.get(0);
        System.out.println("Get the first account: " + acc.toString());

        // unlockAccount before deployContract or send a transaction.
        System.out.println("Try to unlock the first account.");
        System.out.println("Please press the enter key to go next step!");
        scan = new Scanner(System.in);
        scan.nextLine();
        System.out.println("Please input the password of the first account: ");
        String password = scan.nextLine();

        apiMsg.set(api.getWallet().unlockAccount(acc, password, 300));
        if (apiMsg.isError()) {
            System.out.println(
                    "Unlock account failed! Please check your password input! "
                            + apiMsg.getErrString());
            return;
        }

        System.out.println("Account unlocked!");
        System.out.println();

        api.destroyApi();
        System.out.println("===== Demo UnlockAccount finish =====");
        System.out.println();
    }

    public void DemoHelloWorld() {

        System.out.println(
                "===============  Demo Hello contract transaction ======================");

        System.out.println(
                "Create api instance and connect, please press enter key to go next step!");
        Scanner scan = new Scanner(System.in);
        scan.nextLine();

        IAionAPI api = IAionAPI.init();
        ApiMsg apiMsg = api.connect(url);
        if (apiMsg.isError()) {
            System.out.println("Connect server failed, exit test! " + apiMsg.getErrString());
            return;
        }
        System.out.println("Get server connected!");
        System.out.println();

        System.out.println("Get accounts from server, please press enter the key to go next step!");
        scan = new Scanner(System.in);
        scan.nextLine();

        apiMsg.set(api.getWallet().getAccounts());
        if (apiMsg.isError()) {
            System.out.println("GetAccounts failed! " + apiMsg.getErrString());
            return;
        }
        List accs = apiMsg.getObject();

        if (accs.size() < 1) {
            System.out.println(
                    "The number of accounts in the server is lower than 1, please check the server has a least 1 accounts to support the test!");
            return;
        }

        System.out.println("Get " + accs.size() + " accounts!");

        NewAddress acc = (NewAddress) accs.get(0);
        System.out.println("Get the first account: " + acc.toString());
        System.out.println();

        // unlockAccount before deployContract or send a transaction.
        System.out.println("Unlock account before deploy smart contract or send transactions.");
        System.out.println("Please press the enter key to go next step!");
        scan = new Scanner(System.in);
        scan.nextLine();
        System.out.println("Please input the password of the first account: ");
        String password = scan.nextLine();

        apiMsg.set(api.getWallet().unlockAccount(acc, password, 300));
        if (apiMsg.isError() || !(boolean) apiMsg.getObject()) {
            System.out.println(
                    "Unlock account failed! Please check your password input! "
                            + apiMsg.getErrString());
            return;
        }

        System.out.println("Account unlocked!");
        System.out.println();

        System.out.println("Prepare to deploy the token contract.");
        System.out.println("Please press the enter key to go next step!");
        scan = new Scanner(System.in);
        scan.nextLine();

        apiMsg =
                api.getContractController()
                        .createFromSource(
                                helloworldSC, acc, NRG_LIMIT_CONTRACT_CREATE_MAX, NRG_PRICE_MIN);

        if (apiMsg.isError()) {
            System.out.println("Deploy contract failed!" + apiMsg.getErrString());
            return;
        }

        System.out.println("Contract deployed!");
        System.out.println();

        // Check initial default account balance
        System.out.println("Prepare print greenting! Please press the enter key to go next step!");
        scan = new Scanner(System.in);
        scan.nextLine();

        IContract contract = api.getContractController().getContract();

        apiMsg.set(
                contract.newFunction("greeting")
                        .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                        .setTxNrgPrice(NRG_PRICE_MIN)
                        .build()
                        .execute());

        if (apiMsg.isError()) {
            System.out.println("Function exceution isError! " + apiMsg.getErrString());
            return;
        }

        ContractResponse contractResponse = apiMsg.getObject();
        if (contractResponse.isStatusError()) {
            System.out.println("ContractResponse isError! " + contractResponse.statusToString());
        }

        for (Object a : contractResponse.getData()) {
            System.out.println(a.toString());
        }

        System.out.println();
        System.out.println("Disconnect connection between api and node!");
        api.destroyApi();
        System.out.println(
                "===============  Demo helloWorld contract finish ======================");
        System.out.println();
    }
}
