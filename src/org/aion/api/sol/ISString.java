package org.aion.api.sol;

import org.aion.api.sol.impl.SString;

/**
 * ISString is an interface of the class SString inherited from the class SolidityAbstractType. Use
 * for function arguments input / output.
 *
 * @author Jay Tseng
 */
public interface ISString extends ISolidityArg {

    /**
     * Generates an ISString object from a String.
     *
     * @param in {@link java.lang.String}.
     * @return interface itself.
     */
    static ISString copyFrom(String in) {
        return SString.copyFrom(in);
    }
}
