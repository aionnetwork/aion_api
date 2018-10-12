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

import java.util.List;
import org.aion.base.type.Address;
import org.aion.base.util.ByteArrayWrapper;

/**
 * The KeyExport class wrapped the keystore information
 *
 * param keyFiles When execute the accountBackup, the keyFiles will be the whole keystore file
 * wrapped as a byte array, otherwise the keyFiles will be the 64-bytes private key.
 *
 * @author Jay Tseng
 * @see org.aion.api.IAccount#accountBackup(java.util.List) accountBackup
 * @see org.aion.api.IAccount#accountExport(java.util.List) accountExport
 */

public final class KeyExport {

    private final List<ByteArrayWrapper> keyFiles;
    private final List<Address> invalidAddress;

    public KeyExport(final List<ByteArrayWrapper> keyfiles, final List<Address> invalidAddr) {
        if (keyfiles == null || invalidAddr == null) {
            throw new NullPointerException();
        }
        this.keyFiles = keyfiles;
        this.invalidAddress = invalidAddr;
    }

    public List<ByteArrayWrapper> getKeyFiles() {
        return this.keyFiles;
    }

    public List<Address> getInvalidAddress() {
        return this.invalidAddress;
    }
}

