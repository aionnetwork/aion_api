package org.aion.api.type.core.tx;

import org.aion.aion_types.NewAddress;
import org.aion.api.log.AionLoggerFactory;
import org.aion.api.log.LogEnum;
import org.aion.base.type.ITransaction;
import org.aion.crypto.ISignature;
import org.aion.vm.api.interfaces.Address;
import org.slf4j.Logger;

/** @author jin */
public abstract class AbstractTransaction implements ITransaction {

    private static final int nrgDigits = 64;

    static final Logger LOG = AionLoggerFactory.getLogger(LogEnum.TRX.toString());

    /* SHA3 hash of the RLP encoded transaction */
    protected byte[] hash;

    /* the amount of ether to transfer (calculated as wei) */
    protected byte[] value;

    /* An unlimited size byte array specifying
     * input [data] of the message call or
     * Initialization code for a new contract */
    protected byte[] data;

    /* the address of the destination account
     * In creation transaction the receive address is - 0 */
    protected NewAddress to;

    /* a counter used to make sure each transaction can only be processed once */
    protected byte[] nonce;

    /* timeStamp is a 8-bytes array shown the time of the transaction signed by the kernel, the unit is nanosecond. */
    byte[] timeStamp;

    long nrg;

    protected long nrgPrice;

    /* define transaction type. */
    protected byte type;

    /* the elliptic curve signature
     * (including public key recovery bits) */
    ISignature signature;

    AbstractTransaction() {}

    private AbstractTransaction(byte[] nonce, NewAddress receiveAddress, byte[] value, byte[] data) {
        this.nonce = nonce;
        this.to = receiveAddress;
        this.value = value;
        this.data = data;
        // default type 0x01; reserve date for multi-type transaction
        this.type = 0x01;
    }

    AbstractTransaction(
            byte[] nonce,
            NewAddress receiveAddress,
            byte[] value,
            byte[] data,
            long nrg,
            long nrgPrice) {
        this(nonce, receiveAddress, value, data);
        this.nrg = nrg;
        this.nrgPrice = nrgPrice;
    }

    public AbstractTransaction(
            byte[] nonce,
            NewAddress receiveAddress,
            byte[] value,
            byte[] data,
            long nrg,
            long nrgPrice,
            byte type)
            throws Exception {
        this(nonce, receiveAddress, value, data, nrg, nrgPrice);

        if (type == 0x00) {
            throw new Exception("Incorrect tx type!");
        }

        this.type = type;
    }

    //
    //    private byte[] checkBI(byte[] _nrg, int _digits) {
    //        if (_nrg == null || _nrg.length == 0) {
    //            return ByteUtil.EMPTY_BYTE_ARRAY;
    //        } else {
    //            BigInteger bi = new BigInteger(_nrg);
    //            if (bi.signum() > 0 && bi.bitLength() <= _digits) {
    //                return _nrg;
    //            } else {
    //                return ByteUtil.EMPTY_BYTE_ARRAY;
    //            }
    //        }
    //    }

    public void setSignature(final ISignature signature) {
        this.signature = signature;
    }

    public abstract byte[] getEncoded();

    public abstract Address getSenderAddress();

    public abstract Address getDestinationAddress();

    public abstract byte[] getNonce();

    public abstract byte[] getTimestamp();

    public abstract Address getContractAddress();

    public abstract AbstractTransaction clone();

    public abstract long getNrgConsume();

    public abstract void setNrgConsume(long consume);

    public abstract byte getTargetVM();
}
