package org.aion.api.sol;

import java.util.List;
import org.aion.api.sol.impl.Uint;

/**
 * IUint is an interface of the class Uint inherited from the class SolidityAbstractType. Contains
 * functions for encoding String, decoding String, datatype checking, Most functions used are not
 * intended to be user facing and should be left unused by the user.
 *
 * @author Jay Tseng
 */
public interface IUint extends ISolidityArg {

    /**
     * Generates an Integer object from a String object.
     *
     * @param in {@link java.lang.String}.
     * @return interface itself.
     */
    static IUint copyFrom(String in) {
        return Uint.copyFrom(in);
    }

    /**
     * Generates an Integer object from a Integer object.
     *
     * @param in {@link java.lang.Integer}
     * @return interface itself.
     */
    static IUint copyFrom(Integer in) {
        return Uint.copyFrom(in);
    }

    /**
     * Generates an Uint object from a Long object.
     *
     * @param in {@link java.lang.Long}
     * @return interface itself.
     */
    static IUint copyFrom(Long in) {
        return Uint.copyFrom(in);
    }

    /**
     * Generates an Int object from an ArrayList, String or byte array, this structure should match
     * the list structure defined in the ABI and consist only of Bytes.
     *
     * @param l {@link java.util.List} of {@link java.lang.String}, {@link java.lang.Integer} or
     *     {@link java.lang.Long}.
     * @return interface itself.
     */
    static IUint copyFrom(List l) {
        return Uint.copyFrom(l);
    }
}
