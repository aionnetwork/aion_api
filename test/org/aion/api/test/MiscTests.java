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

import org.aion.api.IUtils;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.sol.impl.DynamicBytes;
import org.aion.api.sol.impl.Uint;
import org.junit.Test;

/**
 * Created by Jay Tseng on 15/12/16.
 */
public class MiscTests {

    @Test
    public void SolTypeTests() throws Throwable {
        System.out.println(IUtils.bytes2Hex(ApiUtils.toTwosComplement(-2)));

        Uint a = Uint.copyFrom(1);

        String s = a.formatToString(ApiUtils.hex2Bytes("0ffffffe"));
        System.out.println(s);

        DynamicBytes bytes = DynamicBytes.copyFrom(ApiUtils.hex2Bytes("01"));
        assert bytes != null;
        System.out.println(bytes.isType("bytes32"));
        System.out.println(bytes.isType("bytes8"));
        System.out.println(bytes.isType("bytes16"));
        System.out.println(bytes.isType("bytes256"));
        System.out.println(bytes.isType("bytes"));
    }

}
