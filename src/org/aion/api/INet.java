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

package org.aion.api;

import org.aion.api.type.ApiMsg;

/**
 * This interface provides methods for fetching the Aion blockchain network status and the specific information.
 * @author Jay Tseng
 */

public interface INet {

    /**
     * Check connecting node syncing detailed information.
     *
     * @return the class {@link org.aion.api.type.SyncInfo } containing all relevant information wrapped into ApiMsg.
     * You can retrieve through this method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg syncInfo();

    /**
     * Check connected node's sync status.
     *
     * @return the boolean value wrapped into ApiMsg.
     * You can retrieve through this method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg isSyncing();

    /**
     * Returns the current version of the Aion modules.
     *
     * @return the class {@link org.aion.api.type.Protocol Protocol} wrapped into ApiMsg.
     * You can retrieve through this method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg getProtocolVersion();

    /**
     * Returns the current active connected nodes.
     *
     * @return the interface {@link java.util.List } of the class {@link org.aion.api.type.Node} wrapped into ApiMsg.
     * You can retrieve through this method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg getActiveNodes();

    /**
     * Returns the whole consensus network nodes list.
     *
     * @return the interface {@link java.util.List } of the class {@link java.lang.String } wrapped into ApiMsg.
     * You can retrieve through this method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg getStaticNodes();

}
