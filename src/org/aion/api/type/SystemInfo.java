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
 * The SystemInfo return data structure
 *
 * @see org.aion.api.IAdmin#getSystemInfo() getSystemInfo
 */

public final class SystemInfo {

    private final long dbSize;
    private final long memUsage;
    private final float cpuUsage;

    public SystemInfo(long dbSize, long memUsage, float cpuUsage) {
        this.dbSize = dbSize;
        this.memUsage = memUsage;
        this.cpuUsage = cpuUsage;
    }

    public long getDbSize() {
        return dbSize;
    }

    public float getCpuUsage() {
        return cpuUsage;
    }

    public float getMemUsage() {
        return memUsage;
    }
}
