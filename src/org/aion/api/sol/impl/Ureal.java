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

import org.aion.api.impl.ErrId;
import org.aion.api.impl.internal.ApiUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by yao on 20/09/16.
 */
public final class Ureal extends SolidityAbstractType {

    private Ureal(List in) {
        super();
        if (in != null) {
            this.valArray = in;
        }
    }

    private Ureal() {
        super();
    }

    public static Ureal copyFrom(Double input) {
        if (input == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[copyFrom] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        List<byte[]> inputArray = new ArrayList<>();
        inputArray.add(formatInputReal(input));
        return new Ureal(inputArray);
    }

    private static byte[] formatInputReal(double input) {
        double mult = input * (Math.pow(2, 128));

        //TODO: Verify precision
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(mult);

        return bytes;
    }

    public static Ureal copyFrom(ArrayList l) {
        // at this point we don't know about type yet
        // assume first variable is the correct Type
        if (l == null || l.size() == 0) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[copyFrom] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        List inputArrayList = copyFromHelper(l);
        return new Ureal(inputArrayList);
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
            if (entry instanceof Double) {
                localArrayList.add(formatInputReal((Double) entry));
            } else if (entry instanceof ArrayList) {
                localArrayList.add(copyFromHelper((ArrayList) entry));
            } else {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[copyFromHelper] {}", ErrId.getErrString(-124L));
                }
                return null;
            }
        }
        return localArrayList;
    }

    public static Ureal createForDecode() {
        return new Ureal();
    }

    public boolean isType(String in) {
        if (in == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[isType] {}", ErrId.getErrString(-315L));
            }
            return false;
        }
        return Pattern.matches("ureal([0-9]*)?(\\[([0-9]*)\\])?", in);
    }

    public String formatToString(byte[] entry) {
        if (entry == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[formatToString] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        return ApiUtils.toHexPadded16(entry);
    }

    public byte[] decodeToSolidityType(byte[] data, int offset) {
        if (data == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[decodeToSolidityType] {}", ErrId.getErrString(-315L));
            }
            return null;
        }
        return Arrays.copyOfRange(data, offset, offset + encodeUnitLength);
    }

    @Override
    protected boolean isDoubleUnit() {
        return false;
    }
}
