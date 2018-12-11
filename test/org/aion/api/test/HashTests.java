package org.aion.api.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.keccak.Keccak256;
import org.junit.Test;

/** Created by yao on 03/10/16. */
public class HashTests {

    @Test
    public void Keccak256HashTest() {
        String input = "val()";

        Keccak256 hasher = new Keccak256();
        byte[] hashed = hasher.digest(input.getBytes());
        assertThat(
                hashed,
                is(
                        equalTo(
                                ApiUtils.hex2Bytes(
                                        "3c6bb436052bdd000ec25d32e5747129050bc7b2b9eaf3b17fdf2ce964a1dd8a"))));
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
