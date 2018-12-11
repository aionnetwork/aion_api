package org.aion.api.sol;

import java.util.List;
import javax.annotation.Nonnull;
import org.aion.api.sol.impl.Address;

/**
 * IAddress is an interface of the class Address that inherits from SolidityAbstractType. This is
 * used for processing account addresses, contract addresses and transaction addresses input /
 * output.
 *
 * @author Jay Tseng
 */
public interface IAddress extends ISolidityArg {

    /**
     * Generates an Address object from an ArrayList of hexidecimal strings, this structure should
     * match the list structure defined in the ABI and should consist only of hexidecimal strings,
     * or byte arrays.
     *
     * @param l {@link java.util.List} of {@link java.lang.String} or bytes array.
     * @return interface itself.
     */
    static IAddress copyFrom(@Nonnull List l) {
        return Address.copyFrom(l);
    }

    /**
     * Generates an Address object from a hexidecimal string.
     *
     * @param in {@link java.lang.String}.
     * @return interface itself.
     */
    static IAddress copyFrom(@Nonnull String in) {
        return Address.copyFrom(in);
    }

    /**
     * Generates an Address object from a byte array.
     *
     * @param in 32 bytes array.
     * @return interface itself.
     */
    static IAddress copyFrom(@Nonnull byte[] in) {
        return Address.copyFrom(in);
    }
}
