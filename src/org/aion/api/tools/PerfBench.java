package org.aion.api.tools;

import static java.lang.Math.round;
import static java.lang.System.exit;
import static org.aion.api.IAionAPI.API_VERSION;
import static org.aion.api.ITx.NRG_LIMIT_CONTRACT_CREATE_MAX;
import static org.aion.api.ITx.NRG_LIMIT_TX_MAX;
import static org.aion.api.ITx.NRG_PRICE_MIN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.aion.api.IAionAPI;
import org.aion.api.IContract;
import org.aion.api.IUtils;
import org.aion.api.sol.IAddress;
import org.aion.api.sol.ISString;
import org.aion.api.sol.ISolidityArg;
import org.aion.api.sol.IUint;
import org.aion.api.sol.impl.SString;
import org.aion.api.sol.impl.Uint;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.ContractResponse;
import org.aion.api.type.MsgRsp;
import org.aion.base.type.Address;
import org.aion.base.util.ByteArrayWrapper;

/*
 Created by jay on 24/11/16.
*/

/*
 * This class show Aion api perfomance test.
 * "java -cp nucoapi.jar PerfBench -h"
 * Case 1: get the highest block number in the connecting blockchain network.
 * Case 2: deploy a token contract and send nonblocking transactions. Check the transaction status
 * after we sent the transactions.
 * Case 3: deploy a token contract and send blocking transaction. Each transaction will confirm
 * finished then do next send.
 * Case 4: deploy a token contract and keep send transactions.
 */

public class PerfBench {

