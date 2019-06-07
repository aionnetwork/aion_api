package org.aion.api.sol.impl;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.aion.api.impl.ErrId;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.sol.IUint;

/** Class Uint extends from SolidityAbstractType. use for function arguments input/output. */
public class Uint extends SolidityAbstractType implements IUint {

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
     * @param in {@link String}
     * @return Uint object.
     */
    public static Uint copyFrom(@Nonnull String in) {
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
        l.add(new BigInteger(1, bytes).toByteArray());
        return new Uint(l);
    }

    /**
     * Generates an Uint object from a Integer.
     *
     * @param in
     * @return Uint object.
     */
    public static Uint copyFrom(int in) {
        List<Object> l = new ArrayList<>();
        l.add(formatInputUint(in));

        return new Uint(l);
    }

    /** for contract internal encode/decode. */
    private static byte[] formatInputUint(@Nonnull Integer input) {
        return ApiUtils.toTwosComplement(input);
    }

    /**
     * Generates an Uint object from a Long.
     *
     * @param in long value
     * @return Uint object.
     */
    public static Uint copyFrom(long in) {
        List<Object> l = new ArrayList<>();
        l.add(formatInputUint(in));

        return new Uint(l);
    }

    /**
     * Generates an Uint object from a BigInteger.
     *
     * @param in
     * @return Uint object.
     */
    public static Uint copyFrom(@Nonnull BigInteger in) {
        if (in.signum() == -1 || in.bitLength() > 128) {
            throw new IllegalArgumentException();
        }

        List<Object> l = new ArrayList<>();
        l.add(formatInputUint(in));

        return new Uint(l);
    }

    /** for contract internal encode/decode. */
    private static byte[] formatInputUint(long input) {
        return ApiUtils.toTwosComplement(input);
    }

    private static byte[] formatInputUint(BigInteger input) {
        byte[] b = input.toByteArray();
        return ByteBuffer.allocate(16).put(b).array();
    }

    /**
     * Generates an Uint object from an ArrayList, String or byte array, this structure should match
     * the list structure defined in the ABI and consist only of Bytes.
     *
     * @param l
     * @return Uint object.
     */
    public static Uint copyFrom(@Nonnull List l) {
        // at this point we don't know about type yet
        // assume first variable is the correct Type
        if (l.isEmpty()) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[copyFrom] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        List inputArrayList = copyFromHelper(l);
        return new Uint(inputArrayList);
    }

    /** for contract internal encode/decode. */
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

        List localArrayList = new ArrayList();

        for (Object entry : l) {
            if (entry instanceof Integer) {
                localArrayList.add(formatInputUint((int) entry));
            } else if (entry instanceof String) {
                localArrayList.add(formatInputUint((String) entry));
            } else if (entry instanceof Long) {
                localArrayList.add(formatInputUint((long) entry));
            } else if (entry instanceof BigInteger) {
                localArrayList.add(formatInputUint((BigInteger) entry));
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
        return Pattern.matches("^uint([0-9]*)?(\\[([0-9]*)])*$", in);
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
     * @param data
     * @param offset
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
