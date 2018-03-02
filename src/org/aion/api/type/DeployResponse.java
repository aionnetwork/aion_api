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

package org.aion.api.type;

import org.aion.base.type.Address;
import org.aion.base.type.Hash256;

/**
 * Contains the response information after deployed a contract by
 * {@link org.aion.api.ITx#contractDeploy(org.aion.api.type.ContractDeploy) contractDeploy}
 * The response includes the deployed contract address and the transactionID in this execution.
 *
 * @author Jay Tseng
 */

public final class DeployResponse {
    private final Address address;
    private final Hash256 txid;

    public DeployResponse(Address address, Hash256 txid) {
        this.address = address;
        this.txid = txid;
    }

    public Address getAddress() {
        return address;
    }

    public Hash256 getTxid() {
        return txid;
    }
}