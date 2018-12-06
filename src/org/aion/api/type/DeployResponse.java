package org.aion.api.type;

import org.aion.base.type.AionAddress;
import org.aion.base.type.Hash256;

/**
 * Contains the response information after deployed a contract by {@link
 * org.aion.api.ITx#contractDeploy(org.aion.api.type.ContractDeploy) contractDeploy} The response
 * includes the deployed contract address and the transactionID in this execution.
 *
 * @author Jay Tseng
 */
public final class DeployResponse {

    private final AionAddress address;
    private final Hash256 txid;

    public DeployResponse(AionAddress address, Hash256 txid) {
        this.address = address;
        this.txid = txid;
    }

    public AionAddress getAddress() {
        return address;
    }

    public Hash256 getTxid() {
        return txid;
    }
}
