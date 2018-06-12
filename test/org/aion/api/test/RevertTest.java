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

import org.aion.api.IAionAPI;
import org.aion.api.IContract;
import org.aion.api.impl.AionAPIImpl;
import org.aion.api.sol.IUint;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.ContractResponse;
import org.aion.base.type.Address;
import org.junit.Test;

import java.util.List;
import java.util.Scanner;

import static org.aion.api.ITx.NRG_LIMIT_CONTRACT_CREATE_MAX;
import static org.aion.api.ITx.NRG_LIMIT_TX_MAX;
import static org.aion.api.ITx.NRG_LIMIT_TX_MIN;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class RevertTest {

    // Make sure the password of the testing account been set properly
    private static final String DEFAULT_PASSWORD = "";

    private static String readContract() {
        StringBuilder contract = new StringBuilder();
        Scanner s = new Scanner(RevertTest.class.getResourceAsStream("Revert.sol"));
        while (s.hasNextLine()) {
            contract.append(s.nextLine());
            contract.append("\n");
        }
        s.close();

        return contract.toString();
    }

    @Test
    public void testRevertOperation() {
        String contract = readContract();

        IAionAPI api = IAionAPI.init();
        api.connect(AionAPIImpl.LOCALHOST_URL);

        /* unlock account */
        List<Address> accs = api.getWallet().getAccounts().getObject();
        assertThat(accs.get(0), not(equalTo(null)));
        assertTrue(api.getWallet().unlockAccount(accs.get(0), DEFAULT_PASSWORD, 3600).getObject());

        /* deploy contract */
        ApiMsg msg = api.getContractController()
            .createFromSource(contract, accs.get(0), NRG_LIMIT_CONTRACT_CREATE_MAX,
                NRG_LIMIT_TX_MIN);
        if (msg.isError()) {
            System.out.println("Deploy contract failed! " + msg.getErrString());
        }

        IContract ct = api.getContractController().getContract();
        assertNotNull(ct.getContractAddress());
        System.out.println("Contract Address: " + ct.getContractAddress().toString());

        /* getData */
        ContractResponse rsp = ct.newFunction("getData")
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_LIMIT_TX_MIN)
            .build()
            .execute()
            .getObject();
        assertThat(rsp.getData().get(0), is(equalTo(3L)));

        /* setData and getData */
        ct.newFunction("setData")
            .setParam(IUint.copyFrom(5L))
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_LIMIT_TX_MIN)
            .build()
            .execute()
            .getObject();

        rsp = ct.newFunction("getData")
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_LIMIT_TX_MIN)
            .build()
            .execute()
            .getObject();

        assertThat(rsp.getData().get(0), is(equalTo(5L)));

        /* setData2 and getData */
        ct.newFunction("setData2")
            .setParam(IUint.copyFrom(7L))
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_LIMIT_TX_MIN)
            .build()
            .execute()
            .getObject();

        rsp = ct.newFunction("getData")
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_LIMIT_TX_MIN)
            .build()
            .execute()
            .getObject();

        assertThat(rsp.getData().get(0), is(equalTo(5L)));

        api.destroyApi();
    }
}
