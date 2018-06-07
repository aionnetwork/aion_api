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

package org.aion.api;

import org.aion.api.type.ApiMsg;
import org.aion.base.type.Address;

/**
 * This interface is an interface for the class Wallet. Assists in account related interactions.
 *
 * @author Jay Tseng
 */

public interface IWallet {

    /**
     * Retrieves a list of accounts currently available on the local node.
     *
     * @return the interface {@link java.util.List List} of the class {@link Address Address}
     * represent the public address of the return accounts wrapped into ApiMsg. You can retrieve
     * through the method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg getAccounts();

    /**
     * Unlocks the desired account in 60 seconds given the correct passphrase,
     *
     * @param acc the class {@link Address Address} represent the account address.
     * @param passphrase the class {@link java.lang.String String} represent the passphrase of the
     * account.
     * @return a Boolean indicating the success of the unlock wrapped into ApiMsg. You can retrieve
     * through this method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg unlockAccount(Address acc, String passphrase);

    /**
     * Unlocks the desired account, given the correct passphrase, the user also sets the duration
     * (milliseconds) in which the account stays unlocked.
     *
     * @param acc the class {@link Address Address} represent the account address.
     * @param passphrase the class {@link java.lang.String String} represent the passphrase of the
     * account.
     * @param duration the duration by int value that account stays unlocked (seconds). If the
     * duration set more then 86400 (1 day), if the kernel will unlock the given account 1 day.
     * @return returns a Boolean indicating the success of the unlock wrapped into ApiMsg. You can
     * retrieve through the method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg unlockAccount(Address acc, String passphrase, int duration);

    /**
     * Returns the default miner account of the backend. miner account refers to the address
     * utilized when isMining.
     *
     * @return the class {@link Address Address} represent the miner's account address of the
     * connected Aion kernel wrapped into ApiMsg. You can retrieve through the method {@link
     * ApiMsg#getObject() getObject}.
     */
    ApiMsg getMinerAccount();

    /**
     * Set a defaultAccount in the client api instance.
     *
     * @param acc the class {@link Address Address} represent the account address.
     * @return a Boolean value indicating the success of account set wrapped into ApiMsg. You can
     * retrieve through {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg setDefaultAccount(Address acc);

    /**
     * Returns the default account of the user latest set.
     *
     * @return the class {@link Address Address} represent the default account of the user latest
     * set wrapped into ApiMsg. You can retrieve through the method {@link ApiMsg#getObject()
     * getObject}.
     */
    ApiMsg getDefaultAccount();

    /**
     * lock the desired account given the correct passphrase,
     *
     * @param acc the class {@link Address Address} represent the account address.
     * @param passphrase the class {@link java.lang.String String} represent the passphrase of the
     * account.
     * @return a Boolean indicating the success of the unlock wrapped into ApiMsg. You can retrieve
     * through this method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg lockAccount(Address acc, String passphrase);
}
