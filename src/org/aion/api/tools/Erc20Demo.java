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

package org.aion.api.tools;

import static java.lang.System.exit;
import static org.aion.api.ITx.NRG_LIMIT_CONTRACT_CREATE_MAX;
import static org.aion.api.ITx.NRG_LIMIT_TX_MAX;
import static org.aion.api.ITx.NRG_PRICE_MIN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import org.aion.api.IAionAPI;
import org.aion.api.IContract;
import org.aion.api.IUtils;
import org.aion.api.sol.IAddress;
import org.aion.api.sol.ISString;
import org.aion.api.sol.ISolidityArg;
import org.aion.api.sol.IUint;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.ContractEvent;
import org.aion.api.type.ContractResponse;
import org.aion.base.type.AionAddress;

/** Created by Jay Tseng on 26/06/17. */
public class Erc20Demo {

    private static final String tokenSC_ERC20 =
            "pragma solidity ^0.4.0;\n"
                    + "contract MyToken{\n"
                    + "    event Transfer(address  _from, address  _to, uint128 _value);\n"
                    + "    event Approval(address  _owner, address  _spender, uint128 _value);\n"
                    + "\n"
                    + "\n"
                    + "    string public name;  \n"
                    + "    string public symbol;  \n"
                    + "    uint8 public decimals; \n"
                    + "    uint128 tokenSupply;\n"
                    + "    \n"
                    + "    mapping(address => uint128) balances; \n"
                    + "    mapping(address => mapping (address => uint128)) allowed;\n"
                    + "\n"
                    + "\n"
                    + "    function MyToken(uint128 INITIALSUPPLY, string tokenName, uint8 decimalUnits, string tokenSymbol){ \n"
                    + "        balances[msg.sender] = INITIALSUPPLY; \n"
                    + "        tokenSupply = INITIALSUPPLY;\n"
                    + "        name = tokenName;\n"
                    + "        symbol = tokenSymbol;\n"
                    + "        decimals = decimalUnits;\n"
                    + "    }\n"
                    + "    \n"
                    + "    function totalSupply() constant returns (uint128 totalSupply){\n"
                    + "        return tokenSupply;\n"
                    + "    }\n"
                    + "    \n"
                    + "    function balanceOf(address _owner) constant returns (uint128 balance) {\n"
                    + "        return balances[_owner];\n"
                    + "    }\n"
                    + "    \n"
                    + "    function transfer(address _to, uint128 _value) returns (bool success) {\n"
                    + "        if (balances[msg.sender] < _value \n"
                    + "            || _value <= 0\n"
                    + "            || balances[_to] + _value <= balances[_to]) return false;    \n"
                    + "            \n"
                    + "        balances[msg.sender] -= _value;\n"
                    + "        balances[_to] += _value;    \n"
                    + "\n"
                    + "\n"
                    + "        Transfer(msg.sender, _to, _value);\n"
                    + "        return true;\n"
                    + "    }\n"
                    + "    \n"
                    + "    function transferFrom(address _from, address _to, uint128 _value) returns (bool success) {\n"
                    + "        if (balances[_from] < _value \n"
                    + "            || _value <= 0\n"
                    + "            || balances[_to] + _value < balances[_to]\n"
                    + "            || allowed[_from][msg.sender] < _value) return false;\n"
                    + "            \n"
                    + "        balances[_from] -= _value;\n"
                    + "        balances[_to] += _value;  \n"
                    + "        allowed[_from][msg.sender] -= _value;\n"
                    + "        \n"
                    + "        Transfer(msg.sender, _to, _value);\n"
                    + "        return true;\n"
                    + "    }\n"
                    + "    \n"
                    + "    function approve(address _spender, uint128 _value) returns (bool success) {\n"
                    + "        if (balances[msg.sender] < _value) return false;\n"
                    + "        \n"
                    + "        allowed[msg.sender][_spender] = _value;\n"
                    + "        Approval(msg.sender, _spender, _value);\n"
                    + "        return true;\n"
                    + "    }\n"
                    + "    \n"
                    + "    function allowance(address _owner, address _spender) constant returns (uint128 remaining) {\n"
                    + "        return allowed[_owner][_spender];\n"
                    + "    }\n"
                    + "}";

