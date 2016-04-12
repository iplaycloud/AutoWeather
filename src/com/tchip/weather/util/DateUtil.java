package com.tchip.weather.util;

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
	 * 根据秒钟获取“00:00:00”格式时间字符串，一天以上显示“0-00:00:00”
	 * 
	 * @param secondCount
	 * @return
	 */
	public static String getFormatTimeBySecond(int secondCount) {
		String strTime = "";
		if (secondCount < 10) { // [0,10秒)
			strTime = "00 : 0" + secondCount;
		} else if (secondCount < 60) { // [10秒,1分)
			strTime = "00 : " + secondCount;
		} else if (secondCount < 600) { // [1分,10分)
			int minutes = secondCount / 60;
			int seconds = secondCount % 60;
			if (seconds < 10)
				strTime = "0" + minutes + " : 0" + seconds;
			else
				strTime = "0" + minutes + " : " + seconds;
		} else if (secondCount < 3600) { // [10分,1时)
			int minutes = secondCount / 60;
			int seconds = secondCount % 60;
			if (seconds < 10)
				strTime = "00:" + minutes + ":0" + seconds;
			else
				strTime = "00:" + minutes + ":" + seconds;
		} else if (secondCount < 36000) { // [1时,10时)
			int hour = secondCount / 3600;
			int minutes = (secondCount - hour * 3600) / 60;
			int seconds = secondCount % 60;
			if (minutes < 10) {
				if (seconds < 10)
					strTime = "0" + hour + ":0" + minutes + ":0" + seconds;
				else
					strTime = "0" + hour + ":0" + minutes + ":" + seconds;
			} else {
				if (seconds < 10)
					strTime = "0" + hour + ":" + minutes + ":0" + seconds;
				else
					strTime = "0" + hour + ":" + minutes + ":" + seconds;
			}
		} else if (secondCount < 86400) { // [10时,1天)
			int hour = secondCount / 3600;
			int minutes = (secondCount - hour * 3600) / 60;
			int seconds = secondCount % 60;
			if (minutes < 10) {
				if (seconds < 10) {
					strTime = hour + ":0" + minutes + ":0" + seconds;
				} else {
					strTime = hour + ":0" + minutes + ":" + seconds;
				}
			} else {
				if (seconds < 10) {
					strTime = hour + ":" + minutes + ":0" + seconds;
				} else {
					strTime = hour + ":" + minutes + ":" + seconds;
				}
			}
		} else { // [1天,+oo)
			int day = secondCount / 86400;
			int hour = (secondCount - day * 86400) / 3600;
			int minutes = (secondCount - day * 86400 - hour * 3600) / 60;
			int seconds = secondCount % 60;
			if (hour < 10) {
				if (minutes < 10) {
					if (seconds < 10)
						strTime = day + "-0" + hour + ":0" + minutes + ":0"
								+ seconds;
					else
						strTime = day + "-0" + hour + ":0" + minutes + ":"
								+ seconds;
				} else {
					if (seconds < 10)
						strTime = day + "-0" + hour + ":" + minutes + ":0"
								+ seconds;
					else
						strTime = day + "-0" + hour + ":" + minutes + ":"
								+ seconds;
				}
			} else {
				if (minutes < 10) {
					if (seconds < 10)
						strTime = day + "-" + hour + ":0" + minutes + ":0"
								+ seconds;
					else
						strTime = day + "-" + hour + ":0" + minutes + ":"
								+ seconds;
				} else {
					if (seconds < 10)
						strTime = day + "-" + hour + ":" + minutes + ":0"
								+ seconds;
					else
						strTime = day + "-" + hour + ":" + minutes + ":"
								+ seconds;
				}
			}
		}
		return strTime;

	}
}
