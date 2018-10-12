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
import org.aion.api.impl.ErrId;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.sol.IUint;

/**
 * Created by yao on 16/09/16.
 */

/**
 * Class Uint extends from SolidityAbstractType. use for function arguments input/output.
 */
public class Uint extends SolidityAbstractType implements IUint {
    // use copyFrom instead

    private Uint(List val) {
        super();
        if (val != null) {
            this.valArray = val;
        }
    }

    private Uint() {
        super();
    }

    /**
     * Generates an Uint object from a String.
     *
     * @return Uint object.
     */
    public static Uint copyFrom(String in) {
        if (in == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[copyFrom] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

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

        ArrayList<Object> l = new ArrayList<>();
        l.add(new BigInteger(bytes).toByteArray());
        return new Uint(l);
    }

    /**
     * Generates an Uint object from a Integer.
     *
     * @return Uint object.
     */
    public static Uint copyFrom(Integer in) {
        if (in == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[copyFrom] {}", ErrId.getErrString(-315L));
            }
            return null;
        }
        List<Object> l = new ArrayList<>();
        l.add(formatInputUint(in));

        return new Uint(l);
    }

    /**
     * for contract internal encode/decode.
     */
    private static byte[] formatInputUint(Integer input) {
        if (input == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[formatInputUint] {}", ErrId.getErrString(-315L));
            }
            return null;
        }
        return ApiUtils.toTwosComplement(input);
    }

    /**
     * Generates an Uint object from a Long.
     *
     * @return Uint object.
     */
    public static Uint copyFrom(long in) {
        List<Object> l = new ArrayList<>();
        l.add(formatInputUint(in));

        return new Uint(l);
    }

    /**
     * for contract internal encode/decode.
     */
    private static byte[] formatInputUint(long input) {
        return ApiUtils.toTwosComplement(input);
    }

    /**
     * Generates an Uint object from an ArrayList, String or byte array, this structure should match
     * the list structure defined in the ABI and consist only of Bytes.
     *
     * @return Uint object.
     */
    public static Uint copyFrom(List l) {
        // at this point we don't know about type yet
        // assume first variable is the correct Type
        if (l == null || l.size() == 0) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[copyFrom] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        List inputArrayList = copyFromHelper(l);
        return new Uint(inputArrayList);
    }

    /**
     * for contract internal encode/decode.
     */
    private static byte[] formatInputUint(String input) {
        if (input == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[formatInputUint] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        if (input.length() == 1) {
            input = "0" + input;
        }

        byte[] bytes = ApiUtils.hex2Bytes(input);

        if (bytes.length > encodeUnitLength) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[formatInputUint] {}", ErrId.getErrString(-115L));
            }
            return null;
        }
        return bytes;
    }

    private static List copyFromHelper(List l) {
        if (l == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[copyFromHelper] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        List localArrayList = new ArrayList();

        for (Object entry : l) {
            if (entry instanceof Integer) {
                localArrayList.add(formatInputUint((Integer) entry));
            } else if (entry instanceof String) {
                localArrayList.add(formatInputUint((String) entry));
            } else if (entry instanceof Long) {
                localArrayList.add(formatInputUint((Long) entry));
            } else if (entry instanceof ArrayList) {
                localArrayList.add(copyFromHelper((ArrayList) entry));
            } else {
                LOGGER.error("[copyFromHelper] {}", ErrId.getErrString(-117L));
                return null;
            }
        }
        return localArrayList;
    }

    /**
     * Instantiates an empty Uint object for decoding purposes, not user facing.
     *
     * @return {@link Uint}
     */
    public static Uint createForDecode() {
        return new Uint();
    }

    /**
     * Checks that inputted string is the correct type. To be used with ABI.
     *
     * @param in Solidity Type.
     * @return returns a boolean indicating the type is Uint.
     */
    public boolean isType(String in) {
        if (in == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[formatInputAddress] {}", ErrId.getErrString(-315L));
            }
            return false;
        }
        return Pattern.matches("^uint([0-9]*)?(\\[([0-9]*)\\])*$", in);
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

        if ((entry[0] & (byte) 0x80) == (byte) 0x80) {
            return ApiUtils.toHexPaddedNegative16(entry);
        } else {
            return ApiUtils.toHexPadded16(entry);
        }
    }

    /**
     * Returns a correctly formatted hex string, given an input byte array (usually 32 bytes).
     * Encoding varies depending on the solidity type being encoded.
     *
     * @return decoded Solidity data.
     */
    public Long decodeToSolidityType(byte[] data, int offset) {
        if (data == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[decodeToSolidityType] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        if (offset + encodeUnitLength > data.length) {
            return 0L;
        }

        return ApiUtils.toUnsignedLong(data, offset, encodeUnitLength);
    }

    @Override
    protected boolean isDoubleUnit() {
        return false;
    }
}
