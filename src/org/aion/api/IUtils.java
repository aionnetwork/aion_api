package org.aion.api;

import org.aion.api.impl.ErrId;
import org.aion.api.impl.internal.ApiUtils;

/**
 * IUtils is an interface class that provides common utility methods.
 *
 * @author Jay Tseng
 */
public interface IUtils {

    /**
     * It is a help function to convert a byte array to it's hex representation as string. Returns
     * hex string given a valid byte array.
     *
     * @param bytes the variable bytes array.
     * @return the class {@link java.lang.String String}.
     */
    static String bytes2Hex(byte[] bytes) {
        if (bytes == null) {
            System.err.println(
                    new Throwable().getStackTrace()[0].getMethodName() + ErrId.getErrString(-313L));
            return null;
        }

        if (bytes.length == 1) {
            switch (bytes[0]) {
                case (byte) 0x1:
                    return "1";
                case (byte) 0x2:
                    return "2";
                case (byte) 0x3:
                    return "3";
                case (byte) 0x4:
                    return "4";
                case (byte) 0x5:
                    return "5";
                case (byte) 0x6:
                    return "6";
                case (byte) 0x7:
                    return "7";
                case (byte) 0x8:
                    return "8";
                case (byte) 0x9:
                    return "9";
                case (byte) 0xA:
                    return "A";
                case (byte) 0xB:
                    return "B";
                case (byte) 0xC:
                    return "C";
                case (byte) 0xD:
                    return "D";
                case (byte) 0xE:
                    return "E";
                case (byte) 0xF:
                    return "F";
                default:
                    break;
            }
        }

        char[] hexArray = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
        };
        char[] hexChars = new char[bytes.length << 1];

        int v;
        for (int i = 0; i < bytes.length; i++) {
            v = bytes[i] & 0xFF;
            hexChars[i << 1] = hexArray[v >>> 4];
            hexChars[(i << 1) + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Returns the byte representation of a hex string, note that this function translates the hex
     * into byte, not a direct getBytes() conversion.
     *
     * @param hexstr the class {@link java.lang.String String} of the bytes.
     * @return the variable bytes array represented of the hex string.
     */
    static byte[] hex2Bytes(String hexstr) {
        if (hexstr == null) {
            System.err.println(
                    new Throwable().getStackTrace()[0].getMethodName() + ErrId.getErrString(-314L));
            return null;
        }

        int len = hexstr.length();
        byte[] out_arr = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            out_arr[i / 2] =
                    (byte)
                            ((Character.digit(hexstr.charAt(i), 16) << 4)
                                    + Character.digit(hexstr.charAt(i + 1), 16));
        }
        return out_arr;
    }

    /**
     * Check the transaction hash is a valid length byte[].
     *
     * @param status the int value represent the transaction status is the end status.
     * @return the boolean value.
     */
    static boolean endTxStatus(int status) {
        return ApiUtils.endTxStatus(status);
    }

    /**
     * Returns the bytes array represent the sha3 hashing result giving input
     *
     * @param in the bytes array want to do sha3 hashing.
     * @return the result of the bytes array after sha3.
     */
    static byte[] sha3(byte[] in) {
        return ApiUtils.keccak(in);
    }
}
