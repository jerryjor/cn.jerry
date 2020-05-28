package cn.jerry.test.log;

import cn.jerry.log4j2.annotation.definition.DailyLoggerDefinition;

@DailyLoggerDefinition(basePackage = "net", fileName = "/data/logs/jerry/utils.log", copy2Console = true)
public class LoggerDef {
}
