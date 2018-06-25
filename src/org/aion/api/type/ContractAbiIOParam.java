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

import java.util.List;

/**
 * Represents the name and type of the associated I/O parameter within a ABI Entry (function),
 * typically only type is significant.
 *
 * @author Jay Tseng
 */

public final class ContractAbiIOParam {

    private boolean indexed;
    private String type;
    private String name;

    private List<Integer> paramLengths;

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int _lv) {
        StringBuilder lv = new StringBuilder();
        int level = _lv;
        while (level-- > 0) {
            lv.append("  ");
        }

        StringBuilder sb = new StringBuilder()
            .append(lv).append("indexed: ").append(String.valueOf(indexed)).append(",\n")
            .append(lv).append("type: ").append(type).append(",\n")
            .append(lv).append("name: ").append(name).append(",\n")
            .append(lv).append("paramLengths: ").append("\n")
            .append(lv).append("[").append("\n");

        for (Integer i : paramLengths) {
            sb.append(lv).append(String.valueOf(i)).append(",");
        }
        sb.append(lv).append("]").append("\n");

        return sb.toString();
    }

    public List<Integer> getParamLengths() {
        return paramLengths;
    }

    void setParamLengths(List<Integer> paramLengths) {
        this.paramLengths = paramLengths;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}