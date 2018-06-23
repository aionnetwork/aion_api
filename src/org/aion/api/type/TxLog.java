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

import org.aion.base.type.Address;
import org.aion.base.util.ByteArrayWrapper;

import java.util.List;

/**
 * TxLog class containing all relevant information to transaction log utilized by {@link
 * org.aion.api.ITx#getTxReceipt(org.aion.base.type.Hash256) getTxReceipt}.
 *
 * @author Jay Tseng
 * @see org.aion.api.type.TxReceipt TxReceipt
 */

public final class TxLog {

    private final Address address;
    private final ByteArrayWrapper data;
    private final List<String> topics;

    public TxLog(Address address, ByteArrayWrapper data, List<String> topics) {
        this.address = address;
        this.data = data;
        this.topics = topics;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int _level) {

        StringBuilder lv = new StringBuilder();
        int level = _level;
        while (level-- > 0) {
            lv.append("  ");
        }

        StringBuilder sb = new StringBuilder()
            .append(lv).append("address: ").append("0x").append(address.toString()).append(",\n")
            .append(lv).append("data: ").append("0x").append(data.toString()).append(",\n")
            .append(lv).append("topics: ").append("\n")
            .append(lv).append("[").append("\n");

        int cnt = topics.size();
        for (String s : topics) {
            sb.append(lv).append("  0x").append(s);
            if (--cnt > 0) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append(lv).append("]").append("\n");

        return sb.toString();
    }

    public Address getAddress() {
        return address;
    }

    public ByteArrayWrapper getData() {
        return data;
    }

    public List<String> getTopics() {
        return topics;
    }
}

