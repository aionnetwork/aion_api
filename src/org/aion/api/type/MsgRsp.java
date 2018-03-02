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

import org.aion.base.type.Hash256;
import org.aion.base.util.ByteArrayWrapper;

/**
 * Represents the transaction status including the execution status and the API session hash.
 *
 * <pre>
 * param preStatus
 *      the transaction previous execute status.
 * param status
 *      the transaction execute status.
 * param msgHash
 *      it's for the internal Java API use to recognize this class is belong to which transaction API operation.
 * param txHash
 *      the class {@link Hash256 Hash256} represent to the 32bytes transaction ID
 * param txResult
 *      the class {@link ByteArrayWrapper ByteArrayWrapper} represent the transaction result.
 * param txDeploy
 *      the class {@link ByteArrayWrapper ByteArrayWrapper} represent the deploy result if it is a contract deploy.
 *
 * @see org.aion.api.ITx#sendTransaction(org.aion.api.type.TxArgs)
 * @see org.aion.api.ITx#sendRawTransaction(org.aion.base.util.ByteArrayWrapper)
 * @see org.aion.api.ITx#sendSignedTransaction(org.aion.api.type.TxArgs, org.aion.base.util.ByteArrayWrapper, java.lang.String)
 *
 * </pre>
 * @author Jay Tseng
 */

public class MsgRsp {
    private byte preStatus;
    private byte status;
    private ByteArrayWrapper msgHash;
    private Hash256 txHash;
    private ByteArrayWrapper txResult;
    private ByteArrayWrapper txDeploy;

    private MsgRsp() {}

    public MsgRsp(int rValue, ByteArrayWrapper hash) {
        this.setPreStatus((byte) 106);
        this.setStatus((byte) rValue);
        this.setMsgHash(hash);
        this.setTxHash(Hash256.ZERO_HASH());
        this.setTxResult(ByteArrayWrapper.wrap(ByteArrayWrapper.NULL_BYTE));
        this.setTxDeploy(ByteArrayWrapper.wrap(ByteArrayWrapper.NULL_BYTE));
    }

    public static MsgRsp copy(MsgRsp in) throws CloneNotSupportedException {
        MsgRsp newMsg = new MsgRsp();
        newMsg.setPreStatus(in.getPreStatus());
        newMsg.setStatus(in.getStatus());
        newMsg.setMsgHash(ByteArrayWrapper.wrap(in.getMsgHash().toBytes().clone()));
        newMsg.setTxHash(in.getTxHash().clone());
        newMsg.setTxResult(ByteArrayWrapper.wrap(in.getTxResult().toBytes().clone()));
        newMsg.setTxDeploy(ByteArrayWrapper.wrap(in.getTxDeploy().toBytes().clone()));
        return newMsg;
    }

    private byte getPreStatus() {
        return preStatus;
    }

    public void setPreStatus(byte preStatus) {
        this.preStatus = preStatus;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public ByteArrayWrapper getMsgHash() {
        return msgHash;
    }

    public void setMsgHash(ByteArrayWrapper msgHash) {
        this.msgHash = msgHash;
    }

    public Hash256 getTxHash() {
        return txHash;
    }

    public void setTxHash(Hash256 txHash) {
        this.txHash = txHash;
    }

    public ByteArrayWrapper getTxResult() {
        return txResult;
    }

    public void setTxResult(ByteArrayWrapper txResult) {
        this.txResult = txResult;
    }

    public ByteArrayWrapper getTxDeploy() { return txDeploy; }

    public void setTxDeploy(ByteArrayWrapper txDeploy) {
        this.txDeploy = txDeploy;
    }
}
