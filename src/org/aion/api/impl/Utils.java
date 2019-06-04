package org.aion.api.impl;

import org.aion.aion_types.NewAddress;
import org.aion.api.IUtils;

/** Created by Jay Tseng on 14/11/16. */
public class Utils implements IUtils {

    public static NewAddress ZERO_ADDRESS() {
        return new NewAddress(new byte[32]);
    }

    public static NewAddress wrapAddress(String addressString) {
        if (addressString == null) {
            throw new IllegalArgumentException();
        } else {
            byte[] hexByte = IUtils.hex2Bytes(addressString);
            return new NewAddress(hexByte);
        }
    }

    // please call IUtils 's static method directly.
    @Deprecated
    public static String bytes2Hex(byte[] bytes) {
        return IUtils.bytes2Hex(bytes);
    }

    @Deprecated
    public byte[] hex2Bytes(String hexstr) {
        return IUtils.hex2Bytes(hexstr);
    }
}
