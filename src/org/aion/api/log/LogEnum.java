package org.aion.api.log;

/** Created by Jay Tseng on 23/03/17. */
public enum LogEnum {
    GEN,
    BSE,
    CHN,
    CNT,
    MNE,
    NET,
    TRX,
    UTL,
    WLT,
    EXE,
    ADM,
    SOL,
    ACC; // discover

    public static boolean contains(String _module) {
        for (LogEnum module : values())
            if (module.name().equals(_module)) {
                return true;
            }
        return false;
    }
}