    private static boolean UNLOCKLOOP = true;
    private static int CNT = 0;
    private static long INITIALSUPPLY = 0;
    private static AionAddress COINBASE;
    private static IContract CONTRACT;
    private static IAionAPI API = null;
    private static int ERROR_CNT = 3;
    private static String FUNCTION_NAME = "";
    private static List<String> FUNCTION_ARGS = new ArrayList<>();
    private static AionAddress MSGSENDER;

    public static void main(String[] args) {
        System.out.println("Erc20 demo start:");

        if (!ConnectServer()) {
            API.destroyApi();
            exit(0);
            return;
        }

        if (!GetAccount()) {
            API.destroyApi();
            exit(0);
            return;
        }

        if (!UnlockAccount()) {
            API.destroyApi();
            exit(0);
            return;
        }

        if (!DeployContract()) {
            API.destroyApi();
            exit(0);
            return;
        }

        PrintFunctionList();

        Scanner scan = new Scanner(System.in);
        while (true) {
            System.out.println("Please input the command:");
            String input = scan.nextLine();

            switch (input) {
                case "-q":
                case "-Q":
                    System.out.println("Exit this demo! ");
                    exit(0);
                case "-h":
                case "-H":
                    PrintFunctionList();
                    break;
                default:
                    if (parseInput(input)) {
                        excuteAction();
                    }
                    break;
            }

            System.out.println();
        }
    }

    private static void excuteAction() {
        switch (FUNCTION_NAME) {
            case "totalSupply":
                totalSupply();
                break;
            case "balanceOf":
                balanceOf();
                break;
            case "transfer":
                transfer();
                break;
            case "transferFrom":
                transferFrom();
                break;
            case "approve":
                approve();
                break;
            case "allowance":
                allowance();
                break;
            case "getAccounts":
                getAccounts();
                break;
            case "account":
                account();
                break;
            case "unlockAccount":
                unlockAccount();
                break;
            case "setMsgSender":
                setMsgSender();
                break;
            case "getMsgSender":
                getMsgSender();
                break;
            default:
                System.out.println("Not support the function input.");
                break;
        }
    }

    private static void setMsgSender() {
        if (validArgs(1)) {
            Optional.ofNullable(validAddr(0))
                    .ifPresent(
                            (String sd) ->
                                    Optional.ofNullable(getServerAccounts())
                                            .ifPresent(
                                                    accs ->
                                                            accs.forEach(
                                                                    acc -> {
                                                                        if (sd.equals(
                                                                                acc.toString())) {
                                                                            System.out.println(
                                                                                    "Account found, message sender set to "
                                                                                            + sd);
                                                                            MSGSENDER =
                                                                                    AionAddress.wrap(
                                                                                            sd);
                                                                        }
                                                                    })));
        }
    }

    private static void balanceOf() {
        if (validArgs(1)) {
            Optional.ofNullable(validAddr(0))
                    .ifPresent(
                            acc -> {
                                ApiMsg apiMsg =
                                        CONTRACT.newFunction("balanceOf")
                                                .setFrom(COINBASE)
                                                .setParam(IAddress.copyFrom(acc))
                                                .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                                                .setTxNrgPrice(NRG_PRICE_MIN)
                                                .build()
                                                .nonBlock()
                                                .execute();

                                if (apiMsg.isError()) {
                                    System.out.println(
                                            "Function execution isError! " + apiMsg.getErrString());
                                    exit(0);
                                    return;
                                }

                                ContractResponse contractResponse = apiMsg.getObject();

                                for (Object a : contractResponse.getData()) {
                                    System.out.println(
                                            "The balance Of the account: "
                                                    + acc
                                                    + " is "
                                                    + a.toString());
                                }
                            });
        }
    }

