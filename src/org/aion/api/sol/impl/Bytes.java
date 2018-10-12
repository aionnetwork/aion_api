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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.aion.api.IUtils;
import org.aion.api.impl.ErrId;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.sol.IBytes;

/**
 * Created by yao on 20/09/16.
 */

/**
 * Class Bytes extends from SolidityAbstractType. use for function arguments input/output.
 */
public final class Bytes extends SolidityAbstractType implements IBytes {

    private Bytes(List in) {
        super();
        if (in != null) {
            this.valArray = in;
        }
    }

    private Bytes() {
        super();
    }

    /**
     * for contract internal encode/decode.
     */
    private static byte[] formatInputBytes(String in) {
        if (in == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[formatInputBytes] {}", ErrId.getErrString(-315L));
            }
            return null;
        }
        return ApiUtils.hex2Bytes(in);
    }

    /**
     * Generates an Bytes object from a Bytes array.
     *
     * @return {@link Bytes}
     */
    public static Bytes copyFrom(byte[] in) {
        if (in == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[copyFrom] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        ArrayList<byte[]> entryList = new ArrayList<>();
        entryList.add(formatInputBytes(in));
        return new Bytes(entryList);
    }

    /**
     * for contract internal encode/decode.
     */
    private static byte[] formatInputBytes(byte[] in) {
        if (in == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[formatInputBytes] {}", ErrId.getErrString(-315L));
            }
            return null;
        }
        return in;
    }

    /**
     * Generates an Bytes object from an ArrayList, String or byte array, this structure should
     * match the list structure defined in the ABI and consist only of Bytes.
     *
     * @return {@link Bytes}
     */
    public static Bytes copyFrom(List l) {
        // at this point we don't know about type yet
        // assume first variable is the correct Type
        if (l == null || l.size() == 0) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[copyFrom] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        List inputArrayList = copyFromHelper(l);
        return new Bytes(inputArrayList);
    }

    private static List copyFromHelper(List l) {
        if (l == null || l.size() == 0) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[copyFromHelper] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        List localArrayList = new ArrayList();

        for (Object entry : l) {
            if (entry instanceof byte[]) {
                localArrayList.add(formatInputBytes((byte[]) entry));
            } else if (entry instanceof String) {
                localArrayList.add(formatInputBytes((String) entry));
            } else if (entry instanceof ArrayList) {
                localArrayList.add(copyFromHelper((ArrayList) entry));
            } else {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[copyFromHelper] {}", ErrId.getErrString(-119L));
                }
                return null;
            }
        }
        return localArrayList;
    }

    /**
     * Instantiates an empty Bytes object for decoding purposes, not user facing.
     *
     * @return {@link Bytes}
     */
    public static Bytes createForDecode() {
        return new Bytes();
    }

    /**
     * Checks that inputted string is the correct type. To be used with ABI.
     *
     * @param in Solidity Type.
     * @return returns a boolean indicating the type is Bytes.
     */
    public boolean isType(String in) {
        if (in == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[formatInputAddress] {}", ErrId.getErrString(-315L));
            }
            return false;
        }
        return Pattern.matches("^bytes([0-9]{1,})(\\[([0-9]*)\\])*$", in);
    }

    /**
     * Returns a correctly formatted hex string, given an input byte array (usually 16 bytes).
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
        String res = IUtils.bytes2Hex(entry);

        int encodeLen = isDoubleUnit() ? encodeUnitLengthDouble : encodeUnitLength;

        assert res != null;
        int length = (res.length() + encodeLen - 1) / encodeLen;
        return ApiUtils.padRight(res, (length * encodeLen));
    }

    /**
     * Returns a correctly formatted hex string, given an input byte array (usually 32 bytes).
     * Encoding varies depending on the solidity type being encoded.
     *
     * @return decoded Solidity data
     */
    public byte[] decodeToSolidityType(byte[] data, int offset) {
        if (data == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[decodeToSolidityType] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        int encodeLen = isDoubleUnit() ? encodeUnitLengthDouble : encodeUnitLength;

        if (offset + encodeLen > data.length) {
            return new byte[encodeLen];
        }

        return Arrays.copyOfRange(data, offset, offset + encodeLen);
    }

    @Override
    protected boolean isDoubleUnit() {
        String t = this.type;
        int len = Integer.valueOf(t.replace("bytes", ""));
        return len > 16;
    }
}
