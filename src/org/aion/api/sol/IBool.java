package org.aion.api.sol;

import java.util.List;
import javax.annotation.Nonnull;
import org.aion.api.sol.impl.Bool;

/**
 * IBool is an interface of the class Bool inherited from SolidityAbstractType. Used for function
 * arguments input / output.
 *
 * @author Jay Tseng
 */
public interface IBool extends ISolidityArg {

    /**
     * Generates an Bool object from a Boolean value.
     *
     * @param in a boolean value.
     * @return interface itself.
     */
    static IBool copyFrom(boolean in) {
        return Bool.copyFrom(in);
    }

    /**
     * Generates an Bool object from an ArrayList of Boolean, this structure should match the list
     * structure defined in the ABI and must consist only of Boolean type.
     *
     * @param l {@link java.util.List} of boolean.
     * @return interface itself.
     */
    static IBool copyFrom(@Nonnull List l) {
        return Bool.copyFrom(l);
    }
}