    private static final int multiply = 1000000000;
    private static final int timeMultiply = 1000000;
    private static final int unlockTime = 86400;
    private static final String tokenSC =
            "contract MyToken{  \n"
                    + "    event Transfer(address indexed from, address indexed to, uint value); \n"
                    + "    string public name;  \n"
                    + "    string public symbol;  \n"
                    + "    uint8 public decimals; \n"
                    + "    mapping(address=>uint) public balanceOf; \n"
                    + "    function MyToken(uint initialSupply, string tokenName, uint8 decimalUnits, string tokenSymbol){ \n"
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
    private static boolean warmup = false;
    private static boolean runTest = false;
    private static int repeat = 1; // default benchmark function execution times.
    private static String url = IAionAPI.LOCALHOST_URL;
    private static boolean loopTest = false;
    private static int loopCount = 0;
    private static List<Long> benchMark;
    private static NetworkLatency nl = null;
    private static int worker = 1;
    private static boolean calculateLatency = false;
    private static List<Double> latencyBenchmark;
    private static boolean fastTest = false;
    private static List<ByteArrayWrapper> txArr = Collections.synchronizedList(new ArrayList<>());
    private static String pw = "PLAT4life";

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
                        System.out.println(
                                "  Ex. java -jar aionapi.jar -t 1 -r 1000 -l tcp://localhost:18547 -w -cl -lt");
                        System.out.println(
                                "Mandatory arguments to long options are mandatory for short options too.");
                        System.out.println("  -v, --version             get api version.");
                        System.out.println("  -t, --test [NUM]          run benchmark tests");
                        System.out.println("              NUM = 1       run getblock number test.");
                        System.out.println("              NUM = 2       run Smart Contract test.");
                        System.out.println(
                                "              NUM = 3       run Smart Contract transaction blocking test.");
                        System.out.println(
                                "              NUM = 4       run nostop contract transaction.");
                        System.out.println("              NUM = 0       run all benchmark test.");
                        System.out.println(
                                "  -l, --url [STRING]        set api binding address, the default address is tcp://localhost:8547");
                        System.out.println(
                                "  -r, --repeat [NUM]        set execution times, the default number is 1000.");
                        System.out.println(
                                "  -w,                       enable warmup option to increase target classes executing speed");
                        System.out.println(
                                "  -cl,                      enable api latency calculation ");
                        System.out.println("  -lt,                      enable loop test");
                        System.out.println(
                                "  -ft,                      enable fast test, skip all keyboard input");
                        System.out.println(
                                "  -wk,                      define the number of worker thread, the default worker is 1");
                        System.out.println(
                                "  -p, --pw                  the password of the first account");
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
                    case "--pw":
                    case "-p":
                        if (++i < args.length) {
                            pw = args[i];
                        } else {
                            System.out.println("No url input.");
                            return;
                        }
                        break;
                    case "--repeat":
                    case "-r":
                        try {
                            if (++i < args.length) {
                                repeat = Integer.parseInt(args[i]);
                                if (repeat < 0) {
                                    repeat = 10;
                                    System.out.println(
                                            "The input repeat number is too low, set to 10.");
                                } else if (repeat > 1000000) {
                                    repeat = 1000000;
                                    System.out.println(
                                            "The input repeat number is too large, set to 1000000.");
                                }
                            } else {
                                System.out.println("No repeat number input.");
                                return;
                            }
                        } catch (Exception e) {
                            System.err.println("Can not recognize input args");
                            return;
                        }
                        break;
                    case "--warmup":
                    case "-w":
                        warmup = true;
                        break;
                    case "-lt":
                        loopTest = true;
                        benchMark = new ArrayList<>();
                        break;
                    case "-cl":
                        calculateLatency = true;
                        latencyBenchmark = new ArrayList<>();
                        nl = new NetworkLatency(repeat);
                        break;
                    case "-ft":
                        fastTest = true;
                        break;
                    case "-wk":
                        try {
                            if (++i < args.length) {
                                worker = Integer.parseInt(args[i]);
                                if (worker < 0) {
                                    worker = 1;
                                    System.out.println(
                                            "The input worker number is too low, set to 1.");
                                } else if (worker
                                        > Runtime.getRuntime().availableProcessors() >> 1) {
                                    worker = Runtime.getRuntime().availableProcessors() >> 1;
                                    System.out.println(
                                            "The input worker number is too large, set to "
                                                    + worker
                                                    + ".");
                                }
                            } else {
                                System.out.println("No worker number input.");
                                return;
                            }
                        } catch (Exception e) {
                            System.err.println("Can not recognize input args");
                            return;
                        }
                        break;
                }
            }

            if (runTest) {
                int finalCaseNum = caseNum;
                IntStream.range(0, loopTest ? 10 : 1)
                        .forEach(
                                i -> {
                                    loopCount = i + 1;
                                    switch (finalCaseNum) {
                                        case 0:
                                            new PerfBench().TestGetBlockNumber();
                                            new PerfBench().TestSmartContractTx();
                                            new PerfBench().TestSmartContractTxBlock();
                                            break;
                                        case 1:
                                            new PerfBench().TestGetBlockNumber();
                                            break;
                                        case 2:
                                            new PerfBench().TestSmartContractTx();
                                            break;
                                        case 3:
                                            new PerfBench().TestSmartContractTxBlock();
                                            break;
                                        case 4:
                                            try {
                                                new PerfBench().NonStopTx();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            break;
                                        case 5487:
                                            try {
                                                new PerfBench().NonStopRandomTx();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        default:
                                            System.out.println("Wrong input test case number");
                                    }
                                });

                if (loopTest) {
                    System.out.println("Benchmark: " + benchMark);
                    // benchMark.remove(benchMark.indexOf(Collections.min(benchMark)));
                    // benchMark.remove(benchMark.indexOf(Collections.max(benchMark)));
                    // System.out.println("Benchmark remove H&L: " + benchMark);
                    System.out.println(
                            "Benchmark avg: "
                                    + benchMark.stream().mapToLong(a -> a).average().getAsDouble()
                                    + " per second.");

                    if (calculateLatency) {
                        System.out.println("Laterncy benchmark: " + latencyBenchmark);
                        // latencyBenchmark.remove(latencyBenchmark.indexOf(Collections.min(latencyBenchmark)));
                        // latencyBenchmark.remove(latencyBenchmark.indexOf(Collections.max(latencyBenchmark)));
                        // System.out.println("Laterncy benchmark remove H&L: " +
                        // latencyBenchmark);
                        System.out.println(
                                "Laterncy benchmark avg: "
                                        + latencyBenchmark
                                                .stream()
                                                .mapToDouble(a -> a)
                                                .average()
                                                .getAsDouble()
                                        + " ms.");
                    }
                }
            }
        } else {
            System.out.println("Must input arg, please use -h or --help to see the details");
            System.out.println(
                    "if you are unable to run this jar, try \"java -jar -Djava.library.path=./ libAionApi.jar -t 1\" .");
        }

        exit(0);
    }

    private void TestGetBlockNumber() {
        if (loopTest) {
            System.out.println(
                    "===== Testing getBlockNumber benchmark loop " + loopCount + " =====");
        } else {
            System.out.println(
                    "===============  Testing getBlockNumber benchmark ======================");
        }

        IAionAPI api = IAionAPI.init();
        ApiMsg apiMsg = api.connect(url);

        if (apiMsg.isError()) {
            System.out.println("Connect server failed, exit test! " + apiMsg.getErrString());
            return;
        }

        System.out.println("Get server connected!");

        // TODO: recap warmup class later
        //        if (warmup) {
        //            System.out.println("WarmUp Start!");
        //            new AionAPIImpl(true, 2);
        //            System.out.println("WarmUp Finish!");
        //        }

        System.out.println("Firing " + repeat + " get BlockNumber!");

        long start = System.nanoTime();
        if (calculateLatency) {
            nl.clear();
        }
        IntStream.range(0, repeat)
                .forEach(
                        i -> {
                            if (calculateLatency) {
                                nl.addStart(System.nanoTime());
                                api.getChain().blockNumber();
                                nl.addEnd(System.nanoTime());
                            } else {
                                api.getChain().blockNumber();
                            }
                        });

        long end = System.nanoTime();
        System.out.println("Sent " + repeat + " get BlockNumber!");
        long bm = round((double) repeat * multiply / (end > start ? (end - start) : 1));
        if (loopTest) {
            benchMark.add(bm);
        }
        System.out.println(bm + " transactions per sec.");

        if (calculateLatency) {
            List<Long> diffLt = new ArrayList<>();
            IntStream.range(0, repeat).forEach(i -> diffLt.add(nl.diff(i)));
            Double avg = diffLt.stream().mapToDouble(a -> a).average().getAsDouble() / timeMultiply;
            latencyBenchmark.add(avg);
            System.out.println("Avg latency is: " + avg + " ms.");
        }

        api.destroyApi();
        System.out.println("===== Get benchmark finish =====");
        System.out.println();
    }

    /**
     * This test show how many transactions send to server per second after deploy a new Token
     * contract.
     */
    private void TestSmartContractTx() {
        if (loopTest) {
            System.out.println(
                    "===== Testing smart contract transaction benchmark loop "
                            + loopCount
                            + " =====");
        } else {
            System.out.println(
                    "===============  Testing smart contract transaction benchmark ======================");
        }

        if (!loopTest && !fastTest) {
            System.out.println(
                    "Create api instance and connect, please press enter key to go next step!");
            Scanner scan = new Scanner(System.in);
            scan.nextLine();
        }
        IAionAPI api = IAionAPI.init();
        ApiMsg apiMsg = api.connect(url);
        if (apiMsg.isError()) {
            System.out.println("Connect server failed, exit test! " + apiMsg.getErrString());
            return;
        }

        System.out.println("Get server connected!");
        System.out.println();

        if (!loopTest && !fastTest) {
            System.out.println(
                    "Get accounts from server, please press enter the key to go next step!");
            Scanner scan = new Scanner(System.in);
            scan.nextLine();
        }

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

        Address acc = (Address) accs.get(0);
        System.out.println("Get the first account: " + acc.toString());
        Address acc2 = (Address) accs.get(1);
        System.out.println("Get the second account: " + acc2.toString());
        System.out.println();

        // unlockAccount before deployContract or send a transaction.
        System.out.println("Unlock account before deploy smart contract or send transactions.");
        String password = pw;
        if (!loopTest && !fastTest) {
            System.out.println("Please press the enter key to go next step!");
            Scanner scan = new Scanner(System.in);
            scan.nextLine();
            System.out.println("Please input the password of the first account: ");
            password = scan.nextLine();
        }

        apiMsg.set(api.getWallet().unlockAccount(acc, password, unlockTime));
        if (apiMsg.isError() || !(boolean) apiMsg.getObject()) {
            System.out.println(
                    "Unlock account failed! Please check your password input! "
                            + apiMsg.getErrString());
            return;
        }

        System.out.println("Account unlocked!");
        System.out.println();

        ArrayList<ISolidityArg> param = new ArrayList<>();
        param.add(IUint.copyFrom(100000));
        param.add(ISString.copyFrom("Aion token"));
        param.add(IUint.copyFrom(10));
        param.add(ISString.copyFrom("AION"));

        System.out.println("Prepare to deploy the token contract.");
        if (!loopTest && !fastTest) {
            System.out.println("Please press the enter key to go next step!");
            Scanner scan = new Scanner(System.in);
            scan.nextLine();
        }

        apiMsg.set(
                api.getContractController()
                        .createFromSource(
                                tokenSC, acc, NRG_LIMIT_CONTRACT_CREATE_MAX, NRG_PRICE_MIN, param));

        if (apiMsg.isError()) {
            System.out.println("Deploy contract failed!" + apiMsg.getErrorCode());
            return;
        }

        System.out.println("Contract deployed!");
        System.out.println();

        // Check initial default account balance
        if (!loopTest && !fastTest) {
            System.out.println(
                    "Check the balance of the first account, please press the enter key to go next step!");
            Scanner scan = new Scanner(System.in);
            scan.nextLine();
        }

        IContract contract = api.getContractController().getContract();

        apiMsg.set(
                contract.newFunction("balanceOf")
                        .setParam(IAddress.copyFrom(acc.toString()))
                        .setTxNrgPrice(NRG_PRICE_MIN)
                        .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                        .build()
                        .nonBlock()
                        .execute());

        if (apiMsg.isError()) {
            System.out.println(
                    "Function balances execution isError! "
                            + acc.toString()
                            + apiMsg.getErrString());
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
                contract.newFunction("transfer")
                        .setFrom(acc)
                        .setParam(IAddress.copyFrom(acc2.toString()))
                        .setTxNrgPrice(NRG_PRICE_MIN)
                        .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                        .setParam(Uint.copyFrom(1))
                        .build();

        if (tmp.error()) {
            System.out.println("Function build isError! " + tmp.getErrString());
            return;
        }

        // TODO : recap warmup
        //        if (warmup) {
        //            System.out.println("WarmUp Start!");
        //            new AionAPIImpl(true);
        //            System.out.println("WarmUp Finish!");
        //        }

        System.out.println(
                "Prepare to firing "
                        + repeat
                        + " transactions, send 1 unit each transaction from the first account to the second account!");
        if (!loopTest && !fastTest) {
            System.out.println("Please press the enter key to go next step!");
            Scanner scan = new Scanner(System.in);
            scan.nextLine();
        }

        if (calculateLatency) {
            nl.clear();
        }

        ExecutorService es = Executors.newFixedThreadPool(1);
        es.execute(() -> getTxStatus(api));

        List<ByteArrayWrapper> msgTxArr = new ArrayList<>();
        long start = System.nanoTime();
        for (int i = 0; i < repeat; i++) {
            if (calculateLatency) {
                nl.addStart(System.nanoTime());
                msgTxArr.add(
                        ((ContractResponse) tmp.nonBlock().execute().getObject()).getMsgHash());
                nl.addEnd(System.nanoTime());
            } else {
                ContractResponse cr = tmp.nonBlock().execute().getObject();
                if (cr.hitTxPendingPoolLimit()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    i--;
                } else {
                    txArr.add(cr.getMsgHash());
                }
            }
        }

        long end = System.nanoTime();

        System.out.println("Sent " + repeat + " transactions!");
        long bm = round((double) repeat * multiply / (end > start ? (end - start) : 1));
        if (loopTest) {
            benchMark.add(bm);
        }
        System.out.println(bm + " transactions per sec.");

        if (calculateLatency) {
            List<Long> diffLt = new ArrayList<>();
            IntStream.range(0, repeat).forEach(i -> diffLt.add(nl.diff(i)));
            Double avg = diffLt.stream().mapToDouble(a -> a).average().getAsDouble() / timeMultiply;
            latencyBenchmark.add(avg);
            System.out.println("Avg latency is: " + avg + " ms.");
        }

        while (!txArr.isEmpty()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println();
        System.out.println("Check the blance of the first account!");
        if (!loopTest && !fastTest) {
            System.out.println("Please press the enter key to go next step!");
            Scanner scan = new Scanner(System.in);
            scan.nextLine();
        }

        apiMsg.set(
                contract.newFunction("balanceOf")
                        .setParam(IAddress.copyFrom(acc.toString()))
                        .setTxNrgPrice(NRG_PRICE_MIN)
                        .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                        .build()
                        .nonBlock()
                        .execute());

        if (apiMsg.isError()) {
            System.out.println(
                    "Function balances execution isError! "
                            + acc.toString()
                            + apiMsg.getErrString());
        }

        contractResponse = apiMsg.getObject();

        for (Object a : contractResponse.getData()) {
            System.out.println("The balance of " + acc.toString() + ": " + a.toString());
        }

        System.out.println();
        System.out.println("Check the blance of the second account!");
        if (!loopTest && !fastTest) {
            System.out.println("Please press the enter key to go next step!");
            Scanner scan = new Scanner(System.in);
            scan.nextLine();
        }

        apiMsg.set(
                contract.newFunction("balanceOf")
                        .setParam(IAddress.copyFrom(acc2.toString()))
                        .setTxNrgPrice(NRG_PRICE_MIN)
                        .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                        .build()
                        .nonBlock()
                        .execute());

        if (apiMsg.isError()) {
            System.out.println(
                    "Function balances execution isError! "
                            + acc2.toString()
                            + apiMsg.getErrString());
        }

        contractResponse = apiMsg.getObject();

        for (Object a : contractResponse.getData()) {
            System.out.println("The balance of " + acc2.toString() + ": " + a.toString());
        }

        System.out.println();
        System.out.println("Disconnect connection between api and node!");
        api.destroyApi();
        System.out.println(
                "===============  Smart contract transaction benchmark finish ======================");
        System.out.println();
    }

    private void TestSmartContractTxBlock() {
        System.out.println(
                "===============  Testing smart contract blocking Transaction ======================");
        System.out.println(
                "Create api instance and connect, please press enter key to go next step!");
        if (!fastTest) {
            Scanner scan = new Scanner(System.in);
            scan.nextLine();
        }

        IAionAPI api = IAionAPI.init();
        ApiMsg apiMsg = api.connect(url);
        if (apiMsg.isError()) {
            System.out.println("Connect server failed, exit test! " + apiMsg.getErrString());
            return;
        }

        System.out.println("Get server connected!");
        System.out.println();

        if (!fastTest) {
            System.out.println(
                    "Get accounts from server, please press enter the key to go next step!");
            Scanner scan = new Scanner(System.in);
            scan.nextLine();
        }

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

        Address acc = (Address) accs.get(0);
        System.out.println("Get the first account: " + acc.toString());
        Address acc2 = (Address) accs.get(1);
        System.out.println("Get the second account: " + acc2.toString());
        System.out.println();

        // unlockAccount before deployContract or send a transaction.
        System.out.println("Unlock account before deploy smart contract or send transactions.");
        String password = pw;
        if (!fastTest) {
            System.out.println("Please press the enter key to go next step!");
            Scanner scan = new Scanner(System.in);
            scan.nextLine();
            System.out.println("Please input the password of the first account: ");
            password = scan.nextLine();
        }

        apiMsg.set(api.getWallet().unlockAccount(acc, password, unlockTime));
        if (apiMsg.isError() || !(boolean) apiMsg.getObject()) {
            System.out.println(
                    "Unlock account failed! Please check your password input! "
                            + apiMsg.getErrString());
            return;
        }

        System.out.println("Account unlocked!");
        System.out.println();

        ArrayList<ISolidityArg> param = new ArrayList<>();
        param.add(Uint.copyFrom(100000));
        param.add(SString.copyFrom("Aion coin"));
        param.add(Uint.copyFrom(10));
        param.add(SString.copyFrom("AION"));

        System.out.println("Prepare to deploy the token contract.");
        if (!loopTest && !fastTest) {
            System.out.println("Please press the enter key to go next step!");
            Scanner scan = new Scanner(System.in);
            scan.nextLine();
        }

        apiMsg.set(
                api.getContractController()
                        .createFromSource(
                                tokenSC, acc, NRG_LIMIT_CONTRACT_CREATE_MAX, NRG_PRICE_MIN, param));

        if (apiMsg.isError()) {
            System.out.println("Deploy contract failed!" + apiMsg.getErrorCode());
            return;
        }

        System.out.println("Contract deployed!");
        System.out.println();

        // Check initial default account balance
        if (!loopTest && !fastTest) {
            System.out.println(
                    "Check the balance of the first account, please press the enter key to go next step!");
            Scanner scan = new Scanner(System.in);
            scan.nextLine();
        }

        IContract contract = api.getContractController().getContract();

        apiMsg.set(
                contract.newFunction("balanceOf")
                        .setParam(IAddress.copyFrom(acc.toString()))
                        .setTxNrgPrice(NRG_PRICE_MIN)
                        .setTxNrgLimit(NRG_LIMIT_TX_MAX)
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
                contract.newFunction("transfer")
                        .setFrom(acc)
                        .setParam(IAddress.copyFrom(acc2.toString()))
                        .setParam(Uint.copyFrom(1))
                        .setTxNrgPrice(NRG_PRICE_MIN)
                        .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                        .build();

        if (tmp.error()) {
            System.out.println("Function build isError! " + tmp.getErrString());
            return;
        }

        // TODO : recap later
        //        if (warmup) {
        //            System.out.println("WarmUp Start!");
        //            new AionAPIImpl(true);
        //            System.out.println("WarmUp Finish!");
        //        }

        System.out.println(
                "Prepare to firing "
                        + repeat
                        + " transactions, send 1 unit each transaction from the first account to the second account!");
        if (!loopTest && !fastTest) {
            System.out.println("Please press the enter key to go next step!");
            Scanner scan = new Scanner(System.in);
            scan.nextLine();
        }

        long start = System.nanoTime();
        for (int i = 0; i < repeat; i++) {
            System.out.println("execute Transaction: " + i);
            apiMsg.set(tmp.execute());
            if (apiMsg.isError()) {
                System.out.println(
                        "execute Transaction: " + i + "failed! " + apiMsg.getErrString());
            } else {
                System.out.println("executed Transaction: " + i);
            }
        }
        long end = System.nanoTime();

        System.out.println("Sent " + repeat + " transactions!");
        long bm = round((double) repeat * multiply / (end > start ? (end - start) : 1));
        System.out.println(bm + " transactions per sec.");

        System.out.println();
        System.out.println("Check the blance of the first account!");
        if (!loopTest && !fastTest) {
            System.out.println("Please press the enter key to go next step!");
            Scanner scan = new Scanner(System.in);
            scan.nextLine();
        }

        apiMsg.set(
                contract.newFunction("balanceOf")
                        .setParam(IAddress.copyFrom(acc.toString()))
                        .setTxNrgPrice(NRG_PRICE_MIN)
                        .setTxNrgLimit(NRG_LIMIT_TX_MAX)
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

        for (Object a : contractResponse.getData()) {
            System.out.println("The balance of " + acc.toString() + ": " + a.toString());
        }

        System.out.println();
        System.out.println("Check the blance of the second account!");
        if (!fastTest) {
            System.out.println("Please press the enter key to go next step!");
            Scanner scan = new Scanner(System.in);
            scan.nextLine();
        }

        apiMsg.set(
                contract.newFunction("balanceOf")
                        .setParam(IAddress.copyFrom(acc2.toString()))
                        .setTxNrgPrice(NRG_PRICE_MIN)
                        .setTxNrgLimit(NRG_LIMIT_TX_MAX)
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
        System.out.println(
                "===============  Testing smart contract blocking Transaction finish ======================");
        System.out.println();
    }

    private void NonStopTx() throws Exception {
        System.out.println("===== Testing Non stop transaction" + "=====");

        IAionAPI api = IAionAPI.init();
        ApiMsg apiMsg = api.connect(url);
        if (apiMsg.isError()) {
            System.out.println("Connect server failed, exit test! " + apiMsg.getErrString());
            return;
        }

        System.out.println("# workers: " + worker);

        System.out.println("Get server connected!");
        System.out.println();

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

        Address acc = (Address) accs.get(0);
        System.out.println("Get the first account: " + acc.toString());
        Address acc2 = (Address) accs.get(1);
        System.out.println("Get the second account: " + acc2.toString());
        System.out.println();

        // unlockAccount before deployContract or send a transaction.
        System.out.println("Unlock account before deploy smart contract or send transactions.");
        String password = pw;

        apiMsg.set(api.getWallet().unlockAccount(acc, password, unlockTime));
        if (apiMsg.isError() || !(boolean) apiMsg.getObject()) {
            System.out.println(
                    "Unlock account failed! Please check your password input! "
                            + apiMsg.getErrString());
            return;
        }

        System.out.println("Account unlocked!");
        System.out.println();

        Long initToken = 10000000000L;
        ArrayList<ISolidityArg> param = new ArrayList<>();
        param.add(Uint.copyFrom(initToken));
        param.add(SString.copyFrom("Aion coin"));
        param.add(Uint.copyFrom(10));
        param.add(SString.copyFrom("AION"));

        System.out.println("Prepare to deploy the token contract.");

        apiMsg.set(
                api.getContractController()
                        .createFromSource(
                                tokenSC, acc, NRG_LIMIT_CONTRACT_CREATE_MAX, NRG_PRICE_MIN, param));

        if (apiMsg.isError()) {
            System.out.println("Deploy contract failed!" + apiMsg.getErrorCode());
            return;
        }

        System.out.println("Contract deployed!");
        System.out.println();

        IContract contract = api.getContractController().getContract();
        apiMsg.set(
                contract.newFunction("balanceOf")
                        .setParam(IAddress.copyFrom(acc.toString()))
                        .setTxNrgPrice(NRG_PRICE_MIN)
                        .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                        .build()
                        .nonBlock()
                        .execute());

        if (apiMsg.isError()) {
            System.out.println(
                    "Function balances execution isError! "
                            + acc.toString()
                            + apiMsg.getErrString());
        }

        ContractResponse contractResponse = apiMsg.getObject();

        for (Object a : contractResponse.getData()) {
            System.out.println(
                    "The initial balance Of the first account "
                            + acc.toString()
                            + " is "
                            + a.toString());
        }

        System.out.println(
                "Prepare to firing transactions, send 1 unit each transaction from the first account to the second account! every ms");

        ExecutorService es = Executors.newFixedThreadPool(1);
        es.execute(() -> getTxStatus(api));

        Long accumulateTxs = 0L;
        long lastUnlock = System.nanoTime();

        while (true) {
            IContract tmp =
                    contract.newFunction("transfer")
                            .setFrom(acc)
                            .setParam(IAddress.copyFrom(acc2.toString()))
                            .setParam(Uint.copyFrom(1))
                            .setTxNrgPrice(NRG_PRICE_MIN)
                            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                            .build();

            if (tmp.error()) {
                System.out.println("Function build isError! " + tmp.getErrString());
                return;
            }

            long start = System.nanoTime();
            for (int i = 0; i < repeat; i++) {
                apiMsg.set(tmp.nonBlock().execute());

                if (apiMsg.isError()) {
                    if (apiMsg.getErrorCode() == -15) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        System.out.println("Hit tx pending limit! ");
                    }

                    i--;
                    continue;
                }

                ContractResponse cr = apiMsg.getObject();

                if (cr.getMsgHash() != null) {
                    accumulateTxs++;
                    txArr.add(cr.getMsgHash());
                } else {
                    System.out.println("ContractResponse exception!");
                    throw new Exception("ContractResponse exception!");
                }
            }
            long end = System.nanoTime();

            System.out.println("Sent " + accumulateTxs + " transactions!");
            long bm = round((double) repeat * multiply / (end > start ? (end - start) : 1));
            System.out.println(bm + " transactions per sec.");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Thread sleep exception!");
            }

            System.out.println();
            System.out.println("Check the blance of the first account!");

            apiMsg.set(
                    tmp.newFunction("balanceOf")
                            .setParam(IAddress.copyFrom(acc.toString()))
                            .setTxNrgPrice(NRG_PRICE_MIN)
                            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                            .build()
                            .execute());

            if (apiMsg.isError()) {
                System.out.println("Function exceution isError! " + apiMsg.getErrString());
                return;
            }

            contractResponse = apiMsg.getObject();
            Long balanceA = 0L;
            for (Object a : contractResponse.getData()) {
                balanceA = Long.valueOf(a.toString());
                System.out.println("The balance of " + acc.toString() + ": " + a.toString());
            }

            System.out.println();
            System.out.println("Check the blance of the second account!");

            apiMsg.set(
                    contract.newFunction("balanceOf")
                            .setParam(IAddress.copyFrom(acc2.toString()))
                            .setTxNrgPrice(NRG_PRICE_MIN)
                            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                            .build()
                            .execute());

            if (apiMsg.isError()) {
                System.out.println("Function exceution isError! " + apiMsg.getErrString());
                return;
            }

            contractResponse = apiMsg.getObject();
            Long balanceB = 0L;
            for (Object a : contractResponse.getData()) {
                balanceB = Long.valueOf(a.toString());
                System.out.println("The balance of " + acc2.toString() + ": " + a.toString());
            }

            assert ((balanceA + balanceB) == initToken);

            long now = System.nanoTime();
            if (now - lastUnlock > 80000000000000L) {
                System.out.println("Unlock account!");
                apiMsg.set(api.getWallet().unlockAccount(acc, password, unlockTime));
                if (apiMsg.isError()) {
                    System.out.println(
                            "Unlock account failed! Please check your password input! "
                                    + apiMsg.getErrString());
                    return;
                }
                lastUnlock = now;
            }
        }
    }

    private void NonStopRandomTx() throws Exception {
        System.out.println("===== Testing Non stop random transaction" + "=====");

        IAionAPI api = IAionAPI.init();
        ApiMsg apiMsg = api.connect(url);
        if (apiMsg.isError()) {
            System.out.println("Connect server failed, exit test! " + apiMsg.getErrString());
            return;
        }

        System.out.println("# workers: " + worker);

        System.out.println("Get server connected!");
        System.out.println();

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

        Address acc = (Address) accs.get(0);
        System.out.println("Get the first account: " + acc.toString());
        Address acc2 = (Address) accs.get(1);
        System.out.println("Get the second account: " + acc2.toString());
        System.out.println();

        // unlockAccount before deployContract or send a transaction.
        System.out.println("Unlock account before deploy smart contract or send transactions.");

        apiMsg.set(api.getWallet().unlockAccount(acc, pw, unlockTime));
        if (apiMsg.isError() || !(boolean) apiMsg.getObject()) {
            System.out.println(
                    "Unlock account failed! Please check your password input! "
                            + apiMsg.getErrString());
            return;
        }

        System.out.println("Account unlocked!");
        System.out.println();

        Long initToken = 10000000000L;
        ArrayList<ISolidityArg> param = new ArrayList<>();
        param.add(Uint.copyFrom(initToken));
        param.add(SString.copyFrom("Aion coin"));
        param.add(Uint.copyFrom(10));
        param.add(SString.copyFrom("AION"));

        System.out.println("Prepare to deploy the token contract.");

        apiMsg.set(
                api.getContractController()
                        .createFromSource(
                                tokenSC, acc, NRG_LIMIT_CONTRACT_CREATE_MAX, NRG_PRICE_MIN, param));

        if (apiMsg.isError()) {
            System.out.println("Deploy contract failed!" + apiMsg.getErrString());
            return;
        }

        System.out.println("Contract deployed!");
        System.out.println();

        IContract contract = api.getContractController().getContract();

        apiMsg.set(
                contract.newFunction("balanceOf")
                        .setParam(IAddress.copyFrom(acc.toString()))
                        .build()
                        .setTxNrgPrice(NRG_PRICE_MIN)
                        .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                        .nonBlock()
                        .execute());

        if (apiMsg.isError()) {
            System.out.println(
                    "Function balances execution isError! "
                            + acc.toString()
                            + apiMsg.getErrString());
        }

        ContractResponse contractResponse = apiMsg.getObject();

        for (Object a : contractResponse.getData()) {
            System.out.println(
                    "The initial balance Of the first account "
                            + acc.toString()
                            + " is "
                            + a.toString());
        }

        System.out.println(
                "Prepare to firing transactions, send 1 unit each transaction from the first account to the second account! every ms");

        ExecutorService es = Executors.newFixedThreadPool(1);
        es.execute(() -> getTxStatus(api));

        Long accumulateTxs = 0L;
        long lastUnlock = System.nanoTime();

        Random ran = new Random();

        while (true) {
            int rp = ran.nextInt(9) + 1;
            IContract tmp =
                    contract.newFunction("transfer")
                            .setFrom(acc)
                            .setParam(IAddress.copyFrom(acc2.toString()))
                            .setParam(Uint.copyFrom(1))
                            .setTxNrgPrice(1L)
                            .setTxNrgLimit(100000L)
                            .build();

            if (tmp.error()) {
                System.out.println("Function build isError! " + tmp.getErrString());
                return;
            }

            long start = System.nanoTime();
            for (int i = 0; i < rp; i++) {
                apiMsg.set(tmp.nonBlock().execute());

                if (apiMsg.isError()) {
                    if (apiMsg.getErrorCode() == -15) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        System.out.println("Hit tx pending limit! ");
                    }

                    i--;
                    continue;
                }

                ContractResponse cr = apiMsg.getObject();

                if (cr.getData() != null) {
                    accumulateTxs++;
                    txArr.add(cr.getMsgHash());
                } else {
                    System.out.println("ContractResponse exception!");
                    throw new Exception("ContractResponse exception!");
                }
            }

            try {
                Thread.sleep((ran.nextInt(19) + 1) * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long end = System.nanoTime();

            System.out.println("Sent " + accumulateTxs + " transactions!");
            long bm = round((double) repeat * multiply / (end > start ? (end - start) : 1));
            System.out.println(bm + " transactions per sec.");

            System.out.println();
            System.out.println("Check the blance of the first account!");

            apiMsg.set(
                    tmp.newFunction("balanceOf")
                            .setParam(IAddress.copyFrom(acc.toString()))
                            .setTxNrgPrice(NRG_PRICE_MIN)
                            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                            .build()
                            .execute());

            if (apiMsg.isError()) {
                System.out.println("Function exceution isError! " + apiMsg.getErrString());
                return;
            }

            contractResponse = apiMsg.getObject();
            Long balanceA = 0L;
            for (Object a : contractResponse.getData()) {
                balanceA = Long.valueOf(a.toString());
                System.out.println("The balance of " + acc.toString() + ": " + a.toString());
            }

            System.out.println();
            System.out.println("Check the blance of the second account!");

            apiMsg.set(
                    contract.newFunction("balanceOf")
                            .setParam(IAddress.copyFrom(acc2.toString()))
                            .setTxNrgPrice(NRG_PRICE_MIN)
                            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                            .build()
                            .execute());

            if (apiMsg.isError()) {
                System.out.println("Function exceution isError! " + apiMsg.getErrString());
                return;
            }

            contractResponse = apiMsg.getObject();
            Long balanceB = 0L;
            for (Object a : contractResponse.getData()) {
                balanceB = Long.valueOf(a.toString());
                System.out.println("The balance of " + acc2.toString() + ": " + a.toString());
            }

            assert ((balanceA + balanceB) == initToken);

            long now = System.nanoTime();
            if (now - lastUnlock > 80000000000000L) {
                System.out.println("Unlock account!");
                apiMsg.set(api.getWallet().unlockAccount(acc, pw, unlockTime));
                if (apiMsg.isError()) {
                    System.out.println(
                            "Unlock account failed! Please check your password input! "
                                    + apiMsg.getErrString());
                    return;
                }
                lastUnlock = now;
            }
        }
    }

    private void getTxStatus(IAionAPI api) {
        Long txDone = 0L;
        long start = System.nanoTime();
        while (api.isConnected()) {
            System.out.println("msgTxArr: " + txArr.size());
            for (int i = 0; i < txArr.size(); i++) {
                ApiMsg apiMsg = api.getTx().getMsgStatus(txArr.get(i));
                if (IUtils.endTxStatus(((MsgRsp) apiMsg.getObject()).getStatus())) {
                    txArr.remove(i);
                    i--;
                    txDone++;
                }
            }

            System.out.println("txDone: " + txDone);
            System.out.println(
                    "txDone in kernel per sec: "
                            + (txDone * 1000000000 / ((System.nanoTime() - start))));

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
