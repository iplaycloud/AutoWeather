package com.tchip.weather.util;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.tchip.weather.Constant;
import com.tchip.weather.MyApp;
import com.tchip.weather.R;
import com.tchip.weather.view.WeatherDynamicCloudyView;
import com.tchip.weather.view.WeatherDynamicRainView;

public class WeatherUtil {

	/** 背景图是否根据天气变化 **/
	public static boolean fancyBackground = true;

	public static enum WEATHER_TYPE {
		CLOUD, SUN, RAIN, SNOW, FOG, RAIN_SNOW, HAIL
	}

	/** 当前需要获取天气的城市 **/
	public static String getCityName(Context context) {
		String strNotLocate = context.getResources().getString(
				R.string.not_locate);

		String cityName;
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				Constant.MySP.FILE_NAME, Context.MODE_PRIVATE);

		if (MyApp.isUseLocate) {
			cityName = sharedPreferences.getString(
					Constant.MySP.STR_LOC_CITY_NAME, strNotLocate);

			if (strNotLocate.equals(cityName)) {
				cityName = sharedPreferences.getString(
						Constant.MySP.STR_LOC_CITY_NAME_OLD, strNotLocate);

				if (strNotLocate.equals(cityName)) {
					String locAddress = sharedPreferences.getString(
							Constant.MySP.STR_LOC_ADDRESS, strNotLocate);
					if (locAddress.contains("省") && locAddress.contains("市")) {
						cityName = locAddress.split("省")[1].split("市")[0];
					} else if ((!locAddress.contains("省"))
							&& locAddress.contains("市")) {
						cityName = locAddress.split("市")[0];
					} else {
						cityName = locAddress;
					}
				}
			} else {
				cityName = sharedPreferences.getString(
						Constant.MySP.STR_LOC_CITY_NAME, strNotLocate);
			}
		} else {
			cityName = sharedPreferences.getString(
					Constant.MySP.STR_MANUL_CITY, strNotLocate);
		}
		return cityName;

	}

	/** 当前需要获取天气的城市 **/
	public static String getCityAndDistrictName(Context context) {
		String strNotLocate = context.getResources().getString(
				R.string.not_locate);

		String cityName;
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				Constant.MySP.FILE_NAME, Context.MODE_PRIVATE);

		if (MyApp.isUseLocate) {
			cityName = sharedPreferences.getString(
					Constant.MySP.STR_LOC_CITY_NAME, strNotLocate);

			if (strNotLocate.equals(cityName)) {
				cityName = sharedPreferences.getString(
						Constant.MySP.STR_LOC_CITY_NAME_OLD, strNotLocate);

				if (strNotLocate.equals(cityName)) {
					String locAddress = sharedPreferences.getString(
							Constant.MySP.STR_LOC_ADDRESS, strNotLocate);
					if (locAddress.contains("省") && locAddress.contains("市")) {
						cityName = locAddress.split("省")[1].split("市")[0];
					} else if ((!locAddress.contains("省"))
							&& locAddress.contains("市")) {
						cityName = locAddress.split("市")[0];
					} else {
						cityName = locAddress;
					}
				}
			} else {
				cityName = sharedPreferences.getString(
						Constant.MySP.STR_LOC_CITY_NAME, strNotLocate);
			}
			cityName = cityName + sharedPreferences.getString("district", "");
		} else {
			cityName = sharedPreferences.getString(
					Constant.MySP.STR_MANUL_CITY, strNotLocate);
		}
		return cityName;

	}

	/** 天气是否获取成功 **/
	public static boolean isLocalHasWeather(Context context) {

		SharedPreferences sharedPreferences = context.getSharedPreferences(
				Constant.MySP.FILE_NAME, Context.MODE_PRIVATE);

		String strNotLocate = context.getResources().getString(
				R.string.not_locate);
		String strUnknown = context.getResources().getString(R.string.unknown);

		String weatherToday = sharedPreferences.getString("day0weather",
				strUnknown);

		boolean isGetSuccess = false;
		String cityName = getCityName(context);
		if (strNotLocate.equals(cityName)) {
			isGetSuccess = false;
		} else if (strUnknown.equals(weatherToday)) {
			isGetSuccess = false;
		} else {
			isGetSuccess = true;
		}

		return isGetSuccess;
	}

	public static enum WEATHER_INFO {
		/** 天气：晴 **/
		WEATHER,

		/** 天气发布日期 **/
		POST_TIME,

		/** 日期 **/
		DATE,

		/** 气温 **/
		TEMP,

		/** 湿度 **/
		HUMIDITY,

		/** 风向风速 **/
		WIND,

		/** 所有信息 **/
		ALL

	}

	/**
	 * 获取本地存储的天气信息
	 * 
	 * @param day
	 *            第几天(0-今天,1,2,3,4,5,)
	 * @param type
	 *            信息类型
	 * 
	 *            0-天气：晴
	 * 
	 *            1-日期
	 * @return
	 */
	public static String getLocalWeatherInfo(Context context, int day,
			WEATHER_INFO type) {

		SharedPreferences sharedPreferences = context.getSharedPreferences(
				Constant.MySP.FILE_NAME, Context.MODE_PRIVATE);
		String strUnknown = context.getResources().getString(R.string.unknown);
		String strDefaultWind = context.getResources().getString(
				R.string.weather_default_wind_info);
		String strWeatherColon = context.getResources().getString(
				R.string.weather_colon);

		String cityName = getCityName(context);

		String weather = sharedPreferences.getString("day" + day + "weather",
				strUnknown);

		String tempLow = sharedPreferences.getString("day" + day + "tmpLow",
				"15℃").split("℃")[0];
		String tempHigh = sharedPreferences.getString("day" + day + "tmpHigh",
				"25℃").split("℃")[0];
		String temp = tempLow + "~" + tempHigh;

		String wind = sharedPreferences.getString("day" + day + "wind",
				strDefaultWind);

		String postTime = context.getResources().getString(
				R.string.weather_post_time)
				+ sharedPreferences.getString("postTime", "2015 05:55:55")
						.split(" ")[1];
		// String dateStr = sharedPreferences.getString("day" + day + "date",
		// "2016-05-05"); // 2016-01-09
		// String date = dateStr.substring(0, 4) + "年" + dateStr.substring(5, 7)
		// + "月" + dateStr.substring(8, 10) + "日";

		Calendar calendar = Calendar.getInstance();
		int systemYear = calendar.get(Calendar.YEAR);
		int systemMonth = calendar.get(Calendar.MONTH) + 1;
		int systemDay = calendar.get(Calendar.DAY_OF_MONTH) + day;

		String date = systemYear + "年" + systemMonth + "月" + systemDay + "日";
		if (systemYear < 2016) {
			date = "";
		}

		String allInfo = cityName
				+ date
				+ strWeatherColon
				+ weather
				+ ","
				+ tempLow
				+ context.getResources().getString(
						R.string.weather_temp_range_to) + tempHigh + "℃,"
				+ wind;

		switch (type) {

		case WEATHER:
			return weather;

		case TEMP:
			return temp;

		case WIND:
			return wind;

		case POST_TIME:
			return postTime;

		case DATE:
			return date;

		case ALL:
		default:
			return allInfo;
		}

	}

	/** 多云动画 **/
	public static void cloudAnimation(Context context, FrameLayout flLayout) {
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.weather_cloud_1);
		flLayout.removeAllViews();
		WeatherDynamicCloudyView view1 = new WeatherDynamicCloudyView(context,
				bitmap, -150, 50, 30);
		flLayout.addView(view1);
		view1.move();

		WeatherDynamicCloudyView view2 = new WeatherDynamicCloudyView(context,
				bitmap, 280, 150, 60);
		flLayout.addView(view2);
		view2.move();

		WeatherDynamicCloudyView view3 = new WeatherDynamicCloudyView(context,
				bitmap, 140, 220, 40);
		flLayout.addView(view3);
		view3.move();
	}

	/** 下雨动画 **/
	public static void rainAnimation(Context context, FrameLayout flLayout) {
		flLayout.removeAllViews();

		DisplayMetrics dm = new DisplayMetrics();
		WindowManager manager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		display.getMetrics(dm);
		int screenMax = Math.max(dm.widthPixels, dm.heightPixels);

		int rainSpanX = screenMax / 10;
		int rainSpanY = 150;

		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.weather_rain_drop);
		WeatherDynamicRainView view1 = new WeatherDynamicRainView(context,
				bitmap, 100 + rainSpanX * 0, 50 + rainSpanY * 0, 30);
		flLayout.addView(view1);
		view1.move();

		WeatherDynamicRainView view2 = new WeatherDynamicRainView(context,
				bitmap, 100 + rainSpanX * 1, 50 + rainSpanY * 1, 20);
		flLayout.addView(view2);
		view2.move();

		WeatherDynamicRainView view3 = new WeatherDynamicRainView(context,
				bitmap, 100 + rainSpanX * 2, 50 + rainSpanY * 3, 40);
		flLayout.addView(view3);
		view3.move();

		WeatherDynamicRainView view4 = new WeatherDynamicRainView(context,
				bitmap, 100 + rainSpanX * 3, 50 + rainSpanY * 2, 10);
		flLayout.addView(view4);
		view4.move();

		WeatherDynamicRainView view5 = new WeatherDynamicRainView(context,
				bitmap, 100 + rainSpanX * 4, 50 + rainSpanY * 1, 30);
		flLayout.addView(view5);
		view5.move();

		WeatherDynamicRainView view6 = new WeatherDynamicRainView(context,
				bitmap, 100 + rainSpanX * 5, 50 + rainSpanY * 2, 20);
		flLayout.addView(view6);
		view6.move();

		WeatherDynamicRainView view7 = new WeatherDynamicRainView(context,
				bitmap, 100 + rainSpanX * 6, 50 + rainSpanY * 0, 40);
		flLayout.addView(view7);
		view7.move();

		WeatherDynamicRainView view8 = new WeatherDynamicRainView(context,
				bitmap, 100 + rainSpanX * 7, 50 + rainSpanY * 1, 30);
		flLayout.addView(view8);
		view8.move();

		WeatherDynamicRainView view9 = new WeatherDynamicRainView(context,
				bitmap, 100 + rainSpanX * 8, 50 + rainSpanY * 2, 20);
		flLayout.addView(view9);
		view9.move();
	}

	/** 下雪动画 **/
	public static void snowAnimation(Context context, FrameLayout flLayout) {
		flLayout.removeAllViews();

		DisplayMetrics dm = new DisplayMetrics();
		WindowManager manager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		display.getMetrics(dm);
		int screenMax = Math.max(dm.widthPixels, dm.heightPixels);

		int rainSpanX = screenMax / 10;
		int rainSpanY = 150;

		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.weather_snow_flake);
		WeatherDynamicRainView view1 = new WeatherDynamicRainView(context,
				bitmap, 100 + rainSpanX * 0, 50 + rainSpanY * 0, 60);
		flLayout.addView(view1);
		view1.move();

		WeatherDynamicRainView view2 = new WeatherDynamicRainView(context,
				bitmap, 100 + rainSpanX * 1, 50 + rainSpanY * 1, 40);
		flLayout.addView(view2);
		view2.move();

		WeatherDynamicRainView view3 = new WeatherDynamicRainView(context,
				bitmap, 100 + rainSpanX * 2, 50 + rainSpanY * 3, 80);
		flLayout.addView(view3);
		view3.move();

		WeatherDynamicRainView view4 = new WeatherDynamicRainView(context,
				bitmap, 100 + rainSpanX * 3, 50 + rainSpanY * 2, 60);
		flLayout.addView(view4);
		view4.move();

		WeatherDynamicRainView view5 = new WeatherDynamicRainView(context,
				bitmap, 100 + rainSpanX * 4, 50 + rainSpanY * 1, 50);
		flLayout.addView(view5);
		view5.move();

		WeatherDynamicRainView view6 = new WeatherDynamicRainView(context,
				bitmap, 100 + rainSpanX * 5, 50 + rainSpanY * 2, 70);
		flLayout.addView(view6);
		view6.move();

		WeatherDynamicRainView view7 = new WeatherDynamicRainView(context,
				bitmap, 100 + rainSpanX * 6, 50 + rainSpanY * 0, 40);
		flLayout.addView(view7);
		view7.move();

		WeatherDynamicRainView view8 = new WeatherDynamicRainView(context,
				bitmap, 100 + rainSpanX * 7, 50 + rainSpanY * 1, 90);
		flLayout.addView(view8);
		view8.move();

		WeatherDynamicRainView view9 = new WeatherDynamicRainView(context,
				bitmap, 100 + rainSpanX * 8, 50 + rainSpanY * 2, 50);
		flLayout.addView(view9);
		view9.move();

	}

	/**
	 * 根据天气字段获取天气类型
	 * 
	 * @param weather
	 * @return
	 */
	public static WEATHER_TYPE getTypeByStr(String weather) {

		if (weather.contains("雪")) {
			return WEATHER_TYPE.SNOW;
		} else if (weather.contains("冻雨") || weather.contains("冰雹")) {
			return WEATHER_TYPE.HAIL;
		} else if (weather.contains("雨")) {
			return WEATHER_TYPE.RAIN;
		} else if (weather.contains("雾") || weather.contains("霾")
				|| weather.contains("浮尘") || weather.contains("沙尘")
				|| weather.contains("扬沙")) {
			return WEATHER_TYPE.FOG;
		} else if (weather.contains("阴") || weather.contains("多云")) {
			return WEATHER_TYPE.CLOUD;
		} else {
			return WEATHER_TYPE.SUN;
		}

	}

	/**
	 * 天气界面背景图片
	 * 
	 * @param type
	 * @return
	 */
	public static int getWeatherBackground(WEATHER_TYPE type) {
		if (!fancyBackground) {
			return R.drawable.ui_weather_bg;
		} else {
			switch (type) {
			case SUN:
				return isDay() ? (R.drawable.icon_weather_bg_sun_day)
						: (R.drawable.icon_weather_bg_sun_night);

			case CLOUD:
				return isDay() ? R.drawable.icon_weather_bg_cloud_day
						: R.drawable.icon_weather_bg_cloud_night;

			case RAIN:
				return R.drawable.icon_weather_bg_rain;

			case HAIL:
			case SNOW:
				return R.drawable.icon_weather_bg_snow;

			case RAIN_SNOW:
				return R.drawable.icon_weather_bg_rain_snow;

			case FOG:
				return isDay() ? (R.drawable.icon_weather_bg_sun_day)
						: (R.drawable.icon_weather_bg_sun_day);

			default:
				return isDay() ? (R.drawable.icon_weather_bg_default_day)
						: (R.drawable.icon_weather_bg_default_night);
			}
		}
	}

	/**
	 * 天气大图标
	 * 
	 * @param type
	 * @return
	 */
	public static int getWeatherDrawable(WEATHER_TYPE type) {

		switch (type) {
		case SUN:
			return isDay() ? (R.drawable.ui_weather_sun_day)
					: (R.drawable.ui_weather_sun_night);

		case CLOUD:
			return R.drawable.ui_weather_cloud;

		case RAIN:
			return R.drawable.ui_weather_rain;

		case SNOW:
			return R.drawable.ui_weather_snow;

		case HAIL:
			return R.drawable.ui_weather_hail;

		case RAIN_SNOW:
			return R.drawable.ui_weather_rain_snow;

		case FOG:
			return R.drawable.ui_weather_fog;

		default:
			return isDay() ? (R.drawable.icon_weather_bg_default_day)
					: (R.drawable.icon_weather_bg_default_night);
		}
	}

	/**
	 * 是否是白天
	 * 
	 * @return
	 */
	public static boolean isDay() {
		Date date = new Date();
		int hour = date.getHours();
		if (hour > 18 || hour < 6)
			return false;
		return true;
	}
}
