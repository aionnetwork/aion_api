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

package org.aion.api.test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertSame;
import static junit.framework.TestCase.assertTrue;
import static org.aion.api.ITx.NRG_LIMIT_CONTRACT_CREATE_MAX;
import static org.aion.api.ITx.NRG_LIMIT_TX_MAX;
import static org.aion.api.ITx.NRG_LIMIT_TX_MIN;
import static org.aion.api.ITx.NRG_PRICE_MIN;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import org.aion.api.IAccount;
import org.aion.api.IAionAPI;
import org.aion.api.ITx;
import org.aion.api.IUtils;
import org.aion.api.impl.internal.Message.Retcode;
import org.aion.api.type.AccountDetails;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.Block;
import org.aion.api.type.BlockDetails;
import org.aion.api.type.BlockSql;
import org.aion.api.type.CompileResponse;
import org.aion.api.type.ContractAbiEntry;
import org.aion.api.type.ContractDeploy;
import org.aion.api.type.DeployResponse;
import org.aion.api.type.Key;
import org.aion.api.type.KeyExport;
import org.aion.api.type.MsgRsp;
import org.aion.api.type.Node;
import org.aion.api.type.Protocol;
import org.aion.api.type.SyncInfo;
import org.aion.api.type.Transaction;
import org.aion.api.type.TxArgs;
import org.aion.api.type.TxReceipt;
import org.aion.api.type.core.tx.AionTransaction;
import org.aion.base.type.Address;
import org.aion.base.type.Hash256;
import org.aion.base.util.ByteArrayWrapper;
import org.aion.base.util.Bytesable;
import org.aion.crypto.ECKey;
import org.aion.crypto.ECKeyFac;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by yao on 01/10/16. Contributors Jay Tseng.
 */
public class BaseAPITests {

    // Make sure the password of the testing account been set properly
    private final String pw = "PLAT4life";
    private final String url = IAionAPI.LOCALHOST_URL;
    private static final String TICKER = "contract ticker { uint public val; function tick () { val+= 1; } }";
    private static final String ERROR_TICKER = "pragma solidity ^0.4.6;\n contract ticker { uint public val; function tick () { val+= 1; } ";
    private static final String VAL = "val";
    private static final String FUNCTION = "function";
    private static final IAionAPI api = IAionAPI.init();

    @Test public void TestApiConnect() {
        System.out.println("run TestApiConnect.");
        connectAPI();

        api.destroyApi();
    }

    @Test
    public void TestApiMultiConnect() {
        System.out.println("run TestApiMultiConnect.");
        ApiMsg apiMsg;

        IAionAPI api1 = IAionAPI.init();
        apiMsg = api1.connect(url);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        IAionAPI api2 = IAionAPI.init();
        apiMsg = api2.connect(url);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        IAionAPI api3 = IAionAPI.init();
        apiMsg = api3.connect(url);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        System.out
            .println("api 1 blocknumber: " + api1.getChain().blockNumber().getObject().toString());
        System.out
            .println("api 2 blocknumber: " + api2.getChain().blockNumber().getObject().toString());
        System.out
            .println("api 3 blocknumber: " + api3.getChain().blockNumber().getObject().toString());

        api1.destroyApi();
        api2.destroyApi();
        api3.destroyApi();
    }

    @Test
    public void TestGetProtocolVersion() {
        System.out.println("run TestGetProtocolVersion.");
        connectAPI();

        ApiMsg apiMsg = api.getNet().getProtocolVersion();
        assertFalse(apiMsg.isError());

        Protocol pv = apiMsg.getObject();
        assertNotNull(pv);
        assertEquals(pv.getApi(), "2");

        api.destroyApi();
    }

    @Test
    public void TestGetMinerAccount() {
        System.out.println("run TestGetMinerAccount.");
        connectAPI();

        ApiMsg apiMsg = api.getWallet().getMinerAccount();
        assertFalse(apiMsg.isError());

        Address buff = apiMsg.getObject();
        assertNotNull(buff);

        api.destroyApi();
    }

    @Test
    public void TestBlockNumber() {
        System.out.println("run TestBlockNumber.");

        connectAPI();

        ApiMsg apiMsg = api.getChain().blockNumber();
        assertFalse(apiMsg.isError());
        Long blockNumber = apiMsg.getObject();

        assertThat(blockNumber, is(greaterThan(0L)));
        api.destroyApi();
    }

    @Test
    public void TestGetBalance() {
        System.out.println("run TestGetBalance.");
        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());

        // Make sure your connecting kernel at least has one account in the keystore.
        List accs = apiMsg.getObject();
        assertNotNull(accs);
        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        apiMsg = api.getChain().getBalance((Address) accs.get(0));
        assertFalse(apiMsg.isError());

