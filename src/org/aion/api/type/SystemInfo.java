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
