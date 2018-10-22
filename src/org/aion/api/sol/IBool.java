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
    static IBool copyFrom(List l) {
        return Bool.copyFrom(l);
    }
}
