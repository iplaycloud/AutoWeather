package com.tchip.weather.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.TextUnderstander;
import com.iflytek.cloud.TextUnderstanderListener;
import com.iflytek.cloud.UnderstanderResult;
import com.tchip.weather.Constant;
import com.tchip.weather.MyApp;
import com.tchip.weather.R;
import com.tchip.weather.util.MyLog;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.text.TextUtils;

public class WeatherService extends Service {
	private TextUnderstander mTextUnderstander;
	private SharedPreferences preferences;
	private Editor editor;

	private String strNotLocate, cityName;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		preferences = getSharedPreferences(Constant.MySP.FILE_NAME,
				Context.MODE_PRIVATE);
		editor = preferences.edit();

		mTextUnderstander = TextUnderstander.createTextUnderstander(
				getApplicationContext(), textUnderstanderListener);

		strNotLocate = getResources().getString(R.string.not_locate);
		if (MyApp.isUseLocate) {
			cityName = preferences.getString(Constant.MySP.STR_LOC_CITY_NAME,
					strNotLocate);
		} else {
			cityName = preferences.getString(Constant.MySP.STR_MANUL_CITY,
					strNotLocate);
		}

		if ("肇庆市".equals(cityName) || "肇庆".equals(cityName)) {
			cityName = "佛山市";
		} else if ("江门市".equals(cityName) || "江门".equals(cityName)) {
			cityName = "中山市";
		} else if ("梅州市".equals(cityName) || "梅州".equals(cityName)) {
			cityName = "潮州市";
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (strNotLocate.equals(cityName)) {
			cityName = preferences
					.getString("cityNameRealButOld", strNotLocate);
			if (strNotLocate.equals(cityName)) {
				String addrStr = preferences.getString("addrStr", strNotLocate);
				if (addrStr.contains("省") && addrStr.contains("市")) {
					cityName = addrStr.split("省")[1].split("市")[0];
				} else if ((!addrStr.contains("省")) && addrStr.contains("市")) {
					cityName = addrStr.split("市")[0];
				} else {
					cityName = addrStr;
				}
			}
		}
		getWeather(cityName);

		return super.onStartCommand(intent, flags, startId);
	}

	private InitListener textUnderstanderListener = new InitListener() {

		@Override
		public void onInit(int code) {
			if (code != ErrorCode.SUCCESS) {
				// 初始化失败,错误码：" + code
				editor.putString("exception", "初始化失败,错误码：" + code);
			}
		}
	};

	private TextUnderstanderListener textListener = new TextUnderstanderListener() {

		@Override
		public void onResult(final UnderstanderResult result) {

			if (null != result) {
				// 获取结果
				String jsonString = result.getResultString();
				if (!TextUtils.isEmpty(jsonString)) {
					JSONObject jsonObject;
					try {
						jsonObject = new JSONObject(jsonString);
						JSONArray mJSONArray = jsonObject.getJSONObject("data")
								.getJSONArray("result");
						for (int i = 0; i < 7; i++) {
							JSONObject jsonDay = mJSONArray
									.getJSONObject(i + 1); // 跳过天气数组第一个数据
							String tempRange = jsonDay.getString("tempRange"); // 31℃~26℃
							String tempArray[] = tempRange.split("~");
							editor.putString("postTime",
									jsonDay.getString("lastUpdateTime"));

							editor.putString("day" + i + "weather",
									jsonDay.getString("weather"));
							editor.putString("day" + i + "tmpHigh",
									tempArray[0]);
							editor.putString("day" + i + "tmpLow", tempArray[1]);
							if (i == 0) {
								editor.putString("humidity",
										jsonDay.getString("humidity"));
								editor.putString("airQuality",
										jsonDay.getString("airQuality"));
							}

							String windDirection = jsonDay.getString("wind");
							if ("无持续风向微风".equals(windDirection))
								windDirection = "微风";
							editor.putString("day" + i + "wind", windDirection);
							// + jsonDay.getString("windLevel")
							editor.putString("day" + i + "date",
									jsonDay.getString("date"));

							editor.commit();
						}
					} catch (JSONException e) {
						e.printStackTrace();
						editor.putString("exception", e.toString());
						editor.commit();
					} finally {
						stopSelf();
					}
				}
			} else {
				editor.putString("exception", " 识别结果不正确");
				editor.commit();
				// 识别结果不正确
			}

		}

		@Override
		public void onError(SpeechError error) {
			// showTip("onError Code：" + error.getErrorCode());
		}
	};

	private void getWeather(String cityStr) {
		try {
			int ret = 0;// 函数调用返回值
			if (strNotLocate.equals(cityStr)) {
				cityStr = "深圳";
			}

			MyApp.nowCityName = cityStr;

			String text = cityStr + "天气";
			MyLog.v("WeatherService:Get city:" + cityStr);

			if (mTextUnderstander.isUnderstanding()) {
				mTextUnderstander.cancel();
				// showTip("取消");
			} else {
				ret = mTextUnderstander.understandText(text, textListener);
				if (ret != 0) {
					// showTip("语义理解失败,错误码:" + ret);
				}
			}
		} catch (Exception e) {
			MyLog.e("[WeatherService]getWeather catch exception:"
					+ e.toString());
		}
	}

}
