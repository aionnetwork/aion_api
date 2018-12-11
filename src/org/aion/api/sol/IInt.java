package org.aion.api.sol;

import java.util.List;
import org.aion.api.sol.impl.Int;

/**
 * IInt is an interface of the class Int inherited from the class SolidityAbstractType. Contains
 * functions for encoding String, decoding String and datatype checking. Most functions used are not
 * intended to be user facing, and should be left unused by the user.
 *
 * @author Jay Tseng
 */
public interface IInt extends ISolidityArg {

    /**
     * Generates an Integer object from a String object.
     *
     * @param in {@link java.lang.String}.
     * @return the class {@link Int Int}.
     */
    static IInt copyFrom(String in) {
        return Int.copyFrom(in);
    }

    /**
     * Generates an Integer object from a Integer object.
     *
     * @param in {@link java.lang.Integer}
     * @return interface itself.
     */
    static IInt copyFrom(Integer in) {
        return Int.copyFrom(in);
    }

    /**
     * Generates an Int object from a Long object.
     *
     * @param in {@link java.lang.Long}
     * @return interface itself.
     */
    static IInt copyFrom(Long in) {
        return Int.copyFrom(in);
    }

    /**
     * Generates an Int object from an ArrayList, String or byte array, this structure should match
     * the list structure defined in the ABI and consist only of Bytes.
     *
     * @param l {@link java.util.List} of {@link java.lang.String}, {@link java.lang.Integer} or
     *     {@link java.lang.Long}.
     * @return interface itself.
     */
    static IInt copyFrom(List l) {
        return Int.copyFrom(l);
    }
}
