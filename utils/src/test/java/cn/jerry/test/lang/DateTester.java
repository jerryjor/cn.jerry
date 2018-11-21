package cn.jerry.test.lang;

import cn.jerry.lang.DateUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateTester {
	public static void main(String[] args) {
//
//		Calendar c = Calendar.getInstance();
//		c.setTime(new Date());
//		c.add(Calendar.MINUTE, -c.get(Calendar.MINUTE) % 10);
//		//c.set(Calendar.MINUTE, (c.get(Calendar.MINUTE) / 10) * 10);
//		c.set(Calendar.SECOND, 0);
//		System.out.println(c.getTime().getTime());
//		
//
//        Date now = new Date();
//		long currMin = (now.getTime() % (60 * 60 * 1000L)) / (60 * 1000L);
//		System.out.println(currMin);
		try {
            Date date = DateUtil.parseDateAuto("2001-07-04T12:08:56.235+0800");
            System.out.println(Calendar.getInstance().getTimeZone().getRawOffset());
            System.out.println("reqTime:" + date.getTime());
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(date));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
