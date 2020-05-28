package cn.jerry.logging;

import cn.jerry.logging.definition.DailyLoggerDefinition;
import cn.jerry.logging.definition.LoggerDefinitionTool;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.async.AsyncLoggerContextSelector;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.simple.SimpleLoggerContextFactory;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.apache.logging.log4j.util.Strings;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class LogManager {
    public static final String ROOT_LOGGER_NAME = Strings.EMPTY;
    private static final String CONSOLE_APPENDER_NAME = "consolePrint";
    private static final String MESSAGE_USE_SIMPLE = "%s Using SimpleLogger.";
    private static final String FQCN = LogManager.class.getName();
    private static final LoggerContextFactory factory = new Log4jContextFactory(new AsyncLoggerContextSelector());

    static {
        initLoggers();
    }

    private LogManager() {
        super();
    }

    private static void initLoggers() {
        try {
            org.apache.logging.log4j.core.LoggerContext cxt = (org.apache.logging.log4j.core.LoggerContext) getContext(
                    LogManager.class.getClassLoader(), false);
            Configuration cfg = cxt.getConfiguration();
            // collect definitions
            LoggerDefinitionManager collector = new LoggerDefinitionManager();
            collector.collectDefinitions();
            for (DailyLoggerDefinition def : collector.getDefinitions()) {
                registerDailyRollingFileLogger(cfg, def);
            }
            cxt.updateLoggers();
        } catch (Exception e) {
            ConsoleLogger.error(LogManager.class, "Failed to init loggers.", e);
        }
    }

    /**
     * register daily rolling file logger
     */
    private static void registerDailyRollingFileLogger(Configuration cfg, DailyLoggerDefinition def) {
        if (def == null) {
            ConsoleLogger.error(LogManager.class, "registerDailyRollingFileLogger: Definition is NULL.");
            return;
        }
        if (def.basePackage().length() == 0) {
            ConsoleLogger.error(LogManager.class, "registerDailyRollingFileLogger: NO basePackage provided.");
            return;
        }
        if (def.fileName().length() == 0) {
            ConsoleLogger.error(LogManager.class, "registerDailyRollingFileLogger: NO fileName provided.");
            return;
        }

        ArrayList<Appender> appenders = new ArrayList<>();

        try {
            PatternLayout layout = PatternLayout.newBuilder()
                    .withPattern(LoggerDefinitionTool.buildContentPattern(def))
                    .withConfiguration(cfg)
                    .withCharset(StandardCharsets.UTF_8)
                    .withAlwaysWriteExceptions(true)
                    .withNoConsoleNoAnsi(false)
                    .build();

            TriggeringPolicy tp = TimeBasedTriggeringPolicy.newBuilder()
                    .withInterval(1)
                    .withModulate(true)
                    .build();
            RollingFileAppender appender = RollingFileAppender.newBuilder()
                    .withFileName(def.fileName())
                    .withFilePattern(LoggerDefinitionTool.buildFilePattern(def))
                    .withName(def.basePackage())
                    .withPolicy(tp)
                    .withLayout(layout)
                    .setConfiguration(cfg)
                    .withBufferedIo(true)
                    .build();
            appender.start();
            cfg.addAppender(appender);
            appenders.add(appender);

            if (def.copy2Console()) {
                Appender consoleAppender = cfg.getAppender(CONSOLE_APPENDER_NAME);
                if (consoleAppender == null) {
                    consoleAppender = registerConsoleAppender(cfg);
                }
                appenders.add(consoleAppender);
            }
            AppenderRef[] refs = new AppenderRef[appenders.size()];
            for (int i = 0; i < refs.length; i++) {
                refs[i] = AppenderRef.createAppenderRef(appenders.get(i).getName(), null, null);
            }
            LoggerConfig loggerConfig;
            loggerConfig = LoggerConfig.createLogger(false, Level.forName(def.level().name(), def.level().intLevel()),
                    def.basePackage(), "false", refs, null, cfg, null);
            for (int i = 0; i < refs.length; i++) {
                loggerConfig.addAppender(appenders.get(i), null, null);
            }
            cfg.addLogger(def.basePackage(), loggerConfig);
        } catch (Exception e) {
            ConsoleLogger.error(LogManager.class, "Failed to init logger.", e);
        }
    }

    /**
     * register console appender, using DEFAULT_PATTERN
     */
    private static Appender registerConsoleAppender(Configuration cfg) {
        PatternLayout layout = PatternLayout.newBuilder()
                .withPattern(LoggerDefinitionTool.buildConsolePattern())
                .withConfiguration(cfg)
                .withCharset(StandardCharsets.UTF_8)
                .withAlwaysWriteExceptions(true)
                .withNoConsoleNoAnsi(false)
                .build();

        Appender consoleAppender = ConsoleAppender.newBuilder()
                .withName(CONSOLE_APPENDER_NAME)
                .withLayout(layout)
                .build();
        consoleAppender.start();
        cfg.addAppender(consoleAppender);

        return consoleAppender;
    }

    public static boolean exists(String name) {
        return getContext().hasLogger(name);
    }

    public static LoggerContext getContext() {
        try {
            return factory.getContext(FQCN, null, null, true);
        } catch (IllegalStateException e) {
            ConsoleLogger.error(LogManager.class, String.format(MESSAGE_USE_SIMPLE, e.getMessage()));
            return (new SimpleLoggerContextFactory()).getContext(FQCN, null, null, true);
        }
    }

    public static LoggerContext getContext(boolean currentContext) {
        try {
            return factory.getContext(FQCN, null, null, currentContext, null, null);
        } catch (IllegalStateException e) {
            ConsoleLogger.error(LogManager.class, String.format(MESSAGE_USE_SIMPLE, e.getMessage()));
            return (new SimpleLoggerContextFactory()).getContext(FQCN, null, null, currentContext, null, null);
        }
    }

    public static LoggerContext getContext(ClassLoader loader, boolean currentContext) {
        try {
            return factory.getContext(FQCN, loader, null, currentContext);
        } catch (IllegalStateException e) {
            ConsoleLogger.error(LogManager.class, String.format(MESSAGE_USE_SIMPLE, e.getMessage()));
            return (new SimpleLoggerContextFactory()).getContext(FQCN, loader, null, currentContext);
        }
    }

    public static Logger getLogger() {
        return getLogger(StackLocatorUtil.getCallerClass(2));
    }

    public static Logger getLogger(Class<?> clazz) {
        Class<?> cls = callerClass(clazz);
        return getContext(cls.getClassLoader(), false).getLogger(toLoggerName(cls));
    }

    private static Class<?> callerClass(Class<?> clazz) {
        if (clazz != null) {
            return clazz;
        } else {
            Class<?> candidate = StackLocatorUtil.getCallerClass(3);
            if (candidate == null) {
                throw new UnsupportedOperationException("No class provided, and an appropriate one cannot be found.");
            } else {
                return candidate;
            }
        }
    }

    private static String toLoggerName(Class<?> cls) {
        String canonicalName = cls.getCanonicalName();
        return canonicalName != null ? canonicalName : cls.getName();
    }

}
