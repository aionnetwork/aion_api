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

import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.impl.internal.Message;
import org.aion.base.type.Hash256;
import org.aion.base.util.ByteArrayWrapper;
import org.aion.base.util.ByteUtil;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * ALl relevant information pertaining to {@link org.aion.api.IContract IContract} function
 * calls. For non-constant function calls, ContractResponse will contain the
 * transaction msgHash. For constant function calls, ContractResponse will
 * contain an ArrayList of objects.
 *
 * @see org.aion.api.IContract#execute() execute
 *
 * @author Jay Tseng
 */


public final class ContractResponse {
    private final boolean constant;
    private final List<Object> data;
    private final Hash256 txHash;
    private final byte status;
    private final ByteArrayWrapper msgHash;
    private final String error;

    private ContractResponse(ContractResponseBuilder builder) {
        this.constant = builder.constant;
        this.data = builder.data;
        this.txHash = builder.txHash;
        this.status = builder.status;
        this.msgHash = builder.msgHash;
        this.error = builder.error;
    }

    /**
     * The helper function for get detailed Contract transaction processing information.
     */
    public final String statusToString() {
        switch (getStatus()) {
        case Message.Retcode.r_fail_VALUE:
            return "Api failed.";
        case Message.Retcode.r_success_VALUE:
            return "Api success";
        case Message.Retcode.r_wallet_nullcb_VALUE:
            return "No coinbase exist in the server.";
        case Message.Retcode.r_tx_Init_VALUE:
            return "Transaction init.";
        case Message.Retcode.r_tx_Recved_VALUE:
            return "Transaction received.";
        case Message.Retcode.r_tx_Dropped_VALUE:
            return "Transaction dropped.";
        case Message.Retcode.r_tx_NewPending_VALUE:
            return "Transaction new Pending.";
        case Message.Retcode.r_tx_Pending_VALUE:
            return "Transaction Pending.";
        case Message.Retcode.r_tx_Included_VALUE:
            return "Transaction Done.";
        case Message.Retcode.r_NA_VALUE:
            return "Status not available.";
        case Message.Retcode.r_fail_header_len_VALUE:
            return "Header format incorrect.";
        case Message.Retcode.r_fail_service_call_VALUE:
            return "Sevice call incorrect.";
        case Message.Retcode.r_fail_function_call_VALUE:
            return "Function call incorrect";
        case Message.Retcode.r_fail_function_exception_VALUE:
            return "Api function exception.";
        case Message.Retcode.r_fail_api_version_VALUE:
            return "Api version incorrect.";
        case Message.Retcode.r_fail_ct_bytecode_VALUE:
            return "Smart contract bytecode isError.";
        case Message.Retcode.r_fail_null_rsp_VALUE:
            return "Null response.";
        case Message.Retcode.r_fail_null_compile_source_VALUE:
            return "Compile smart contract got a Null message.";
        case Message.Retcode.r_fail_compile_contract_VALUE:
            return "Compile contract incorrect.";
        case Message.Retcode.r_fail_sendTx_null_rep_VALUE:
            return "Send tx got a null response.";
        case Message.Retcode.r_fail_getcode_to_VALUE:
            return "Getcode failed.";
        case Message.Retcode.r_fail_getTxReceipt_null_recp_VALUE:
            return "GetTxReceipt null reply.";
        case Message.Retcode.r_fail_zmqHandler_exception_VALUE:
            return "ZmqHandler exeception.";
        case Message.Retcode.r_fail_hit_pending_tx_limit_VALUE:
            return "Hit_pending_tx_limit.";
        case Message.Retcode.r_fail_txqueue_exception_VALUE:
            return "TxQueue exception.";
        case Message.Retcode.r_fail_unknown_VALUE:
            return "Unknown isError.";
        default:
            return "";
        }
    }

    /**
     * The helper function to check the number of pending transactions stored in the API has reached the limitation
     * of the transaction pool. If yes, the user should stop sending transactions to the kernel.
     */
    public boolean hitTxPendingPoolLimit() {
        return (getStatus() == -15);
    }

    public boolean isStatusError() {
        return (getStatus() < 0 || getStatus() == 102);
    }

    public boolean isConstant() {
        return constant;
    }

    public List<Object> getData() {
        return data;
    }

    public Hash256 getTxHash() {
        return txHash;
    }

    public byte getStatus() {
        return status;
    }

    public ByteArrayWrapper getMsgHash() {
        return msgHash;
    }

    public String getError() {
        return error;
    }

    public boolean isTxError() {
        return error != null && !error.isEmpty();
    }

    /**
     * This Builder class is used to build a {@link ContractResponse} instance.
     */
    public static class ContractResponseBuilder {
        private boolean constant;
        private List<Object> data;
        private Hash256 txHash;
        private byte status;
        private ByteArrayWrapper msgHash;
        private String error;

        public ContractResponseBuilder() {
        }

        public ContractResponse.ContractResponseBuilder constant(final boolean constant) {
            this.constant = constant;
            return this;
        }

        public ContractResponse.ContractResponseBuilder data(final List data) {
            this.data = data;
            return this;
        }

        public ContractResponse.ContractResponseBuilder txHash(final Hash256 txHash) {
            this.txHash = txHash;
            return this;
        }

        public ContractResponse.ContractResponseBuilder status(final byte status) {
            this.status = status;
            return this;
        }

        public ContractResponse.ContractResponseBuilder msgHash(final ByteArrayWrapper msgHash) {
            this.msgHash = msgHash;
            return this;
        }

        public ContractResponse.ContractResponseBuilder error(final String error) {
            if (error == null) {
                return this;
            }

            this.error = error;
            return this;
        }

        public ContractResponse createContractResponse() {

            if (data == null || txHash == null || msgHash == null) {
                throw new NullPointerException(
                        "data#" + String.valueOf(data) +
                                " txHash#" + String.valueOf(txHash) +
                                " msgHash#" + String.valueOf(msgHash));
            }

            if (msgHash.getData().length != ApiUtils.MSG_HASH_LEN) {
                throw new IllegalArgumentException(
                        "msgHash#" + msgHash.getData().length);
            }

            return new ContractResponse(this);
        }
    }
}
