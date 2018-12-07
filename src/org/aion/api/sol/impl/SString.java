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
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.aion.api.IUtils;
import org.aion.api.impl.ErrId;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.sol.ISString;

/** Created by yao on 16/09/16. */

/** Class SString extends from SolidityAbstractType. use for function arguments input/output. */
public final class SString extends SolidityAbstractType implements ISString {

    private String val;

    private SString(String input) {
        super();

        if (input != null) {
            this.val = input;
        }
        this.typeProperty = SolidityValue.SolidityArgsType.DYNAMIC;
    }

    private SString() {
        super();
        this.typeProperty = SolidityValue.SolidityArgsType.DYNAMIC;
    }

    /**
     * Generates an SString object from a String.
     *
     * @param in
     * @return {@link SString}
     */
    public static SString copyFrom(@Nonnull String in) {
        return new SString(in);
    }

    /**
     * Instantiates an empty SString object for decoding purposes, not user facing.
     *
     * @return {@link SString}
     */
    public static SString createForDecode() {
        return new SString();
    }

    /**
     * Checks that inputted string is the correct type. To be used with ABI.
     *
     * @param in Solidity Type.
     * @return returns a boolean indicating the type is SString.
     */
    public boolean isType(String in) {
        if (in == null) {
            LOGGER.error("[isType] {}", ErrId.getErrString(-315L));
            return false;
        }
        return Pattern.matches("^string(\\[([0-9]*)])*$", in);
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
            LOGGER.error("[formatToString] {}", ErrId.getErrString(-315L));
            return null;
        }
        String hexInput = IUtils.bytes2Hex(entry);

        assert hexInput != null;
        int length = hexInput.length() / 2;
        int l = ((hexInput.length() + encodeUnitLengthDouble - 1) / encodeUnitLengthDouble);

        // TODO: optimize
        return ApiUtils.toHexPadded16(BigInteger.valueOf(length).toByteArray())
                + ApiUtils.padRight(hexInput, l * encodeUnitLengthDouble);
    }

    /**
     * Returns a correctly formatted hex string, given an input byte array (usually 16 bytes).
     * Encoding varies depending on the solidity type being encoded.
     *
     * @param data
     * @param offset
     * @return decoded Solidity data.
     */
    public String decodeToSolidityType(byte[] data, int offset) {
        if (data == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[decodeToSolidityType] {}", ErrId.getErrString(-315L));
            }
            return null;
        }
        if (offset + encodeUnitLength > data.length) {
            return "";
        }

        int startOffset = ApiUtils.toInt(data, offset, encodeUnitLength);
        int stringLength = ApiUtils.toInt(data, startOffset, encodeUnitLength);

        return new String(
                Arrays.copyOfRange(
                        data,
                        startOffset + encodeUnitLength,
                        startOffset + encodeUnitLength + stringLength));
    }

    /** for contract internal encode/decode. */
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

    /** for contract internal encode/decode. */
    @Override
    public String getInputFormat() {
        return formatToString(val.getBytes());
    }

    /** for contract internal encode/decode. */
    @Override
    public int getDynamicPartLength() {
        return Objects.requireNonNull(formatToString(val.getBytes())).length();
    }

    @Override
    protected boolean isDoubleUnit() {
        return false;
    }
}