    private static List validAddr(List<Integer> i) {
        List<String> accs = new ArrayList<>();

        i.forEach(idx -> Optional.ofNullable(validAddr(idx)).ifPresent(accs::add));

        return accs.size() != i.size() ? null : accs;
    }

    private static String validAddr(int i) {
        String acc = FUNCTION_ARGS.get(i);
        acc.replace("0x", "");
        acc.replace("0X", "");

        if (acc.length() != 64 || !acc.matches("[0-9a-fA-F]+")) {
            System.out.println("The input argument is invalid.");
            return null;
        }

        return acc;
    }

    private static boolean validArgs(int i) {
        if (FUNCTION_ARGS.size() == i) {
            return true;
        }

        System.out.println("The number of the input argument is invalid.");
        return false;
    }

    private static void transfer() {

        if (validArgs(2) && digitArgs(1)) {
            Optional.ofNullable(validAddr(0))
                    .ifPresent(
                            acc -> {
                                ApiMsg apiMsg =
                                        CONTRACT.newFunction("transfer")
                                                .setFrom(MSGSENDER)
                                                .setParam(IAddress.copyFrom(acc))
                                                .setParam(
                                                        IUint.copyFrom(
                                                                Long.valueOf(FUNCTION_ARGS.get(1))))
                                                .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                                                .setTxNrgPrice(NRG_PRICE_MIN)
                                                .build()
                                                .execute();

                                if (apiMsg.isError()) {
                                    System.out.println(
                                            "Function execution isError! " + apiMsg.getErrString());
                                    return;
                                }

                                System.out.println("Token sent!");

                                ContractResponse contractResponse = apiMsg.getObject();
                                for (Object a : contractResponse.getData()) {
                                    System.out.println(
                                            "The result of the token sent is " + a.toString());
                                }

                                checkEvents();
                            });
        }
    }

    private static void checkEvents() {
        System.out.println("Check events!");
        List<ContractEvent> ctEvt = CONTRACT.getEvents();

        if (!ctEvt.isEmpty()) {
            ctEvt.forEach(
                    e -> {
                        System.out.println("Event call back: " + e.getEventName());
                        final int[] idx = {1};
                        e.getResults()
                                .forEach(
                                        arg -> {
                                            System.out.println(
                                                    "Event arg value "
                                                            + idx[0]++
                                                            + " : "
                                                            + (arg instanceof Long
                                                                    ? arg
                                                                    : IUtils.bytes2Hex(
                                                                            (byte[]) arg)));
                                        });
                    });
        }
    }

    private static void transferFrom() {

        if (validArgs(3) && digitArgs(2)) {
            Optional.ofNullable(validAddr(Arrays.asList(0, 1)))
                    .ifPresent(
                            accs -> {
                                ApiMsg apiMsg =
                                        CONTRACT.newFunction("transferFrom")
                                                .setFrom(MSGSENDER)
                                                .setParam(IAddress.copyFrom((String) accs.get(1)))
                                                .setParam(IAddress.copyFrom((String) accs.get(2)))
                                                .setParam(
                                                        IUint.copyFrom(
                                                                Long.valueOf(FUNCTION_ARGS.get(3))))
                                                .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                                                .setTxNrgPrice(NRG_PRICE_MIN)
                                                .build()
                                                .execute();

                                if (apiMsg.isError()) {
                                    System.out.println(
                                            "Function execution isError! " + apiMsg.getErrString());
                                    return;
                                }

                                System.out.println("Token sent!");

                                ContractResponse contractResponse = apiMsg.getObject();
                                for (Object a : contractResponse.getData()) {
                                    System.out.println(
                                            "The result of the token sent is " + a.toString());
                                }

                                checkEvents();
                            });
        }
    }

    private static void totalSupply() {
        ApiMsg apiMsg =
                CONTRACT.newFunction("totalSupply")
                        .setFrom(COINBASE)
                        .setTxNrgLimit(200_000L)
                        .setTxNrgPrice(1)
                        .build()
                        .nonBlock()
                        .execute();

        if (apiMsg.isError()) {
            System.out.println("Function execution isError! " + apiMsg.getErrString());
            return;
        }

        ContractResponse contractResponse = apiMsg.getObject();
        for (Object a : contractResponse.getData()) {
            System.out.println("The total token supply is " + a.toString());
        }
    }

