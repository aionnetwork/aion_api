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

import org.aion.base.util.ByteArrayWrapper;

import java.util.List;

/**
 * This interface dedicate to the solidity class method operation and the type casting
 *
 * @author Jay Tseng
 * @see org.aion.api.IContract#getInputParams() getInputParams
 * @see org.aion.api.IContract#getOutputParams() getOutputParams
 * @see org.aion.api.IContractController#createFromSource(java.lang.String,
 * org.aion.base.type.Address, long, long, java.util.List) createFromSource
 * @see org.aion.api.IContractController#createFromSource(java.lang.String,
 * org.aion.base.type.Address, long, long, java.math.BigInteger, java.util.Map) createFromSource
 */

public interface ISolidityArg {

    // These methods is for internal use. the develop should not operate these methods.

    void setDynamicParameters(List<Integer> paramLengths);

    void setType(String name);

    int getStaticPartLength();

    boolean getIsDynamic();

    int getDynamicOffset();

    String getInputFormat();

    boolean isType(String in);

    Object decode(int offset, ByteArrayWrapper data);
}
