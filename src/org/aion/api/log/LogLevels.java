package org.aion.api.log;

public enum LogLevels {
    INFO,
    DEBUG,
    ERROR,
    TRACE;

    public static boolean contains(String _level) {
        for (LogLevels LogLevel : values())
            if (LogLevel.name().equals(_level)) {
                return true;
            }
        return false;
    }
}
