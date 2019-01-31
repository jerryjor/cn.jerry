package cn.jerry.springboot.demo.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class DateUtil {
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
    private static final String PATTERN_FULL = PATTERN_YEAR + "%s" + PATTERN_MONTH + "%s" + PATTERN_DAY + "'T'"
            + PATTERN_HOUR + ":" + PATTERN_MINUTE + ":" + PATTERN_SECOND + "." + PATTERN_MILLIS + "Z";

    public static final long MS_PER_DAY = 24 * 60 * 60 * 1000L;
    public static final TimeZone DEFAULT_TIME_ZONE = Calendar.getInstance().getTimeZone();

    private static final Map<String, SimpleDateFormat> FORMATTERS;

    /**
     * 生成常用的日期格式
     * 限定的年月日分别为4位、2位、2位数字
     * 限定常见的日期分隔符和时间分隔符，或不使用分隔符
     *
     * @return
     */
    static {
        FORMATTERS = new HashMap<>();
        String[] dateSplit = new String[]{"", "-", ".", "/"};
        String[] timeSplit = new String[]{"", ":"};
        String[] millisSplit = new String[]{"", ".", ":"};
        String pattern;
        for (int i = 0; i < dateSplit.length; i++) {
            // 日期
            pattern = String.format(PATTERN_DATE, dateSplit[i], dateSplit[i]);
            FORMATTERS.put(pattern, new SimpleDateFormat(pattern));
            // 全日期
            pattern = String.format(PATTERN_FULL, dateSplit[i], dateSplit[i]);
            FORMATTERS.put(pattern, new SimpleDateFormat(pattern));
        }
        for (int j = 0; j < timeSplit.length; j++) {
            // 时间
            pattern = String.format(PATTERN_TIME, timeSplit[j], timeSplit[j]);
            FORMATTERS.put(pattern, new SimpleDateFormat(pattern));
            for (int k = 0; k < millisSplit.length; k++) {
                // 时间，带毫秒
                pattern = String.format(PATTERN_TIME_MILLIS, timeSplit[j], timeSplit[j], millisSplit[k]);
                FORMATTERS.put(pattern, new SimpleDateFormat(pattern));
            }
        }
        for (int i = 0; i < dateSplit.length; i++) {
            for (int j = 0; j < timeSplit.length; j++) {
                // 日期时间
                pattern = String.format(PATTERN_DATETIME, dateSplit[i], dateSplit[i], timeSplit[j], timeSplit[j]);
                FORMATTERS.put(pattern, new SimpleDateFormat(pattern));
                // 日期空格时间
                pattern = String.format(PATTERN_DATE_TIME, dateSplit[i], dateSplit[i], timeSplit[j], timeSplit[j]);
                FORMATTERS.put(pattern, new SimpleDateFormat(pattern));
                for (int k = 0; k < millisSplit.length; k++) {
                    // 日期时间，带毫秒
                    pattern = String.format(PATTERN_DATETIME_MILLIS, dateSplit[i], dateSplit[i], timeSplit[j], timeSplit[j], millisSplit[k]);
                    FORMATTERS.put(pattern, new SimpleDateFormat(pattern));
                    // 日期空格时间，带毫秒
                    pattern = String.format(PATTERN_DATE_TIME_MILLIS, dateSplit[i], dateSplit[i], timeSplit[j], timeSplit[j], millisSplit[k]);
                    FORMATTERS.put(pattern, new SimpleDateFormat(pattern));
                }
            }
        }
    }

    /**
     * 计算日期
     *
     * @param date 基数日期
     * @param days 天数，正数为之后，负数为之前
     * @return 结果日期
     */
    public static Date addDay(Date date, int days) {
        return new Date(date.getTime() + days * MS_PER_DAY);
    }

    /**
     * 计算日期是自1970-1-1以来地几天，以0开始
     *
     * @param date
     * @return
     */
    public static long calcDaysFrom19700101(Date date) {
        return calcDaysFrom19700101(date, DEFAULT_TIME_ZONE);
    }

    /**
     * 计算日期是自1970-1-1以来地几天，以0开始
     *
     * @param date
     * @param tz
     * @return
     */
    public static long calcDaysFrom19700101(Date date, TimeZone tz) {
        return (date.getTime() + tz.getRawOffset()) / MS_PER_DAY;
    }

    /**
     * 截取日期，去除时间
     *
     * @param date
     * @return
     */
    public static Date truncToDay(Date date) {
        return truncToDay(date, DEFAULT_TIME_ZONE);
    }

    /**
     * 截取日期，去除时间
     *
     * @param date
     * @param tz
     * @return
     */
    public static Date truncToDay(Date date, TimeZone tz) {
        long days = (date.getTime() + tz.getRawOffset()) / MS_PER_DAY;
        return new Date(days * MS_PER_DAY - tz.getRawOffset());
    }

    /**
     * 使用指定的格式转换日期
     *
     * @param text
     * @param pattern
     * @return
     */
    public static Date parseDate(String text, String pattern) throws ParseException {
        return parseDate(text, pattern, false);
    }

    /**
     * 使用指定的格式转换日期
     * 如果转换失败，尝试使用auto
     *
     * @param text
     * @param pattern
     * @param tryAutoIfFailed
     * @return
     */
    public static Date parseDate(String text, String pattern, boolean tryAutoIfFailed) throws ParseException {
        if (text == null || text.isEmpty()) return null;
        text = text.trim();

        SimpleDateFormat df = FORMATTERS.get(pattern);
        if (df == null) {
            df = new SimpleDateFormat(pattern);
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
     * 尝试使用所有的格式转换日期
     *
     * @param text
     * @return
     */
    public static Date parseDateAuto(String text) throws ParseException {
        if (text == null || text.isEmpty()) return null;
        text = text.trim();
        String text2 = text.replace("T", "'T'");

        // 默认格式失败时，尝试所有格式
        for (Entry<String, SimpleDateFormat> formatter : FORMATTERS.entrySet()) {
            // 校验字符串长度与格式长度一致性
            if (formatter.getKey().length() != (text2.length() - (formatter.getKey().endsWith("Z") ? 4 : 0))) continue;
            // 校验month
            if (!valid(text2, formatter.getKey().indexOf(PATTERN_MONTH), 2, 12)) continue;
            // 校验day
            if (!valid(text2, formatter.getKey().indexOf(PATTERN_DAY), 2, 31)) continue;
            // 校验hour
            if (!valid(text2, formatter.getKey().indexOf(PATTERN_HOUR), 2, 23)) continue;
            // 校验minute
            if (!valid(text2, formatter.getKey().indexOf(PATTERN_MINUTE), 2, 59)) continue;
            // 校验second
            if (!valid(text2, formatter.getKey().indexOf(PATTERN_SECOND), 2, 59)) continue;

            try {
                return formatter.getValue().parse(text);
            } catch (Exception e1) {
                // do nothing
            }
        }
        // 所有格式尝试失败，判断是否为毫秒数格式
        if (text.matches("\\d+")) {
            System.out.println(text);
            try {
                return new Date(Long.parseLong(text));
            } catch (Exception e1) {
                // do nothing
            }
        }
        throw new ParseException("parse [" + text + "] to date failed.", 0);
    }

    private static boolean valid(String text, int start, int length, int maxVal) {
        if (start < 0) return true;
        try {
            int val = Integer.parseInt(text.substring(start, start + length));
            return val <= maxVal;
        } catch (Exception e1) {
            return false;
        }
    }

    /**
     * 使用默认格式格式化日期
     *
     * @param date
     * @return
     */
    public static String formatDate(Date date) {
        return FORMATTERS.get("yyyy-MM-dd").format(date);
    }

    /**
     * 使用默认格式格式化日期+时间
     *
     * @param date
     * @return
     */
    public static String formatDateTime(Date date) {
        return FORMATTERS.get("yyyy-MM-dd HH:mm:ss").format(date);
    }

    /**
     * 根据传入的日期格式化pattern将传入的日期格式化成字符串。
     *
     * @param date    要格式化的日期对象
     * @param pattern 日期格式化pattern
     * @return 格式化后的日期字符串
     */
    public static String format(final Date date, final String pattern) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat df = FORMATTERS.get(pattern);
        if (df == null) {
            df = new SimpleDateFormat(pattern);
            // FORMATTERS.put(patten, df);
        }

        return df.format(date);
    }
}
