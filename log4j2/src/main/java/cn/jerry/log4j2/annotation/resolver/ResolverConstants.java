package cn.jerry.log4j2.annotation.resolver;

import cn.jerry.log4j2.annotation.definition.DailyLoggerDefinition;

import java.nio.charset.Charset;

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
    public static final Charset UTF_8 = Charset.forName("UTF-8");

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
		final StringBuilder sb = new StringBuilder("NO daily rolling logger definition found.");
		sb.append("\n\tPlease add your definition class in project.");
		sb.append("\n\tThe class must has definition [").append(DailyLoggerDefinition.class.getName()).append("].");
		return sb.toString();
	}

    /**
     * Return the system line separator or \n if an error occurs.
     * @return The system line separator.
     */
    public static String getLineSeparator() {
        String ls = null;
        try {
            ls = System.getProperties().getProperty(ResolverConstants.LINE_SEPARATOR_KEY);
        } catch (final SecurityException ex) {
        }
        return ls == null ? "\n" : ls;
    }
}
