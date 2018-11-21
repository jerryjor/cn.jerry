package cn.jerry.logging;

import cn.jerry.logging.definition.DailyLoggerDefinition;
import cn.jerry.logging.definition.LoggerDefinitionTool;
import cn.jerry.logging.resolver.Resolver;
import cn.jerry.logging.resolver.ResolverConstants;
import cn.jerry.logging.resolver.ResolverTester;
import org.apache.logging.log4j.core.util.Loader;

import java.lang.annotation.Annotation;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class LoggerDefinitionManager {

    private List<DailyLoggerDefinition> definitions = new ArrayList<DailyLoggerDefinition>();

    public List<DailyLoggerDefinition> getDefinitions() {
        return definitions;
    }

    /**
     * Locates all the plugins.
     */
    public void collectDefinitions() {
        definitions = loadFromProject();
    }

    public List<DailyLoggerDefinition> loadFromProject() {
        final long startTime = System.nanoTime();
        final Resolver resolver = new Resolver(Loader.getClassLoader(), new LoggerDefinitionTest());
        resolver.findInProject();

        final List<DailyLoggerDefinition> newDefs = new ArrayList<DailyLoggerDefinition>();
        final StringBuilder sb = new StringBuilder("daily rolling logger definition found:");
        for (final Class<?> clazz : resolver.getClasses()) {
            sb.append("\n\t").append(clazz.getName());
            try {
                newDefs.add(clazz.getAnnotation(DailyLoggerDefinition.class));
                sb.append("\n\t\t").append(LoggerDefinitionTool.getPrintString(
                        clazz.getAnnotation(DailyLoggerDefinition.class)));
            } catch (NullPointerException e) {
            }
        }

        final long endTime = System.nanoTime();
        final DecimalFormat numFormat = new DecimalFormat("#0.000000");
        final double seconds = (endTime - startTime) * 1e-9;
        ConsoleLogger.info(this.getClass(), "Took " + numFormat.format(seconds) + " seconds to load "
                + resolver.getClasses().size() + " plugins from project");
        ConsoleLogger.info(this.getClass(), newDefs.isEmpty() ? ResolverConstants.LOGGER_NOT_FOUND_MSG : sb.toString());

        return newDefs;
    }

    /**
     * A Test that checks to see if each class is implements 'DailyLoggerDefinition'.
     * If it is, then the test returns true, otherwise false.
     *
     * @since 2.1
     */
    class LoggerDefinitionTest extends ResolverTester {
        private String[] basePackages;

        LoggerDefinitionTest() {
            String basePackage = System.getProperty("Log4jDefinitionBasePackages");
            if (basePackage == null || (basePackage = basePackage.trim()).isEmpty()) {
                basePackages = null;
            } else {
                basePackages = basePackage.split("[ \t]*,[ \t]*");
            }
        }

        @Override
        public Class<? extends Annotation> getAnnotationClass() {
            return DailyLoggerDefinition.class;
        }

        @Override
        public String[] getBasePackages() {
            return basePackages;
        }

        @Override
        public String toString() {
            return "implements " + DailyLoggerDefinition.class.getSimpleName();
        }
    }
}
