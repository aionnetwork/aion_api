package org.aion.api.test;

import org.aion.api.IAionAPI;
import org.aion.api.IContract;
import org.aion.api.IUtils;
import org.aion.api.impl.AionAPIImpl;
import org.aion.api.sol.IUint;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.ContractResponse;
import org.aion.api.type.MsgRsp;
import org.aion.base.type.AionAddress;
import org.aion.base.util.ByteArrayWrapper;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.assertTrue;
import static org.aion.api.ITx.NRG_LIMIT_CONTRACT_CREATE_MAX;
import static org.aion.api.ITx.NRG_LIMIT_TX_MAX;
import static org.aion.api.ITx.NRG_PRICE_MIN;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/** Created by Jay Tseng on 18/05/17. */
public class SimpleBench {

    // Make sure the password of the testing account been set properly
    private static final String DEFAULT_PASSWORD = "";
    private static final String sc =
            "pragma solidity ^0.4.0;\n"
                    + "\n"
                    + "contract Simple {\n"
                    + "    uint counter;\n"
                    + "\n"
                    + "    function f(uint n) returns(uint) {\n"
                    + "\n"
                    + "        uint sum = 0;\n"
                    + "        for (uint i = 0; i < n; i++) {\n"
                    + "            sum = sum + i;\n"
                    + "        }\n"
                    + "\n"
                    + "        return sum;\n"
                    + "    }\n"
                    + "    \n"
                    + "    function g(uint n) {\n"
                    + "        counter++;\n"
                    + "    }\n"
                    + "}";

    private static IAionAPI api = null;
    private static IContract ct = null;

    public static void setUp() {
        api = IAionAPI.init();
        api.connect(AionAPIImpl.LOCALHOST_URL);

        /* unlock account */
        List acc = api.getWallet().getAccounts().getObject();
        assertThat(acc.size(), is(greaterThan(0)));

        AionAddress cb = (AionAddress) acc.get(0);
        assertThat(cb, not(equalTo(null)));
        assertTrue(api.getWallet().unlockAccount(cb, DEFAULT_PASSWORD, 3600).getObject());

        /* deploy contract */
        ApiMsg msg =
                api.getContractController()
                        .createFromSource(sc, cb, NRG_LIMIT_CONTRACT_CREATE_MAX, NRG_PRICE_MIN);
        if (msg.isError()) {
            System.out.println("Deploy contract failed! " + msg.getErrString());
        }

        ct = api.getContractController().getContract();
        assertNotNull(ct);
        assertNotNull(ct.getContractAddress());
        System.out.println("Contract Address: " + ct.getContractAddress().toString());
    }

    private static int run(int totalTxs) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger(0);

        BlockingDeque<byte[]> queue = new LinkedBlockingDeque<>();
        Thread t =
                new Thread(
                        () -> {
                            while (true) {
                                try {
                                    Thread.sleep(1000);

                                    int qSize = queue.size();
                                    long duration = (System.currentTimeMillis() - startTime) / 1000;
                                    System.out.println(
                                            "proccessed tx: "
                                                    + count
                                                    + ", queue size: "
                                                    + qSize
                                                    + ", throughput (tx/sec): "
                                                    + count.get() / duration);

                                    for (int i = 0; i < qSize; i++) {
                                        byte[] msgHash = queue.take();
                                        ApiMsg apiMsg =
                                                api.getTx()
                                                        .getMsgStatus(
                                                                ByteArrayWrapper.wrap(msgHash));
                                        if (IUtils.endTxStatus(
                                                ((MsgRsp) apiMsg.getObject()).getStatus())) {
                                            count.incrementAndGet();
                                        } else {
                                            queue.put(msgHash);
                                        }
                                    }
                                } catch (InterruptedException e) {
                                    break;
                                }
                            }
                        });
        t.start();

        IContract tmp =
                ct.newFunction("g")
                        .setParam(IUint.copyFrom(1))
                        .setTxNrgPrice(NRG_PRICE_MIN)
                        .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                        .build();

        while (true) {
            try {
                ContractResponse rsp = tmp.nonBlock().execute().getObject();
                assertNotNull(rsp.getMsgHash());
                assertTrue(!rsp.isTxError());
                queue.add(rsp.getMsgHash().getData());

                if (count.get() >= totalTxs) {
                    t.interrupt();
                    break;
                }
            } catch (Exception e) {
                Thread.sleep(1000);
            }
            Thread.sleep(500);
        }

        return (int) (count.get() / ((System.currentTimeMillis() - startTime) / 1000));
    }

    public static void tearDown() {
        api.destroyApi();
    }

    public static void main(String[] args) throws InterruptedException {
        int totalTxs = 500;

        setUp();
        int throughput = run(totalTxs);
        tearDown();

        System.out.println("Average throughput (tx/sec): " + throughput);
    }
}
