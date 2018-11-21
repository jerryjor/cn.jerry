package cn.jerry.log4j2.annotation.definition;

public class LoggerDefinitionTool {

    public static String buildConsolePattern() {
        return "%d{yyyy-MM-dd HH:mm:ss.SSS} %p [%t] %c{1.} %m %ex%n";
    }

    public static String buildContentPattern(DailyLoggerDefinition def) {
        return "%d{" + def.contentTimePattern() + "}"
                + (def.contentWithLevel() ? " %p" : "")
                + (def.contentWithThread() ? " [%t]" : "")
                + (def.contentWithShortPackage() ? " %c{1.}" : " %c")
                + " %m %ex%n";
    }

    public static String buildFilePattern(DailyLoggerDefinition def) {
        return def.fileName().endsWith(".log")
                ? def.fileName().replace(".log", ".%d{" + def.fileDailyPattern() + "}.log")
                : def.fileName() + ".%d{" + def.fileDailyPattern() + "}";
    }

    public static String getPrintString(DailyLoggerDefinition def) {
        return "{\"basePackage\":\"" + def.basePackage() + "\",\"level\":\"" + def.level().name() + "\",\"filePattern\":\""
                + buildFilePattern(def) + "\",\"contentPattern\":\"" + buildContentPattern(def) + "\",\"copy2Console\":"
                + def.copy2Console() + "}";
    }

}