    private static void approve() {
        System.out.println("approve(spenderAccount, amount)");

        if (validArgs(2) && digitArgs(1)) {
            Optional.ofNullable(validAddr(0))
                    .ifPresent(
                            acc -> {
                                ApiMsg apiMsg =
                                        CONTRACT.newFunction("approve")
                                                .setFrom(MSGSENDER)
                                                .setParam(IAddress.copyFrom(acc))
                                                .setParam(
                                                        IUint.copyFrom(
                                                                Long.valueOf(FUNCTION_ARGS.get(1))))
                                                .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                                                .setTxNrgPrice(NRG_PRICE_MIN)
                                                .build()
                                                .execute();

                                if (apiMsg.isError()) {
                                    System.out.println(
                                            "Function execution isError! " + apiMsg.getErrString());
                                    return;
                                }

                                System.out.println("Token sent!");

                                ContractResponse contractResponse = apiMsg.getObject();
                                for (Object a : contractResponse.getData()) {
                                    System.out.println(
                                            "The approve of " + acc + "is : " + a.toString());
                                }

                                checkEvents();
                            });
        }
    }

    private static void allowance() {
        if (validArgs(2)) {
            Optional.ofNullable(validAddr(Arrays.asList(0, 1)))
                    .ifPresent(
                            accs -> {
                                ApiMsg apiMsg =
                                        CONTRACT.newFunction("allowance")
                                                .setFrom(MSGSENDER)
                                                .setParam(IAddress.copyFrom((String) accs.get(0)))
                                                .setParam(IAddress.copyFrom((String) accs.get(1)))
                                                .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                                                .setTxNrgPrice(NRG_PRICE_MIN)
                                                .build()
                                                .nonBlock()
                                                .execute();

                                if (apiMsg.isError()) {
                                    System.out.println(
                                            "Function execution isError! " + apiMsg.getErrString());
                                    return;
                                }

                                ContractResponse contractResponse = apiMsg.getObject();

                                for (Object a : contractResponse.getData()) {
                                    System.out.println(
                                            "The allowance from "
                                                    + accs.get(0)
                                                    + " to "
                                                    + accs.get(1)
                                                    + " is "
                                                    + a.toString());
                                }
                            });
        }
    }

    private static void unlockAccount() {
        if (validArgs(2)) {
            Optional.ofNullable(validAddr(0))
                    .ifPresent(
                            ulAcc -> {
                                Optional.ofNullable(getServerAccounts())
                                        .ifPresent(
                                                accs -> {
                                                    accs.forEach(
                                                            acc -> {
                                                                if (acc.equals(
                                                                        AionAddress.wrap(ulAcc))) {
                                                                    System.out.println(
                                                                            "UnlockAccount: "
                                                                                    + ulAcc);
                                                                    ApiMsg apiMsg =
                                                                            API.getWallet()
                                                                                    .unlockAccount(
                                                                                            (AionAddress)
                                                                                                    acc,
                                                                                            FUNCTION_ARGS
                                                                                                    .get(
                                                                                                            1),
                                                                                            86400);
                                                                    if (apiMsg.isError()
                                                                            || !(boolean)
                                                                                    apiMsg
                                                                                            .getObject()) {
                                                                        System.out.println(
                                                                                "Unlock account failed! Please check your password input! "
                                                                                        + apiMsg
                                                                                                .getErrString());
                                                                    }
                                                                    System.out.println(
                                                                            "UnlockAccount: success!");
                                                                }
                                                            });
                                                });
                            });
        }
    }

    private static void account() {

        if (validArgs(1) && digitArgs(0)) {
            Optional.ofNullable(getServerAccounts())
                    .ifPresent(
                            accs -> {
                                AionAddress acc =
                                        (AionAddress) accs.get(Integer.valueOf(FUNCTION_ARGS.get(0)));

                                Optional.ofNullable(acc)
                                        .ifPresent(
                                                a -> {
                                                    System.out.println(a.toString());
                                                });

                                System.out.println("Out of the index of the accounts!");
                            });
        }
    }

