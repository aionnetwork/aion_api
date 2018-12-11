package org.aion.api.impl;

import org.aion.api.IUtils;

/** Created by Jay Tseng on 14/11/16. */
public class Utils implements IUtils {

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
