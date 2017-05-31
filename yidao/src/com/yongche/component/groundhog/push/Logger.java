package com.yongche.component.groundhog.push;

public class Logger {
    
    public enum Level {
        NONE(0),
        ERROR(1),
        WARN(2),
        INFO(3),
        DEBUG(4),
        VERBOSE(5),
        ALL(6);

        int weight;
        Level(int weight) {
            this.weight = weight;
        }
    }; 

    public static volatile Level logLevel = CommonConfig.LOG_LEVEL;

    public static void error(String tag, String msg) {
        if (logLevel.weight >= Level.ERROR.weight) {
            //Log.e(tag, msg);
        }
    }

    public static void warn(String tag, String msg) {
        if (logLevel.weight >= Level.WARN.weight) {
            //Log.d(tag, msg);
        }
    }
    
    public static void info(String tag, String msg) {
        if (logLevel.weight >= Level.INFO.weight) {
            //Log.i(tag, msg);
        }
    }

    public static void debug(String tag, String msg) {
        if (logLevel.weight >= Level.DEBUG.weight) {
            //Log.d(tag, msg);
        }
    }

    public static void verbose(String tag, String msg) {
        if (logLevel.weight >= Level.VERBOSE.weight) {
            //Log.d(tag, msg);
        }
    }
}
