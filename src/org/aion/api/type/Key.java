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

package org.aion.api.type;

import org.aion.base.type.Address;
import org.aion.base.util.ByteArrayWrapper;

/**
 * The key class wrapped the user account information.
 *
 * @author Jay Tseng
 * @see org.aion.api.IAccount#accountBackup(java.util.List) accountBackup
 * @see org.aion.api.IAccount#accountExport(java.util.List) accountExport
 * @see org.aion.api.IAccount#accountCreate(java.util.List, boolean) accountCreate
 */

public final class Key {

    // 32 bytes array
    private final Address publicKey;
    // 64 bytes array
    private final ByteArrayWrapper privateKey;
    private final String passPhrase;

    public Key(final Address s, final ByteArrayWrapper k) {
        if (s == null || k == null) {
            throw new NullPointerException();
        }

        this.publicKey = s;
        this.privateKey = k;
        this.passPhrase = null;
    }

    public Key(final Address s, final String pw) {
        if (s == null || pw == null) {
            throw new NullPointerException();
        }

        this.publicKey = s;
        this.passPhrase = pw;
        this.privateKey = null;
    }

    public Address getPubKey() {
        return publicKey;
    }

    public ByteArrayWrapper getPriKey() {
        return privateKey;
    }

    public String getPassPhrase() {
        return passPhrase;
    }
}
