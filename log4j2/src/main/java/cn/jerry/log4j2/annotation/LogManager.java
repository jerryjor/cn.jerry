package cn.jerry.log4j2.annotation;

import cn.jerry.log4j2.annotation.definition.DailyLoggerDefinition;
import cn.jerry.log4j2.annotation.definition.LoggerDefinitionTool;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.StringFormatterMessageFactory;
import org.apache.logging.log4j.simple.SimpleLoggerContextFactory;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.apache.logging.log4j.spi.Provider;
import org.apache.logging.log4j.spi.Terminable;
import org.apache.logging.log4j.util.ProviderUtil;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.apache.logging.log4j.util.Strings;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * The anchor point for the logging system. The most common usage of this class is to obtain a named
 * {@link Logger}. The method {@link #getLogger()} is provided as the most convenient way to obtain
 * a named Logger
 * based on the calling class name. This class also provides method for obtaining named Loggers that
 * use
 * {@link String#format(String, Object...)} style messages instead of the default type of
 * parameterized messages.
 * These are obtained through the {@link #getFormatterLogger(Class)} family of methods. Other
 * service provider methods
 * are given through the {@link #getContext()} and {@link #getFactory()} family of methods; these
 * methods are not
 * normally useful for typical usage of Log4j.
 */
public class LogManager {

    private static volatile LoggerContextFactory factory;

    /**
     * The name of the root Logger.
     */
    public static final String ROOT_LOGGER_NAME = Strings.EMPTY;

    private static final String CONSOLE_APPENDER_NAME = "consolePrint";

    // for convenience
    private static final String FQCN = LogManager.class.getName();

    static {
        // 全局属性，开启异步
        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        factory = initMultipleLoggingFactory();
        if (factory == null) {
            ConsoleLogger.error(LogManager.class,
                    "Log4j2 could not find a logging implementation. Using SimpleLogger to log to the console...");
            factory = new SimpleLoggerContextFactory();
        } else {
            initLoggers();
        }
    }

    protected LogManager() {
    }

    /**
     * Scans the classpath to find all logging implementation. Currently, only one will
     * be used but this could be extended to allow multiple implementations to be used.
     */
    private static LoggerContextFactory initMultipleLoggingFactory() {
        if (!ProviderUtil.hasProviders()) return null;

        // note that the following initial call to ProviderUtil may block
        // until a Provider has been installed when running in an OSGi environment
        final SortedMap<Integer, LoggerContextFactory> factories = collectLoggerContextFactories();
        if (factories.isEmpty()) return null;

        final StringBuilder sb = new StringBuilder("Multiple logging implementations found:");
        for (final Map.Entry<Integer, LoggerContextFactory> entry : factories.entrySet()) {
            sb.append("\n\t").append("Factory: ").append(entry.getValue().getClass().getName())
                    .append(", Weighting: ").append(entry.getKey());
        }
        ConsoleLogger.info(LogManager.class, sb.toString());
        return factories.get(factories.lastKey());
    }

    /**
     * collect provided LoggerContextFactory
     *
     * @return
     */
    private static SortedMap<Integer, LoggerContextFactory> collectLoggerContextFactories() {
        final SortedMap<Integer, LoggerContextFactory> factories = new TreeMap<Integer, LoggerContextFactory>();
        for (final Provider provider : ProviderUtil.getProviders()) {
            final Class<? extends LoggerContextFactory> factoryClass = provider
                    .loadLoggerContextFactory();
            if (factoryClass != null) {
                try {
                    factories.put(provider.getPriority(), factoryClass.newInstance());
                } catch (final Exception e) {
                    ConsoleLogger.error(LogManager.class, "Unable to create class " + factoryClass.getName(), e);
                }
            }
        }
        return factories;
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
                try {
                    registerDailyRollingFileLogger(cfg, def);
                } catch (Throwable t) {
                    ConsoleLogger.error(LogManager.class, "init logger failed.", t);
                }
            }
            cxt.updateLoggers();
        } catch (Throwable t) {
            ConsoleLogger.error(LogManager.class, "init logging failed.", t);
        }
    }

    /**
     * register daily rolling file logger
     *
     * @param cfg
     * @param def
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

        ArrayList<Appender> appenders = new ArrayList<Appender>();

        PatternLayout layout = PatternLayout.newBuilder()
                .withPattern(LoggerDefinitionTool.buildContentPattern(def))
                .withConfiguration(cfg)
                .withCharset(Charset.forName("utf-8"))
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
    }

    /**
     * register console appender, using DEFAULT_PATTERN
     *
     * @param cfg
     * @return
     */
    private static Appender registerConsoleAppender(Configuration cfg) {
        PatternLayout layout = PatternLayout.newBuilder()
                .withPattern(LoggerDefinitionTool.buildConsolePattern())
                .withConfiguration(cfg)
                .withCharset(Charset.forName("utf-8"))
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
            return factory.getContext(FQCN, (ClassLoader) null, (Object) null, true);
        } catch (IllegalStateException var1) {
            ConsoleLogger.error(LogManager.class, var1.getMessage() + " Using SimpleLogger");
            return (new SimpleLoggerContextFactory()).getContext(FQCN, (ClassLoader) null, (Object) null, true);
        }
    }

    public static LoggerContext getContext(boolean currentContext) {
        try {
            return factory.getContext(FQCN, (ClassLoader) null, (Object) null, currentContext, (URI) null, (String) null);
        } catch (IllegalStateException var2) {
            ConsoleLogger.error(LogManager.class, var2.getMessage() + " Using SimpleLogger");
            return (new SimpleLoggerContextFactory()).getContext(FQCN, (ClassLoader) null, (Object) null, currentContext, (URI) null, (String) null);
        }
    }

    public static LoggerContext getContext(ClassLoader loader, boolean currentContext) {
        try {
            return factory.getContext(FQCN, loader, (Object) null, currentContext);
        } catch (IllegalStateException var3) {
            ConsoleLogger.error(LogManager.class, var3.getMessage() + " Using SimpleLogger");
            return (new SimpleLoggerContextFactory()).getContext(FQCN, loader, (Object) null, currentContext);
        }
    }

    public static LoggerContext getContext(ClassLoader loader, boolean currentContext, Object externalContext) {
        try {
            return factory.getContext(FQCN, loader, externalContext, currentContext);
        } catch (IllegalStateException var4) {
            ConsoleLogger.error(LogManager.class, var4.getMessage() + " Using SimpleLogger");
            return (new SimpleLoggerContextFactory()).getContext(FQCN, loader, externalContext, currentContext);
        }
    }

    public static LoggerContext getContext(ClassLoader loader, boolean currentContext, URI configLocation) {
        try {
            return factory.getContext(FQCN, loader, (Object) null, currentContext, configLocation, (String) null);
        } catch (IllegalStateException var4) {
            ConsoleLogger.error(LogManager.class, var4.getMessage() + " Using SimpleLogger");
            return (new SimpleLoggerContextFactory()).getContext(FQCN, loader, (Object) null, currentContext, configLocation, (String) null);
        }
    }

    public static LoggerContext getContext(ClassLoader loader, boolean currentContext, Object externalContext, URI configLocation) {
        try {
            return factory.getContext(FQCN, loader, externalContext, currentContext, configLocation, (String) null);
        } catch (IllegalStateException var5) {
            ConsoleLogger.error(LogManager.class, var5.getMessage() + " Using SimpleLogger");
            return (new SimpleLoggerContextFactory()).getContext(FQCN, loader, externalContext, currentContext, configLocation, (String) null);
        }
    }

    public static LoggerContext getContext(ClassLoader loader, boolean currentContext, Object externalContext, URI configLocation, String name) {
        try {
            return factory.getContext(FQCN, loader, externalContext, currentContext, configLocation, name);
        } catch (IllegalStateException var6) {
            ConsoleLogger.error(LogManager.class, var6.getMessage() + " Using SimpleLogger");
            return (new SimpleLoggerContextFactory()).getContext(FQCN, loader, externalContext, currentContext, configLocation, name);
        }
    }

    protected static LoggerContext getContext(String fqcn, boolean currentContext) {
        try {
            return factory.getContext(fqcn, (ClassLoader) null, (Object) null, currentContext);
        } catch (IllegalStateException var3) {
            ConsoleLogger.error(LogManager.class, var3.getMessage() + " Using SimpleLogger");
            return (new SimpleLoggerContextFactory()).getContext(fqcn, (ClassLoader) null, (Object) null, currentContext);
        }
    }

    protected static LoggerContext getContext(String fqcn, ClassLoader loader, boolean currentContext) {
        try {
            return factory.getContext(fqcn, loader, (Object) null, currentContext);
        } catch (IllegalStateException var4) {
            ConsoleLogger.error(LogManager.class, var4.getMessage() + " Using SimpleLogger");
            return (new SimpleLoggerContextFactory()).getContext(fqcn, loader, (Object) null, currentContext);
        }
    }

    protected static LoggerContext getContext(String fqcn, ClassLoader loader, boolean currentContext, URI configLocation, String name) {
        try {
            return factory.getContext(fqcn, loader, (Object) null, currentContext, configLocation, name);
        } catch (IllegalStateException var6) {
            ConsoleLogger.error(LogManager.class, var6.getMessage() + " Using SimpleLogger");
            return (new SimpleLoggerContextFactory()).getContext(fqcn, loader, (Object) null, currentContext);
        }
    }

    public static void shutdown() {
        shutdown(false);
    }

    public static void shutdown(boolean currentContext) {
        shutdown(getContext(currentContext));
    }

    public static void shutdown(LoggerContext context) {
        if (context != null && context instanceof Terminable) {
            ((Terminable) context).terminate();
        }

    }

    private static String toLoggerName(Class<?> cls) {
        String canonicalName = cls.getCanonicalName();
        return canonicalName != null ? canonicalName : cls.getName();
    }

    public static LoggerContextFactory getFactory() {
        return factory;
    }

    public static void setFactory(LoggerContextFactory factory) {
        factory = factory;
    }

    public static Logger getFormatterLogger() {
        return getFormatterLogger(StackLocatorUtil.getCallerClass(2));
    }

    public static Logger getFormatterLogger(Class<?> clazz) {
        return getLogger((Class) (clazz != null ? clazz : StackLocatorUtil.getCallerClass(2)), (MessageFactory) StringFormatterMessageFactory.INSTANCE);
    }

    public static Logger getFormatterLogger(Object value) {
        return getLogger((Class) (value != null ? value.getClass() : StackLocatorUtil.getCallerClass(2)), (MessageFactory) StringFormatterMessageFactory.INSTANCE);
    }

    public static Logger getFormatterLogger(String name) {
        return name == null ? getFormatterLogger(StackLocatorUtil.getCallerClass(2)) : getLogger((String) name, (MessageFactory) StringFormatterMessageFactory.INSTANCE);
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

    public static Logger getLogger() {
        return getLogger(StackLocatorUtil.getCallerClass(2));
    }

    public static Logger getLogger(Class<?> clazz) {
        Class<?> cls = callerClass(clazz);
        return getContext(cls.getClassLoader(), false).getLogger(toLoggerName(cls));
    }

    public static Logger getLogger(Class<?> clazz, MessageFactory messageFactory) {
        Class<?> cls = callerClass(clazz);
        return getContext(cls.getClassLoader(), false).getLogger(toLoggerName(cls), messageFactory);
    }

    public static Logger getLogger(MessageFactory messageFactory) {
        return getLogger(StackLocatorUtil.getCallerClass(2), messageFactory);
    }

    public static Logger getLogger(Object value) {
        return getLogger(value != null ? value.getClass() : StackLocatorUtil.getCallerClass(2));
    }

    public static Logger getLogger(Object value, MessageFactory messageFactory) {
        return getLogger(value != null ? value.getClass() : StackLocatorUtil.getCallerClass(2), messageFactory);
    }

    public static Logger getLogger(String name) {
        return (Logger) (name != null ? getContext(false).getLogger(name) : getLogger(StackLocatorUtil.getCallerClass(2)));
    }

    public static Logger getLogger(String name, MessageFactory messageFactory) {
        return (Logger) (name != null ? getContext(false).getLogger(name, messageFactory) : getLogger(StackLocatorUtil.getCallerClass(2), messageFactory));
    }

    protected static Logger getLogger(String fqcn, String name) {
        return factory.getContext(fqcn, (ClassLoader) null, (Object) null, false).getLogger(name);
    }

    public static Logger getRootLogger() {
        return getLogger("");
    }

}
