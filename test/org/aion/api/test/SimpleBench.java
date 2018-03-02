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
import org.aion.api.IContract;
import org.aion.api.IUtils;
import org.aion.api.impl.AionAPIImpl;
import org.aion.api.sol.IUint;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.ContractResponse;
import org.aion.api.type.MsgRsp;
import org.aion.base.type.Address;
import org.aion.base.util.ByteArrayWrapper;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;


/**
 * Created by Jay Tseng on 18/05/17.
 */
public class SimpleBench {

    // Make sure the password of the testing account been set properly
    private static final String DEFAULT_PASSWORD = "";
    private static final String sc = "pragma solidity ^0.4.0;\n" +
            "\n" +
            "contract Simple {\n" +
            "    uint counter;\n" +
            "\n" +
            "    function f(uint n) returns(uint) {\n" +
            "\n" +
            "        uint sum = 0;\n" +
            "        for (uint i = 0; i < n; i++) {\n" +
            "            sum = sum + i;\n" +
            "        }\n" +
            "\n" +
            "        return sum;\n" +
            "    }\n" +
            "    \n" +
            "    function g(uint n) {\n" +
            "        counter++;\n" +
            "    }\n" +
            "}";

    private static IAionAPI api = null;
    private static IContract ct = null;

    public static void setUp() {
        api = IAionAPI.init();
        api.connect(AionAPIImpl.LOCALHOST_URL);

        /* unlock account */
        List acc = api.getWallet().getAccounts().getObject();
        assertThat(acc.size(), is(greaterThan(0)));

        Address cb = (Address) acc.get(0);
        assertThat(cb, not(equalTo(null)));
        assertTrue(api.getWallet().unlockAccount(cb, DEFAULT_PASSWORD, 3600).getObject());

        /* deploy contract */
        ApiMsg msg = api.getContractController().createFromSource(sc, cb, 5_000_000L, 1L);
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
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);

                    int qSize = queue.size();
                    long duration = (System.currentTimeMillis() - startTime) / 1000;
                    System.out.println("proccessed tx: " + count + ", queue size: " + qSize
                            + ", throughput (tx/sec): " + count.get() / duration);

                    for (int i = 0; i < qSize; i++) {
                        byte[] msgHash = queue.take();
                        ApiMsg apiMsg = api.getTx().getMsgStatus(ByteArrayWrapper.wrap(msgHash));
                        if (IUtils.endTxStatus(((MsgRsp) apiMsg.getObject()).getStatus())) {
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

        IContract tmp = ct.newFunction("g")
                .setParam(IUint.copyFrom(1))
                .setTxNrgPrice(1L)
                .setTxNrgLimit(5_000_000L)
                .build();

        while (true) {
            try {
                ContractResponse rsp = tmp.nonBlock()
                        .execute()
                        .getObject();
                assertNotNull(rsp.getMsgHash());
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
        int totalTxs = 3;

        setUp();
        int throughput = run(totalTxs);
        tearDown();

        System.out.println("Average throughput (tx/sec): " + throughput);
    }
}
