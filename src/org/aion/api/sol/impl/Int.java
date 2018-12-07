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
package org.aion.api.sol.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.aion.api.impl.ErrId;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.sol.IInt;

/** Created by yao on 20/09/16. */

/**
 * Class Int inherit from class SolidityAbstractType. Contains functions for encoding string,
 * decoding string, datatype checking, Most functions used are not intended to be user facing, and
 * should be left unused by the user.
 */
public final class Int extends SolidityAbstractType implements IInt {
    // use copyFrom instead

    private Int(List val) {
        super();
        if (val != null) {
            this.valArray = val;
        }
    }

    private Int() {
        super();
    }

    /**
     * Generates an Integer object from a String.
     *
     * @param in
     * @return {@link Int}
     */
    public static Int copyFrom(@Nonnull String in) {
        if (in.length() == 1) {
            in = "0" + in;
        }

        byte[] bytes = ApiUtils.hex2Bytes(in);

        if (bytes.length > encodeUnitLength) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[copyFrom] {}", ErrId.getErrString(-115L));
            }
            return null;
        }

        List<Object> l = new ArrayList<>();
        l.add(new BigInteger(bytes).toByteArray());
        return new Int(l);
    }

    /**
     * Generates an Integer object from a Long.
     *
     * @param in int value.
     * @return {@link Int}
     */
    public static Int copyFrom(int in) {
        List<Object> l = new ArrayList<>();
        l.add(formatInputInt(in));

        return new Int(l);
    }

    /** for contract internal encode/decode. */
    private static byte[] formatInputInt(int input) {
        return ApiUtils.toTwosComplement(input);
    }

    /**
     * Generates an Int object from a Long.
     *
     * @param in long value.
     * @return {@link Int}
     */
    public static Int copyFrom(long in) {

        List<Object> l = new ArrayList<>();
        l.add(formatInputInt(in));

        return new Int(l);
    }

    /**
     * Generates an Int object from a BigInteger.
     *
     * @param in {@link BigInteger}
     * @return {@link Int}
     */
    public static Int copyFrom(@Nonnull BigInteger in) {

        List<Object> l = new ArrayList<>();
        l.add(formatInputInt(in));

        return new Int(l);
    }

    /** for contract internal encode/decode. */
    private static byte[] formatInputInt(long input) {
        return ApiUtils.toTwosComplement(input);
    }

    /** for contract internal encode/decode. */
    private static byte[] formatInputInt(@Nonnull BigInteger input) {
        if (input.bitLength() > 127) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[formatInputInt] {}", ErrId.getErrString(-315L));
            }
            return null;
        }
        return input.toByteArray();
    }

    /**
     * Generates an Uint object from an ArrayList, String or byte array, this structure should match
     * the list structure defined in the ABI and consist only of Bytes.
     *
     * @param l
     * @return {@link Int}
     */
    public static Int copyFrom(@Nonnull List l) {

        // at this point we don't know about type yet
        // assume first variable is the correct Type
        if (l.isEmpty()) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[copyFrom] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        List inputArrayList = copyFromHelper(l);
        return new Int(inputArrayList);
    }

    /** for contract internal encode/decode. */
    private static byte[] formatInputInt(@Nonnull String input) {

        if (input.length() == 1) {
            input = "0" + input;
        }

        byte[] bytes = ApiUtils.hex2Bytes(input);

        if (bytes.length > encodeUnitLength) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[formatInputInt] {}", ErrId.getErrString(-115L));
            }
            return null;
        }
        return bytes;
    }

    private static List copyFromHelper(@Nonnull List l) {

        if (l.isEmpty()) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[copyFromHelper] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        List localArrayList = new ArrayList();

        for (Object entry : l) {
            if (entry instanceof Integer) {
                localArrayList.add(formatInputInt((int) entry));
            } else if (entry instanceof String) {
                localArrayList.add(formatInputInt((String) entry));
            } else if (entry instanceof Long) {
                localArrayList.add(formatInputInt((long) entry));
            } else if (entry instanceof BigInteger) {
                localArrayList.add(formatInputInt((BigInteger) entry));
            } else if (entry instanceof ArrayList) {
                localArrayList.add(copyFromHelper((ArrayList) entry));
            } else {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[copyFromHelper] {}", ErrId.getErrString(-118L));
                }
                return null;
            }
        }
        return localArrayList;
    }

    /**
     * Instantiates an empty Int object for decoding purposes, not user facing.
     *
     * @return {@link Int}
     */
    public static Int createForDecode() {
        return new Int();
    }

    /**
     * Checks that inputted string is Int type. To be used with ABI.
     *
     * @param in Solidity Type.
     * @return returns a boolean indicating the type is Int.
     */
    public boolean isType(String in) {
        if (in == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[isType] {}", ErrId.getErrString(-315L));
            }
            return false;
        }

        return Pattern.matches("^int([0-9]*)?(\\[([0-9]*)])*$", in);
    }

    /**
     * Returns a correctly formatted hex string, given an input byte array (usually 32 bytes).
     * Encoding varies depending on the solidity type being encoded.
     *
     * @param entry data need to be formatted.
     * @return formatted string for encode.
     */
    public String formatToString(byte[] entry) {
        if (entry == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[formatToString] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        if ((entry[entry.length - 1] >> 7) == (byte) (0x1)) {
            return ApiUtils.toHexPaddedNegative16(entry);
        } else {
            return ApiUtils.toHexPadded16(entry);
        }
    }

    /**
     * Returns a correctly formatted hex string, given an input byte array (usually 32 bytes).
     * Encoding varies depending on the solidity type being encoded.
     *
     * @param data byte array
     * @param offset int value
     * @return decoded Solidity data.
     */
    public BigInteger decodeToSolidityType(byte[] data, int offset) {
        if (data == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[decodeToSolidityType] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        if (offset + encodeUnitLength > data.length) {
            return BigInteger.ZERO;
        }

        return ApiUtils.toBigInteger(data, offset, encodeUnitLength);
    }

    @Override
    protected boolean isDoubleUnit() {
        return false;
    }
}
