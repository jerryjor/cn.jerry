package cn.jerry.lang;

import java.util.Calendar;
import java.util.Date;

public class CalendarUtil {

    /**
     * 获取Calendar实例
     * 
     * @return
     */
    public static Calendar getCalendar() {
        return Calendar.getInstance();
    }

    /**
     * 获取Calendar实例
     * 
     * @param date
     * @return
     */
    public static Calendar getCalendar(Date date) {
        Calendar c = getCalendar();
        c.setTime(date);
        return c;
    }

    /**
     * 获取日期year
     * 
     * @param c
     * @return
     */
    public static int getYear(Calendar c) {
        return c.get(Calendar.YEAR);
    }

    /**
     * 获取日期month
     * 
     * @param c
     * @return
     */
    public static int getMonth(Calendar c) {
        return c.get(Calendar.MONTH) + 1;
    }

    /**
     * 获取日期day
     * 
     * @param c
     * @return
     */
    public static int getDay(Calendar c) {
        return c.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 取得给定时间的小时数
     * 
     * @param c
     * @return
     */
    public static int getHour(Calendar c) {
        return c.get(Calendar.HOUR);
    }

    /**
     * 取得给定时间的分钟数
     * 
     * @param c
     * @return
     */
    public static int getMinute(Calendar c) {
        return c.get(Calendar.MINUTE);
    }

    /**
     * 取得给定时间的秒数
     * 
     * @param c
     * @return
     */
    public static int getSecond(Calendar c) {
        return c.get(Calendar.SECOND);
    }

    /**
     * 跳至当前月份第一天
     * 
     * @param c
     * @return
     */
    public static Calendar jumpMonthStart(Calendar c) {
        c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
        return c;
    }

    /**
     * 跳至当前月份最后一天
     * 
     * @param c
     * @return
     */
    public static Calendar jumpMonthEnd(Calendar c) {
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        return c;
    }

    /**
     * 在日期上加上整年
     * 
     * @param c
     * @param years
     * @return
     */
    public static Calendar addYear(Calendar c, int years) {
        c.add(Calendar.YEAR, years);
        return c;
    }

    /**
     * 在日期上加上整月
     * 
     * @param c
     * @param months
     * @return
     */
    public static Calendar addMonth(Calendar c, int months) {
        c.add(Calendar.MONTH, months);
        return c;
    }

    /**
     * 在日期上加上整数天
     * 
     * @param c
     * @param days
     * @return
     */
    public static Calendar addDay(Calendar c, int days) {
        c.add(Calendar.DATE, days);
        return c;
    }

    /**
     * 判断日期是否属于月初月末
     * 
     * @param c
     * @param days
     * @return
     */
    public static boolean isMonthHeadOrTail(Calendar c, int days) {
        Date currDay = c.getTime();
        Date maxHeadDay = addDay(jumpMonthStart(c), days).getTime();
        Date minTailDay = addDay(jumpMonthEnd(c), -days).getTime();
        // 恢复时间
        c.setTime(currDay);

        return currDay.before(maxHeadDay) || currDay.after(minTailDay);
    }

}
