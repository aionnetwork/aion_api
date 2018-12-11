package org.aion.api.sol.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.aion.api.IUtils;
import org.aion.api.impl.ErrId;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.sol.IAddress;

/**
 * Class Address extends from SolidityAbstractType. use for precessing account address, contract
 * address and transaction address input/output.
 */
public final class Address extends SolidityAbstractType implements IAddress {

    // use static functions instead
    private Address(List in) {
        super();
        if (in != null) {
            this.valArray = in;
        }
    }

    private Address() {
        super();
    }

    /**
     * Generates an Address object from an ArrayList of hexidecimal strings, this structure should
     * match the list structure defined in the ABI and consist only of hexidecimal strings, or byte
     * arrays.
     *
     * @param l {@link java.util.List List} of {@link java.lang.String String} or bytes array.
     * @return {@link Address Address}
     */
    public static Address copyFrom(List l) {
        // at this point we don't know about type yet
        // assume first variable is the correct Type
        if (l == null || l.size() == 0) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[copyFrom] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        List inputArrayList = copyFromHelper(l);
        return new Address(inputArrayList);
    }

    /**
     * Generates an Address object from a hexidecimal string.
     *
     * @param in {@link java.lang.String String}.
     * @return {@link Address Address}
     * @throws Exception if hex string contains invalid characters, or is not 32 bytes (64
     *     characters) in length.
     */
    public static Address copyFrom(String in) {

        if (in == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[copyFrom] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        List<byte[]> inList = new ArrayList<>();
        inList.add(formatInputAddress(in));

        return new Address(inList);
    }

    /**
     * Generates a 20 bytes array from a string input, this function will convert a hexidecimal
     * string to a bytes array.
     *
     * @param input {@link java.lang.String String}.
     * @return 20 bytes array.
     */
    private static byte[] formatInputAddress(String input) {

        if (input == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[formatInputAddress] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        String inputString = input;

        if (inputString.length() == 1) {
            inputString = "0" + inputString;
        }

        return IUtils.hex2Bytes(inputString);
    }

    /**
     * Generates an Address object from a byte array.
     *
     * @param in 32 bytes array.
     * @return {@link Address}
     */
    public static Address copyFrom(byte[] in) {

        ArrayList<byte[]> inList = new ArrayList<>();
        inList.add(in);

        return new Address(inList);
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
            if (entry instanceof String) {
                localArrayList.add(formatInputAddress((String) entry));
            } else if (entry instanceof ArrayList) {
                localArrayList.add(copyFromHelper((ArrayList) entry));
            } else if (entry instanceof byte[]) {
                localArrayList.add(entry);
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
     * Instantiates an empty Address object for decoding purposes, not user facing.
     *
     * @return {@link Address Address}
     */
    public static Address createForDecode() {
        return new Address();
    }

    /**
     * Checks that inputted string is the correct type. To be used with ABI.
     *
     * @param in Solidity Type.
     * @return returns a boolean indicating the type is Address.
     */
    public boolean isType(String in) {
        if (in == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[isType] {}", ErrId.getErrString(-315L));
            }

            return false;
        }

        return Pattern.matches("address(\\[([0-9]*)])?", in);
    }

    /**
     * @param entry the data represent to a variable bytes array need to be formatted.
     * @return {@link java.lang.String String } formatted string for encode.
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
     * For Contract decodes the response data from server, not user facing.
     *
     * @param data
     * @param offset
     * @return decoded Solidity data.
     */
    public byte[] decodeToSolidityType(byte[] data, int offset) {

        if (data == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[decodeToSolidityType] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        if (offset + encodeUnitLengthDouble > data.length) {
            return new byte[encodeUnitLengthDouble];
        }

        return Arrays.copyOfRange(data, offset, offset + encodeUnitLengthDouble);
    }

    @Override
    protected boolean isDoubleUnit() {
        return true;
    }
}
