package com.tchip.weather;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.iflytek.cloud.SpeechUtility;
import com.tchip.weather.util.MyLog;
import com.tchip.weather.util.MyUncaughtExceptionHandler;

public class MyApp extends Application {

	public static boolean isActivityShowing = true;

	private SharedPreferences sharedPreferences;

	/** 是否使用定位城市（配置） **/
	public static boolean isUseLocate = true;

	/** 是否使用定位城市（当前界面显示） **/
	public static boolean isUseLocateNow = true;

	/** 城市（当前界面显示） **/
	public static String nowCityName;

	/** 是否自动播报天气 **/
	public static boolean autoSpeakWeather;

	@Override
	public void onCreate() {

		// 应用程序入口处调用,避免手机内存过小,杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
		// 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
		// 参数间使用“,”分隔。
		try {
			SpeechUtility.createUtility(this, "appid="
					+ Constant.SDK.XUNFEI_APP_ID);
		} catch (Exception e) {
			MyLog.e("[MyApplication]SpeechUtility.createUtility: Catch Exception!");
		}
		super.onCreate();
		
//		MyUncaughtExceptionHandler myUncaughtExceptionHandler = MyUncaughtExceptionHandler
//				.getInstance();
//		myUncaughtExceptionHandler.init(getApplicationContext());

		/*
		 * 百度地图SDK初始化
		 * 
		 * 初始化全局 context，指定 sdcard 路径，若采用默认路径，请使用initialize(Context context)
		 * 重载函数 参数:
		 * 
		 * sdcardPath - sd 卡路径，请确保该路径能正常使用 context - 必须是 application context，SDK
		 * 各组件中需要用到。
		 */
		// if (isMapSDExists()) {
		// SDKInitializer.initialize(Constant.Path.SD_CARD_MAP,
		// getApplicationContext());
		// } else {
		try {
			// SDKInitializer.initialize(getApplicationContext());
		} catch (Exception e) {
			MyLog.e("[MyApplication]SDKInitializer.initialize: Catch Exception!");
		}
		// }

		sharedPreferences = getSharedPreferences(Constant.MySP.FILE_NAME,
				Context.MODE_PRIVATE);

		// 是否使用定位城市天气
		isUseLocate = sharedPreferences.getBoolean(
				Constant.MySP.STR_IS_USE_LOCATE, true);
		isUseLocateNow = isUseLocate;
		if (isUseLocate) {
			nowCityName = sharedPreferences.getString(
					Constant.MySP.STR_LOC_CITY_NAME, "未定位");
		} else {
			nowCityName = sharedPreferences.getString(
					Constant.MySP.STR_MANUL_CITY,
					nowCityName = sharedPreferences.getString(
							Constant.MySP.STR_LOC_CITY_NAME, "未定位"));
		}
		MyLog.v("[Weather]MyApplication,isUseLocate:" + isUseLocate);

		// 是否自动播报天气
		autoSpeakWeather = sharedPreferences.getBoolean(
				Constant.MySP.STR_AUTO_SPEARK_WEATHER, false);

	}

}
