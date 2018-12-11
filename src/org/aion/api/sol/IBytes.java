package org.aion.api.sol;

import java.util.List;
import javax.annotation.Nonnull;
import org.aion.api.sol.impl.Bytes;

/**
 * IBytes is an interface of the class Bytes inherited from the class SolidityAbstractType. Use for
 * function arguments input / output.
 *
 * @author Jay Tseng
 */
public interface IBytes extends ISolidityArg {

    /**
     * Generates an Bytes object from a Bytes array.
     *
     * @param in a bytes array.
     * @return interface itself.
     */
    static IBytes copyFrom(@Nonnull byte[] in) {
        return Bytes.copyFrom(in);
    }

    /**
     * Generates an Bytes object from an ArrayList, String or byte array, this structure should
     * match the list structure defined in the ABI and consist only of Bytes.
     *
     * @param l {@link java.util.List} of bytes array.
     * @return interface itself.
     */
    static IBytes copyFrom(@Nonnull List l) {
        return Bytes.copyFrom(l);
    }
}
