package cn.jerry.log4j2.annotation;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConsoleLogger {
    private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final PrintStream INFO = getStream(false);
    private static final PrintStream ERROR = getStream(true);

    private ConsoleLogger() {
        super();
    }

    private static PrintStream getStream(boolean error) {
        return error ? System.err : System.out;
    }

    public static void info(Class<?> clazz, String message) {
        INFO.println(new SimpleDateFormat(TIME_FORMAT).format(new Date()) + " " + (clazz == null ? "" : clazz.getName()) + " - " + message);
    }

    public static void error(Class<?> clazz, String message) {
        ERROR.println(new SimpleDateFormat(TIME_FORMAT).format(new Date()) + " " + (clazz == null ? "" : clazz.getName()) + " - " + message);
    }

    public static void error(Class<?> clazz, String message, Throwable t) {
        error(clazz, message);
        t.printStackTrace(ERROR);
    }

}
