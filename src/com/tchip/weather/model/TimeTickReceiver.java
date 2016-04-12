package com.tchip.weather.model;

import java.util.Calendar;

import com.tchip.weather.Constant;
import com.tchip.weather.MyApp;
import com.tchip.weather.R;
import com.tchip.weather.service.LocationService;
import com.tchip.weather.service.SpeakService;
import com.tchip.weather.service.WeatherService;
import com.tchip.weather.util.MyLog;
import com.tchip.weather.util.NetworkUtil;
import com.tchip.weather.util.WeatherUtil;
import com.tchip.weather.util.WeatherUtil.WEATHER_INFO;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;

public class TimeTickReceiver extends BroadcastReceiver {

	private Context context;

	@Override
	public void onReceive(Context context, Intent intent) {

		this.context = context;

		// 获取时间
		Calendar calendar = Calendar.getInstance();
		int minute = calendar.get(Calendar.MINUTE);
		if (minute == 0) {
			int year = calendar.get(Calendar.YEAR);
			MyLog.v("[TimeTickReceiver]Year:" + year);
			if (year >= 2016) {
				int hour = calendar.get(Calendar.HOUR_OF_DAY);
				startSpeak(context, "整点报时:" + hour + "点整");
			}
			// if (MyApp.autoSpeakWeather) {
			// updateWeather();
			// }
		}

	}

	public class UpdateWeatherThread implements Runnable {

		@Override
		public void run() {
			try {
				if (MyApp.isUseLocate) {
					Thread.sleep(5000);
				}
				startWeatherService();
				Thread.sleep(5000);
				Message message = new Message();
				message.what = 1;
				updateWeatherHandler.sendMessage(message);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/** 当获取天气失败时，再次尝试的次数 */
	private int maxTryTime = 3;

	final Handler updateWeatherHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				if (WeatherUtil.isLocalHasWeather(context)) {
					SharedPreferences sharedPreferences = context
							.getSharedPreferences(Constant.MySP.FILE_NAME,
									Context.MODE_PRIVATE);
					Editor editor = sharedPreferences.edit();

					if (!sharedPreferences.getString(
							Constant.MySP.STR_LAST_SPEAK_POST_TIME, "05:55:55")
							.equals(WeatherUtil.getLocalWeatherInfo(context, 0,
									WEATHER_INFO.POST_TIME))) {
						startSpeak(context, WeatherUtil.getLocalWeatherInfo(
								context, 0, WEATHER_INFO.ALL));
						editor.putString(
								Constant.MySP.STR_LAST_SPEAK_POST_TIME,
								WeatherUtil.getLocalWeatherInfo(context, 0,
										WEATHER_INFO.POST_TIME));
						editor.commit();
					}
				} else if (maxTryTime > 0) {
					MyLog.v("[updateWeatherHandler]maxTryTime:" + maxTryTime);
					maxTryTime--;
					updateWeather();
				}
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	private void startLocationService() {
		Intent intent = new Intent(context, LocationService.class);
		context.startService(intent);
	}

	private void startWeatherService() {
		Intent intent = new Intent(context, WeatherService.class);
		context.startService(intent);
	}

	private void updateWeather() {
		if (-1 == NetworkUtil.getNetworkType(context)) {
			return;
		} else {
			MyApp.isUseLocateNow = MyApp.isUseLocate; // 同步显示和配置

			if (MyApp.isUseLocate) {
				// 清除之前的城市信息
				String strNotLocate = context.getResources().getString(
						R.string.not_locate);
				SharedPreferences sharedPreferences = context
						.getSharedPreferences(Constant.MySP.FILE_NAME,
								Context.MODE_PRIVATE);
				Editor editor = sharedPreferences.edit();
				editor.putString(Constant.MySP.STR_LOC_CITY_NAME, strNotLocate);
				editor.putString(Constant.MySP.STR_LOC_CITY_NAME_OLD,
						strNotLocate);
				editor.putString("district", strNotLocate);
				editor.putString(Constant.MySP.STR_LOC_ADDRESS, strNotLocate);
				editor.putString("street", strNotLocate);
				editor.putString(Constant.MySP.STR_LOC_TIME, strNotLocate);
				editor.commit();

				startLocationService(); // 重新定位
			}
			new Thread(new UpdateWeatherThread()).start();
		}
	}

	private void startSpeak(Context context, String content) {
		Intent intent = new Intent(context, SpeakService.class);
		intent.putExtra("content", content);
		context.startService(intent);
	}

}
