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


package org.aion.api.impl;

import java.util.Optional;
import org.aion.api.IAccount;
import org.aion.api.IAdmin;
import org.aion.api.IAionAPI;
import org.aion.api.IChain;
import org.aion.api.IContractController;
import org.aion.api.IMine;
import org.aion.api.INet;
import org.aion.api.ITx;
import org.aion.api.IUtils;
import org.aion.api.IWallet;
import org.aion.api.type.ApiMsg;

/**
 * Nucoapi base class, contains the majority of Aion frontend Java APIs. High chance of finding what
 * you're looking for here, unless it is related to contract transactions. AionAPI provides a static
 * method that returns an instance. All API functionality requires the user to connect to the
 * backend utilizing {@link #connect(String)}.
 *
 * @see Contract
 */
public final class AionAPIImpl extends ApiBase implements IAionAPI {

    private INet net;
    private IChain chain;
    private IWallet wallet;
    private IUtils utils;
    private IMine mine;
    private ITx tx;
    private IAccount account;
    private IAdmin admin;
    private IContractController controller;

    private AionAPIImpl() {
        super();
        net = new Net(this);
        chain = new Chain(this);
        wallet = new Wallet(this);
        utils = new Utils();
        tx = new Tx(this);
        account = new Account(this);
        controller = new ContractController(this);
        mine = new Mine(this);
        admin = new Admin(this);
    }

    public static IAionAPI inst() {
        return new AionAPIImpl();
    }

    public boolean isConnected() {
        if (this.isInitialized.get() && this.msgExecutor.isInitialized.get()) {
            return true;
        }

        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(ErrId.getErrString(-1003L));
        }

        return false;
    }

    public INet getNet() {
        if (Optional.ofNullable(this.getPrivilege().get("Net")).isPresent() && this.getPrivilege()
            .get("Net")) {
            return net;
        }

        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("[getNet]" + ErrId.getErrString(-1011L));
        }

        return null;
    }

    public IChain getChain() {
        if (Optional.ofNullable(this.getPrivilege().get("Chain")).isPresent() && this.getPrivilege()
            .get("Chain")) {
            return chain;
        }

        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("[getChain]" + ErrId.getErrString(-1011L));
        }

        return null;
    }

    public IMine getMine() {
        return mine;
    }

    public IAdmin getAdmin() {
        return admin;
    }

    public ITx getTx() {
        if (Optional.ofNullable(this.getPrivilege().get("Transaction")).isPresent() && this
            .getPrivilege()
            .get("Transaction")) {
            return tx;
        }

        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("[getTx]" + ErrId.getErrString(-1011L));
        }

        return null;
    }

    public IWallet getWallet() {
        if (Optional.ofNullable(this.getPrivilege().get("Wallet")).isPresent() && this.msgExecutor
            .getPrivilege()
            .get("Wallet")) {
            return wallet;
        }

        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("[getWallet]" + ErrId.getErrString(-1011L));
        }

        return null;
    }

    public IUtils getUtils() {
        return utils;
    }

    public IAccount getAccount() {
        if (Optional.ofNullable(this.getPrivilege().get("Account")).isPresent() && this
            .getPrivilege().get("Account")) {
            return account;
        }

        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("[getAccount]" + ErrId.getErrString(-1011L));
        }

        return null;
    }

    public boolean getContractPrivilege() {
        if (Optional.ofNullable(this.getPrivilege().get("Contract")).isPresent()) {
            return this.getPrivilege().get("Contract");
        }

        return false;
    }

    public IContractController getContractController() {
        if (this.getPrivilege().get("Contract") == null || !this.getPrivilege().get("Account")) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("[getAccount]" + ErrId.getErrString(-1011L));
            }
            return null;
        }

        return this.controller;
    }

    @Override
    public ApiMsg destroyApi() {

        if (getTx() != null) {
            ((Tx) getTx()).reset();
        }

        if (getContractController() != null) {
            getContractController().clear();
        }

        return destroyApiBase();
    }
}