package cn.jerry.blockchain.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class DateUtil {
    private static final Logger LOG = LoggerFactory.getLogger(DateUtil.class);

    private static final String PATTERN_YEAR = "yyyy";
    private static final String PATTERN_MONTH = "MM";
    private static final String PATTERN_DAY = "dd";
    private static final String PATTERN_HOUR = "HH";
    private static final String PATTERN_MINUTE = "mm";
    private static final String PATTERN_SECOND = "ss";
    private static final String PATTERN_MILLIS = "SSS";
    private static final String PATTERN_DATE = PATTERN_YEAR + "%s" + PATTERN_MONTH + "%s" + PATTERN_DAY;
    private static final String PATTERN_TIME = PATTERN_HOUR + "%s" + PATTERN_MINUTE + "%s" + PATTERN_SECOND;
    private static final String PATTERN_TIME_MILLIS = PATTERN_TIME + "%s" + PATTERN_MILLIS;
    private static final String PATTERN_DATETIME = PATTERN_DATE + PATTERN_TIME;
    private static final String PATTERN_DATETIME_MILLIS = PATTERN_DATE + PATTERN_TIME_MILLIS;
    private static final String PATTERN_DATE_TIME = PATTERN_DATE + " " + PATTERN_TIME;
    private static final String PATTERN_DATE_TIME_MILLIS = PATTERN_DATE + " " + PATTERN_TIME_MILLIS;
    // 2018-11-13T00:00:00.000+0800
    public static final String PATTERN_FULL_1 = PATTERN_YEAR + "-" + PATTERN_MONTH + "-" + PATTERN_DAY + "'T'"
            + PATTERN_HOUR + ":" + PATTERN_MINUTE + ":" + PATTERN_SECOND + "." + PATTERN_MILLIS + "Z";
    // 2018-11-13T00:00:00.000+0800Z
    public static final String PATTERN_FULL_2 = PATTERN_YEAR + "-" + PATTERN_MONTH + "-" + PATTERN_DAY + "'T'"
            + PATTERN_HOUR + ":" + PATTERN_MINUTE + ":" + PATTERN_SECOND + "." + PATTERN_MILLIS + "Z'Z'";

    public static final long MS_PER_SECOND = 1000L;
    public static final long MS_PER_MINUTE = 60 * MS_PER_SECOND;
    public static final long MS_PER_HOUR = 60 * MS_PER_MINUTE;
    public static final long MS_PER_DAY = 24 * MS_PER_HOUR;
    public static final TimeZone DEFAULT_TIME_ZONE = Calendar.getInstance().getTimeZone();

    private static final Map<String, Integer> PATTERN_MAX_VALUE;
    private static final Map<String, DateFormat> SIMPLE_FORMATTERS;
    private static final Map<String, DateFormat> FULL_FORMATTERS;

    /*
     * 生成常用的日期格式
     * 限定的年月日分别为4位、2位、2位数字
     * 限定常见的日期分隔符和时间分隔符，或不使用分隔符
     */
    static {
        PATTERN_MAX_VALUE = new HashMap<>();
        PATTERN_MAX_VALUE.put(PATTERN_MONTH, 12);
        PATTERN_MAX_VALUE.put(PATTERN_DAY, 31);
        PATTERN_MAX_VALUE.put(PATTERN_HOUR, 23);
        PATTERN_MAX_VALUE.put(PATTERN_MINUTE, 59);
        PATTERN_MAX_VALUE.put(PATTERN_SECOND, 59);
        PATTERN_MAX_VALUE.put(PATTERN_MILLIS, 999);

        // 全日期
        FULL_FORMATTERS = new HashMap<>();
        FULL_FORMATTERS.put(PATTERN_FULL_1, new DateFormat(PATTERN_FULL_1));
        FULL_FORMATTERS.put(PATTERN_FULL_2, new DateFormat(PATTERN_FULL_2));

        SIMPLE_FORMATTERS = new HashMap<>();
        String[] dateSplit = new String[]{"", "-", ".", "/"};
        String[] timeSplit = new String[]{"", ":"};
        String[] millisSplit = new String[]{"", ".", ":"};
        String pattern;
        for (String d : dateSplit) {
            // 日期
            pattern = String.format(PATTERN_DATE, d, d);
            SIMPLE_FORMATTERS.put(pattern, new DateFormat(pattern));
        }
        for (String t : timeSplit) {
            // 时间
            pattern = String.format(PATTERN_TIME, t, t);
            SIMPLE_FORMATTERS.put(pattern, new DateFormat(pattern));
            for (String value : millisSplit) {
                // 时间，带毫秒
                pattern = String.format(PATTERN_TIME_MILLIS, t, t, value);
                SIMPLE_FORMATTERS.put(pattern, new DateFormat(pattern));
            }
        }
        for (String d : dateSplit) {
            for (String t : timeSplit) {
                // 日期时间
                pattern = String.format(PATTERN_DATETIME, d, d, t, t);
                SIMPLE_FORMATTERS.put(pattern, new DateFormat(pattern));
                // 日期空格时间
                pattern = String.format(PATTERN_DATE_TIME, d, d, t, t);
                SIMPLE_FORMATTERS.put(pattern, new DateFormat(pattern));
                for (String m : millisSplit) {
                    // 日期时间，带毫秒
                    pattern = String.format(PATTERN_DATETIME_MILLIS, d, d, t, t, m);
                    SIMPLE_FORMATTERS.put(pattern, new DateFormat(pattern));
                    // 日期空格时间，带毫秒
                    pattern = String.format(PATTERN_DATE_TIME_MILLIS, d, d, t, t, m);
                    SIMPLE_FORMATTERS.put(pattern, new DateFormat(pattern));
                }
            }
        }
    }

    private DateUtil() {
        super();
    }

    /**
     * 计算日期
     */
    public static Date addSecond(Date date, int seconds) {
        return new Date(date.getTime() + seconds * MS_PER_SECOND);
    }

    /**
     * 计算日期
     */
    public static Date addMinute(Date date, int minutes) {
        return new Date(date.getTime() + minutes * MS_PER_MINUTE);
    }

    /**
     * 计算日期
     */
    public static Date addHour(Date date, int hours) {
        return new Date(date.getTime() + hours * MS_PER_HOUR);
    }

    /**
     * 计算日期
     */
    public static Date addDay(Date date, int days) {
        return new Date(date.getTime() + days * MS_PER_DAY);
    }

    /**
     * 计算日期是自1970-1-1以来第几天，以0开始
     */
    public static long calcDaysFrom19700101(Date date) {
        return calcDaysFrom19700101(date, DEFAULT_TIME_ZONE);
    }

    /**
     * 计算日期是自1970-1-1以来第几天，以0开始
     */
    public static long calcDaysFrom19700101(Date date, TimeZone tz) {
        return (date.getTime() + tz.getRawOffset()) / MS_PER_DAY;
    }

    /**
     * 截取日期，去除时间
     */
    public static Date truncToDay(Date date) {
        return truncToDay(date, DEFAULT_TIME_ZONE);
    }

    /**
     * 截取日期，去除时间
     */
    public static Date truncToDay(Date date, TimeZone tz) {
        long days = calcDaysFrom19700101(date);
        return new Date(days * MS_PER_DAY - tz.getRawOffset());
    }

    /**
     * 截取日期到次日前1毫秒
     */
    public static Date truncToTomorrow(Date date) {
        return truncToTomorrow(date, DEFAULT_TIME_ZONE);
    }

    /**
     * 截取日期到次日前1毫秒
     */
    public static Date truncToTomorrow(Date date, TimeZone tz) {
        long days = calcDaysFrom19700101(date) + 1;
        return new Date(days * MS_PER_DAY - tz.getRawOffset() - 1);
    }

    /**
     * 使用指定的格式转换日期
     */
    public static Date parseDate(String text, String pattern) throws ParseException {
        return parseDate(text, pattern, false);
    }

    /**
     * 使用指定的格式转换日期，不抛异常，调用方提供解决方案
     */
    public static Date parseDate(String text, String pattern, Date failover) {
        try {
            return parseDate(text, pattern);
        } catch (ParseException pe) {
            LOG.error("Failed to parse {} to date in format {}.", text, pattern, pe);
            return failover;
        }
    }

    /**
     * 使用指定的格式转换日期
     * 如果转换失败，尝试使用auto
     */
    public static Date parseDate(String text, String pattern, boolean tryAutoIfFailed) throws ParseException {
        if (text == null || text.isEmpty()) return null;
        text = text.trim();

        DateFormat df = SIMPLE_FORMATTERS.get(pattern);
        if (df == null) {
            df = FULL_FORMATTERS.get(pattern);
            if (df == null) {
                df = new DateFormat(pattern);
            }
        }

        try {
            return df.parse(text);
        } catch (ParseException pe) {
            if (tryAutoIfFailed) {
                return parseDateAuto(text);
            } else {
                throw pe;
            }
        }
    }

    /**
     * 使用指定的格式转换日期
     * 如果转换失败，尝试使用auto
     * 不抛异常，调用方提供解决方案
     */
    public static Date parseDate(String text, String pattern, boolean tryAutoIfFailed, Date failover) {
        try {
            return parseDate(text, pattern, tryAutoIfFailed);
        } catch (ParseException pe) {
            LOG.error("Failed to parse {} to date in format {}.", text, pattern, pe);
            return failover;
        }
    }

    /**
     * 尝试使用所有的格式转换日期
     */
    public static Date parseDateAuto(String text) throws ParseException {
        if (text == null || text.isEmpty()) return null;
        text = text.trim();

        // 全日期格式
        boolean containsT = text.contains("T");
        if (containsT) {
            return FULL_FORMATTERS.get(text.endsWith("Z") ? PATTERN_FULL_2 : PATTERN_FULL_1).parse(text);
        }

        // 自定义日期格式
        Date date = tryAllSimple(text);
        // 所有格式尝试失败，判断是否为毫秒数格式
        if (date == null && text.matches("\\d+")) {
            try {
                date = new Date(Long.parseLong(text));
            } catch (Exception e1) {
                // do nothing
            }
        }
        if (date == null) {
            throw new ParseException("parse [" + text + "] to date failed.", 0);
        } else {
            return date;
        }
    }

    /**
     * 尝试使用所有的格式转换日期
     * 不抛异常，调用方提供解决方案
     */
    public static Date parseDateAuto(String text, Date failover) {
        try {
            return parseDateAuto(text);
        } catch (ParseException pe) {
            LOG.error("Failed to parse {} to date.", text, pe);
            return failover;
        }
    }

    private static Date tryAllSimple(String text) {
        for (Entry<String, DateFormat> formatter : SIMPLE_FORMATTERS.entrySet()) {
            // 校验数据与格式定义是否匹配
            if (valid(text, formatter.getKey())) {
                // 尝试parse
                try {
                    return formatter.getValue().parse(text);
                } catch (Exception e1) {
                    // ignore
                }
            }
        }
        return null;
    }

    private static boolean valid(String text, String formatter) {
        if (text.length() != formatter.length()) {
            return false;
        }
        for (Entry<String, Integer> conf : PATTERN_MAX_VALUE.entrySet()) {
            int start = formatter.indexOf(conf.getKey());
            if (start >= 0) {
                int length = conf.getKey().length();
                int maxVal = conf.getValue();
                try {
                    int val = Integer.parseInt(text.substring(start, start + length));
                    if (val > maxVal) {
                        return false;
                    }
                } catch (Exception e1) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 使用默认格式格式化日期
     */
    public static String formatDate(Date date) {
        return SIMPLE_FORMATTERS.get("yyyy-MM-dd").format(date);
    }

    /**
     * 使用默认格式格式化日期+时间
     */
    public static String formatDateTime(Date date) {
        return SIMPLE_FORMATTERS.get("yyyy-MM-dd HH:mm:ss").format(date);
    }

    /**
     * 根据传入的日期格式化pattern将传入的日期格式化成字符串。
     */
    public static String format(final Date date, final String pattern) {
        if (date == null) {
            return null;
        }
        DateFormat df = SIMPLE_FORMATTERS.get(pattern);
        if (df == null) {
            df = FULL_FORMATTERS.get(pattern);
            if (df == null) {
                df = new DateFormat(pattern);
            }
        }

        return df.format(date);
    }

    /**
     * SimpleDateFormat 内部使用Calendar，不支持并发
     * 故需要每次new出来再使用
     */
    static class DateFormat {
        private final String pattern;

        DateFormat(String pattern) {
            this.pattern = pattern;
        }

        String format(Date date) {
            return new SimpleDateFormat(pattern).format(date);
        }

        Date parse(String text) throws ParseException {
            return new SimpleDateFormat(pattern).parse(text);
        }
    }

}
