/*
 * Copyright (c) 2017-2018 Aion foundation.
 *
 *     This file is part of the aion network project.
 *
 *     The aion network project is free software: you can redistribute it
 *     and/or modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation, either version 3 of
 *     the License, or any later version.
 *
 *     The aion network project is distributed in the hope that it will
 *     be useful, but WITHOUT ANY WARRANTY; without even the implied
 *     warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *     See the GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the aion network project source files.
 *     If not, see <https://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Aion foundation.
 */

package org.aion.api.sol;

import java.math.BigInteger;
import java.util.List;
import javax.annotation.Nonnull;
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
    static IUint copyFrom(@Nonnull String in) {
        return Uint.copyFrom(in);
    }

    /**
     * Generates an Integer object from a Integer object.
     *
     * @param in int value
     * @return interface itself.
     */
    static IUint copyFrom(int in) {
        return Uint.copyFrom(in);
    }

    /**
     * Generates an Uint object from a Long object.
     *
     * @param in long value
     * @return interface itself.
     */
    static IUint copyFrom(long in) {
        return Uint.copyFrom(in);
    }

    /**
     * Generates an Uint object from a Long object.
     *
     * @param in {@link java.math.BigInteger}
     * @return interface itself.
     */
    static IUint copyFrom(@Nonnull BigInteger in) {
        return Uint.copyFrom(in);
    }

    /**
     * Generates an Int object from an ArrayList, String or byte array, this structure should match
     * the list structure defined in the ABI and consist only of Bytes.
     *
     * @param l {@link java.util.List} of {@link java.lang.String}, int, long, or {@link
     *     java.math.BigInteger}.
     * @return interface itself.
     */
    static IUint copyFrom(@Nonnull List l) {
        return Uint.copyFrom(l);
    }
}
