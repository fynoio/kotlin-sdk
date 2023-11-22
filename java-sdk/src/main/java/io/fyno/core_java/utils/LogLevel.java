package io.fyno.core_java.utils;

public enum LogLevel {
    VERBOSE(1),
    DEBUG(2),
    INFO(3),
    ERROR(4),
    OFF(Integer.MAX_VALUE);

    private final int num;

    LogLevel(int num) {
        this.num = num;
    }

    public int getNum() {
        return num;
    }
}
