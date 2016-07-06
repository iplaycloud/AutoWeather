package com.tchip.weather.ui;

import java.util.Calendar;

import com.tchip.weather.Constant;
import com.tchip.weather.MyApp;
import com.tchip.weather.R;
import com.tchip.weather.model.Titanic;
import com.tchip.weather.model.Typefaces;
import com.tchip.weather.service.LocationService;
import com.tchip.weather.service.WeatherService;
import com.tchip.weather.util.DateUtil;
import com.tchip.weather.util.MyLog;
import com.tchip.weather.util.NetworkUtil;
import com.tchip.weather.util.WeatherUtil;
import com.tchip.weather.util.WeatherUtil.WEATHER_INFO;
import com.tchip.weather.view.ResideMenu;
import com.tchip.weather.view.TitanicTextView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextClock;
import android.widget.TextView;

public class MainActivity extends Activity {
	private Context context;
	private SharedPreferences sharedPreferences;
	private FrameLayout frameLayout;
	private String[] weatherArray;

	/** 是否定位 **/
	private boolean isLocated = false;

	/** 获取天气信息是否成功 **/
	private boolean isGetSuccess = false;

	private String strNoLoction, strNoWeather;

	private ProgressBar updateProgress;
	private Button updateButton;

	/** 左侧帮助侧边栏 */
	// private ResideMenu resideMenu;

	private boolean isResideMenuClose = true;

