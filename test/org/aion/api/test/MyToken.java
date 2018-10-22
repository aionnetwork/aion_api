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

import static org.aion.api.ITx.NRG_LIMIT_CONTRACT_CREATE_MAX;
import static org.aion.api.ITx.NRG_LIMIT_TX_MAX;
import static org.aion.api.ITx.NRG_PRICE_MIN;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.aion.api.IAionAPI;
import org.aion.api.IContract;
import org.aion.api.sol.IAddress;
import org.aion.api.sol.ISString;
import org.aion.api.sol.ISolidityArg;
import org.aion.api.sol.IUint;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.ContractResponse;
import org.aion.base.type.Address;
import org.junit.Test;

public class MyToken {

    @Test
    public void tokenTest() {
        String s =
                "contract MyToken{  event Transfer(address indexed from, address indexed to, uint128 value); string public name;  string public symbol;  uint8 public decimals; mapping(address=>uint128) public balanceOf; function MyToken(uint128 initialSupply, string tokenName, uint8 decimalUnits, string tokenSymbol){ balanceOf[msg.sender]=initialSupply;    name = tokenName;    symbol = tokenSymbol;    decimals = decimalUnits;  } function transfer(address _to,uint64 _value){    if (balanceOf[msg.sender] < _value || balanceOf[_to] + _value < balanceOf[_to]) throw;    balanceOf[msg.sender] -= _value;    balanceOf[_to] += _value;    Transfer(msg.sender, _to, _value); }}";

        IAionAPI api = IAionAPI.init();
        api.connect(IAionAPI.LOCALHOST_URL);

        // unlockAccount before deployContract or send a transaction.
        List<Address> accs = api.getWallet().getAccounts().getObject();
        assertThat(accs.size(), is(greaterThanOrEqualTo(2)));
        String password = "PLAT4life";
        Address acc = accs.get(0);
        Address acc2 = accs.get(1);

        assertTrue(api.getWallet().unlockAccount(acc, password, 300).getObject());

        ArrayList<ISolidityArg> param = new ArrayList<>();
        param.add(IUint.copyFrom(100000));
        param.add(ISString.copyFrom("Aion Token"));
        param.add(IUint.copyFrom(10));
        param.add(ISString.copyFrom("AION"));

        ApiMsg msg =
                api.getContractController()
                        .createFromSource(
                                s, acc, NRG_LIMIT_CONTRACT_CREATE_MAX, NRG_PRICE_MIN, param);
        if (msg.isError()) {
            System.out.println("Deploy contract failed! " + msg.getErrString());
        }

        IContract contract = api.getContractController().getContract();

        // Check initial default account balance
        ContractResponse cr =
                contract.newFunction("balanceOf")
                        .setFrom(acc)
                        .setParam(IAddress.copyFrom(acc.toString()))
                        .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                        .setTxNrgPrice(NRG_PRICE_MIN)
                        .build()
                        .execute()
                        .getObject();

        for (Object a : cr.getData()) {
            System.out.println("balanceOf " + acc.toString() + ": " + a.toString());
            assertThat(a.toString(), is(equalTo("100000")));
        }

        // Transfer balance to another account
        cr =
                contract.newFunction("transfer")
                        .setParam(IAddress.copyFrom(acc2.toString()))
                        .setParam(IUint.copyFrom(1))
                        .setTxNrgLimit(5_000_000L)
                        .setTxNrgPrice(1L)
                        .build()
                        .execute()
                        .getObject();

        // Check account2's balance
        cr =
                contract.newFunction("balanceOf")
                        .setParam(IAddress.copyFrom(acc2.toString()))
                        .setTxNrgLimit(500_000L)
                        .setTxNrgPrice(1L)
                        .build()
                        .execute()
                        .getObject();

        for (Object a : cr.getData()) {
            System.out.println("new balanceOf " + acc2.toString() + ": " + a.toString());
            assertThat(a.toString(), is(equalTo("1")));
        }

        // Check account1's balance
        cr =
                contract.newFunction("balanceOf")
                        .setParam(IAddress.copyFrom(acc.toString()))
                        .setTxNrgLimit(500_000L)
                        .setTxNrgPrice(1L)
                        .build()
                        .execute()
                        .getObject();

        for (Object a : cr.getData()) {
            System.out.println("new balanceOf " + acc.toString() + ": " + a.toString());
            assertThat(a.toString(), is(equalTo("99999")));
        }

        api.destroyApi();
    }
}
