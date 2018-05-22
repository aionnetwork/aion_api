/*******************************************************************************
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
 *
 ******************************************************************************/

package org.aion.api.test;

import org.aion.api.IAionAPI;
import org.aion.api.IUtils;
import org.aion.api.type.*;
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

import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import java.util.stream.IntStream;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Created by yao on 01/10/16.
 * Contributors Jay Tseng.
 */
public class BaseAPITests {
    // Make sure the password of the testing account been set properly
    private final String pw = "";
    private final String url = IAionAPI.LOCALHOST_URL;
    private static final String TICKER = "contract ticker { uint public val; function tick () { val+= 1; } }";
    private static final String ERROR_TICKER = "pragma solidity ^0.4.6;\n contract ticker { uint public val; function tick () { val+= 1; } }";
    private static final String VAL = "val";
    private static final String FUNCTION = "function";
    private static final long NRG_PRICE = 10000000000L;

    @Test
    public void TestApiConnect() {
        System.out.println("run TestApiConnect.");
        IAionAPI api = IAionAPI.init();
        ApiMsg apiMsg = api.connect(url);

        assertFalse(apiMsg.isError());

        api.destroyApi();
    }

    @Test
    public void TestApiMultiConnect() {
        System.out.println("run TestApiMultiConnect.");
        ApiMsg apiMsg = new ApiMsg();

        IAionAPI api1 = IAionAPI.init();
        apiMsg = api1.connect(url);
        assertFalse(apiMsg.isError());

        IAionAPI api2 = IAionAPI.init();
        apiMsg = api2.connect(url);
        assertFalse(apiMsg.isError());

        IAionAPI api3 = IAionAPI.init();
        apiMsg = api3.connect(url);
        assertFalse(apiMsg.isError());

        System.out.println("api 1 blocknumber: " + api1.getChain().blockNumber().getObject().toString());
        System.out.println("api 2 blocknumber: " + api1.getChain().blockNumber().getObject().toString());
        System.out.println("api 3 blocknumber: " + api1.getChain().blockNumber().getObject().toString());

        api1.destroyApi();
        api2.destroyApi();
        api3.destroyApi();
    }

    @Test
    public void TestGetProtocolVersion() {
        System.out.println("run TestGetProtocolVersion.");
        IAionAPI api = IAionAPI.init();
        api.connect(url);
        Protocol pv = api.getNet().getProtocolVersion().getObject();

        assertNotNull(pv);
        assertEquals(pv.getApi(), "2");
        api.destroyApi();
    }

    @Test
    public void TestGetMinerAccount()  {
        System.out.println("run TestGetMinerAccount.");
        IAionAPI api = IAionAPI.init();
        api.connect(url);
        Address buff = api.getWallet().getMinerAccount().getObject();
        assertNotNull(buff);
        api.destroyApi();
    }

    @Test
    public void TestBlockNumber() {
        System.out.println("run TestBlockNumber.");
        IAionAPI api = IAionAPI.init();
        api.connect(url);
        Long blockNumber = api.getChain().blockNumber().getObject();
        assertThat(blockNumber, is(greaterThan(0L)));
        api.destroyApi();
    }

    @Test
    public void TestGetBalance() {
        System.out.println("run TestGetBalance.");
        IAionAPI api = IAionAPI.init();
        api.connect(url);

        List accs = api.getWallet().getAccounts().getObject();
        assertTrue(accs.size() > 0);

        BigInteger balance = api.getChain().getBalance((Address) accs.get(0)).getObject();
        // BigInteger maybe overflow with Long format
        assertTrue(balance.compareTo(BigInteger.ZERO) > -1);
        api.destroyApi();
    }

    @Test
    public void TestGetBlockByNumber()  {
        System.out.println("run TestGetBlockByNumber.");
        IAionAPI api = IAionAPI.init();

        api.connect(url);
        Block block = api.getChain().getBlockByNumber(1L).getObject();

        assertTrue(block.getNumber() == 1L);
        assertTrue(block.getBloom().getData().length == 256);
        assertTrue(block.getDifficulty().compareTo(BigInteger.ZERO) > 0);
        assertTrue(block.getExtraData().getData().length == 32);
        assertTrue(block.getNrgConsumed() >= 0L);
        assertTrue(block.getNrgLimit() >= 0L);
        assertTrue(block.getTimestamp() <= Instant.now().toEpochMilli());
        assertTrue(block.getTxHash().size() == 0);
        assertTrue(block.getTotalDifficulty().compareTo(BigInteger.valueOf(100L)) > 0);
        assertTrue(block.getNonce().compareTo(BigInteger.ZERO) != 0);
        api.destroyApi();
    }

    @Test
    public void TestGetTransactionByBlockHashAndIndex()  {
        System.out.println("run TestGetTransactionByBlockHashAndIndex.");
        IAionAPI api = IAionAPI.init();
        api.connect(url);

        List accs = api.getWallet().getAccounts().getObject();
        assertThat(accs.size(), is(greaterThan(0)));

        Address acc = (Address) accs.get(0);
        Address acc2 = (Address) accs.get(1);

        assertTrue(api.getWallet().unlockAccount(acc, pw).getObject());

        String testData = "12345678";
        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(testData.getBytes()))
            .from(acc)
            .to(acc2)
            .nrgLimit(100000)
            .nrgPrice(NRG_PRICE)
            .value(BigInteger.ONE)
            .nonce(BigInteger.ZERO);

        api.getTx().fastTxbuild(builder.createTxArgs());

        long ts = Instant.now().toEpochMilli()*1000;
        MsgRsp msgRsp = api.getTx().sendTransaction(null).getObject();
        assertNotNull(msgRsp.getMsgHash());

        TxReceipt txRecpt = api.getTx().getTxReceipt(msgRsp.getTxHash()).getObject();
        assertNotNull(txRecpt);

        Transaction transaction = api.getChain()
            .getTransactionByBlockHashAndIndex(txRecpt.getBlockHash(), 0)
            .getObject();

        assertNotNull(transaction);
        assertNotNull(transaction.getBlockHash());
        assertNotNull(transaction.getTxHash());

        assertThat(transaction.getBlockNumber(), is(greaterThan(0L)));
        assertTrue(transaction.getFrom().equals(acc));
        assertTrue(transaction.getNrgConsumed() > 21000L);
        assertThat(transaction.getNrgPrice(), is(equalTo(NRG_PRICE)));
        assertTrue(transaction.getTo().equals(acc2));

        assertThat(transaction.getNonce(), is(greaterThanOrEqualTo(BigInteger.ZERO)));
        assertThat(transaction.getTransactionIndex(), is(equalTo(0)));
        assertThat(transaction.getValue(), is(equalTo(BigInteger.ONE)));
        assertThat(transaction.getTimeStamp(), is(greaterThan(ts)));
        assertTrue(builder.createTxArgs().getData().equals(transaction.getData()));

