package org.aion.api.sol;

import org.aion.api.sol.impl.DynamicBytes;

/**
 * IDynamicBytes is an interface of the class DynamicBytes inherited from the class
 * SolidityAbstractType. Use for function arguments input / output.
 *
 * @author Jay Tseng
 */
public interface IDynamicBytes extends ISolidityArg {

    /**
     * Generates an DynamicBytes object from a Bytes array.
     *
     * @param in a variable bytes array.
     * @return interface itself.
     */
    static IDynamicBytes copyFrom(byte[] in) {
        return DynamicBytes.copyFrom(in);
    }
}
