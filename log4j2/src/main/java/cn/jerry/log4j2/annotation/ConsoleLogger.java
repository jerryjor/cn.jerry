package cn.jerry.log4j2.annotation;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ConsoleLogger {
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static void info(Class<?> clazz, String message) {
        System.out.println(SDF.format(new Date()) + " " + (clazz == null ? "" : clazz.getName()) + " - " + message);
    }

    public static void error(Class<?> clazz, String message) {
        System.err.println(SDF.format(new Date()) + " " + (clazz == null ? "" : clazz.getName()) + " - " + message);
    }

    public static void error(Class<?> clazz, String message, Throwable t) {
        error(clazz, message);
        t.printStackTrace();
    }

}
