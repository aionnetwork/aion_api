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

package org.aion.api.sol.impl;

import org.aion.api.IUtils;
import org.aion.api.impl.ErrId;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.sol.IDynamicBytes;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by yao on 20/09/16.
 */

/**
 * Class DynamicBytes extends from SolidityAbstractType. use for function
 * arguments input/output.
 */
public final class DynamicBytes extends SolidityAbstractType implements IDynamicBytes {

    private DynamicBytes(List in) {
        super();
        if (in != null) {
            this.valArray = in;
        }
        this.typeProperty = SolidityValue.SolidityArgsType.DYNAMIC;
    }

    private DynamicBytes() {
        super();
        this.typeProperty = SolidityValue.SolidityArgsType.DYNAMIC;
    }

    /**
     * for contract internal encode/decode.
     */
    public static byte[] formatInputBytes(String in) {
        if (in == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[formatInputBytes] {}", ErrId.getErrString(-315L));
            }
            return null;
        }
        return ApiUtils.hex2Bytes(in);
    }

    /**
     * Generates an DynamicBytes object from a Bytes array.
     *
     * @param in
     * @return {@link DynamicBytes}
     */
    public static DynamicBytes copyFrom(byte[] in) {
        if (in == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[copyFrom] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        ArrayList<byte[]> entryList = new ArrayList<>();
        entryList.add(formatInputBytes(in));
        return new DynamicBytes(entryList);
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
     * Instantiates an empty DynamicBytes object for decoding purposes, not user
     * facing.
     *
     * @return {@link DynamicBytes}
     */
    public static DynamicBytes createForDecode() {
        return new DynamicBytes();
    }

    /**
     * Checks that inputted string is the correct type. To be used with ABI.
     *
     * @param in
     *         Solidity Type.
     * @return returns a boolean indicating the type is DynamicBytes.
     */
    public boolean isType(String in) {
        if (in == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[isType] {}", ErrId.getErrString(-315L));
            }
            return false;
        }
        return Pattern.matches("^bytes(\\[([0-9]*)\\])*$", in);
    }

    /**
     * Returns a correctly formatted hex string, given an input byte array
     * (usually 16 bytes). Encoding varies depending on the solidity type being
     * encoded.
     *
     * @param entry
     *         data need to be formatted.
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
        assert res != null;
        int size = res.length() / 2;
        int length = (res.length() + encodeUnitLengthDouble-1) / encodeUnitLengthDouble;
        //return ApiUtils.toHexPadded16(ByteBuffer.allocate(4).putInt(size).array()) + ApiUtils
        //                .padRight(res, (length * encodeUnitLengthDouble));
        return ApiUtils.toHexPadded16(BigInteger.valueOf(size).toByteArray()) + ApiUtils.padRight(res, length * encodeUnitLengthDouble);

    }

    /**
     * Returns a correctly formatted hex string, given an input byte array
     * (usually 16 bytes). Encoding varies depending on the solidity type being
     * encoded.
     *
     * @param data
     * @param offset
     * @return decoded Solidity data
     */
    public byte[] decodeToSolidityType(byte[] data, int offset) {
        if (data == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[decodeToSolidityType] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        if (offset + encodeUnitLength > data.length) {
            return new byte[encodeUnitLength];
        }

        int startOffset = ApiUtils.toInt(data, offset, encodeUnitLength);
        int length = ApiUtils.toInt(data, startOffset, encodeUnitLength);

        return Arrays.copyOfRange(data, startOffset + encodeUnitLength, startOffset + encodeUnitLength + length);
    }

    /**
     * for contract internal encode/decode.
     */
    @Override
    public void setType(String type) {
        if (type == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[setType] {}", ErrId.getErrString(-315L));
            }
            return;
        }
        this.type = type;
    }

    /**
     * for contract internal encode/decode.
     */
    @Override
    public String getInputFormat() {
        return formatToString((byte[]) valArray.get(0));
    }

    /**
     * for contract internal encode/decode.
     */
    @Override
    public int getDynamicPartLength() {
        return formatToString((byte[]) valArray.get(0)).length();
    }

    @Override
    protected boolean isDoubleUnit() {
        return false;
    }
}
