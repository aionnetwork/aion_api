package org.aion.api.sol.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.aion.api.impl.ErrId;
import org.aion.api.impl.internal.ApiUtils;

/** Created by yao on 20/09/16. */
public final class Real extends SolidityAbstractType {

    private Real(List in) {
        super();
        if (in != null) {
            this.valArray = in;
        }
    }

    private Real() {
        super();
    }

    public static Real copyFrom(Double input) {
        if (input == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[copyFrom] {}", ErrId.getErrString(-315L));
            }
            return null;
        }
        List<byte[]> inputArray = new ArrayList<>();
        inputArray.add(formatInputReal(input));
        return new Real(inputArray);
    }

    private static byte[] formatInputReal(double input) {
        double mult = input * (Math.pow(2, 128));

        // TODO: Verify precision
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(mult);

        return bytes;
    }

    public static Real copyFrom(ArrayList l) {
        if (l == null || l.size() == 0) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[copyFrom] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        List inputArrayList = copyFromHelper(l);
        return new Real(inputArrayList);
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
                    LOGGER.error("[copyFromHelper] {}", ErrId.getErrString(-120L));
                }
                return null;
            }
        }
        return localArrayList;
    }

    public static Real createForDecode() {
        return new Real();
    }

    public boolean isType(String in) {
        if (in == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[isType] {}", ErrId.getErrString(-315L));
            }
            return false;
        }
        return Pattern.matches("real([0-9]*)?(\\[([0-9]*)\\])?", in);
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