    private static boolean digitArgs(int i) {
        if (!FUNCTION_ARGS.get(i).chars().allMatch(Character::isDigit)) {
            System.out.println("The input argument is invalid.");
            return false;
        }

        return true;
    }

    private static List getServerAccounts() {
        ApiMsg apiMsg = API.getWallet().getAccounts();
        if (apiMsg.isError()) {
            System.out.println("GetAccounts failed! " + apiMsg.getErrString());
            return null;
        }
        return apiMsg.getObject();
    }

    private static void getAccounts() {
        Optional.ofNullable(getServerAccounts())
                .ifPresent(
                        accs -> {
                            System.out.println("List " + accs.size() + " account addresses!");
                            accs.forEach(acc -> System.out.println(acc.toString()));
                        });
    }

    private static boolean parseInput(String input) {
        resetFunction();
        boolean validInput = false;
        String[] parts = input.split("\\(");
        if (parts.length == 2) {
            FUNCTION_NAME = parts[0].replaceAll("\\s", "");

            if (parts[1].matches("\\)")) {
                return true;
            }

            String[] parts2 = parts[1].split("\\)");
            if (parts2.length == 1) {
                if (parts2[0].contains(",")) {
                    String[] parts3 = parts2[0].split(",");
                    for (String s : parts3) {
                        FUNCTION_ARGS.add(s.replaceAll("\\s?\"?", ""));
                    }
                } else {
                    FUNCTION_ARGS.add(parts2[0].replaceAll("\\s", ""));
                }
                validInput = true;
            }
        }
        return validInput;
    }

    private static void PrintFunctionList() {
        System.out.println("The contract support functions list shown as follows:");
        System.out.println("totalSupply()");
        System.out.println("balanceOf(ownerAccount)");
        System.out.println("transfer(toAccount, amount)");
        System.out.println("transferFrom(fromAccount, toAccount, amount)");
        System.out.println("approve(spenderAccount, amount)");
        System.out.println("allowance(ownerAccount, spenderAccount)");
        System.out.println();
        System.out.println("The kernel support api list shown as follows:");
        System.out.println("getAccounts(), get the account address by the input index.");
        System.out.println("account(index), get the account address by the input index.");
        System.out.println("unlockAccount(Account, password), unlock the account address.");
        System.out.println(
                "SetMsgSender(senderAccount), set the message sender, default is the coinbase account.");
        System.out.println("getMsgSender(), get the current message sender.");
        System.out.println();
        System.out.println("The general command.");
        System.out.println("-h or -H: help, show this list.");
        System.out.println("-q or -Q: quit this demo.");
    }

    private static boolean DeployContract() {
        resetLoopCondition();
        Scanner scan = new Scanner(System.in);
        System.out.println(
                "Now prepare to initial the token contract, please input the initial token supply:");
        while (UNLOCKLOOP) {
            String input = scan.nextLine();

            if (!input.isEmpty() && input.chars().allMatch(Character::isDigit)) {
                try {
                    INITIALSUPPLY = Long.valueOf(input);
                } catch (Exception e) {
                    System.out.println("Invalid initail token input, cause: " + e.toString());
                    System.out.println("Please input a number again:");
                    CNT++;
                    continue;
                }
            }

            if (INITIALSUPPLY < 1) {
                System.out.println("Invalid input, please input a number again:");
                if (CNT++ > ERROR_CNT) {
                    System.out.println("Input wrong value 3 times, quit demo! ");
                    return false;
                }
            } else {
                UNLOCKLOOP = false;
            }
        }

        ArrayList<ISolidityArg> param = new ArrayList<>();
        param.add(IUint.copyFrom(INITIALSUPPLY));
        param.add(ISString.copyFrom("Nuco coin"));
        param.add(IUint.copyFrom(10));
        param.add(ISString.copyFrom("NUCO"));

        System.out.println("Prepare to deploy the token contract.");
        System.out.println("Please press the enter key to go next step!");
        scan = new Scanner(System.in);
        scan.nextLine();

        ApiMsg apiMsg =
                API.getContractController()
                        .createFromSource(
                                tokenSC_ERC20,
                                COINBASE,
                                NRG_LIMIT_CONTRACT_CREATE_MAX,
                                NRG_PRICE_MIN,
                                param);

        if (apiMsg.isError()) {
            System.out.println("Deploy contract failed!" + apiMsg.getErrString());
            return false;
        }

        System.out.println("Contract deployed!");
        System.out.println();

        System.out.println("Now, register the events!");
        List<String> evts = new ArrayList<>();
        evts.add("Transfer");
        evts.add("Approval");

        CONTRACT = API.getContractController().getContract();

        apiMsg = CONTRACT.newEvents(evts).register();

        if (apiMsg.isError()) {
            System.out.println("Register events failed! " + apiMsg.getErrString());
            return false;
        }

        return true;
    }