        api.destroyApi();
    }

    @Test
    public void TestGetTransactionByBlockNumberAndIndex() {
        System.out.println("run TestGetTransactionByBlockNumberAndIndex.");
        IAionAPI api = IAionAPI.init();
        api.connect(url);

        List accs = api.getWallet().getAccounts().getObject();
        assertThat(accs.size(), is(greaterThan(0)));

        Address acc = (Address) accs.get(0);

        assertThat(api.getWallet().unlockAccount(acc, pw, 300).getObject(), is(equalTo(true)));

        String testData = "12345678";
        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(testData.getBytes()))
            .from(acc)
            .to(acc)
            .nrgLimit(100000)
            .nrgPrice(NRG_PRICE)
            .value(BigInteger.ONE)
            .nonce(BigInteger.ZERO);

        api.getTx().fastTxbuild(builder.createTxArgs());

        long ts = Instant.now().toEpochMilli()*1000;

        Hash256 txHash = ((MsgRsp)api.getTx().sendTransaction(null).getObject()).getTxHash();
        assertNotNull(txHash);

        TxReceipt txRecpt = api.getTx().getTxReceipt(txHash).getObject();
        assertNotNull(txRecpt);

        Transaction transaction = api.getChain().getTransactionByBlockNumberAndIndex(txRecpt.getBlockNumber(), 0).getObject();
        assertNotNull(transaction);


        assertNotNull(transaction.getBlockHash());
        assertNotNull(transaction.getTxHash());

        assertThat(transaction.getBlockNumber(), is(greaterThan(0L)));
        assertTrue(transaction.getFrom().equals(acc));
        assertThat(transaction.getNrgConsumed(), is(greaterThan(21000L)));
        assertThat(transaction.getNrgPrice(), is(equalTo(NRG_PRICE)));
        assertTrue(transaction.getTo().equals(acc));
        assertThat(transaction.getNonce(), is(greaterThanOrEqualTo(BigInteger.ZERO)));
        assertThat(transaction.getTransactionIndex(), is(equalTo(0)));
        assertThat(transaction.getValue(), is(equalTo(BigInteger.ONE)));
        assertThat(transaction.getTimeStamp(), is(greaterThan(ts)));
        assertTrue(builder.createTxArgs().getData().equals(transaction.getData()));


        api.destroyApi();
    }

    @Test
    public void TestGetTransactionByBlockNumberAndIndex2() {
        System.out.println("run TestGetTransactionByBlockNumberAndIndex2.");

        IAionAPI api =  IAionAPI.init();
        api.connect(url);

        List<Address> accs = api.getWallet().getAccounts().getObject();
        assertThat(accs.size(), is(greaterThan(0)));

        assertTrue(api.getWallet().unlockAccount(accs.get(0), pw, 300).getObject());

        Map<String, CompileResponse> contracts = api.getTx().compile(TICKER).getObject();
        String key = "ticker";
        assertTrue(contracts.containsKey(key));

        List<ContractAbiEntry> abiDef = contracts.get(key).getAbiDefinition();
        assertNotNull(abiDef);

        assertTrue(abiDef.size() == 2);
        assertFalse(abiDef.get(0).anonymous);
        assertFalse(abiDef.get(0).payable);
        assertTrue(abiDef.get(0).constant);

        assertTrue(abiDef.get(0).name.contentEquals(VAL));
        assertTrue(abiDef.get(0).type.contentEquals(FUNCTION));

        assertTrue(contracts.get(key).getSource().contentEquals(TICKER));

        long ts = Instant.now().toEpochMilli()*1000;
        CompileResponse contract = contracts.get(key);

        ContractDeploy.ContractDeployBuilder builder = new ContractDeploy.ContractDeployBuilder()
            .compileResponse(contract)
            .from(accs.get(0))
            .data(ByteArrayWrapper.wrap(Bytesable.NULL_BYTE))
            .nrgLimit(500_000)
            .nrgPrice(NRG_PRICE)
            .value(BigInteger.ZERO);

        DeployResponse contractResponse = api.getTx().contractDeploy(builder.createContractDeploy()).getObject();

        assertNotNull(contractResponse);
        assertNotNull(contractResponse.getAddress());
        assertNotNull(contractResponse.getTxid());

        ApiMsg apimsg = api.getChain().getTransactionByHash(contractResponse.getTxid());
        assertFalse(apimsg.isError());

        Transaction tx = apimsg.getObject();
        long blkNr = tx.getBlockNumber();

        Transaction transaction = api.getChain().getTransactionByBlockNumberAndIndex(blkNr, 0).getObject();
        assertNotNull(transaction);

        assertNotNull(transaction.getTxHash());
        assertTrue(transaction.getBlockNumber() == blkNr);
        assertTrue(transaction.getFrom().equals(accs.get(0)));
        assertTrue(transaction.getNrgConsumed() > 21000);
        assertTrue(transaction.getNrgPrice() == NRG_PRICE);
        assertTrue(transaction.getTo().equals(Address.EMPTY_ADDRESS()));
        assertTrue(transaction.getNonce().compareTo(BigInteger.ZERO) > -1);
        assertTrue(transaction.getTransactionIndex() == 0);
        assertTrue(transaction.getValue().equals(BigInteger.ZERO));
        assertTrue(transaction.getTimeStamp() > ts);

        api.destroyApi();
    }

    @Test
    public void TestGetTransactionReceipt() {
        System.out.println("run TestGetTransactionReceipt.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        List accs = api.getWallet().getAccounts().getObject();
        assertNotNull(accs);
        assertTrue(accs.size() > 0);

        Address acc = (Address) accs.get(0);
        assertTrue(api.getWallet().unlockAccount(acc, pw, 300).getObject());

        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(IUtils.hex2Bytes("00000000")))
            .from(acc)
            .to(acc)
            .nrgLimit(100000)
            .nrgPrice(NRG_PRICE)
            .value(BigInteger.ZERO)
            .nonce(BigInteger.ZERO);

        api.getTx().fastTxbuild(builder.createTxArgs());

        Hash256 txHash = ((MsgRsp)api.getTx().sendTransaction(null).getObject()).getTxHash();
        assertNotNull(txHash);

        TxReceipt txRecpt = api.getTx().getTxReceipt(txHash).getObject();
        assertNotNull(txRecpt);
        assertNotNull(txRecpt.getTxLogs());
        assertNotNull(txRecpt.getBlockHash());
        assertTrue(txRecpt.getBlockNumber() > 0);
        assertNotNull(txRecpt.getContractAddress());
        assertTrue(txRecpt.getCumulativeNrgUsed() > 21000);
        assertTrue(txRecpt.getNrgConsumed() > 21000);
        assertTrue(txRecpt.getFrom().equals(acc));
        assertTrue(txRecpt.getTo().equals(acc));
        assertTrue(txRecpt.getTxIndex() > -1);

        api.destroyApi();
    }

    @Test
    public void TestGetAccounts() throws Throwable {
        System.out.println("run TestGetAccounts.");

        IAionAPI api = IAionAPI.init();
        ApiMsg msg = api.connect(url);

        assertFalse(msg.isError());
        Thread.sleep(10000);


        List<Address> accounts = api.getWallet().getAccounts().getObject();
        assertTrue(accounts.size() > 0);
        api.destroyApi();
    }

    @Test
    public void TestUnlockAccount() throws Throwable {
        System.out.println("run TestUnlockAccount.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        List<Address> accs = api.getWallet().getAccounts().getObject();
        Address acc = accs.get(0);

        assertTrue(api.getWallet().unlockAccount(acc, pw, 99999).getObject());
        assertFalse(api.getWallet().unlockAccount(acc, "fake", 99999).getObject());

        if (accs.size() > 1) {
            Address acc2 = accs.get(1);
            assertTrue(api.getWallet().unlockAccount(acc2, pw, 99999).getObject());
            assertFalse(api.getWallet().unlockAccount(acc2, "fake", 99999).getObject());
        }

        Address fakeAcc = Address.ZERO_ADDRESS();
        assertTrue(api.getWallet().unlockAccount(acc, pw, 0).getObject());
        assertTrue(api.getWallet().unlockAccount(acc, "", -1).isError());
        assertFalse(api.getWallet().unlockAccount(fakeAcc, "", 99999).getObject());

        boolean expectGoCatch = false;
        try {
            Address fakeAcc2 = Address.wrap(new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1});  // 33bytes
            assertTrue(api.getWallet().unlockAccount(fakeAcc2, "", 99999).isError());
        } catch (Exception e) {
            System.out.println("fakeAcc2 exeception#" + e.getMessage());
            expectGoCatch = true;
        }

        assertTrue(expectGoCatch);

        api.destroyApi();
    }

    @Test
    public void TestSendTransaction() throws Throwable {
        System.out.println("run TestSendTransaction.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        List accs = api.getWallet().getAccounts().getObject();
        assertTrue(accs.size() > 0);

        Address acc = (Address) accs.get(0);
        assertTrue(api.getWallet().unlockAccount(acc, pw).getObject());


        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap("TestSendTransaction!".getBytes()))
            .from(acc)
            .to(acc)
            .nrgLimit(100000)
            .nrgPrice(NRG_PRICE)
            .value(BigInteger.ZERO)
            .nonce(BigInteger.ZERO);

        api.getTx().fastTxbuild(builder.createTxArgs());

        Hash256 hash = ((MsgRsp)api.getTx().sendTransaction(null).getObject()).getTxHash();
        assertNotNull(hash);
        api.destroyApi();
    }

    @Test
    public void TestCompile() throws Throwable {
        System.out.println("run TestCompile.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        Map<String, CompileResponse> contracts = api.getTx().compile(TICKER).getObject();
        String key = "ticker";
        assertTrue(contracts.containsKey(key));

        List<ContractAbiEntry> abiDef = contracts.get(key).getAbiDefinition();
        assertNotNull(abiDef);
        assertTrue(abiDef.size() == 2);
        assertFalse(abiDef.get(0).anonymous);
        assertFalse(abiDef.get(0).payable);
        assertTrue(abiDef.get(0).constant);
        assertTrue(abiDef.get(0).name.contentEquals(VAL));
        assertTrue(abiDef.get(0).type.contentEquals(FUNCTION));
        assertTrue(contracts.get(key).getSource().contentEquals(TICKER));

        api.destroyApi();
    }

    @Test
    @Ignore
    public void TestCompileError() throws Throwable {
        System.out.println("run TestCompileError.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        ApiMsg apiMsg = api.getTx().compile(ERROR_TICKER);

        assertTrue(apiMsg.getErrorCode() == -10);

        System.out.println("Compile error: " + apiMsg.getObject());

        api.destroyApi();
    }

    @Test
    public void TestGetTransactionByHash() throws Throwable {
        System.out.println("run TestGetTransactionByHash.");

        IAionAPI api =  IAionAPI.init();
        api.connect(url);

        List accs = api.getWallet().getAccounts().getObject();
        assertThat(accs.size(), is(greaterThan(0)));

        Address acc = (Address) accs.get(0);

        assertThat(api.getWallet().unlockAccount(acc, pw, 300).getObject(), is(equalTo(true)));

        String testData = "12345678";

        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(testData.getBytes()))
            .from(acc)
            .to(acc)
            .nrgLimit(100000)
            .nrgPrice(NRG_PRICE)
            .value(BigInteger.ONE)
            .nonce(BigInteger.ZERO);

        api.getTx().fastTxbuild(builder.createTxArgs());

        long ts = Instant.now().toEpochMilli()*1000;

        Hash256 txHash = ((MsgRsp)api.getTx().sendTransaction(null).getObject()).getTxHash();
        assertNotNull(txHash);

        TxReceipt txRecpt = api.getTx().getTxReceipt(txHash).getObject();
        assertNotNull(txRecpt);
        assertNotNull(txRecpt.getTxLogs());

        Transaction transaction = api.getChain().getTransactionByHash(txRecpt.getTxHash()).getObject();
        assertNotNull(transaction);

        assertNotNull(transaction.getTxHash());
        assertTrue(transaction.getBlockNumber() == txRecpt.getBlockNumber());
        assertTrue(transaction.getFrom().equals(acc));
        assertTrue(transaction.getNrgConsumed() > 21000);
        assertTrue(transaction.getNrgPrice() == NRG_PRICE);
        assertTrue(transaction.getTo().equals(acc));
        assertTrue(transaction.getNonce().compareTo(BigInteger.ZERO) > -1);
        assertTrue(transaction.getTransactionIndex() == 0);
        assertTrue(transaction.getValue().equals(BigInteger.ONE));
        assertTrue(transaction.getTimeStamp() > ts);
        assertEquals(builder.createTxArgs().getData(), transaction.getData());

        api.destroyApi();
    }

    @Test
    @Ignore
    public void TestGetCode() throws Throwable {
        System.out.println("run TestGetCode.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        List<Address> accs = api.getWallet().getAccounts().getObject();
        assertTrue(accs.size() > 0);

        boolean ret = api.getWallet().unlockAccount(accs.get(0), pw).getObject();
        assertTrue(ret);

        Map<String, CompileResponse> contracts = api.getTx().compile(TICKER).getObject();
        String key = "ticker";
        assertTrue(contracts.containsKey(key));

        List<ContractAbiEntry> abiDef = contracts.get(key).getAbiDefinition();
        assertNotNull(abiDef);
        assertTrue(abiDef.size() == 2);
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
            .nrgPrice(NRG_PRICE)
            .nrgLimit(500_000)
            .data(ByteArrayWrapper.wrap(Bytesable.NULL_BYTE))
            .from(accs.get(0));

        DeployResponse contractResponse = api.getTx().contractDeploy(builder.createContractDeploy()).getObject();
        assertNotNull(contractResponse);
        assertNotNull(contractResponse.getAddress());
        assertNotNull(contractResponse.getTxid());

        TxReceipt txRecpt = api.getTx().getTxReceipt(contractResponse.getTxid()).getObject();
        assertNotNull(txRecpt);
        assertNotNull(txRecpt.getTxLogs());

        Long blockNumber = -1L;
        byte[] code = api.getTx().getCode(contractResponse.getAddress(), blockNumber).getObject();
        assertNotNull(code);

        String cc = "0x" + IUtils.bytes2Hex(code);
        assertTrue(cc.contentEquals(contract.getCode()));

        api.destroyApi();
    }

    @Test
    public void TestContractDeploy() throws Throwable {
        System.out.println("run TestContractDeploy.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        List<Address> accs = api.getWallet().getAccounts().getObject();
        assertThat(accs.size(), is(greaterThan(0)));

        boolean ret = api.getWallet().unlockAccount(accs.get(0), pw).getObject();
        assertTrue(ret);

        Map<String, CompileResponse> contracts = api.getTx().compile(TICKER).getObject();
        String key = "ticker";
        assertTrue(contracts.containsKey(key));

        List<ContractAbiEntry> abiDef = contracts.get(key).getAbiDefinition();
        assertNotNull(abiDef);
        assertTrue(abiDef.size() == 2);
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
            .nrgPrice(NRG_PRICE)
            .nrgLimit(500_000)
            .data(ByteArrayWrapper.wrap(Bytesable.NULL_BYTE))
            .from(accs.get(0));

        DeployResponse contractResponse = api.getTx().contractDeploy(builder.createContractDeploy()).getObject();
        assertNotNull(contractResponse);
        assertNotNull(contractResponse.getAddress());
        assertNotNull(contractResponse.getTxid());

        api.destroyApi();
    }

    @Test
    public void GetBlockByHash() throws Throwable {
        System.out.println("run GetBlockByHash.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        List accs = api.getWallet().getAccounts().getObject();
        assertThat(accs.size(), is(greaterThan(0)));

        Address acc = (Address) accs.get(0);
        assertTrue(api.getWallet().unlockAccount(acc, pw, 300).getObject());

        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(IUtils.hex2Bytes("00000000")))
            .from(acc)
            .to(acc)
            .nrgLimit(100000)
            .nrgPrice(NRG_PRICE)
            .value(BigInteger.ZERO)
            .nonce(BigInteger.ZERO);

        api.getTx().fastTxbuild(builder.createTxArgs());

        Hash256 txHash = ((MsgRsp)api.getTx().sendTransaction(null).getObject()).getTxHash();
        assertNotNull(txHash);

        TxReceipt txRecpt = api.getTx().getTxReceipt(txHash).getObject();
        assertNotNull(txRecpt);
        assertNotNull(txRecpt.getTxLogs());

        Hash256 blockHash = txRecpt.getBlockHash();
        Block block = api.getChain().getBlockByHash(blockHash).getObject();
        assertTrue(block.getDifficulty().compareTo(BigInteger.ZERO) > 0);
        assertTrue(block.getExtraData().getData().length == 32);
        assertTrue(block.getNrgLimit() > 21000);
        assertTrue(block.getNrgConsumed() > 21000);
        assertNotNull(block.getParentHash());
        assertTrue(block.getBloom().getData().length == 256);
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

    @Test
    public void GetBlockByHash2() throws Throwable {
        System.out.println("run GetBlockByHash2.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        List accs = api.getWallet().getAccounts().getObject();
        assertThat(accs.size(), is(greaterThan(0)));

        Address acc = (Address) accs.get(0);

        assertTrue(api.getWallet().unlockAccount(acc, pw, 300).getObject());

        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(IUtils.hex2Bytes("00000000")))
            .from(acc)
            .to(acc)
            .nrgLimit(100000)
            .nrgPrice(NRG_PRICE)
            .value(BigInteger.ZERO)
            .nonce(BigInteger.ZERO);


        api.getTx().fastTxbuild(builder.createTxArgs());

        ApiMsg msg = api.getTx().nonBlock().sendTransaction(null);

        assertFalse(msg.isError());

        ByteArrayWrapper msgHash = ((MsgRsp)msg.getObject()).getMsgHash();

        Thread.sleep(10000);

        msg.set(api.getTx().getMsgStatus(msgHash));

        assertFalse(msg.isError());

        Hash256 txHash = ((MsgRsp)msg.getObject()).getTxHash();
        assertNotNull(txHash);

        TxReceipt txRecpt = api.getTx().getTxReceipt(txHash).getObject();
        assertNotNull(txRecpt);
        assertNotNull(txRecpt.getTxLogs());

        Hash256 blockHash = txRecpt.getBlockHash();
        Block block = api.getChain().getBlockByHash(blockHash).getObject();

        assertTrue(block.getDifficulty().compareTo(BigInteger.ZERO) > 0);
        assertTrue(block.getExtraData().getData().length == 32);
        assertTrue(block.getNrgLimit() > 21000);
        assertTrue(block.getNrgConsumed() > 21000);
        assertNotNull(block.getParentHash());
        assertTrue(block.getBloom().getData().length == 256);
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
        assertTrue(block.getTxHash().size() > 0);
        assertNotNull(block.getMinerAddress());

        api.destroyApi();
    }

    @Test
    public void TestGetTransactionCount() throws Throwable {
        System.out.println("run TestGetTransactionCount.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        List accs = api.getWallet().getAccounts().getObject();
        assertThat(accs.size(), is(greaterThan(0)));

        Address acc = (Address) accs.get(0);

        assertTrue(api.getWallet().unlockAccount(acc, pw).getObject());

        long count1 = 0;

        String testData = "12345678";
        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(testData.getBytes()))
            .from(acc)
            .to(acc)
            .nrgLimit(100000)
            .nrgPrice(NRG_PRICE)
            .value(BigInteger.ZERO)
            .nonce(BigInteger.ZERO);

        api.getTx().fastTxbuild(builder.createTxArgs());

        final Hash256[] hash = {null};
        IntStream.range(0, 3).forEach(i -> hash[0] = ((MsgRsp)api.getTx().sendTransaction(null).getObject()).getTxHash());

        assertNotNull(hash[0]);

        TxReceipt txRecpt = api.getTx().getTxReceipt(hash[0]).getObject();
        assertNotNull(txRecpt);
        assertNotNull(txRecpt.getTxLogs());

        long count2 = api.getChain().getTransactionCount(acc, txRecpt.getBlockNumber()).getObject();
        assertTrue((count2-count1) > 0);
        api.destroyApi();
    }

    @Test
    public void TestGetBlockTransactionCountByHash() throws Throwable {
        System.out.println("run TestGetBlockTransactionCountByHash.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        List accs = api.getWallet().getAccounts().getObject();
        assertThat(accs.size(), is(greaterThan(0)));

        Address acc = (Address) accs.get(0);
        assertTrue(api.getWallet().unlockAccount(acc, pw).getObject());

        String testData = "12345678";
        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(testData.getBytes()))
            .from(acc)
            .to(acc)
            .nrgLimit(100000)
            .nrgPrice(NRG_PRICE)
            .value(BigInteger.ZERO)
            .nonce(BigInteger.ZERO);


        api.getTx().fastTxbuild(builder.createTxArgs());

        final Hash256[] hash = {null};
        IntStream.range(0, 3).forEach(i -> hash[0] = ((MsgRsp)api.getTx().sendTransaction(null).getObject()).getTxHash());

        assertNotNull(hash[0]);

        TxReceipt txRecpt = api.getTx().getTxReceipt(hash[0]).getObject();
        assertNotNull(txRecpt);
        assertNotNull(txRecpt.getTxLogs());

        int count = api.getChain().getBlockTransactionCountByHash(txRecpt.getBlockHash()).getObject();
        assertTrue(count == 1);

        api.destroyApi();
    }

    @Test
    public void TestGetBlockTransactionCountByNumber() throws Throwable {
        System.out.println("run TestGetBlockTransactionCountByNumber.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        List accs = api.getWallet().getAccounts().getObject();
        assertThat(accs.size(), is(greaterThan(0)));

        Address acc = (Address) accs.get(0);

        assertTrue(api.getWallet().unlockAccount(acc, pw,86400).getObject());

        String testData = "12345678";
        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(testData.getBytes()))
            .from(acc)
            .to(acc)
            .nrgLimit(40_000L)
            .nrgPrice(NRG_PRICE)
            .value(BigInteger.ZERO)
            .nonce(BigInteger.ZERO);

        api.getTx().fastTxbuild(builder.createTxArgs());

        final Hash256[] hash = {null};
        for (int i=0 ; i<1000 ; i++) {
            hash[0] = ((MsgRsp)api.getTx().nonBlock().sendTransaction(null).getObject()).getTxHash();
        }

        Thread.sleep(60000);
        assertNotNull(hash[0]);

        TxReceipt txRecpt = api.getTx().getTxReceipt(hash[0]).getObject();
        assertNotNull(txRecpt);
        assertNotNull(txRecpt.getTxLogs());

        int count = api.getChain().getBlockTransactionCountByNumber(txRecpt.getBlockNumber()).getObject();
        assertTrue(count > 1);
        api.destroyApi();
    }

    @Test
    public void TestCall() throws Throwable {
        System.out.println("run TestCall.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        List<Address> accs = api.getWallet().getAccounts().getObject();
        assertTrue(accs.size() > 0);
        assertTrue(api.getWallet().unlockAccount(accs.get(0), pw).getObject());

        Map<String, CompileResponse> contracts = api.getTx().compile(TICKER).getObject();
        String key = "ticker";
        assertTrue(contracts.containsKey(key));

        List<ContractAbiEntry> abiDef = contracts.get(key).getAbiDefinition();
        assertNotNull(abiDef);
        assertTrue(abiDef.size() == 2);
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
            .nrgPrice(NRG_PRICE)
            .nrgLimit(500_000)
            .data(ByteArrayWrapper.wrap(Bytesable.NULL_BYTE))
            .from(accs.get(0));


        DeployResponse contractResponse = api.getTx().contractDeploy(builder.createContractDeploy()).getObject();
        assertNotNull(contractResponse);
        assertNotNull(contractResponse.getAddress());
        assertNotNull(contractResponse.getTxid());

        Long blockNumber = -1L;
        byte[] code = api.getTx().getCode(contractResponse.getAddress(), blockNumber).getObject();

        //byte[] codeCheck = new byte[] {96,96,96,64,82,96,-32,96,2,10,96,0,53,4,99,60,107,-76,54,-127,20,96,38,87,-128,99,62
        //        ,-81,93,-97,20,96,50,87,91,96,2,86,91,52,96,2,87,96,67,96,0,84,-127,86,91,52,96,2,87,96,85,96,0,-128,84
        //        ,96,1,1,-112,85,86,91,96,64,-128,81,-111,-126,82,81,-112,-127,-112,3,96,32,1,-112,-13,91,0};
        //
        //assertThat(code, is(equalTo(codeCheck)));

        // call function from deployed contract
        String functionCall = VAL + "()";

        // retrieve first 8 bytes of keccak-256 hashed value
        String hashFunctionCall = IUtils.bytes2Hex(IUtils.sha3(Arrays.copyOfRange(functionCall.getBytes(), 0, 5))).substring(0, 8);
        byte[] hashFunctionCallBytes = IUtils.hex2Bytes(hashFunctionCall);
        assertThat(hashFunctionCall, is(equalTo("3c6bb436")));

        Address acc = accs.get(0);

        assert hashFunctionCallBytes != null;
        TxArgs txArgs = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(hashFunctionCallBytes))
            .from(acc)
            .to(contractResponse.getAddress())
            .nrgLimit(40_000L)
            .nrgPrice(NRG_PRICE)
            .value(BigInteger.ZERO)
            .nonce(BigInteger.ZERO)
            .createTxArgs();

        ByteArrayWrapper returnHash = ByteArrayWrapper.wrap(api.getTx().call(txArgs).getObject());
        // 16-bytes
        assertTrue(returnHash.equals(ByteArrayWrapper.wrap(IUtils.hex2Bytes("00000000000000000000000000000000"))));
        api.destroyApi();
    }

    @Test
    public void TestEstimateNrg() throws Throwable {
        System.out.println("run TestEstimateNrg.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        List<Address> accs = api.getWallet().getAccounts().getObject();
        assertTrue(accs.size() > 0);
        assertTrue(api.getWallet().unlockAccount(accs.get(0), pw).getObject());

        Map<String, CompileResponse> contracts = api.getTx().compile(TICKER).getObject();
        String key = "ticker";
        assertTrue(contracts.containsKey(key));

        List<ContractAbiEntry> abiDef = contracts.get(key).getAbiDefinition();
        assertNotNull(abiDef);
        assertTrue(abiDef.size() == 2);
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
            .nrgPrice(NRG_PRICE)
            .nrgLimit(500_000)
            .data(ByteArrayWrapper.wrap(Bytesable.NULL_BYTE))
            .from(accs.get(0));


        DeployResponse contractResponse = api.getTx().contractDeploy(builder.createContractDeploy()).getObject();
        assertNotNull(contractResponse);
        assertNotNull(contractResponse.getAddress());
        assertNotNull(contractResponse.getTxid());

        Long blockNumber = -1L; // does this actually matter?
        byte[] code = api.getTx().getCode(contractResponse.getAddress(), blockNumber).getObject();

        //byte[] codeCheck = new byte[] {96,96,96,64,82,96,-32,96,2,10,96,0,53,4,99,60,107,-76,54,-127,20,96,38,87,-128,99,62
        //        ,-81,93,-97,20,96,50,87,91,96,2,86,91,52,96,2,87,96,67,96,0,84,-127,86,91,52,96,2,87,96,85,96,0,-128,84
        //        ,96,1,1,-112,85,86,91,96,64,-128,81,-111,-126,82,81,-112,-127,-112,3,96,32,1,-112,-13,91,0};
        //
        //assertThat(code, is(equalTo(codeCheck)));

        // call function from deployed contract
        String functionCall = VAL + "()";

        // retrieve first 8 bytes of keccak-256 hashed value
        String hashFunctionCall = IUtils.bytes2Hex(IUtils.sha3(Arrays.copyOfRange(functionCall.getBytes(), 0, 5))).substring(0, 8);
        byte[] hashFunctionCallBytes = IUtils.hex2Bytes(hashFunctionCall);
        assertThat(hashFunctionCall, is(equalTo("3c6bb436")));

        Address acc = accs.get(0);

        assert hashFunctionCallBytes != null;
        TxArgs txArgs = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(hashFunctionCallBytes))
            .from(acc)
            .to(contractResponse.getAddress())
            .nrgLimit(40_000L)
            .nrgPrice(NRG_PRICE)
            .value(BigInteger.ZERO)
            .nonce(BigInteger.ZERO)
            .createTxArgs();

        long nrg = api.getTx().estimateNrg(txArgs).getObject();
        assertTrue(nrg > 21000L);
        api.destroyApi();
    }


    @Test
    public void TestIsSyncing() throws Throwable {
        System.out.println("run TestIsSyncing.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        ApiMsg apiMsg = api.getNet().isSyncing();
        assertFalse(apiMsg.isError());
        assertFalse(apiMsg.getObject());
        api.destroyApi();
    }

    @Test
    public void TestGetActiveNodes() throws Throwable {
        System.out.println("run TestGetActiveNodes.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        ApiMsg apiMsg = api.getNet().getActiveNodes();
        assertFalse(apiMsg.isError());

        List<Node> nodeList = apiMsg.getObject();

        // TODO: check return data


        api.destroyApi();
    }

    @Test
    public void TestGetStaticNodes() throws Throwable {
        System.out.println("run TestGetStaticNodes.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        ApiMsg apiMsg = api.getNet().getStaticNodes();
        assertFalse(apiMsg.isError());
        List<String> nodeList = apiMsg.getObject();

        // TODO: check return data

        api.destroyApi();
    }

    @Test
    public void TestGetSolcVersion() throws Throwable {
        System.out.println("run TestGetSolcVersion.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        ApiMsg apiMsg = api.getTx().getSolcVersion();
        assertFalse(apiMsg.isError());

        String version = apiMsg.getObject();
        assertTrue(version.contains("0.4"));

        api.destroyApi();
    }

    @Test
    public void GetSyncInfo() throws Throwable {
        System.out.println("run GetSyncInfo.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        ApiMsg apiMsg = api.getNet().syncInfo();

        assertFalse(apiMsg.isError());

        SyncInfo info = apiMsg.getObject();

        assertTrue(info.getChainBestBlock() > -1L);
        assertTrue(info.getNetworkBestBlock() > -1L);
        assertTrue(info.getMaxImportBlocks() > 0L);
        System.out.println("Is Syncing:" + (info.isSyncing() ? "true" : "false"));

        api.destroyApi();
    }

    @Test
    public void GetIsMining() throws Throwable {
        System.out.println("run GetIsMining.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        ApiMsg apiMsg = api.getMine().isMining();

        assertFalse(apiMsg.isError());

        boolean isMining = apiMsg.getObject();
        System.out.println("Is mining: " + (isMining ? "true" : "false"));

        api.destroyApi();
    }

    @Test
    public void TestApiConnectTwice() throws Throwable {
        System.out.println("run TestApiConnectTwice.");

        IAionAPI api = IAionAPI.init();
        ApiMsg apiMsg = api.connect(url);
        assertFalse(apiMsg.isError());

        apiMsg.set(api.getNet().getProtocolVersion());
        assertFalse(apiMsg.isError());

        Protocol pro = apiMsg.getObject();
        assertNotNull(pro);

        api.destroyApi();

        api.connect(url);


        apiMsg.set(api.getNet().getProtocolVersion());
        assertFalse(apiMsg.isError());

        pro = apiMsg.getObject();
        assertNotNull(pro);
        api.destroyApi();
    }

    @Test
    public void TestApiReConnect() throws Throwable {
        System.out.println("run TestApiReConnect.");

        IAionAPI api = IAionAPI.init();
        ApiMsg apiMsg = api.connect(url, true);
        assertFalse(apiMsg.isError());
        api.destroyApi();
    }

    @Test
    public void TestAccountCreate() throws Throwable {
        System.out.println("run TestAccountCreate.");

        IAionAPI api = IAionAPI.init();
        ApiMsg apiMsg = api.connect(url, true);
        assertFalse(apiMsg.isError());

        apiMsg.set(api.getAccount().accountCreate(Collections.singletonList(pw), false));

        assertFalse(apiMsg.isError());

        List<Key> k = apiMsg.getObject();
        assertNotNull(k);
        assertTrue(k.size() == 1);

        assertNotNull(k.get(0).getPubKey());
        assertTrue(k.get(0).getPriKey().getData() == ByteArrayWrapper.NULL_BYTE);

        apiMsg.set(api.getAccount().accountCreate(Collections.singletonList(pw), true));
        assertFalse(apiMsg.isError());

        k = apiMsg.getObject();
        assertNotNull(k);
        assertNotNull(k.get(0).getPubKey());
        assertNotNull(k.get(0).getPriKey().getData());

        List<String> sList = new ArrayList<>();
        for (int i=0 ; i<10 ; i++) {
            sList.add("");
        }

        apiMsg.set(api.getAccount().accountCreate(sList, false));
        assertFalse(apiMsg.isError());

        k = apiMsg.getObject();
        assertNotNull(k);
        assertTrue(k.size() == 10);

        for (int i=0; i< 10 ; i++) {
            assertNotNull(k.get(i).getPubKey());
            assertTrue(k.get(i).getPriKey().getData() == ByteArrayWrapper.NULL_BYTE);
        }

        apiMsg.set(api.getAccount().accountCreate(sList, true));
        assertFalse(apiMsg.isError());

        k = apiMsg.getObject();
        assertNotNull(k);
        assertTrue(k.size() == 10);
        for (int i=0; i< 10 ; i++) {
            assertNotNull(k.get(0).getPubKey());
            assertNotNull(k.get(0).getPriKey().getData());
        }

        api.destroyApi();
    }

    @Test
    public void TestAccountExport() throws Throwable {
        System.out.println("run TestAccountExport.");

        IAionAPI api = IAionAPI.init();
        ApiMsg apiMsg = api.connect(url, true);
        assertFalse(apiMsg.isError());

        List<Address> accs = api.getWallet().getAccounts().getObject();
        assertTrue(accs.size() > 0);

        List<Key> keys = new ArrayList<>();
        for (int i=0 ; i<accs.size() && i<2 ; i++) {
            keys.add(new Key(accs.get(i), pw));
        }

        apiMsg.set(api.getAccount().accountExport(keys));
        assertFalse(apiMsg.isError());

        KeyExport ke = apiMsg.getObject();

        assertNotNull(ke);
        assertNotNull(ke.getInvalidAddress());
        assertNotNull(ke.getKeyFiles());
        assertTrue( keys.size() == ke.getKeyFiles().size() + ke.getInvalidAddress().size());

        api.destroyApi();
    }

    @Test
    public void TestAccountBackup() throws Throwable {
        System.out.println("run TestAccountBackup.");

        IAionAPI api = IAionAPI.init();
        ApiMsg apiMsg = api.connect(url, true);
        assertFalse(apiMsg.isError());

        List<Address> accs = api.getWallet().getAccounts().getObject();
        assertTrue(accs.size() > 0);

        List<Key> keys = new ArrayList<>();
        for (int i=0 ; i<accs.size() && i<2 ; i++) {
            keys.add(new Key(accs.get(i), pw));
        }

        apiMsg.set(api.getAccount().accountBackup(keys));
        assertFalse(apiMsg.isError());

        KeyExport ke = apiMsg.getObject();

        assertNotNull(ke);
        assertNotNull(ke.getInvalidAddress());
        assertNotNull(ke.getKeyFiles());
        assertTrue( keys.size() == ke.getKeyFiles().size() + ke.getInvalidAddress().size());

        api.destroyApi();
    }

    @Test
    @Ignore
    public void TestAccountImport() throws Throwable {
        System.out.println("run TestAccountImport.");

        IAionAPI api = IAionAPI.init();
        ApiMsg apiMsg = api.connect(url, true);
        assertFalse(apiMsg.isError());

        List<Address> accs = api.getWallet().getAccounts().getObject();
        assertTrue(accs.size() > 0);

        List<Key> keys = new ArrayList<>();
        for (int i=0 ; i<accs.size() && i<10 ; i++) {
            keys.add(new Key(accs.get(i), pw));
        }

        apiMsg.set(api.getAccount().accountBackup(keys));
        assertFalse(apiMsg.isError());
        KeyExport ke = apiMsg.getObject();
        assertNotNull(ke);
        assertNotNull(ke.getInvalidAddress());
        assertNotNull(ke.getKeyFiles());
        assertTrue( keys.size() == ke.getKeyFiles().size() + ke.getInvalidAddress().size());

        Map<String, String> keyMap = new HashMap<>();
        for(ByteArrayWrapper s : ke.getKeyFiles()) {
            keyMap.put(s.toString(), pw);
        }

        apiMsg.set(api.getAccount().accountImport(keyMap));
        assertFalse(apiMsg.isError());

        List<String> invalidKey = apiMsg.getObject();
        assertNotNull(invalidKey);
        assertTrue(invalidKey.size() == keyMap.size());

        api.destroyApi();
    }

    @Test
    public void TestlockAccount() throws Throwable {
        System.out.println("run TestlockAccount.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);
        List<Address> accs = api.getWallet().getAccounts().getObject();
        Address acc = accs.get(0);
        assertTrue(api.getWallet().lockAccount(acc, pw).getObject());
        assertFalse(api.getWallet().lockAccount(acc, "fake").getObject());

        api.destroyApi();
    }

    @Test
    public void TestSendSignedTransaction() throws Throwable {
        System.out.println("run TestSendSignedTransaction.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        List accs = api.getWallet().getAccounts().getObject();
        assertTrue(accs.size() > 0);

        Address acc = (Address) accs.get(0);
        Key key = new Key(acc, pw);

        ApiMsg msg = api.getAccount().accountExport(Collections.singletonList(key));
        assertFalse(msg.isError());

        KeyExport ke = msg.getObject();
        assertNotNull(ke);
        assertTrue(ke.getKeyFiles().size() == 1);

        ByteArrayWrapper bw = ke.getKeyFiles().get(0);
        assertNotNull(bw);

        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap("TestSendTransaction!".getBytes()))
            .from(acc)
            .to(acc)
            .nrgLimit(100000)
            .nrgPrice(NRG_PRICE)
            .value(BigInteger.ZERO)
            // make sure the nonce setting is correct
            .nonce(BigInteger.ZERO);

        msg.set(api.getTx().sendSignedTransaction(builder.createTxArgs() ,bw, pw));
        assertFalse(msg.isError());

        Hash256 hash = ((MsgRsp)msg.getObject()).getTxHash();
        assertNotNull(hash);
        api.destroyApi();
    }

    @Test
    public void TestSendRawTransaction() throws Throwable {
        System.out.println("run TestSendSignedTransaction.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        // unlock an account
        List accs = api.getWallet().getAccounts().getObject();
        assertTrue(accs.size() > 0);

        Address acc = (Address) accs.get(0);
        Key key = new Key(acc, pw);

        ApiMsg msg = api.getAccount().accountExport(Collections.singletonList(key));
        assertFalse(msg.isError());

        KeyExport ke = msg.getObject();
        assertNotNull(ke);
        assertTrue(ke.getKeyFiles().size() == 1);

        // create an ECKey object
        ByteArrayWrapper bw = ke.getKeyFiles().get(0);
        assertNotNull(bw);

        ECKey ecKey = ECKeyFac.inst().create().fromPrivate(bw.toBytes());
        assertNotNull(ecKey);

        msg.set(api.getChain().getNonce(acc));
        assertFalse(msg.isError());
        BigInteger nonce = msg.getObject();

        BigInteger b0 = api.getChain().getBalance(acc).getObject();
        // BigInteger maybe overflow with Long format
        assertTrue(b0.compareTo(BigInteger.ZERO) > -1);
        long transfer = 1000L;
        AionTransaction tx0 = new AionTransaction(nonce.toByteArray()
            , (Address) accs.get(1)
            , (BigInteger.valueOf(transfer)).toByteArray()
            , "TestSendTransaction!".getBytes()
            , 100000L
            , NRG_PRICE);
        tx0.sign(ecKey);

        msg.set(api.getTx().sendRawTransaction(ByteArrayWrapper.wrap(tx0.getEncoded())));
        assertFalse(msg.isError());

        Hash256 hash = ((MsgRsp)msg.getObject()).getTxHash();
        assertNotNull(hash);

        BigInteger b1 = api.getChain().getBalance(acc).getObject();
        // BigInteger maybe overflow with Long format
        assertTrue(b1.compareTo(BigInteger.ZERO) > -1);

        ApiMsg apimsg = api.getChain().getTransactionByHash(hash);
        assertFalse(apimsg.isError());

        Transaction tx1 = apimsg.getObject();
        BigInteger diff = b0.subtract(b1);

        assertTrue(((tx1.getNrgConsumed() * NRG_PRICE) + transfer) == diff.longValue());

        api.destroyApi();
    }

    @Test
    public void TestGetBlockDetailsByNumber() throws Throwable {
        System.out.println("run TestGetBlockDetailsByNumber.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        String blks = "1665-1680";
        ApiMsg msg = api.getAdmin().getBlockDetailsByNumber(blks);
        assertFalse(msg.isError());

        List<BlockDetails> bds = msg.getObject();
        assertNotNull(bds);
        assertTrue(bds.size() == 16);

        api.destroyApi();
    }

    @Test
    public void TestGetBlockDetailsByLatest() throws Throwable {
        System.out.println("run TestGetBlockDetailsByLatest.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        ApiMsg msg = api.getAdmin().getBlockDetailsByLatest(100L);
        assertFalse(msg.isError());

        List<BlockDetails> bds = msg.getObject();
        assertNotNull(bds);
        assertTrue(bds.size() == 100);

        api.destroyApi();
    }

    @Test
    public void TestGetBlocksByLatest() throws Throwable {
        System.out.println("run TestGetBlocksByLatest.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        ApiMsg msg = api.getAdmin().getBlocksByLatest(100L);
        assertFalse(msg.isError());

        List<Block> blks = msg.getObject();
        assertNotNull(blks);
        assertTrue(blks.size() == 100);

        api.destroyApi();
    }

    @Test
    public void TestGetBlocksSqlByRange() throws Throwable {
        System.out.println("run TestGetBlocksSqlByRange.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        long t0 = System.currentTimeMillis();

        ApiMsg msg = api.getAdmin().getBlockSqlByRange(1570L, 1620L);
        assertFalse(msg.isError());
        List<BlockSql> blks = msg.getObject();

        long t1 = System.currentTimeMillis();

        long totalTime = t1 - t0;
        long totalTxns = 0;
        for (BlockSql b : blks) {
            System.out.println("#: " + b.getNumber() + " [" + b.getTransactions().size() + "]");
            totalTxns += b.getTransactions().size();
        }

        System.out.println("bench: " + (t1 - t0) + " ms");
        System.out.println("time/txn: " + totalTime / (double)totalTxns);

        assertNotNull(blks);

        api.destroyApi();
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void TestGetBlocksDetailsByRange() throws Throwable {
        System.out.println("run TestGetBlocksDetailsByRange.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

        long t0 = System.currentTimeMillis();

        ApiMsg msg = api.getAdmin().getBlockDetailsByRange(1L, 500L);
        assertFalse(msg.isError());
        List<BlockDetails> blks = msg.getObject();

        long t1 = System.currentTimeMillis();

        long totalTime = t1 - t0;
        long totalTxns = 0;
        for (BlockDetails b : blks) {
            System.out.println("#: " + b.getNumber() + " [" + b.getTxDetails().size() + "]");
            totalTxns += b.getTxDetails().size();
        }

        System.out.println("bench: " + (t1 - t0) + " ms");
        System.out.println("time/txn: " + totalTime / (double)totalTxns);

        assertNotNull(blks);

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

        long latest = api.getChain().blockNumber().getObject();

        long t0 = System.currentTimeMillis();

        ApiMsg msg = null;
        int retry = 0;
        do {
            try {
                if (!api.isConnected()) {
                    System.out.println("Api not connected.");
                    api.destroyApi();
                    api.connect(url);
                }

                msg = api.getAdmin().getBlockSqlByRange(0L, latest);
                retry = 400;
            } catch (Exception e) {
                System.out.println("Retrying api call ------------------------------");
                e.printStackTrace();
                retry++;
                Thread.sleep(1000);
            }
        }
        while (retry < 100);

        assertFalse(msg == null);
        assertFalse(msg.isError());
        List<BlockSql> blks = msg.getObject();

        long t1 = System.currentTimeMillis();

        long totalTime = t1 - t0;
        long totalTxns = 0;
        for (BlockSql b : blks) {
            System.out.println("#: " + b.getNumber() + " [" + b.getTransactions().size() + "]");
            totalTxns += b.getTransactions().size();
        }

        System.out.println("bench: " + (t1 - t0) + " ms");
        System.out.println("time/txn: " + totalTime / (double)totalTxns);

        assertNotNull(blks);

        api.destroyApi();
    }

    @Test
    public void TestGetAccountDetailsByAddressList() throws Throwable {
        System.out.println("run TestGetAccountDetailsByAddressList.");

        IAionAPI api = IAionAPI.init();
        api.connect(url);

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

        IAionAPI api = IAionAPI.init();
        ApiMsg apiMsg = api.connect(url);
        assertFalse(apiMsg.isError());
        api.connect(url);

        apiMsg.set(api.getWallet().getAccounts());
        assertFalse(apiMsg.isError());
        List accs = apiMsg.getObject();
        assertFalse(accs.isEmpty());

        Address acc = (Address) accs.get(0);
        apiMsg.set(api.getChain().getNonce(acc));
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

        IAionAPI api = IAionAPI.init();
        ApiMsg apiMsg = api.connect(url);
        assertFalse(apiMsg.isError());

        // !! Change this to an account and password that has positive balance.
        String accStr = "0xa0764b690b0c9e07d5ca4763b7c1de8d8599901d0f3936b6fce91adc0602777f";
        String pass = "j";
        Address acc = new Address(accStr);
        BigInteger prevNonce = api.getChain().getNonce(acc).getObject();
        assertNotNull(prevNonce);

        apiMsg.set(api.getChain().getBalance(acc));
        assertFalse(apiMsg.isError());
        BigInteger balance = apiMsg.getObject();
        System.out.println("The account balance is " + balance);
        assertFalse(balance.equals(BigInteger.ZERO));

        Assert.assertTrue(api.getWallet().unlockAccount(acc, pw, 300).getObject());

        TxArgs.TxArgsBuilder builder = new TxArgs.TxArgsBuilder()
            .data(ByteArrayWrapper.wrap(IUtils.hex2Bytes("00000000")))
            .from(acc)
            .to(acc)
            .nrgLimit(100000)
            .nrgPrice(10000000000L)
            .value(BigInteger.ZERO)
            .nonce(BigInteger.ZERO);
        api.getTx().fastTxbuild(builder.createTxArgs());

        Hash256 txHash = ((MsgRsp)api.getTx().sendTransaction(null).getObject()).getTxHash();
        assertNotNull(txHash);

        apiMsg.set(api.getChain().getNonce(acc));
        assertFalse(apiMsg.isError());
        BigInteger currNonce = apiMsg.getObject();
        assertNotNull(currNonce);
        System.out.println("Previous nonce " + prevNonce + " current nonce " + currNonce);
        Assert.assertTrue(currNonce.subtract(prevNonce).equals(BigInteger.ONE));

        api.destroyApi();
    }

    @Test
    public void TestApiShutdown() {
        for (int i=0; i<3; i++) {
            System.out.println("run TestGracefulShutdown.");

            IAionAPI api = IAionAPI.init();
            api.connect(url);

            long t0 = System.currentTimeMillis();

            ApiMsg msg = api.getAdmin().getBlockDetailsByRange(1L, 10L);
            assertFalse(msg.isError());
            List<BlockDetails> blks = msg.getObject();

            long t1 = System.currentTimeMillis();

            long totalTime = t1 - t0;
            long totalTxns = 0;
            for (BlockDetails b : blks) {
                System.out.println("#: " + b.getNumber() + " [" + b.getTxDetails().size() + "]");
                totalTxns += b.getTxDetails().size();
            }

            System.out.println("bench: " + (t1 - t0) + " ms");
            System.out.println("time/txn: " + totalTime / (double) totalTxns);

            assertNotNull(blks);

            api.destroyApi();

            System.out.println("Api Destroyed");
        }

        int nbRunning = 0;
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getState()==Thread.State.RUNNABLE) nbRunning++;
        }

        assertEquals(4, nbRunning);

        System.out.println("test done");
    }
}
