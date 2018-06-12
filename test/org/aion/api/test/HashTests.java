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

import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.keccak.Keccak256;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by yao on 03/10/16.
 */
public class HashTests {

    @Test
    public void Keccak256HashTest() {
        String input = "val()";

        Keccak256 hasher = new Keccak256();
        byte[] hashed = hasher.digest(input.getBytes());
        assertThat(hashed, is(equalTo(ApiUtils
            .hex2Bytes("3c6bb436052bdd000ec25d32e5747129050bc7b2b9eaf3b17fdf2ce964a1dd8a"))));
    }

    @Test
    public void NewKeccakHashPerformanceTest() {
        byte[] input = "val()".getBytes();
        for (int i = 0; i < 1000; i++) {
            ApiUtils.keccak(input);
        }
    }

    @Test
    public void OldKeccakHashPerformanceTest() {
        byte[] input = "val()".getBytes();
        for (int i = 0; i < 1000; i++) {
            ApiUtils.keccak256(input);
        }
    }
}
