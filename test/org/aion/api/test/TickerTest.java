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
import org.aion.api.impl.AionAPIImpl;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.ContractEvent;
import org.aion.api.type.ContractResponse;
import org.aion.base.type.Address;
import org.junit.Test;

import java.util.List;
import java.util.Scanner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TickerTest {

    // Make sure the password of the testing account been set properly
    private static final String DEFAULT_PASSWORD = "";

    private static String readContract() {
        StringBuilder contract = new StringBuilder();
        Scanner s = new Scanner(TickerTest.class.getResourceAsStream("Ticker.sol"));
        while (s.hasNextLine()) {
            contract.append(s.nextLine());
            contract.append("\n");
        }
        s.close();

        return contract.toString();
    }

    @Test
    public void testTickerOperation() throws Throwable {
        String sc = readContract();

        IAionAPI api = IAionAPI.init();
        api.connect(AionAPIImpl.LOCALHOST_URL);

        /* unlock account */
        Address cb = ((List<Address>)api.getWallet().getAccounts().getObject()).get(0);
        assertThat(cb, not(equalTo(null)));
        assertTrue(api.getWallet().unlockAccount(cb, DEFAULT_PASSWORD, 86400).getObject());

        /* deploy contract */
        ApiMsg msg = api.getContractController().createFromSource(sc, cb, 5_000_000L, 1L);
        if (msg.isError()) {
            System.out.println("Deploy contract failed! " + msg.getErrString());
        }

        IContract ct = api.getContractController().getContract();
        assertNotNull(ct);
        assertNotNull(ct.getContractAddress());
        System.out.println("Contract Address: " + ct.getContractAddress().toString());
        msg.set(ct.allEvents().register());
        if (msg.isError()) {
            System.out.println("contract event register failed! " + msg.getErrString());
        }

        /* getData */
        ContractResponse rsp = ct.newFunction("getData")
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();
        assertThat(rsp.getData().get(0), is(equalTo(1L)));

        /* setData and getData */
        ct.newFunction("tick")
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();

        rsp = ct.newFunction("getData")
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();
        assertThat(rsp.getData().get(0), is(equalTo(2L)));

        ct.newFunction("tick")
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();

        ct.newFunction("tick")
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();

        rsp = ct.newFunction("getData")
                .setTxNrgLimit(5_000_000L)
                .setTxNrgPrice(1L)
                .build()
                .execute()
                .getObject();
        assertThat(rsp.getData().get(0), is(equalTo(4L)));

        List<ContractEvent> ce = ct.getEvents();

        assertThat(ce.size(), is(equalTo(3)));

        //TODO: test ContractEventFilter
        //ContractEventFilter.ContractEventFilterBuilder builder = new ContractEventFilter.ContractEventFilterBuilder()
        //        .fromBlock("latest")
        //        .toBlock("10")
        //        .topics(new ArrayList())
        //        .expireTime(0)
        //        .addresses(new ArrayList());

        //msg = ct.queryEvents(builder.createContractEventFilter());
        //if (msg.isError()) {
        //    throw new Exception();
        //}
        //
        //List<ContractEvent> cts = msg.getObject();
        //assertThat(cts.size(), is(equalTo(4)));
        //
        //List<String> topics = new ArrayList();
        //topics.add("Ti");
        //
        //builder.topics(topics);
        //
        //msg = ct.queryEvents(builder.createContractEventFilter());
        //if (msg.isError()) {
        //    throw new Exception();
        //}
        //
        //cts = msg.getObject();
        //assertThat(cts.size(), is(equalTo(1)));

        api.destroyApi();
    }
}
