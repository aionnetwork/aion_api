package org.aion.api.sol.impl;

/** Created by Jay Tseng on 05/10/16. */
public final class SolidityValue {

    /** Internal enumeration used in {@link SolidityAbstractType} to determine the solidity type. */
    public enum SolidityTypeEnum {
        ADDRESS,
        BOOL,
        BYTES,
        DYNAMICBYTES,
        INT,
        REAL,
        STRING,
        UINT,
        UREAL
    }

    /** Internal enum used to determine the the status of the solidity type. */
    public enum SolidityArgsType {
        STATIC,
        DYNAMIC,
        DYNAMICARRAY,
        STATICARRAY
    }
}
