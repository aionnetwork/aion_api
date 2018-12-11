package org.aion.api;

import java.util.List;
import java.util.Map;
import org.aion.api.impl.Account;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.Key;

/**
 * This interface dedicate to account manipulations.
 *
 * @author Jay Tseng
 */
public interface IAccount {

    /**
     * Create new accounts by given password.
     *
     * @param passphrase the interface {@link java.util.List List} of the class {@link
     *     java.lang.String String} represent the passphrase choose for each new generate account.
     * @param privateKey The boolean value represent the key return including the private key or
     *     just the account address.
     * @return the interface {@link java.util.List List} of the class {@link org.aion.api.type.Key
     *     Key} wrapped into ApiMsg. You can retrieve through the method {@link ApiMsg#getObject()
     *     getObject}.
     *     <p>The kernel will only return the first 100 new Keys if user put more than 100
     *     passphrases.
     */
    ApiMsg accountCreate(List<String> passphrase, boolean privateKey);

    /**
     * Export accounts by given the password and the account address
     *
     * @param keys the interface {@link java.util.List List} of the class {@link
     *     org.aion.api.type.Key Key} object represent the passphrase and the account public
     *     address.
     * @return the class {@link org.aion.api.type.KeyExport KeyExport} include the 64 bytes private
     *     key as a List of ByteArrayWrapper and invalid address list wrapped into ApiMsg. You can
     *     retrieve through the method {@link ApiMsg#getObject() getObject}.
     *     <p>The kernel will only return the first 100 Keys if user try to export more then 100
     *     account.
     */
    ApiMsg accountExport(List<Key> keys);

    /**
     * Export accounts by given the password and the account address
     *
     * @param keys the interface {@link java.util.List List} of the class {@link
     *     org.aion.api.type.Key Key} represent the passphrase and the account public address.
     * @return {@link org.aion.api.type.KeyExport KeyExport} include the keyfiles as a List of
     *     ByteArrayWrapper and invalid address list wrapped into ApiMsg. You can retrieve through
     *     the method {@link ApiMsg#getObject() getObject}.
     *     <p>The kernel will only return the first 100 Keys if user try to export more then 100
     *     accounts.
     */
    ApiMsg accountBackup(List<Key> keys);

    /**
     * Import accounts by given the password and the account address
     *
     * @param keys the interface {@link java.util.Map Map} of the class {@link java.lang.String
     *     String} object represent the passphrase and the account private key as a string.
     * @return the interface {@link java.util.List List} of the class {@link java.lang.String
     *     String} represent the invalid account address. You can retrieve through the method {@link
     *     ApiMsg#getObject() getObject}.
     *     <p>The kernel will only accept the first 100 Keys import. The the user try to import
     *     account more than 100. The import keys after 100 will be returned as the invalid account
     *     addresses.
     */
    ApiMsg accountImport(Map<String, String> keys);

    static ApiMsg keystoreCreateLocal(List<String> passphrase) {
        return Account.keystoreCreateLocal(passphrase);
    }
}