        BigInteger balance = apiMsg.getObject();
        assertTrue(balance.compareTo(BigInteger.ZERO) > -1);
        api.destroyApi();
    }

    @Test
    public void TestGetBlockByNumber() {

        System.out.println("run TestGetBlockByNumber.");
        connectAPI();

        ApiMsg apiMsg = api.getChain().getBlockByNumber(1L);
        assertFalse(apiMsg.isError());
        Block block = apiMsg.getObject();
        assertNotNull(block);

        assertEquals(1L, block.getNumber());
        assertEquals(256, block.getBloom().getData().length);
        assertTrue(block.getDifficulty().compareTo(BigInteger.ZERO) > 0);
        assertEquals(32, block.getExtraData().getData().length);
        assertTrue(block.getNrgConsumed() >= 0L);
        assertTrue(block.getNrgLimit() >= 0L);
        assertTrue(block.getTimestamp() <= Instant.now().toEpochMilli());
        assertEquals(0, block.getTxHash().size());
        assertTrue(block.getTotalDifficulty().compareTo(BigInteger.valueOf(4L)) > 0);
        assertTrue(block.getNonce().compareTo(BigInteger.ZERO) != 0);
        api.destroyApi();
    }

    @Test
    public void TestGetTransactionByBlockHashAndIndex() {
        System.out.println("run TestGetTransactionByBlockHashAndIndex.");
        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());

        List accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.size() < 2) {
            System.out.println("this test must have 2 accounts, skip this test!");
            return;
        }

        Address acc = (Address) accs.get(0);
        Address acc2 = (Address) accs.get(1);

        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        String testData = "12345678";
        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(testData.getBytes()))
            .from(acc)
            .to(acc2)
            .nrgLimit(NRG_LIMIT_TX_MAX)
            .nrgPrice(NRG_PRICE_MIN)
            .value(BigInteger.ONE)
            .nonce(BigInteger.ZERO);

        api.getTx().fastTxbuild(builder.createTxArgs());

        long ts = Instant.now().toEpochMilli() * 1000;
        apiMsg = api.getTx().sendTransaction(null);
        assertFalse(apiMsg.isError());

        MsgRsp msgRsp = apiMsg.getObject();
        assertNotNull(msgRsp);
        assertNotNull(msgRsp.getMsgHash());

        apiMsg = api.getTx().getTxReceipt(msgRsp.getTxHash());
        assertFalse(apiMsg.isError());

        TxReceipt txRecpt = apiMsg.getObject();
        assertNotNull(txRecpt);

        apiMsg = api.getChain()
            .getTransactionByBlockHashAndIndex(txRecpt.getBlockHash(), 0);
        assertFalse(apiMsg.isError());

        Transaction transaction = apiMsg.getObject();
        assertNotNull(transaction);
        assertNotNull(transaction.getBlockHash());
        assertNotNull(transaction.getTxHash());
        assertThat(transaction.getBlockNumber(), is(greaterThan(0L)));
        assertEquals(transaction.getFrom(), acc);
        assertTrue(transaction.getNrgConsumed() > NRG_LIMIT_TX_MIN);
        assertThat(transaction.getNrgPrice(), is(equalTo(NRG_PRICE_MIN)));
        assertEquals(transaction.getTo(), acc2);

        assertThat(transaction.getNonce(), is(greaterThanOrEqualTo(BigInteger.ZERO)));
        assertThat(transaction.getTransactionIndex(), is(equalTo(0)));
        assertThat(transaction.getValue(), is(equalTo(BigInteger.ONE)));
        assertThat(transaction.getTimeStamp(), is(greaterThan(ts)));
        assertEquals(builder.createTxArgs().getData(), transaction.getData());

        api.destroyApi();
    }

    @Test
    public void TestGetTransactionByBlockNumberAndIndex() {
        System.out.println("run TestGetTransactionByBlockNumberAndIndex.");
        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());

        List accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = (Address) accs.get(0);

        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        String testData = "12345678";
        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(testData.getBytes()))
            .from(acc)
            .to(acc)
            .nrgLimit(NRG_LIMIT_TX_MAX)
            .nrgPrice(NRG_PRICE_MIN)
            .value(BigInteger.ONE)
            .nonce(BigInteger.ZERO);

        api.getTx().fastTxbuild(builder.createTxArgs());

        long ts = Instant.now().toEpochMilli() * 1000;
        apiMsg = api.getTx().sendTransaction(null);
        assertFalse(apiMsg.isError());

        MsgRsp msgRsp = apiMsg.getObject();
        assertNotNull(msgRsp);

        Hash256 txHash = msgRsp.getTxHash();
        assertNotNull(txHash);

        apiMsg = api.getTx().getTxReceipt(txHash);
        assertFalse(apiMsg.isError());

        TxReceipt txRecpt = apiMsg.getObject();
        assertNotNull(txRecpt);

        apiMsg = api.getChain().getTransactionByBlockNumberAndIndex(txRecpt.getBlockNumber(), 0);
        assertFalse(apiMsg.isError());

        Transaction transaction = apiMsg.getObject();
        assertNotNull(transaction);

        assertNotNull(transaction.getBlockHash());
        assertNotNull(transaction.getTxHash());
        assertThat(transaction.getBlockNumber(), is(greaterThan(0L)));
        assertEquals(transaction.getFrom(), acc);
        assertThat(transaction.getNrgConsumed(), is(greaterThan(NRG_LIMIT_TX_MIN)));
        assertEquals(transaction.getNrgPrice(), NRG_PRICE_MIN);
        assertEquals(transaction.getTo(), acc);
        assertThat(transaction.getNonce(), is(greaterThanOrEqualTo(BigInteger.ZERO)));
        assertEquals(transaction.getTransactionIndex(), 0);
        assertEquals(transaction.getValue(), BigInteger.ONE);
        assertThat(transaction.getTimeStamp(), is(greaterThan(ts)));
        assertEquals(builder.createTxArgs().getData(), transaction.getData());

        api.destroyApi();
    }

    @Test
    public void TestGetTransactionByBlockNumberAndIndex2() {
        System.out.println("run TestGetTransactionByBlockNumberAndIndex2.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());

        List<Address> accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        if (!isEnoughBalance(accs.get(0))) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(accs.get(0), pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        apiMsg = api.getTx().compile(TICKER);
        assertFalse(apiMsg.isError());

        Map<String, CompileResponse> contracts = apiMsg.getObject();
        assertNotNull(contracts);

        String key = "ticker";
        assertTrue(contracts.containsKey(key));

        List<ContractAbiEntry> abiDef = contracts.get(key).getAbiDefinition();
        assertNotNull(abiDef);

        assertEquals(2, abiDef.size());
        assertFalse(abiDef.get(0).anonymous);
        assertFalse(abiDef.get(0).payable);
        assertTrue(abiDef.get(0).constant);

        assertTrue(abiDef.get(0).name.contentEquals(VAL));
        assertTrue(abiDef.get(0).type.contentEquals(FUNCTION));

        assertTrue(contracts.get(key).getSource().contentEquals(TICKER));

        long ts = Instant.now().toEpochMilli() * 1000;
        CompileResponse contract = contracts.get(key);

        ContractDeploy.ContractDeployBuilder builder = new ContractDeploy.ContractDeployBuilder()
            .compileResponse(contract)
            .from(accs.get(0))
            .data(ByteArrayWrapper.wrap(Bytesable.NULL_BYTE))
            .nrgLimit(NRG_LIMIT_TX_MAX)
            .nrgPrice(NRG_PRICE_MIN)
            .value(BigInteger.ZERO);

        apiMsg = api.getTx().contractDeploy(builder.createContractDeploy());
        assertFalse(apiMsg.isError());

        DeployResponse contractResponse = apiMsg.getObject();
        assertNotNull(contractResponse);
        assertNotNull(contractResponse.getAddress());
        assertNotNull(contractResponse.getTxid());

        apiMsg = api.getChain().getTransactionByHash(contractResponse.getTxid());
        assertFalse(apiMsg.isError());

        Transaction tx = apiMsg.getObject();
        assertNotNull(tx);
        long blkNr = tx.getBlockNumber();

        apiMsg = api.getChain().getTransactionByBlockNumberAndIndex(blkNr, 0);
        assertFalse(apiMsg.isError());

        Transaction transaction = apiMsg.getObject();
        assertNotNull(transaction);

        assertNotNull(transaction.getTxHash());
        assertEquals(transaction.getBlockNumber(), blkNr);
        assertEquals(transaction.getFrom(), accs.get(0));
        assertTrue(transaction.getNrgConsumed() > NRG_LIMIT_TX_MIN);
        assertEquals(transaction.getNrgPrice(), NRG_PRICE_MIN);
        assertEquals(transaction.getTo(), Address.EMPTY_ADDRESS());
        assertTrue(transaction.getNonce().compareTo(BigInteger.ZERO) > -1);
        assertEquals(0, transaction.getTransactionIndex());
        assertEquals(transaction.getValue(), BigInteger.ZERO);
        assertTrue(transaction.getTimeStamp() > ts);

        api.destroyApi();
    }

    private boolean isEnoughBalance(Address address) {
        ApiMsg apiMsg = api.getChain().getBalance(address);
        assertFalse(apiMsg.isError());
        BigInteger balance = apiMsg.getObject();
        assertNotNull(balance);

        return balance.compareTo(BigInteger.valueOf(ITx.NRG_LIMIT_CONTRACT_CREATE_MAX)
            .multiply(BigInteger.valueOf(ITx.NRG_PRICE_MIN))) > 0;
    }

    @Test
    public void TestGetTransactionReceipt() {
        System.out.println("run TestGetTransactionReceipt.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = (Address) accs.get(0);
        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(Objects.requireNonNull(IUtils.hex2Bytes("00000000"))))
            .from(acc)
            .to(acc)
            .nrgLimit(NRG_LIMIT_TX_MAX)
            .nrgPrice(NRG_PRICE_MIN)
            .value(BigInteger.ZERO)
            .nonce(BigInteger.ZERO);

        api.getTx().fastTxbuild(builder.createTxArgs());

        apiMsg = api.getTx().sendTransaction(null);
        assertFalse(apiMsg.isError());

        MsgRsp msgRsp = apiMsg.getObject();
        assertNotNull(msgRsp);
        Hash256 txHash = msgRsp.getTxHash();
        assertNotNull(txHash);

        apiMsg = api.getTx().getTxReceipt(txHash);
        assertFalse(apiMsg.isError());
        TxReceipt txRecpt = apiMsg.getObject();
        assertNotNull(txRecpt);
        assertNotNull(txRecpt.getTxLogs());
        assertNotNull(txRecpt.getBlockHash());
        assertTrue(txRecpt.getBlockNumber() > 0);
        assertNotNull(txRecpt.getContractAddress());
        assertTrue(txRecpt.getCumulativeNrgUsed() > NRG_LIMIT_TX_MIN);
        assertTrue(txRecpt.getNrgConsumed() > NRG_LIMIT_TX_MIN);
        assertEquals(txRecpt.getFrom(), acc);
        assertEquals(txRecpt.getTo(), acc);
        assertTrue(txRecpt.getTxIndex() > -1);

        api.destroyApi();
    }

    @Test
    public void TestGetAccounts() {
        System.out.println("run TestGetAccounts.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());

        List accs = apiMsg.getObject();
        assertNotNull(accs);

        api.destroyApi();
    }

    @Test
    public void TestUnlockAccount() {
        System.out.println("run TestUnlockAccount.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = (Address) accs.get(0);
        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        apiMsg = api.getWallet().unlockAccount(acc, pw, 99999);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        apiMsg = api.getWallet().unlockAccount(acc, "fake", 99999);
        assertFalse(apiMsg.isError());
        assertFalse(apiMsg.getObject());

        if (accs.size() > 1) {
            Address acc2 = (Address) accs.get(1);

            apiMsg = api.getWallet().unlockAccount(acc2, pw, 99999);
            assertFalse(apiMsg.isError());
            assertTrue(apiMsg.getObject());

            apiMsg = api.getWallet().unlockAccount(acc2, "fake", 99999);
            assertFalse(apiMsg.isError());
            assertFalse(apiMsg.getObject());
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw, 0);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        apiMsg = api.getWallet().unlockAccount(acc, "", -1);
        assertTrue(apiMsg.isError());
        System.out.println(apiMsg.getErrString());

        Address fakeAcc = Address.ZERO_ADDRESS();
        apiMsg = api.getWallet().unlockAccount(fakeAcc, "", 99999);
        assertFalse(apiMsg.isError());
        assertFalse(apiMsg.getObject());

        boolean expectGoCatch = false;
        try {
            Address fakeAcc2 = Address.wrap(
                new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 1});  // 33bytes

            api.getWallet().unlockAccount(fakeAcc2, "", 99999);
        } catch (Exception e) {
            System.out.println("fakeAcc2 exception#" + e.getMessage());
            expectGoCatch = true;
        }

        assertTrue(expectGoCatch);
        api.destroyApi();
    }

    @Test
    public void TestSendTransaction() {
        System.out.println("run TestSendTransaction.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = (Address) accs.get(0);
        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap("TestSendTransaction!".getBytes()))
            .from(acc)
            .to(acc)
            .nrgLimit(NRG_LIMIT_TX_MAX)
            .nrgPrice(NRG_PRICE_MIN)
            .value(BigInteger.ZERO)
            .nonce(BigInteger.ZERO);

        api.getTx().fastTxbuild(builder.createTxArgs());

        apiMsg = api.getTx().sendTransaction(null);
        assertFalse(apiMsg.isError());

        MsgRsp msgRsp = apiMsg.getObject();
        assertNotNull(msgRsp);

        Hash256 hash = msgRsp.getTxHash();
        assertNotNull(hash);

        api.destroyApi();
    }

    @Test
    public void TestCompile() {
        System.out.println("run TestCompile.");

        connectAPI();

        ApiMsg apiMsg = api.getTx().compile(TICKER);
        assertFalse(apiMsg.isError());
        Map<String, CompileResponse> contracts = apiMsg.getObject();
        assertNotNull(contracts);

        String key = "ticker";
        assertTrue(contracts.containsKey(key));

        List<ContractAbiEntry> abiDef = contracts.get(key).getAbiDefinition();
        assertNotNull(abiDef);
        assertEquals(2, abiDef.size());
        assertFalse(abiDef.get(0).anonymous);
        assertFalse(abiDef.get(0).payable);
        assertTrue(abiDef.get(0).constant);
        assertTrue(abiDef.get(0).name.contentEquals(VAL));
        assertTrue(abiDef.get(0).type.contentEquals(FUNCTION));
        assertTrue(contracts.get(key).getSource().contentEquals(TICKER));

        api.destroyApi();
    }

    @Test
    public void TestCompileError() {
        System.out.println("run TestCompileError.");

        connectAPI();

        ApiMsg apiMsg = api.getTx().compile(ERROR_TICKER);
        assertTrue(apiMsg.isError());
        assertEquals(apiMsg.getErrorCode(), -10);

        System.out.println(apiMsg.getErrString());
        api.destroyApi();
    }

    @Test
    public void TestGetTransactionByHash() {
        System.out.println("run TestGetTransactionByHash.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List accs = apiMsg.getObject();

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = (Address) accs.get(0);
        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        String testData = "12345678";
        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(testData.getBytes()))
            .from(acc)
            .to(acc)
            .nrgLimit(NRG_LIMIT_TX_MAX)
            .nrgPrice(NRG_PRICE_MIN)
            .value(BigInteger.ONE)
            .nonce(BigInteger.ZERO);

        api.getTx().fastTxbuild(builder.createTxArgs());

        long ts = Instant.now().toEpochMilli() * 1000;
        apiMsg = api.getTx().sendTransaction(null);
        assertFalse(apiMsg.isError());

        MsgRsp msgRsp = apiMsg.getObject();
        assertNotNull(msgRsp);

        Hash256 txHash = msgRsp.getTxHash();
        assertNotNull(txHash);

        apiMsg = api.getTx().getTxReceipt(txHash);
        assertFalse(apiMsg.isError());

        TxReceipt txRecpt = apiMsg.getObject();
        assertNotNull(txRecpt);
        assertNotNull(txRecpt.getTxHash());

        apiMsg = api.getChain().getTransactionByHash(txRecpt.getTxHash());
        assertFalse(apiMsg.isError());

        Transaction transaction = apiMsg.getObject();
        assertNotNull(transaction);

        assertNotNull(transaction.getTxHash());
        assertEquals(transaction.getBlockNumber(), txRecpt.getBlockNumber());
        assertEquals(transaction.getFrom(), acc);
        assertTrue(transaction.getNrgConsumed() > NRG_LIMIT_TX_MIN);
        assertEquals(transaction.getNrgPrice(), NRG_PRICE_MIN);
        assertEquals(transaction.getTo(), acc);
        assertTrue(transaction.getNonce().compareTo(BigInteger.ZERO) > -1);
        assertEquals(0, transaction.getTransactionIndex());
        assertEquals(transaction.getValue(), BigInteger.ONE);
        assertTrue(transaction.getTimeStamp() > ts);
        assertEquals(builder.createTxArgs().getData(), transaction.getData());

        api.destroyApi();
    }

    @Test
    @Ignore
    public void TestGetCode() {
        System.out.println("run TestGetCode.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());

        List<Address> accs = apiMsg.getObject();
        assertNotNull(accs);
        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        if (!isEnoughBalance(accs.get(0))) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(accs.get(0), pw);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        apiMsg = api.getTx().compile(TICKER);
        assertFalse(apiMsg.isError());
        Map<String, CompileResponse> contracts = apiMsg.getObject();
        assertNotNull(contracts);

        String key = "ticker";
        assertTrue(contracts.containsKey(key));

        List<ContractAbiEntry> abiDef = contracts.get(key).getAbiDefinition();
        assertNotNull(abiDef);
        assertEquals(2, abiDef.size());
        assertFalse(abiDef.get(0).anonymous);
        assertFalse(abiDef.get(0).payable);
        assertTrue(abiDef.get(0).constant);
        assertTrue(abiDef.get(0).name.contentEquals(VAL));
        assertTrue(abiDef.get(0).type.contentEquals(FUNCTION));
        assertTrue(contracts.get(key).getSource().contentEquals(TICKER));

        CompileResponse contract = contracts.get(key);
        ContractDeploy.ContractDeployBuilder builder = new ContractDeploy.ContractDeployBuilder()
            .compileResponse(contract)
            .value(BigInteger.ZERO)
            .nrgPrice(NRG_PRICE_MIN)
            .nrgLimit(NRG_LIMIT_CONTRACT_CREATE_MAX)
            .data(ByteArrayWrapper.wrap(Bytesable.NULL_BYTE))
            .from(accs.get(0));

        apiMsg = api.getTx().contractDeploy(builder.createContractDeploy());
        assertFalse(apiMsg.isError());

        DeployResponse contractResponse = apiMsg.getObject();
        assertNotNull(contractResponse);
        assertNotNull(contractResponse.getAddress());
        assertNotNull(contractResponse.getTxid());

        apiMsg = api.getTx().getTxReceipt(contractResponse.getTxid());
        assertFalse(apiMsg.isError());
        TxReceipt txRecpt = apiMsg.getObject();
        assertNotNull(txRecpt);
        assertNotNull(txRecpt.getTxLogs());

        Long blockNumber = -1L;
        apiMsg = api.getTx().getCode(contractResponse.getAddress(), blockNumber);
        assertFalse(apiMsg.isError());
        byte[] code = apiMsg.getObject();
        assertNotNull(code);

        String cc = "0x" + IUtils.bytes2Hex(code);
        assertEquals(cc, contract.getCode());

        api.destroyApi();
    }

    @Test
    public void TestContractDeploy() {
        System.out.println("run TestContractDeploy.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());

        List<Address> accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        if (!isEnoughBalance(accs.get(0))) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(accs.get(0), pw);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        apiMsg = api.getTx().compile(TICKER);
        assertFalse(apiMsg.isError());
        Map<String, CompileResponse> contracts = apiMsg.getObject();
        assertNotNull(contracts);
        String key = "ticker";
        assertTrue(contracts.containsKey(key));

        List<ContractAbiEntry> abiDef = contracts.get(key).getAbiDefinition();
        assertNotNull(abiDef);
        assertEquals(2, abiDef.size());
        assertFalse(abiDef.get(0).anonymous);
        assertFalse(abiDef.get(0).payable);
        assertTrue(abiDef.get(0).constant);
        assertTrue(abiDef.get(0).name.contentEquals(VAL));
        assertTrue(abiDef.get(0).type.contentEquals(FUNCTION));
        assertTrue(contracts.get(key).getSource().contentEquals(TICKER));

        CompileResponse contract = contracts.get(key);
        ContractDeploy.ContractDeployBuilder builder = new ContractDeploy.ContractDeployBuilder()
            .compileResponse(contract)
            .value(BigInteger.ZERO)
            .nrgPrice(NRG_PRICE_MIN)
            .nrgLimit(NRG_LIMIT_CONTRACT_CREATE_MAX)
            .data(ByteArrayWrapper.wrap(Bytesable.NULL_BYTE))
            .from(accs.get(0));

        apiMsg = api.getTx().contractDeploy(builder.createContractDeploy());
        assertFalse(apiMsg.isError());

        DeployResponse contractResponse = apiMsg.getObject();
        assertNotNull(contractResponse);
        assertNotNull(contractResponse.getAddress());
        assertNotNull(contractResponse.getTxid());

        api.destroyApi();
    }

    @Test
    public void GetBlockByHash() {
        System.out.println("run GetBlockByHash.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());

        List<Address> accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        if (!isEnoughBalance(accs.get(0))) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        Address acc = accs.get(0);
        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(Objects.requireNonNull(IUtils.hex2Bytes("00000000"))))
            .from(acc)
            .to(acc)
            .nrgLimit(NRG_LIMIT_TX_MAX)
            .nrgPrice(NRG_PRICE_MIN)
            .value(BigInteger.ZERO)
            .nonce(BigInteger.ZERO);

        api.getTx().fastTxbuild(builder.createTxArgs());

        apiMsg = api.getTx().sendTransaction(null);
        assertFalse(apiMsg.isError());
        MsgRsp msgRsp = apiMsg.getObject();
        assertNotNull(msgRsp);
        Hash256 txHash = msgRsp.getTxHash();
        assertNotNull(txHash);

        apiMsg = api.getTx().getTxReceipt(txHash);
        assertFalse(apiMsg.isError());
        TxReceipt txRecpt = apiMsg.getObject();
        assertNotNull(txRecpt);
        assertNotNull(txRecpt.getBlockHash());

        Hash256 blockHash = txRecpt.getBlockHash();
        apiMsg = api.getChain().getBlockByHash(blockHash);
        assertFalse(apiMsg.isError());
        Block block = apiMsg.getObject();
        assertNotNull(block);

        assertTrue(block.getDifficulty().compareTo(BigInteger.ZERO) > 0);
        assertEquals(32, block.getExtraData().getData().length);
        assertTrue(block.getNrgLimit() > NRG_LIMIT_TX_MIN);
        assertTrue(block.getNrgConsumed() > NRG_LIMIT_TX_MIN);
        assertNotNull(block.getParentHash());
        assertEquals(256, block.getBloom().getData().length);
        assertTrue(block.getNonce().compareTo(BigInteger.ZERO) != 0);
        assertTrue(block.getNumber() > 0L);
        assertNotNull(block.getReceiptTxRoot());
        assertNotNull(block.getSolution());
        assertTrue(block.getSolution().getData().length > 1000);

        assertNotNull(block.getStateRoot());
        assertTrue(block.getTimestamp() <= Instant.now().toEpochMilli());
        assertNotNull(block.getTxTrieRoot());
        assertTrue(block.getTxHash().size() > 0);
        assertNotNull(block.getMinerAddress());

        api.destroyApi();
    }

    @Test(timeout = 90000)
    public void GetBlockByHash2() {
        System.out.println("run GetBlockByHash2.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());

        List<Address> accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        if (!isEnoughBalance(accs.get(0))) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        Address acc = accs.get(0);
        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        apiMsg = api.getChain().blockNumber();
        assertFalse(apiMsg.isError());
        long blkNumber = apiMsg.getObject();

        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(Objects.requireNonNull(IUtils.hex2Bytes("00000000"))))
            .from(acc)
            .to(acc)
            .nrgLimit(NRG_LIMIT_TX_MAX)
            .nrgPrice(NRG_PRICE_MIN)
            .value(BigInteger.ZERO)
            .nonce(BigInteger.ZERO);

        api.getTx().fastTxbuild(builder.createTxArgs());

        apiMsg = api.getTx().nonBlock().sendTransaction(null);
        assertFalse(apiMsg.isError());

        MsgRsp msgRsp = apiMsg.getObject();
        assertNotNull(msgRsp);
        assertNotNull(msgRsp.getMsgHash());
        ByteArrayWrapper msgHash = msgRsp.getMsgHash();

        // assume the transaction should get within next 2 blocks
        while (true) {
            apiMsg = api.getChain().blockNumber();
            assertFalse(apiMsg.isError());
            long bestBlock = apiMsg.getObject();

            if (bestBlock > blkNumber + 1) {
                break;
            }
        }

        apiMsg = api.getTx().getMsgStatus(msgHash);
        assertFalse(apiMsg.isError());

        msgRsp = apiMsg.getObject();
        assertNotNull(msgRsp);

        Hash256 txHash = msgRsp.getTxHash();
        assertNotNull(txHash);

        apiMsg = api.getTx().getTxReceipt(txHash);
        assertFalse(apiMsg.isError());

        TxReceipt txRecpt = apiMsg.getObject();
        assertNotNull(txRecpt);
        assertNotNull(txRecpt.getBlockHash());

        apiMsg = api.getChain().getBlockByHash(txRecpt.getBlockHash());
        assertFalse(apiMsg.isError());
        Block block = apiMsg.getObject();
        assertNotNull(block);

        assertTrue(block.getDifficulty().compareTo(BigInteger.ZERO) > 0);
        assertEquals(32, block.getExtraData().getData().length);
        assertTrue(block.getNrgLimit() > NRG_LIMIT_TX_MIN);
        assertTrue(block.getNrgConsumed() > NRG_LIMIT_TX_MIN);
        assertNotNull(block.getParentHash());
        assertEquals(256, block.getBloom().getData().length);
        assertTrue(block.getNonce().compareTo(BigInteger.ZERO) != 0);
        assertTrue(block.getNumber() > 0L);
        assertNotNull(block.getReceiptTxRoot());
        assertNotNull(block.getSolution());
        assertTrue(block.getSolution().getData().length > 1000);
        //assertTrue(block.getSize);

        assertNotNull(block.getStateRoot());
        assertTrue(block.getTimestamp() <= Instant.now().toEpochMilli());
        //assertTrue(block.getTotalDiff);
        assertNotNull(block.getTxTrieRoot());
        assertFalse(block.getTxHash().isEmpty());
        assertNotNull(block.getMinerAddress());

        api.destroyApi();
    }

    @Test(timeout = 90000)
    public void TestGetTransactionCount() {
        System.out.println("run TestGetTransactionCount.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());

        List<Address> accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        if (!isEnoughBalance(accs.get(0))) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        Address acc = accs.get(0);
        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        String testData = "12345678";
        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(testData.getBytes()))
            .from(acc)
            .to(acc)
            .nrgLimit(NRG_LIMIT_TX_MAX)
            .nrgPrice(NRG_PRICE_MIN)
            .value(BigInteger.ZERO)
            .nonce(BigInteger.ZERO);

        api.getTx().fastTxbuild(builder.createTxArgs());

        apiMsg = api.getChain().blockNumber();
        assertFalse(apiMsg.isError());
        long blkNumber = apiMsg.getObject();

        ByteArrayWrapper msgHash = null;
        for (int i = 0; i < 3; i++) {
            apiMsg = api.getTx().nonBlock().sendTransaction(null);
            assertFalse(apiMsg.isError());
            MsgRsp msgRsp = apiMsg.getObject();
            msgHash = msgRsp.getMsgHash();
            assertNotNull(msgHash);
        }

        // assume the transaction should get within next 2 blocks
        while (true) {
            apiMsg = api.getChain().blockNumber();
            assertFalse(apiMsg.isError());
            long bestBlock = apiMsg.getObject();

            if (bestBlock > blkNumber + 1) {
                break;
            }
        }

        apiMsg = api.getTx().getMsgStatus(msgHash);
        assertFalse(apiMsg.isError());
        MsgRsp msgRsp = apiMsg.getObject();
        assertNotNull(msgRsp);

        Hash256 txHash = msgRsp.getTxHash();
        assertNotNull(txHash);

        apiMsg = api.getTx().getTxReceipt(txHash);
        assertFalse(apiMsg.isError());

        TxReceipt txRecpt = apiMsg.getObject();
        assertNotNull(txRecpt);

        apiMsg = api.getChain().getTransactionCount(acc, txRecpt.getBlockNumber());
        assertFalse(apiMsg.isError());
        long count2 = apiMsg.getObject();

        assertEquals(3, count2);
        api.destroyApi();
    }

    @Test(timeout = 90000)
    public void TestGetBlockTransactionCountByHash() {
        System.out.println("run TestGetBlockTransactionCountByHash.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());

        List<Address> accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        if (!isEnoughBalance(accs.get(0))) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        Address acc = accs.get(0);
        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        String testData = "12345678";
        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(testData.getBytes()))
            .from(acc)
            .to(acc)
            .nrgLimit(NRG_LIMIT_TX_MAX)
            .nrgPrice(NRG_PRICE_MIN)
            .value(BigInteger.ZERO)
            .nonce(BigInteger.ZERO);

        api.getTx().fastTxbuild(builder.createTxArgs());

        apiMsg = api.getChain().blockNumber();
        assertFalse(apiMsg.isError());
        long blkNumber = apiMsg.getObject();

        ByteArrayWrapper msgHash = null;
        for (int i = 0; i < 3; i++) {
            apiMsg = api.getTx().nonBlock().sendTransaction(null);
            assertFalse(apiMsg.isError());
            MsgRsp msgRsp = apiMsg.getObject();
            msgHash = msgRsp.getMsgHash();
            assertNotNull(msgHash);
        }

        // assume the transaction should get within next 2 blocks
        while (true) {
            apiMsg = api.getChain().blockNumber();
            assertFalse(apiMsg.isError());
            long bestBlock = apiMsg.getObject();

            if (bestBlock > blkNumber + 1) {
                break;
            }
        }

        apiMsg = api.getTx().getMsgStatus(msgHash);
        assertFalse(apiMsg.isError());
        MsgRsp msgRsp = apiMsg.getObject();
        assertNotNull(msgRsp);

        Hash256 txHash = msgRsp.getTxHash();
        assertNotNull(txHash);

        apiMsg = api.getTx().getTxReceipt(txHash);
        assertFalse(apiMsg.isError());

        TxReceipt txRecpt = apiMsg.getObject();
        assertNotNull(txRecpt);
        assertNotNull(txRecpt.getBlockHash());

        apiMsg = api.getChain().getBlockTransactionCountByHash(txRecpt.getBlockHash());
        assertFalse(apiMsg.isError());

        int count = apiMsg.getObject();
        assertEquals(3, count);

        api.destroyApi();
    }

    @Test(timeout = 90000)
    public void TestGetBlockTransactionCountByNumber() {
        System.out.println("run TestGetBlockTransactionCountByNumber.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());

        List<Address> accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        if (!isEnoughBalance(accs.get(0))) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        Address acc = accs.get(0);
        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        String testData = "12345678";
        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(testData.getBytes()))
            .from(acc)
            .to(acc)
            .nrgLimit(NRG_LIMIT_TX_MAX)
            .nrgPrice(NRG_PRICE_MIN)
            .value(BigInteger.ZERO)
            .nonce(BigInteger.ZERO);

        api.getTx().fastTxbuild(builder.createTxArgs());

        apiMsg = api.getChain().blockNumber();
        assertFalse(apiMsg.isError());
        long blkNumber = apiMsg.getObject();

        ByteArrayWrapper msgHash = null;
        for (int i = 0; i < 10; i++) {
            apiMsg = api.getTx().nonBlock().sendTransaction(null);
            assertFalse(apiMsg.isError());
            MsgRsp msgRsp = apiMsg.getObject();
            msgHash = msgRsp.getMsgHash();
            assertNotNull(msgHash);
        }

        // assume the transaction should get within next 2 blocks
        while (true) {
            apiMsg = api.getChain().blockNumber();
            assertFalse(apiMsg.isError());
            long bestBlock = apiMsg.getObject();

            if (bestBlock > blkNumber + 1) {
                break;
            }
        }

        apiMsg = api.getTx().getMsgStatus(msgHash);
        assertFalse(apiMsg.isError());
        MsgRsp msgRsp = apiMsg.getObject();
        assertNotNull(msgRsp);

        Hash256 txHash = msgRsp.getTxHash();
        assertNotNull(txHash);

        apiMsg = api.getTx().getTxReceipt(txHash);
        assertFalse(apiMsg.isError());

        TxReceipt txRecpt = apiMsg.getObject();
        assertNotNull(txRecpt);

        apiMsg = api.getChain().getBlockTransactionCountByNumber(txRecpt.getBlockNumber());
        assertFalse(apiMsg.isError());

        int count = apiMsg.getObject();
        assertEquals(10, count);

        api.destroyApi();
    }

    @Test
    public void TestCall() {
        System.out.println("run TestCall.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());

        List<Address> accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        if (!isEnoughBalance(accs.get(0))) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        Address acc = accs.get(0);
        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        apiMsg = api.getTx().compile(TICKER);
        assertFalse(apiMsg.isError());

        Map<String, CompileResponse> contracts = apiMsg.getObject();
        assertNotNull(contracts);
        String key = "ticker";
        assertTrue(contracts.containsKey(key));

        List<ContractAbiEntry> abiDef = contracts.get(key).getAbiDefinition();
        assertNotNull(abiDef);
        assertEquals(2, abiDef.size());
        assertFalse(abiDef.get(0).anonymous);
        assertFalse(abiDef.get(0).payable);
        assertTrue(abiDef.get(0).constant);
        assertTrue(abiDef.get(0).name.contentEquals(VAL));
        assertTrue(abiDef.get(0).type.contentEquals(FUNCTION));
        assertTrue(contracts.get(key).getSource().contentEquals(TICKER));

        CompileResponse contract = contracts.get(key);
        ContractDeploy.ContractDeployBuilder builder = new ContractDeploy.ContractDeployBuilder()
            .compileResponse(contract)
            .value(BigInteger.ZERO)
            .nrgPrice(NRG_PRICE_MIN)
            .nrgLimit(NRG_LIMIT_CONTRACT_CREATE_MAX)
            .data(ByteArrayWrapper.wrap(Bytesable.NULL_BYTE))
            .from(accs.get(0));

        apiMsg = api.getTx().contractDeploy(builder.createContractDeploy());
        assertFalse(apiMsg.isError());

        DeployResponse contractResponse = apiMsg.getObject();

        assertNotNull(contractResponse);
        assertNotNull(contractResponse.getAddress());

        apiMsg = api.getTx().getCode(contractResponse.getAddress(), -1L);
        assertFalse(apiMsg.isError());

        byte[] code = apiMsg.getObject();
        assertNotNull(code);

        // call function from deployed contract
        String functionCall = VAL + "()";

        // retrieve first 8 bytes of keccak-256 hashed value
        String hashFunctionCall = Objects.requireNonNull(IUtils
            .bytes2Hex(IUtils.sha3(Arrays.copyOfRange(functionCall.getBytes(), 0, 5))))
            .substring(0, 8);
        byte[] hashFunctionCallBytes = IUtils.hex2Bytes(hashFunctionCall);
        assertThat(hashFunctionCall, is(equalTo("3c6bb436")));

        assert hashFunctionCallBytes != null;
        TxArgs txArgs = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(hashFunctionCallBytes))
            .from(acc)
            .to(contractResponse.getAddress())
            .nrgLimit(NRG_LIMIT_TX_MAX)
            .nrgPrice(NRG_PRICE_MIN)
            .value(BigInteger.ZERO)
            .nonce(BigInteger.ZERO)
            .createTxArgs();

        apiMsg = api.getTx().call(txArgs);
        assertFalse(apiMsg.isError());

        ByteArrayWrapper returnHash = ByteArrayWrapper.wrap(apiMsg.getObject());
        // 16-bytes
        assertEquals(returnHash, ByteArrayWrapper.wrap(Objects
            .requireNonNull(IUtils.hex2Bytes("00000000000000000000000000000000"))));
        api.destroyApi();
    }

    private void connectAPI() {
        ApiMsg apiMsg = api.connect(url);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());
    }

    @Test
    public void TestEstimateNrg() {
        System.out.println("run TestEstimateNrg.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());

        List<Address> accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = accs.get(0);
        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        apiMsg = api.getTx().compile(TICKER);
        assertFalse(apiMsg.isError());

        Map<String, CompileResponse> contracts = apiMsg.getObject();
        assertNotNull(contracts);

        String key = "ticker";
        assertTrue(contracts.containsKey(key));

        List<ContractAbiEntry> abiDef = contracts.get(key).getAbiDefinition();
        assertNotNull(abiDef);
        assertEquals(2, abiDef.size());
        assertFalse(abiDef.get(0).anonymous);
        assertFalse(abiDef.get(0).payable);
        assertTrue(abiDef.get(0).constant);
        assertTrue(abiDef.get(0).name.contentEquals(VAL));
        assertTrue(abiDef.get(0).type.contentEquals(FUNCTION));
        assertTrue(contracts.get(key).getSource().contentEquals(TICKER));

        CompileResponse contract = contracts.get(key);
        ContractDeploy.ContractDeployBuilder builder = new ContractDeploy.ContractDeployBuilder()
            .compileResponse(contract)
            .value(BigInteger.ZERO)
            .nrgPrice(NRG_PRICE_MIN)
            .nrgLimit(NRG_LIMIT_CONTRACT_CREATE_MAX)
            .data(ByteArrayWrapper.wrap(Bytesable.NULL_BYTE))
            .from(acc);

        apiMsg = api.getTx().contractDeploy(builder.createContractDeploy());
        assertFalse(apiMsg.isError());

        DeployResponse contractResponse = apiMsg.getObject();
        assertNotNull(contractResponse);
        assertNotNull(contractResponse.getAddress());

        apiMsg = api.getTx().getCode(contractResponse.getAddress(), -1L);
        assertFalse(apiMsg.isError());

        byte[] code = apiMsg.getObject();
        assertNotNull(code);

        // call function from deployed contract
        String functionCall = VAL + "()";

        // retrieve first 8 bytes of keccak-256 hashed value
        String hashFunctionCall = Objects.requireNonNull(IUtils
            .bytes2Hex(IUtils.sha3(Arrays.copyOfRange(functionCall.getBytes(), 0, 5))))
            .substring(0, 8);
        byte[] hashFunctionCallBytes = IUtils.hex2Bytes(hashFunctionCall);
        assertThat(hashFunctionCall, is(equalTo("3c6bb436")));

        assert hashFunctionCallBytes != null;
        TxArgs txArgs = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(hashFunctionCallBytes))
            .from(acc)
            .to(contractResponse.getAddress())
            .nrgLimit(NRG_LIMIT_TX_MAX)
            .nrgPrice(NRG_PRICE_MIN)
            .value(BigInteger.ZERO)
            .nonce(BigInteger.ZERO)
            .createTxArgs();

        apiMsg = api.getTx().estimateNrg(txArgs);
        assertFalse(apiMsg.isError());
        long nrg = apiMsg.getObject();
        assertTrue(nrg > NRG_LIMIT_TX_MIN);
        api.destroyApi();
    }


    @Test
    public void TestIsSyncing() {
        System.out.println("run TestIsSyncing.");

        connectAPI();

        ApiMsg apiMsg = api.getNet().isSyncing();
        assertFalse(apiMsg.isError());

        System.out.println("Syncing: " + apiMsg.getObject().toString());
        api.destroyApi();
    }

    @Test
    public void TestGetActiveNodes() {
        System.out.println("run TestGetActiveNodes.");

        connectAPI();

        ApiMsg apiMsg = api.getNet().getActiveNodes();
        assertFalse(apiMsg.isError());

        List<Node> nodeList = apiMsg.getObject();
        assertNotNull(nodeList);

        // TODO: check return data

        api.destroyApi();
    }

    @Test
    public void TestGetStaticNodes() {
        System.out.println("run TestGetStaticNodes.");

        connectAPI();

        ApiMsg apiMsg = api.getNet().getStaticNodes();
        assertFalse(apiMsg.isError());
        List<String> nodeList = apiMsg.getObject();
        assertNotNull(nodeList);

        // TODO: check return data

        api.destroyApi();
    }

    @Test
    public void TestGetSolcVersion() {
        System.out.println("run TestGetSolcVersion.");

        connectAPI();

        ApiMsg apiMsg = api.getTx().getSolcVersion();
        assertFalse(apiMsg.isError());

        String version = apiMsg.getObject();
        assertNotNull(version);
        assertTrue(version.contains("0.4"));

        api.destroyApi();
    }

    @Test
    public void GetSyncInfo() {
        System.out.println("run GetSyncInfo.");

        connectAPI();

        ApiMsg apiMsg = api.getNet().syncInfo();
        assertFalse(apiMsg.isError());

        SyncInfo info = apiMsg.getObject();
        assertNotNull(info);

        assertTrue(info.getChainBestBlock() > -1L);
        assertTrue(info.getNetworkBestBlock() > -1L);
        assertTrue(info.getMaxImportBlocks() > 0L);
        System.out.println("Is Syncing:" + (info.isSyncing() ? "true" : "false"));

        api.destroyApi();
    }

    @Test
    public void GetIsMining() {
        System.out.println("run GetIsMining.");

        connectAPI();

        ApiMsg apiMsg = api.getMine().isMining();
        assertFalse(apiMsg.isError());

        boolean isMining = apiMsg.getObject();
        System.out.println("Is mining: " + (isMining ? "true" : "false"));

        api.destroyApi();
    }

    @Test
    public void TestApiConnectTwice() {
        System.out.println("run TestApiConnectTwice.");

        connectAPI();
        api.destroyApi();

        connectAPI();
        api.destroyApi();
    }

    @Test
    public void TestApiReConnect() {
        System.out.println("run TestApiReConnect.");

        IAionAPI api = IAionAPI.init();
        ApiMsg apiMsg = api.connect(url, true);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());
        api.destroyApi();
    }

    @Test
    public void TestAccountCreate() {
        System.out.println("run TestAccountCreate.");

        connectAPI();

        ApiMsg apiMsg = api.getAccount().accountCreate(Collections.singletonList(pw), false);
        assertFalse(apiMsg.isError());

        List<Key> k = apiMsg.getObject();
        assertNotNull(k);
        assertEquals(1, k.size());

        assertNotNull(k.get(0).getPubKey());
        assertSame(k.get(0).getPriKey().getData(), ByteArrayWrapper.NULL_BYTE);

        apiMsg = api.getAccount().accountCreate(Collections.singletonList(pw), true);
        assertFalse(apiMsg.isError());
        k = apiMsg.getObject();
        assertNotNull(k);
        assertNotNull(k.get(0).getPubKey());
        assertNotNull(k.get(0).getPriKey().getData());

        List<String> sList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            sList.add("");
        }

        apiMsg = api.getAccount().accountCreate(sList, false);
        assertFalse(apiMsg.isError());

        k = apiMsg.getObject();
        assertNotNull(k);
        assertEquals(3, k.size());

        for (int i = 0; i < 3; i++) {
            assertNotNull(k.get(i).getPubKey());
            assertSame(k.get(i).getPriKey().getData(), ByteArrayWrapper.NULL_BYTE);
        }

        apiMsg = api.getAccount().accountCreate(sList, true);
        assertFalse(apiMsg.isError());

        k = apiMsg.getObject();
        assertNotNull(k);
        assertEquals(10, k.size());
        for (int i = 0; i < 10; i++) {
            assertNotNull(k.get(0).getPubKey());
            assertNotNull(k.get(0).getPriKey().getData());
        }

        api.destroyApi();
    }

    @Test
    public void TestAccountExport() {
        System.out.println("run TestAccountExport.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List<Address> accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        List<Key> keys = new ArrayList<>();
        for (int i = 0; i < accs.size() && i < 2; i++) {
            keys.add(new Key(accs.get(i), pw));
        }

        apiMsg = api.getAccount().accountExport(keys);
        assertFalse(apiMsg.isError());

        KeyExport ke = apiMsg.getObject();
        assertNotNull(ke);
        assertNotNull(ke.getInvalidAddress());
        assertNotNull(ke.getKeyFiles());
        assertEquals(keys.size(), ke.getKeyFiles().size() + ke.getInvalidAddress().size());

        api.destroyApi();
    }

    @Test
    public void TestAccountBackup() {
        System.out.println("run TestAccountBackup.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List<Address> accs = apiMsg.getObject();
        assertNotNull(accs);
        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        List<Key> keys = new ArrayList<>();
        for (int i = 0; i < accs.size() && i < 2; i++) {
            keys.add(new Key(accs.get(i), pw));
        }

        apiMsg = api.getAccount().accountBackup(keys);
        assertFalse(apiMsg.isError());

        KeyExport ke = apiMsg.getObject();
        assertNotNull(ke);
        assertNotNull(ke.getInvalidAddress());
        assertNotNull(ke.getKeyFiles());
        assertEquals(keys.size(), ke.getKeyFiles().size() + ke.getInvalidAddress().size());

        api.destroyApi();
    }

    @Test
    @Ignore
    public void TestAccountImport() {
        System.out.println("run TestAccountImport.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List<Address> accs = apiMsg.getObject();
        assertNotNull(accs);
        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        List<Key> keys = new ArrayList<>();
        for (int i = 0; i < accs.size() && i < 10; i++) {
            keys.add(new Key(accs.get(i), pw));
        }

        apiMsg = api.getAccount().accountBackup(keys);
        assertFalse(apiMsg.isError());
        KeyExport ke = apiMsg.getObject();
        assertNotNull(ke);
        assertNotNull(ke.getInvalidAddress());
        assertNotNull(ke.getKeyFiles());
        assertEquals(keys.size(), ke.getKeyFiles().size() + ke.getInvalidAddress().size());

        Map<String, String> keyMap = new HashMap<>();
        for (ByteArrayWrapper s : ke.getKeyFiles()) {
            keyMap.put(s.toString(), pw);
        }

        apiMsg = api.getAccount().accountImport(keyMap);
        assertFalse(apiMsg.isError());

        List<String> invalidKey = apiMsg.getObject();
        assertNotNull(invalidKey);
        assertEquals(invalidKey.size(), keyMap.size());

        api.destroyApi();
    }

    @Test
    public void TestlockAccount() {
        System.out.println("run TestlockAccount.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List<Address> accs = apiMsg.getObject();
        assertNotNull(accs);
        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = accs.get(0);
        apiMsg = api.getWallet().lockAccount(acc, pw);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        apiMsg = api.getWallet().lockAccount(acc, "fake");
        assertFalse(apiMsg.isError());
        assertFalse(apiMsg.getObject());

        api.destroyApi();
    }

    @Test
    @Ignore
    public void TestSendSignedTransaction() {
        System.out.println("run TestSendSignedTransaction.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());

        List<Address> accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = accs.get(0);
        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        Key key = new Key(acc, pw);

        apiMsg = api.getAccount().accountExport(Collections.singletonList(key));
        assertFalse(apiMsg.isError());

        KeyExport ke = apiMsg.getObject();
        assertNotNull(ke);
        assertEquals(1, ke.getKeyFiles().size());

        ByteArrayWrapper bw = ke.getKeyFiles().get(0);
        assertNotNull(bw);

        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap("TestSendTransaction!".getBytes()))
            .from(acc)
            .to(acc)
            .nrgLimit(NRG_LIMIT_TX_MAX)
            .nrgPrice(NRG_PRICE_MIN)
            .value(BigInteger.ZERO)
            // make sure the nonce setting is correct
            .nonce(BigInteger.ZERO);

        apiMsg = api.getTx().sendSignedTransaction(builder.createTxArgs(), bw);
        assertFalse(apiMsg.isError());

        MsgRsp msgRsp = apiMsg.getObject();
        assertNotNull(msgRsp);

        Hash256 hash = msgRsp.getTxHash();
        assertNotNull(hash);
        api.destroyApi();
    }

    @Test
    public void TestSendRawTransaction() {
        System.out.println("run TestSendSignedTransaction.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());

        List<Address> accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.size() < 2) {
            System.out.println("this test need two accounts, skip this test!");
            return;
        }

        Address acc = accs.get(0);
        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        Key key = new Key(acc, pw);

        apiMsg = api.getAccount().accountExport(Collections.singletonList(key));
        assertFalse(apiMsg.isError());

        KeyExport ke = apiMsg.getObject();
        assertNotNull(ke);
        assertEquals(1, ke.getKeyFiles().size());

        // create an ECKey object
        ByteArrayWrapper bw = ke.getKeyFiles().get(0);
        assertNotNull(bw);

        ECKey ecKey = ECKeyFac.inst().create().fromPrivate(bw.toBytes());
        assertNotNull(ecKey);

        apiMsg = api.getChain().getNonce(acc);
        assertFalse(apiMsg.isError());
        BigInteger nonce = apiMsg.getObject();
        assertNotNull(nonce);

        BigInteger b0 = api.getChain().getBalance(acc).getObject();
        // BigInteger maybe overflow with Long format
        assertTrue(b0.compareTo(BigInteger.ZERO) > -1);
        long transfer = 1000L;
        AionTransaction tx0 = new AionTransaction(nonce.toByteArray()
            , accs.get(1)
            , (BigInteger.valueOf(transfer)).toByteArray()
            , "TestSendTransaction!".getBytes()
            , NRG_LIMIT_TX_MAX
            , NRG_PRICE_MIN);
        tx0.sign(ecKey);

        apiMsg = api.getTx().sendRawTransaction(ByteArrayWrapper.wrap(tx0.getEncoded()));
        assertFalse(apiMsg.isError());

        MsgRsp msgRsp = apiMsg.getObject();
        assertNotNull(msgRsp);

        Hash256 hash = msgRsp.getTxHash();
        assertNotNull(hash);

        apiMsg = api.getChain().getBalance(acc);
        assertFalse(apiMsg.isError());

        BigInteger b1 = apiMsg.getObject();
        assertNotNull(b1);
        // BigInteger maybe overflow with Long format
        assertTrue(b1.compareTo(BigInteger.ZERO) > -1);

        apiMsg = api.getChain().getTransactionByHash(hash);
        assertFalse(apiMsg.isError());

        Transaction tx1 = apiMsg.getObject();
        assertNotNull(tx1);
        BigInteger diff = b0.subtract(b1);

        assertEquals(((tx1.getNrgConsumed() * NRG_PRICE_MIN) + transfer), diff.longValue());

        api.destroyApi();
    }

    @Test
    public void TestGetBlockDetailsByNumber() {
        System.out.println("run TestGetBlockDetailsByNumber.");

        connectAPI();

        ApiMsg apiMsg = api.getChain().blockNumber();
        assertFalse(apiMsg.isError());

        long blkNumber = apiMsg.getObject();
        if (blkNumber < 16) {
            System.out.println(
                "the connect kernel doesn't have enough blocks to query, skip this test case!");
            return;
        }

        String blks = "1-16";
        ApiMsg msg = api.getAdmin().getBlockDetailsByNumber(blks);
        assertFalse(msg.isError());

        List<BlockDetails> bds = msg.getObject();
        assertNotNull(bds);
        assertEquals(16, bds.size());

        api.destroyApi();
    }

    @Test
    public void TestGetBlockDetailsByLatest() {
        System.out.println("run TestGetBlockDetailsByLatest.");

        connectAPI();

        ApiMsg apiMsg = api.getChain().blockNumber();
        assertFalse(apiMsg.isError());

        long blkNumber = apiMsg.getObject();
        if (blkNumber < 100) {
            System.out.println(
                "the connect kernel doesn't have enough blocks to query, skip this test case!");
            return;
        }

        apiMsg = api.getAdmin().getBlockDetailsByLatest(100L);
        assertFalse(apiMsg.isError());

        List<BlockDetails> bds = apiMsg.getObject();
        assertNotNull(bds);
        assertEquals(100, bds.size());

        api.destroyApi();
    }

    @Test
    public void TestGetBlocksByLatest() {
        System.out.println("run TestGetBlocksByLatest.");

        connectAPI();

        ApiMsg apiMsg = api.getChain().blockNumber();
        assertFalse(apiMsg.isError());

        long blkNumber = apiMsg.getObject();
        if (blkNumber < 100) {
            System.out.println(
                "the connect kernel doesn't have enough blocks to query, skip this test case!");
            return;
        }

        ApiMsg msg = api.getAdmin().getBlocksByLatest(100L);
        assertFalse(msg.isError());

        List<Block> blks = msg.getObject();
        assertNotNull(blks);
        assertEquals(100, blks.size());

        api.destroyApi();
    }

    @Test
    public void TestGetBlocksSqlByRange() {
        System.out.println("run TestGetBlocksSqlByRange.");

        connectAPI();

        ApiMsg apiMsg = api.getChain().blockNumber();
        assertFalse(apiMsg.isError());

        long blkNumber = apiMsg.getObject();
        if (blkNumber < 100) {
            System.out.println(
                "the connect kernel doesn't have enough blocks to query, skip this test case!");
            return;
        }

        long t0 = System.currentTimeMillis();

        ApiMsg msg = api.getAdmin().getBlockSqlByRange(50L, 100L);
        assertFalse(msg.isError());
        List<BlockSql> blks = msg.getObject();
        assertNotNull(blks);

        long t1 = System.currentTimeMillis();

        long totalTime = t1 - t0;
        long totalTxns = 0;
        for (BlockSql b : blks) {
            System.out.println("#: " + b.getNumber() + " [" + b.getTransactions().size() + "]");
            totalTxns += b.getTransactions().size();
        }

        System.out.println("bench: " + (t1 - t0) + " ms");
        System.out.println("time/txn: " + totalTime / (double) totalTxns);

        api.destroyApi();
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void TestGetBlocksDetailsByRange() {
        System.out.println("run TestGetBlocksDetailsByRange.");

        connectAPI();

        ApiMsg apiMsg = api.getChain().blockNumber();
        assertFalse(apiMsg.isError());

        long blkNumber = apiMsg.getObject();
        if (blkNumber < 500) {
            System.out.println(
                "the connect kernel doesn't have enough blocks to query, skip this test case!");
            return;
        }

        long t0 = System.currentTimeMillis();

        ApiMsg msg = api.getAdmin().getBlockDetailsByRange(1L, 500L);
        assertFalse(msg.isError());
        List<BlockDetails> blks = msg.getObject();
        assertNotNull(blks);

        long t1 = System.currentTimeMillis();

        long totalTime = t1 - t0;
        long totalTxns = 0;
        for (BlockDetails b : blks) {
            System.out.println("#: " + b.getNumber() + " [" + b.getTxDetails().size() + "]");
            totalTxns += b.getTxDetails().size();
        }

        System.out.println("bench: " + (t1 - t0) + " ms");
        System.out.println("time/txn: " + totalTime / (double) totalTxns);

        api.destroyApi();
    }

    @Test
    public void TestGetBlocksSqlByRangeLatest() throws Throwable {
        System.out.println("run TestGetBlocksSqlByRangeLatest.");

        IAionAPI api = IAionAPI.init();
        api.connect(url, false, 1, 1000);

        if (!api.isConnected()) {
            System.out.println("Api not connected.");
            api.destroyApi();
            return;
        }

        ApiMsg apiMsg = api.getChain().blockNumber();
        assertFalse(apiMsg.isError());
        long latest = apiMsg.getObject();

        long t0 = System.currentTimeMillis();

        int retry = 0;
        do {
            try {
                if (!api.isConnected()) {
                    System.out.println("Api not connected.");
                    api.destroyApi();
                    api.connect(url);
                }

                apiMsg = api.getAdmin().getBlockSqlByRange(0L, latest);
                assertFalse(apiMsg.isError());
                retry = 400;
            } catch (Exception e) {
                System.out.println("Retrying api call ------------------------------");
                e.printStackTrace();
                retry++;
                Thread.sleep(1000);
            }
        }
        while (retry < 100);

        assertNotNull(apiMsg);
        assertFalse(apiMsg.isError());
        List<BlockSql> blks = apiMsg.getObject();

        long t1 = System.currentTimeMillis();

        long totalTime = t1 - t0;
        long totalTxns = 0;
        for (BlockSql b : blks) {
            System.out.println("#: " + b.getNumber() + " [" + b.getTransactions().size() + "]");
            totalTxns += b.getTransactions().size();
        }

        System.out.println("bench: " + (t1 - t0) + " ms");
        System.out.println("time/txn: " + totalTime / (double) totalTxns);

        assertNotNull(blks);

        api.destroyApi();
    }

    @Test
    public void TestGetAccountDetailsByAddressList() {
        System.out.println("run TestGetAccountDetailsByAddressList.");

        connectAPI();

        String accounts = "";
        accounts += "0x159e9fec71602cbc1bc0739776e485428b7b7dceda6398ee76cca828014e7ba3";
        accounts += ",59fa6193611849755a479fae869f1c4d67c29dda5cae73df2ab06e179feb5d3d";
        accounts += ",0xd549820376ca9241008c19059969c6659c1439df5a908d1bb925d19eaf234390";
        accounts += ",0xba860af4a05376b30e4beab44f41ef5020b985a16382eb2c806f7fd2838eb4c0";
        accounts += ",70a6eb5c465c4d20a3966731d23332089b4798f2b9a78dcc35b2fad6dfea301a";
        accounts += ",0x653255a3316632a2b82ab7a5019217c13dfb30ca54d55b6c603c4ac62c8587a4";
        accounts += ",0x0000000000000000000000000000000000000000000000000000000000000000";
        accounts += ",0x653255a3316632a2b82ab7a5019217c13dfb30ca54d55b6c603c4ac62c8587a5";
        accounts += ",0x653255a3316632a2b82ab7a5019217c13dfb30ca54d55b6c603c4ac62c8587a5";

        ApiMsg msg = api.getAdmin().getAccountDetailsByAddressList(accounts);
        assertFalse(msg.isError());

        List<AccountDetails> accs = msg.getObject();
        assertNotNull(accs);

        api.destroyApi();
    }

    /**
     * Tests that getNonce works when used on the first account in the account list.
     */
    @Test
    public void TestGetNonce() {
        System.out.println("run TestGetNonce.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List accs = apiMsg.getObject();
        assertFalse(accs.isEmpty());

        Address acc = (Address) accs.get(0);
        apiMsg = api.getChain().getNonce(acc);
        assertFalse(apiMsg.isError());

        BigInteger no = apiMsg.getObject();
        assertNotNull(no);
        System.out.println("Nonce of account " + acc.toString() + " is " + no);
        api.destroyApi();

    }

    /**
     * Tests that getNonce returns a nonce that is 1 greater than the nonce prior to making a
     * transaction.
     */
    @Test
    public void TestNonceAfterTransaction() {
        System.out.println("run TestNonceAfterTransaction.");

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());

        List<Address> accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = accs.get(0);
        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getChain().getNonce(acc);
        assertFalse(apiMsg.isError());

        BigInteger prevNonce = apiMsg.getObject();
        assertNotNull(prevNonce);

        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(Objects.requireNonNull(IUtils.hex2Bytes("00000000"))))
            .from(acc)
            .to(acc)
            .nrgLimit(NRG_LIMIT_TX_MAX)
            .nrgPrice(NRG_PRICE_MIN)
            .value(BigInteger.ZERO)
            .nonce(BigInteger.ZERO);
        api.getTx().fastTxbuild(builder.createTxArgs());

        apiMsg = api.getTx().sendTransaction(null);
        assertFalse(apiMsg.isError());
        MsgRsp msgRsp = apiMsg.getObject();
        assertNotNull(msgRsp);

        Hash256 txHash = msgRsp.getTxHash();
        assertNotNull(txHash);

        apiMsg = api.getChain().getNonce(acc);
        assertFalse(apiMsg.isError());
        BigInteger currNonce = apiMsg.getObject();
        assertNotNull(currNonce);
        System.out.println("Previous nonce " + prevNonce + " current nonce " + currNonce);
        Assert.assertEquals(currNonce.subtract(prevNonce), BigInteger.ONE);

        api.destroyApi();
    }

    /**
     * Tests that getNrgPrice does not throw an error but returns an appropriate long value.
     */
    @Test
    public void TestGetNrgPrice() {
        System.out.println("run TestGetNrgPrice.");

        connectAPI();

        ApiMsg apiMsg = api.getTx().getNrgPrice();
        assertFalse(apiMsg.isError());

        long nrg = apiMsg.getObject();
        System.out.println("The recommended energy price is " + nrg);
        api.destroyApi();
    }

    @Test
    public void TestApiShutdown() {
        System.out.println("run TestGracefulShutdown.");

        for (int i = 0; i < 4; i++) {
            connectAPI();
            long t0 = System.currentTimeMillis();

            ApiMsg msg = api.getAdmin().getBlockDetailsByRange(1L, 10L);
            assertFalse(msg.isError());
            List<BlockDetails> blks = msg.getObject();
            assertNotNull(blks);

            long t1 = System.currentTimeMillis();

            long totalTime = t1 - t0;
            long totalTxns = 0;
            for (BlockDetails b : blks) {
                System.out.println("#: " + b.getNumber() + " [" + b.getTxDetails().size() + "]");
                totalTxns += b.getTxDetails().size();
            }

            System.out.println("bench: " + (t1 - t0) + " ms");
            System.out.println("time/txn: " + totalTime / (double) totalTxns);

            api.destroyApi();

            System.out.println("Api Destroyed");
        }

        int nbRunning = 0;
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getState() == Thread.State.RUNNABLE) {
                System.out.println("thread " + t.getName() + " Runnable");
                nbRunning++;
            }
        }

        assertEquals(3, nbRunning);

        System.out.println("test done");
    }

    @Test
    public void TestKeystoreCreateLocal() {

        List<String> passphrase = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            passphrase.add(String.valueOf(i));
        }

        ApiMsg apiMsg = IAccount.keystoreCreateLocal(passphrase);
        assertFalse(apiMsg.isError());

        List<String> newAddrs = apiMsg.getObject();
        assertNotNull(newAddrs);
        assertEquals(newAddrs.size(), 10);
    }

    @Test
    public void TestGetBlocksDetailsByHash() {
        System.out.println("run TestGetBlocksDetailsByHash.");

        connectAPI();

        ApiMsg apiMsg = api.getChain().blockNumber();
        assertFalse(apiMsg.isError());

        apiMsg = api.getChain().getBlockByNumber(apiMsg.getObject());
        assertFalse(apiMsg.isError());

        Block blk = apiMsg.getObject();
        assertNotNull(blk);
        assertNotNull(blk.getHash());

        apiMsg = api.getAdmin().getBlockDetailsByHash(blk.getHash());

        if (apiMsg.isError()) {
            if (apiMsg.getErrorCode() == Retcode.r_fail_function_call_VALUE) {
                System.out.println("Can't find the block by given hash");
            } else {
                System.out.println(apiMsg.getErrString());
            }
        }

        BlockDetails bd = apiMsg.getObject();
        assertNotNull(bd);
        assertEquals(blk.getHash(), bd.getHash());

        api.destroyApi();
    }

    @Test
    public void TestContractEstimateNrg() {
        System.out.println("run TestContractEstimateNrg.");

        connectAPI();

        // compile code
        ApiMsg apiMsg = api.getTx()
            .compile("contract ticker { uint public val; function tick () { val+= 1; } }");
        assertFalse(apiMsg.isError());

        Map<String, CompileResponse> result = apiMsg.getObject();
        assertNotNull(result);

        String key = "ticker";
        CompileResponse contract = result.get(key);

        // get NRG estimate
        apiMsg = api.getTx().estimateNrg(contract.getCode());
        assertFalse(apiMsg.isError());

        long estimate = apiMsg.getObject();
        assertEquals(estimate, 233661);
        api.destroyApi();
    }
}
