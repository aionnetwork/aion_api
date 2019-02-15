package org.aion.api.type;

import java.util.List;
import org.aion.util.bytes.ByteArrayWrapper;
import org.aion.type.api.interfaces.common.Address;

/**
 * The KeyExport class wrapped the keystore information
 *
 * <p>param keyFiles When execute the accountBackup, the keyFiles will be the whole keystore file
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