	private ImageView imageShowResideMenu;
	private ImageView imageLocate; // 定位图标

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);

		sharedPreferences = getSharedPreferences(Constant.MySP.FILE_NAME,
				Context.MODE_PRIVATE);

		// 刷新按钮和进度条
		updateProgress = (ProgressBar) findViewById(R.id.updateProgress);
		updateProgress.setVisibility(View.INVISIBLE);
		updateButton = (Button) findViewById(R.id.updateButton);
		updateButton.setVisibility(View.VISIBLE);
		updateButton.setOnClickListener(new MyOnClickListener());

		initialLayout();
		initialWeatherLayout();

		if (-1 == NetworkUtil.getNetworkType(getApplicationContext())) {
			NetworkUtil.noNetworkHint(getApplicationContext());
		} else {
			updateWeather(); // 会再次调用initialLayout
			// if (WeatherUtil.isLocalHasWeather(MainActivity.this)) {
			// speakWeather(0);
			// } else {
			// updateWeather(); // 会再次调用initialLayout
			// }
		}

		// 侧边划出
		// resideMenu = new ResideMenu(this);
		// resideMenu.setBackground(R.color.grey_dark_light);
		// resideMenu.attachToActivity(this);
		// resideMenu.setMenuListener(menuListener);
		// resideMenu.setScaleValue(0.6f);
		// resideMenu.setDirectionDisable(ResideMenu.DIRECTION_RIGHT);
	}

	@Override
	protected void onResume() {
		super.onResume();
		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

		MyLog.v("[Weather]onResume");
		MyApp.isActivityShowing = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		MyLog.v("[Weather]onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		MyLog.v("[Weather]onStop");

		// 关闭LocationService
		Intent intentLocationService = new Intent(this, LocationService.class);
		stopService(intentLocationService);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MyLog.v("[Weather]onDestroy");
		MyApp.isActivityShowing = false;
	}

	private void speakVoice(String content) {
		sendBroadcast(new Intent(Constant.Broadcast.TTS_SPEAK).putExtra(
				"content", content));
	}

	/** 侧边栏打开关闭监听 **/
	private ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener() {
		@Override
		public void openMenu() {
			isResideMenuClose = false;
			// layoutHelp.setVisibility(View.GONE);
		}

		@Override
		public void closeMenu() {
			isResideMenuClose = true;

			String configCityName = ""; // 配置文件中应该显示的城市

			configCityName = sharedPreferences.getString(
					MyApp.isUseLocate ? Constant.MySP.STR_LOC_CITY_NAME
							: Constant.MySP.STR_MANUL_CITY, sharedPreferences
							.getString(Constant.MySP.STR_LOC_CITY_NAME, "未定位"));

			MyLog.v("[closeMenu]isUseLocate:" + MyApp.isUseLocate
					+ ",isUseLocateNow:" + MyApp.isUseLocateNow
					+ ",configCityName:" + configCityName + ",nowCityName:"
					+ MyApp.nowCityName);

			if (!MyApp.nowCityName.equals(configCityName)) {
				// 应该显示的城市，和正在显示的城市不同，则进行更新
				updateWeather();
			}

		}
	};

	// public ResideMenu getResideMenu() {
	// return resideMenu;
	// }

	private void initialLayout() {
		imageShowResideMenu = (ImageView) findViewById(R.id.imageShowResideMenu);
		imageShowResideMenu.setOnClickListener(new MyOnClickListener());
	}

	private void initialWeatherLayout() {
		weatherArray = new String[6];

		strNoLoction = getResources()
				.getString(R.string.locate_fail_no_weather);

		strNoWeather = getResources().getString(R.string.get_weather_fail);

		// 定位图标
		imageLocate = (ImageView) findViewById(R.id.imageLocate);
		imageLocate.setVisibility(MyApp.isUseLocate ? View.VISIBLE
				: View.INVISIBLE);

		// 时钟信息
		int weekToday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

		TextClock textWeek = (TextClock) findViewById(R.id.textWeek);
		TextClock textDate = (TextClock) findViewById(R.id.textDate);

		textDate.setTypeface(Typefaces.get(this, Constant.Path.FONT
				+ "Font-Helvetica-Neue-LT-Pro.otf"));
		textWeek.setTypeface(Typefaces.get(this, Constant.Path.FONT
				+ "Font-Helvetica-Neue-LT-Pro.otf"));
		// Day 0 (Today) Weather and Time, Location Info
		String strNotLocate = getResources().getString(R.string.not_locate);
		String strUnknown = getResources().getString(R.string.unknown);
		String strDefaultWind = getResources().getString(
				R.string.weather_default_wind_info);
		String cityName = WeatherUtil.getCityName(MainActivity.this);
		TextView textLocation = (TextView) findViewById(R.id.textLocation);
		textLocation.setText(cityName);
		cityName = WeatherUtil.getCityName(MainActivity.this);

		String weatherToday = WeatherUtil.getLocalWeatherInfo(this, 0,
				WEATHER_INFO.WEATHER);

		// 背景
		// RelativeLayout layoutWeather = (RelativeLayout)
		// findViewById(R.id.layoutWeather);
		// layoutWeather.setBackground(getResources().getDrawable(
		// WeatherUtil.getWeatherBackground(WeatherUtil
		// .getTypeByStr(weatherToday)))); // Background

		ImageView imageTodayWeather = (ImageView) findViewById(R.id.imageTodayWeather);
		imageTodayWeather.setImageResource(WeatherUtil.getWeatherDrawable(
				WeatherUtil.getTypeByStr(weatherToday), false));
		TextView textTodayWeather = (TextView) findViewById(R.id.textTodayWeather);
		textTodayWeather.setText(weatherToday);

		TitanicTextView textTempRange = (TitanicTextView) findViewById(R.id.textTempRange);
		textTempRange.setText(WeatherUtil.getLocalWeatherInfo(this, 0,
				WEATHER_INFO.TEMP));
		textTempRange.setTypeface(Typefaces.get(this, Constant.Path.FONT
				+ "Font-Helvetica-Neue-LT-Pro.otf"));
		if (Constant.Module.hasWeatherAnimation) {
			new Titanic().start(textTempRange);
		} else {
			textTempRange.setTextColor(Color.WHITE);
		}
		TextView textWetLevel = (TextView) findViewById(R.id.textWetLevel);
		textWetLevel.setText(getResources().getString(R.string.humidity_colon)
				+ sharedPreferences.getString("humidity", "55.55%"));

		TextView textWind = (TextView) findViewById(R.id.textWind);
		String day0windStr = WeatherUtil.getLocalWeatherInfo(this, 0,
				WEATHER_INFO.WIND);
		textWind.setText(day0windStr);

		TextView textUpdateTime = (TextView) findViewById(R.id.textUpdateTime);
		textUpdateTime.setText(WeatherUtil.getLocalWeatherInfo(this, 0,
				WEATHER_INFO.POST_TIME));

		if (strNotLocate.equals(cityName)) {
			weatherArray[0] = strNoLoction;
			isLocated = false;
		} else if (strUnknown.equals(weatherToday)) {
			weatherArray[0] = strNoWeather;
			isGetSuccess = false;
		} else {
			weatherArray[0] = WeatherUtil.getLocalWeatherInfo(this, 0,
					WEATHER_INFO.ALL);
			isLocated = true;
			isGetSuccess = true;
		}

		// Day 1
		TextView day1Week = (TextView) findViewById(R.id.day1week);
		String day1weekStr = DateUtil.getWeekStrByInt(weekToday + 1);
		day1Week.setText(day1weekStr);

		TextView day1date = (TextView) findViewById(R.id.day1date);
		day1date.setText(sharedPreferences.getString("day1date", "2015-01-01")
				.substring(5, 10));

		ImageView day1image = (ImageView) findViewById(R.id.day1image);
		TextView day1weather = (TextView) findViewById(R.id.day1weather);
		String day1weatherStr = WeatherUtil.getLocalWeatherInfo(this, 1,
				WEATHER_INFO.WEATHER);
		day1weather.setText(day1weatherStr);
		day1image.setImageResource(WeatherUtil.getWeatherDrawable(
				WeatherUtil.getTypeByStr(day1weatherStr), true));

		TextView day1tmpRange = (TextView) findViewById(R.id.day1tmpRange);
		day1tmpRange.setText(WeatherUtil.getLocalWeatherInfo(this, 1,
				WEATHER_INFO.TEMP) + "°");
		day1tmpRange.setTypeface(Typefaces.get(this, Constant.Path.FONT
				+ "Font-Helvetica-Neue-LT-Pro.otf"));

		TextView day1wind = (TextView) findViewById(R.id.day1wind);
		String day1windStr = WeatherUtil.getLocalWeatherInfo(this, 1,
				WEATHER_INFO.WIND);
		day1wind.setText(day1windStr);

		if (!isLocated) {
			weatherArray[1] = strNoLoction;
		} else if (!isGetSuccess) {
			weatherArray[1] = strNoWeather;
		} else {
			weatherArray[1] = WeatherUtil.getLocalWeatherInfo(this, 1,
					WEATHER_INFO.ALL);
		}

		// Day 2
		TextView day2week = (TextView) findViewById(R.id.day2week);
		String day2WeekStr = DateUtil.getWeekStrByInt(weekToday + 2);
		day2week.setText(day2WeekStr);

		TextView day2date = (TextView) findViewById(R.id.day2date);
		day2date.setText(sharedPreferences.getString("day2date", "2015-01-01")
				.substring(5, 10));

		ImageView day2image = (ImageView) findViewById(R.id.day2image);
		TextView day2weather = (TextView) findViewById(R.id.day2weather);
		String day2WeatherStr = WeatherUtil.getLocalWeatherInfo(this, 2,
				WEATHER_INFO.WEATHER);
		day2weather.setText(day2WeatherStr);
		day2image.setImageResource(WeatherUtil.getWeatherDrawable(
				WeatherUtil.getTypeByStr(day2WeatherStr), true));

		TextView day2tmpRange = (TextView) findViewById(R.id.day2tmpRange);
		day2tmpRange.setText(WeatherUtil.getLocalWeatherInfo(this, 2,
				WEATHER_INFO.TEMP) + "°");
		day2tmpRange.setTypeface(Typefaces.get(this, Constant.Path.FONT
				+ "Font-Helvetica-Neue-LT-Pro.otf"));

		TextView day2wind = (TextView) findViewById(R.id.day2wind);
		String day2windStr = WeatherUtil.getLocalWeatherInfo(this, 2,
				WEATHER_INFO.WIND);
		day2wind.setText(day2windStr);

		if (!isLocated) {
			weatherArray[2] = strNoLoction;
		} else if (!isGetSuccess) {
			weatherArray[2] = strNoWeather;
		} else {
			weatherArray[2] = WeatherUtil.getLocalWeatherInfo(this, 2,
					WEATHER_INFO.ALL);
		}

		// Day 3
		TextView day3week = (TextView) findViewById(R.id.day3week);
		String day3WeekStr = DateUtil.getWeekStrByInt(weekToday + 3);
		day3week.setText(day3WeekStr);

		TextView day3date = (TextView) findViewById(R.id.day3date);
		day3date.setText(sharedPreferences.getString("day3date", "2015-01-01")
				.substring(5, 10));

		ImageView day3image = (ImageView) findViewById(R.id.day3image);
		String day3WeatherStr = WeatherUtil.getLocalWeatherInfo(this, 3,
				WEATHER_INFO.WEATHER);
		TextView day3weather = (TextView) findViewById(R.id.day3weather);
		day3weather.setText(day3WeatherStr);
		day3image.setImageResource(WeatherUtil.getWeatherDrawable(
				WeatherUtil.getTypeByStr(day3WeatherStr), true));

		TextView day3tmpRange = (TextView) findViewById(R.id.day3tmpRange);
		day3tmpRange.setText(WeatherUtil.getLocalWeatherInfo(this, 3,
				WEATHER_INFO.TEMP) + "°");
		day3tmpRange.setTypeface(Typefaces.get(this, Constant.Path.FONT
				+ "Font-Helvetica-Neue-LT-Pro.otf"));

		TextView day3wind = (TextView) findViewById(R.id.day3wind);
		String day3windStr = sharedPreferences.getString("day3wind",
				strDefaultWind);
		day3wind.setText(day3windStr);

		if (!isLocated) {
			weatherArray[3] = strNoLoction;
		} else if (!isGetSuccess) {
			weatherArray[3] = strNoWeather;
		} else {
			weatherArray[3] = WeatherUtil.getLocalWeatherInfo(this, 3,
					WEATHER_INFO.ALL);
		}

		// Day 4
		TextView day4week = (TextView) findViewById(R.id.day4week);
		String day4WeekStr = DateUtil.getWeekStrByInt(weekToday + 4);
		day4week.setText(day4WeekStr);

		TextView day4date = (TextView) findViewById(R.id.day4date);
		day4date.setText(sharedPreferences.getString("day4date", "2015-01-01")
				.substring(5, 10));

		ImageView day4image = (ImageView) findViewById(R.id.day4image);
		String day4WeatherStr = WeatherUtil.getLocalWeatherInfo(this, 4,
				WEATHER_INFO.WEATHER);
		TextView day4weather = (TextView) findViewById(R.id.day4weather);
		day4weather.setText(day4WeatherStr);
		day4image.setImageResource(WeatherUtil.getWeatherDrawable(
				WeatherUtil.getTypeByStr(day4WeatherStr), true));

		TextView day4tmpRange = (TextView) findViewById(R.id.day4tmpRange);
		day4tmpRange.setText(WeatherUtil.getLocalWeatherInfo(this, 4,
				WEATHER_INFO.TEMP) + "°");
		day4tmpRange.setTypeface(Typefaces.get(this, Constant.Path.FONT
				+ "Font-Helvetica-Neue-LT-Pro.otf"));

		TextView day4wind = (TextView) findViewById(R.id.day4wind);
		String day4windStr = sharedPreferences.getString("day4wind",
				strDefaultWind);
		day4wind.setText(day4windStr);

		if (!isLocated) {
			weatherArray[4] = strNoLoction;
		} else if (!isGetSuccess) {
			weatherArray[4] = strNoWeather;
		} else {
			weatherArray[4] = WeatherUtil.getLocalWeatherInfo(this, 4,
					WEATHER_INFO.ALL);
		}

		// Day 5
		TextView day5week = (TextView) findViewById(R.id.day5week);
		String day5WeekStr = DateUtil.getWeekStrByInt(weekToday + 5);
		day5week.setText(day5WeekStr);

		TextView day5date = (TextView) findViewById(R.id.day5date);
		day5date.setText(sharedPreferences.getString("day5date", "2015-01-01")
				.substring(5, 10));

		ImageView day5image = (ImageView) findViewById(R.id.day5image);
		String day5WeatherStr = WeatherUtil.getLocalWeatherInfo(this, 5,
				WEATHER_INFO.WEATHER);
		TextView day5weather = (TextView) findViewById(R.id.day5weather);
		day5weather.setText(day5WeatherStr);
		day5image.setImageResource(WeatherUtil.getWeatherDrawable(
				WeatherUtil.getTypeByStr(day5WeatherStr), true));

		TextView day5tmpRange = (TextView) findViewById(R.id.day5tmpRange);
		day5tmpRange.setText(WeatherUtil.getLocalWeatherInfo(this, 5,
				WEATHER_INFO.TEMP) + "°");
		day5tmpRange.setTypeface(Typefaces.get(this, Constant.Path.FONT
				+ "Font-Helvetica-Neue-LT-Pro.otf"));

		TextView day5wind = (TextView) findViewById(R.id.day5wind);
		String day5windStr = WeatherUtil.getLocalWeatherInfo(this, 5,
				WEATHER_INFO.WIND);
		day5wind.setText(day5windStr);

		if (!isLocated) {
			weatherArray[5] = strNoLoction;
		} else if (!isGetSuccess) {
			weatherArray[5] = strNoWeather;
		} else {
			weatherArray[5] = WeatherUtil.getLocalWeatherInfo(this, 5,
					WEATHER_INFO.ALL);
		}

		LinearLayout layoutDay1 = (LinearLayout) findViewById(R.id.layoutDay1);
		layoutDay1.setOnClickListener(new MyOnClickListener());

		LinearLayout layoutDay2 = (LinearLayout) findViewById(R.id.layoutDay2);
		layoutDay2.setOnClickListener(new MyOnClickListener());

		LinearLayout layoutDay3 = (LinearLayout) findViewById(R.id.layoutDay3);
		layoutDay3.setOnClickListener(new MyOnClickListener());

		LinearLayout layoutDay4 = (LinearLayout) findViewById(R.id.layoutDay4);
		layoutDay4.setOnClickListener(new MyOnClickListener());

		LinearLayout layoutDay5 = (LinearLayout) findViewById(R.id.layoutDay5);
		layoutDay5.setOnClickListener(new MyOnClickListener());

	}

	class MyOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.imageShowResideMenu:
				// resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
				break;

			case R.id.layoutDay1:
				speakVoice(weatherArray[1]);
				break;

			case R.id.layoutDay2:
				speakVoice(weatherArray[2]);
				break;

			case R.id.layoutDay3:
				speakVoice(weatherArray[3]);
				break;

			case R.id.layoutDay4:
				speakVoice(weatherArray[4]);
				break;
			case R.id.layoutDay5:
				speakVoice(weatherArray[5]);
				break;

			case R.id.layoutDay6:
				speakVoice(weatherArray[6]);
				break;

			case R.id.updateButton:
				updateWeather();
				break;
			}
		}
	}

	private void backToMain() {
		// 弊端：放到后台耗费CPU资源
		// Intent intent = new Intent(Intent.ACTION_MAIN);
		// intent.addCategory(Intent.CATEGORY_HOME);
		// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// startActivity(intent);

		// 弊端：每次进入都要重新初始化
		finish();
		overridePendingTransition(R.anim.zms_translate_up_out,
				R.anim.zms_translate_up_in);
	}

	private void updateWeather() {
		if (-1 == NetworkUtil.getNetworkType(getApplicationContext())) {
			NetworkUtil.noNetworkHint(getApplicationContext());
		} else {
			// 同步显示和配置
			MyApp.isUseLocateNow = MyApp.isUseLocate;
			if (MyApp.isUseLocate) {
				startLocationService();
			}
			updateButton.setVisibility(View.INVISIBLE);
			updateProgress.setVisibility(View.VISIBLE);
			new Thread(new UpdateWeatherThread()).start();
		}
	}

	public class UpdateWeatherThread implements Runnable {

		@Override
		public void run() {
			try {
				if (MyApp.isUseLocate) {
					Thread.sleep(3000);
				}
				startWeatherService();
				Thread.sleep(3000);
				Message message = new Message();
				message.what = 1;
				updateWeatherHandler.sendMessage(message);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/** 当获取天气失败时，再次尝试的次数 **/
	private int maxTryTime = 3;

	final Handler updateWeatherHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				if (MyApp.isActivityShowing) {
					updateButton.setVisibility(View.VISIBLE);
					updateProgress.setVisibility(View.INVISIBLE);
					initialWeatherLayout();
					MyLog.v("[updateWeatherHandler]isGetSuccess:"
							+ isGetSuccess);
					if (isGetSuccess) {
						speakVoice(weatherArray[0]);
					} else if (maxTryTime > 0) {
						MyLog.v("[updateWeatherHandler]maxTryTime:"
								+ maxTryTime);
						maxTryTime--;
						updateWeather();
					}
				}
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	private void startLocationService() {
		Intent intent = new Intent(this, LocationService.class);
		startService(intent);
	}

	private void startWeatherService() {
		Intent intent = new Intent(this, WeatherService.class);
		startService(intent);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isResideMenuClose) {
				backToMain();
			} else {
				// resideMenu.closeMenu();
			}
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}

}
