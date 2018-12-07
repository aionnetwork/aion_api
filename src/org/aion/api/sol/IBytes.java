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
