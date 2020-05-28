package cn.jerry.log4j2.annotation.resolver;

import cn.jerry.log4j2.annotation.ConsoleLogger;
import cn.jerry.log4j2.annotation.definition.DailyLoggerDefinition;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Log4j ResolverConstants.
 */
public final class ResolverConstants {

	/**
     * Line separator.
     */
	public static final String LINE_SEPARATOR_KEY = "line.separator";
	public static final String LINE_SEPARATOR = getLineSeparator();

    /**
     * Number of milliseconds in a second.
     */
    public static final int MILLIS_IN_SECONDS = 1000;
    
    /**
     * Equivalent to StandardCharsets.UTF_8.
     */
    public static final Charset UTF_8 = StandardCharsets.UTF_8;

    /**
     * Default size of ByteBuffers used to encode LogEvents without allocating temporary objects.
     * @see org.apache.logging.log4j.core.layout.ByteBufferDestination
     * @since 2.6
     */
    public static final int ENCODER_BYTE_BUFFER_SIZE = 8 * 1024;

	public static final String LOGGER_NOT_FOUND_MSG = contactLoggerNotFoundMsg();

    /**
     * Prevent class instantiation.
     */
    private ResolverConstants() {
    }

	private static String contactLoggerNotFoundMsg() {
        return "NO daily rolling logger definition found."
                + "\n\tPlease add your definition class in project."
                + "\n\tThe class must has definition [" + DailyLoggerDefinition.class.getName() + "].";
	}

    /**
     * Return the system line separator or \n if an error occurs.
     * @return The system line separator.
     */
    public static String getLineSeparator() {
        String ls = null;
        try {
            ls = System.getProperties().getProperty(LINE_SEPARATOR_KEY);
        } catch (final SecurityException ex) {
            ConsoleLogger.error(ResolverConstants.class, "Failed to get system property: " + LINE_SEPARATOR_KEY);
        }
        return ls == null ? "\n" : ls;
    }
}
