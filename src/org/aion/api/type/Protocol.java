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

/**
 * Represents the version of each Aion blockchain module.
 *
 * @author Jay Tseng
 * @see org.aion.api.INet#getProtocolVersion() getProtocolVersion
 */
public class Protocol {

    private final String kernel;
    private final String net;
    private final String api;
    private final String vm;
    private final String db;
    private final String miner;
    private final String txpool;

    private Protocol(ProtocolBuilder builder) {
        this.kernel = builder.kernel;
        this.net = builder.net;
        this.api = builder.api;
        this.vm = builder.vm;
        this.db = builder.db;
        this.miner = builder.miner;
        this.txpool = builder.txpool;
    }

    public String getKernel() {
        return kernel;
    }

    public String getNet() {
        return net;
    }

    public String getApi() {
        return api;
    }

    public String getVm() {
        return vm;
    }

    public String getDb() {
        return db;
    }

    public String getMiner() {
        return miner;
    }

    public String getTxpool() {
        return txpool;
    }

    public static class ProtocolBuilder {

        private String kernel;
        private String net;
        private String api;
        private String vm;
        private String db;
        private String miner;
        private String txpool;

        public ProtocolBuilder() {}

        public ProtocolBuilder kernel(String kernel) {
            this.kernel = kernel;
            return this;
        }

        public ProtocolBuilder net(String net) {
            this.net = net;
            return this;
        }

        public ProtocolBuilder api(String api) {
            this.api = api;
            return this;
        }

        public ProtocolBuilder vm(String vm) {
            this.vm = vm;
            return this;
        }

        public ProtocolBuilder db(String db) {
            this.db = db;
            return this;
        }

        public ProtocolBuilder miner(String miner) {
            this.miner = miner;
            return this;
        }

        public ProtocolBuilder txpool(String txpool) {
            this.txpool = txpool;
            return this;
        }

        public Protocol createProtocol() {

            if (kernel == null
                    || net == null
                    || api == null
                    || vm == null
                    || db == null
                    || miner == null
                    || txpool == null) {
                throw new NullPointerException(
                        "kernel#"
                                + String.valueOf(kernel)
                                + " net#"
                                + String.valueOf(net)
                                + " api#"
                                + String.valueOf(api)
                                + " vm#"
                                + String.valueOf(vm)
                                + " db#"
                                + String.valueOf(db)
                                + " miner#"
                                + String.valueOf(miner)
                                + " txpool#"
                                + String.valueOf(txpool));
            }

            return new Protocol(this);
        }
    }
}
