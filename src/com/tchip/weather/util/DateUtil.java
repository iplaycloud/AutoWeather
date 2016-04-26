package com.tchip.weather.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

	public static String getWeekStrByInt(int week) {
		if (week > 7)
			week = week % 7;
		switch (week) {
		case 1:
			return "星期日";
		case 2:
			return "星期一";
		case 3:
			return "星期二";
		case 4:
			return "星期三";
		case 5:
			return "星期四";
		case 6:
			return "星期五";
		case 7:
			return "星期六";
		default:
			return "星期日";
		}
	}

	/**
	 * 计算daySpan天后的日期
	 * 
	 * @param nowDate
	 *            yyyy-MM-dd
	 * @return
	 */
	public static Calendar changeDate(String nowDate, int daySpan) {
		Calendar calendar = Calendar.getInstance();
		Date date = null;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
					.parse(nowDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		calendar.setTime(date);
		int day = calendar.get(Calendar.DATE);
		calendar.set(Calendar.DATE, day + daySpan);
		return calendar;
	}

}