    private static boolean GetAccount() {
        System.out.println("Get accounts from server, please press enter the key to go next step!");
        Scanner scan = new Scanner(System.in);
        scan.nextLine();

        ApiMsg apiMsg = API.getWallet().getAccounts();
        if (apiMsg.isError()) {
            System.out.println("GetAccounts failed! " + apiMsg.getErrString());
            return false;
        }

        List<AionAddress> accs = apiMsg.getObject();
        System.out.println("Get " + accs.size() + " accounts!");

        if (accs.size() < 3) {
            System.out.println(
                    "The number of accounts in the server is lower than 3, please check the server has a least 3 accounts to support the demo!");
            return false;
        }

        COINBASE = accs.get(0);
        MSGSENDER = COINBASE;
        System.out.println("Get the first account: " + COINBASE.toString());
        System.out.println();

        return true;
    }

    private static boolean UnlockAccount() {

        System.out.println(
                "Unlock account before deploy smart contract and execute tokens transfer.");
        System.out.println("Please press the enter key to go next step!");
        Scanner scan = new Scanner(System.in);
        scan.nextLine();
        System.out.println("Please input the password of the first account: ");
        String input = scan.nextLine();

        ApiMsg apiMsg = new ApiMsg();
        // unlock the coinbase account 1 day long
        while (UNLOCKLOOP) {
            apiMsg.set(API.getWallet().unlockAccount(COINBASE, input, 86400));
            if (apiMsg.isError() || !(boolean) apiMsg.getObject()) {
                System.out.println(
                        "Unlock account failed! Please check your password input! "
                                + apiMsg.getErrString());
                System.out.println(
                        "Please check your password input or press q to exit this demo! ");

                input = scan.nextLine();
                if (Objects.equals(input, "q") || Objects.equals(input, "Q")) {
                    System.out.println("Exit this demo!");
                    return false;
                }

                if (CNT++ > ERROR_CNT) {
                    System.out.println("Input wrong password 3 times, quit demo! ");
                    return false;
                }
            } else {
                UNLOCKLOOP = false;
            }
        }

        System.out.println("Account unlocked!");
        System.out.println();

        return true;
    }

    private static boolean ConnectServer() {

        System.out.println(
                "Create api instance and connect, please press enter key to go next step!");
        Scanner scan = new Scanner(System.in);
        scan.nextLine();

        API = IAionAPI.init();
        ApiMsg apiMsg = API.connect(IAionAPI.LOCALHOST_URL);
        if (apiMsg.isError()) {
            System.out.println("Connect server failed, exit test! " + apiMsg.getErrString());
            return false;
        }
        System.out.println("Get server connected!");
        System.out.println();

        return true;
    }

    private static void resetLoopCondition() {
        UNLOCKLOOP = true;
        CNT = 0;
    }

    private static void resetFunction() {
        FUNCTION_NAME = "";
        FUNCTION_ARGS.clear();
    }

    private static void getMsgSender() {
        System.out.println("MSGSENDER: " + MSGSENDER.toString());
    }
}
