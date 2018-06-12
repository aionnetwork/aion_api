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

import static org.junit.Assert.assertFalse;

import org.aion.api.IAionAPI;
import org.aion.api.type.ApiMsg;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConnectingTests {

    static IAionAPI api = IAionAPI.init();

    @Test
    public void TestApiConnect() {
        System.out.println("run TestApiConnect.");

        ApiMsg apiMsg = api.connect(IAionAPI.LOCALHOST_URL);

        assertFalse(apiMsg.isError());
        api.destroyApi();

        System.out.println("run TestApiConnect again.");
        apiMsg = api.connect(IAionAPI.LOCALHOST_URL);
        assertFalse(apiMsg.isError());

        api.destroyApi();
    }
}
