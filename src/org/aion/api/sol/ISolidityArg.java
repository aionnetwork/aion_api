package org.aion.api.sol;

import java.util.List;
import org.aion.base.util.ByteArrayWrapper;

/**
 * This interface dedicate to the solidity class method operation and the type casting
 *
 * @author Jay Tseng
 * @see org.aion.api.IContract#getInputParams() getInputParams
 * @see org.aion.api.IContract#getOutputParams() getOutputParams
 * @see org.aion.api.IContractController#createFromSource(java.lang.String,
 *     org.aion.base.type.Address, long, long, java.util.List) createFromSource
 * @see org.aion.api.IContractController#createFromSource(java.lang.String,
 *     org.aion.base.type.Address, long, long, java.math.BigInteger, java.util.Map) createFromSource
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
