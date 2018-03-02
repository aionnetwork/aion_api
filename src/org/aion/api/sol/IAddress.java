/*******************************************************************************
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
 *
 ******************************************************************************/

package org.aion.api.sol;

import org.aion.api.sol.impl.Address;

import java.util.List;

/**
 * IAddress is an interface of the class Address that inherits from SolidityAbstractType.
 * This is used for processing account addresses, contract addresses and transaction addresses input / output.
 *
 * @author Jay Tseng
 */

public interface IAddress extends ISolidityArg{
    /**
     * Generates an Address object from an ArrayList of hexidecimal strings,
     * this structure should match the list structure defined in the ABI and
     * should consist only of hexidecimal strings, or byte arrays.
     *
     * @param l
     *         {@link java.util.List} of {@link java.lang.String} or bytes array.
     * @return interface itself.
     */
    static IAddress copyFrom(List l) { return Address.copyFrom(l); }

    /**
     * Generates an Address object from a hexidecimal string.
     *
     * @param in
     *         {@link java.lang.String}.
     * @return interface itself.
     */
    static IAddress copyFrom(String in) { return Address.copyFrom(in); }

    /**
     * Generates an Address object from a byte array.
     *
     * @param in
     *         32 bytes array.
     * @return interface itself.
     */
    static IAddress copyFrom(byte[] in) { return Address.copyFrom(in);}
}
