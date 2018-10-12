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
import org.aion.api.impl.ErrId;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.sol.IBool;

/**
 * Created by yao on 16/09/16.
 */

/**
 * Class Bool extends from SolidityAbstractType. use for function arguments input/output.
 */
public final class Bool extends SolidityAbstractType implements IBool {

    private Bool(List in) {
        super();
        if (in != null) {
            this.valArray = in;
        }
    }

    private Bool() {
        super();
    }

    /**
     * Generates an Bool object from a Boolean value.
     *
     * @return {@link Bool}
     */
    public static Bool copyFrom(boolean in) {
        List<byte[]> l = new ArrayList<>();
        byte[] out = {(byte) (in ? 1 : 0)};
        l.add(out);
        return new Bool(l);
    }

    /**
     * Generates an Bool object from an ArrayList of Boolean, this structure should match the list
     * structure defined in the ABI and consist only of Boolean.
     *
     * @return {@link Bool}.
     */
    public static Bool copyFrom(List l) {
        // at this point we don't know about type yet
        // assume first variable is the correct Type
        if (l == null || l.size() == 0) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[copyFrom] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        List inputArrayList = copyFromHelper(l);
        return new Bool(inputArrayList);
    }

    private static List copyFromHelper(List l) {

        if (l == null || l.size() == 0) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[copyFromHelper] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        ArrayList localArrayList = new ArrayList();

        for (Object entry : l) {
            if (entry instanceof Boolean) {
                byte[] out = {(byte) ((Boolean) entry ? 1 : 0)};
                localArrayList.add(out);
            } else if (entry instanceof ArrayList) {
                localArrayList.add(copyFromHelper((ArrayList) entry));
            } else {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[copyFromHelper] {}", ErrId.getErrString(-123L));
                }
                return null;
            }
        }
        return localArrayList;
    }

    /**
     * Instantiates an empty Bool object for decoding purposes, not user facing.
     *
     * @return {@link Bool}
     */
    public static Bool createForDecode() {
        return new Bool();
    }

    /**
     * Checks that inputted string is the correct type. To be used with ABI.
     *
     * @param in Solidity Type.
     * @return returns a boolean indicating the type is Bool.
     */
    public boolean isType(String in) {
        if (in == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[formatInputAddress] {}", ErrId.getErrString(-315L));
            }
            return false;
        }
        return Pattern.matches("^bool(\\[([0-9]*)\\])*$", in);
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
        return ApiUtils.toHexPadded(entry);
    }

    /**
     * Returns a correctly formatted hex string, given an input byte array (usually 32 bytes).
     * Encoding varies depending on the solidity type being encoded.
     *
     * @return decoded Solidity data.
     */
    public Boolean decodeToSolidityType(byte[] data, int offset) {

        if (data == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[decodeToSolidityType] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        if (offset + encodeUnitLength > data.length) {
            return false;
        }

        byte[] dat = Arrays.copyOfRange(data, offset, offset + encodeUnitLength);

        return dat[dat.length - 1] == (byte) 0x01;
    }

    @Override
    protected boolean isDoubleUnit() {
        return false;
    }
}
