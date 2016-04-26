package com.tchip.weather.service;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.tchip.weather.Constant;
import com.tchip.weather.util.MyLog;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;

public class LocationService extends Service {
	private LocationClient mLocationClient;

	private int scanSpan = 1000; // 采集轨迹点间隔(ms)
	private String locCityName = "未定位";
	private SharedPreferences preferences;
	private Editor editor;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		MyLog.v("[LocationService]onCreate");
		preferences = getSharedPreferences(Constant.MySP.FILE_NAME,
				Context.MODE_PRIVATE);
		editor = preferences.edit();
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		MyLog.v("[LocationService]onStartCommand");
		InitLocation(LocationMode.Hight_Accuracy, "bd09ll", scanSpan, true);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		MyLog.v("[LocationService]onDestroy");
		mLocationClient.stop();
		super.onDestroy();
	}

	/**
	 * 
	 * @param tempMode
	 *            LocationMode.Hight_Accuracy-高精度
	 *            LocationMode.Battery_Saving-低功耗
	 *            LocationMode.Device_Sensors-仅设备
	 * @param tempCoor
	 *            gcj02-国测局加密经纬度坐标 bd09ll-百度加密经纬度坐标 bd09-百度加密墨卡托坐标
	 * @param frequence
	 *            MIN_SCAN_SPAN = 1000; MIN_SCAN_SPAN_NETWORK = 3000;
	 * @param isNeedAddress
	 *            是否需要地址
	 */
	private void InitLocation(LocationMode tempMode, String tempCoor,
			int frequence, boolean isNeedAddress) {

		mLocationClient = new LocationClient(this.getApplicationContext());
		mLocationClient.registerLocationListener(new MyLocationListener());
		// mGeofenceClient = new GeofenceClient(getApplicationContext());

		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(tempMode);
		option.setCoorType(tempCoor);
		option.setScanSpan(frequence);
		option.setIsNeedAddress(isNeedAddress);
		mLocationClient.setLocOption(option);
		mLocationClient.start();
	}

	class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {

			locCityName = location.getCity();

			if ((locCityName != null) && (!locCityName.equals("未定位"))) {
				MyLog.v("[Weather]LocationService:onReceiveLocation");

				editor.putString(Constant.MySP.STR_LOC_CITY_NAME, locCityName);
				editor.putString(Constant.MySP.STR_LOC_CITY_NAME_OLD,
						locCityName);
				editor.putString(Constant.MySP.STR_LOC_LATITUDE,
						"" + location.getLatitude());
				editor.putString(Constant.MySP.STR_LOC_LONGITUDE,
						"" + location.getLongitude());
				editor.putString("district", location.getDistrict());
				// editor.putString("floor", location.getFloor());
				editor.putString(Constant.MySP.STR_LOC_ADDRESS,
						location.getAddrStr());
				editor.putString("street", location.getStreet());
				// editor.putString("streetNum", location.getStreetNumber());
				// editor.putFloat("speed", location.getSpeed());
				// editor.putString(Constant.MySP.STR_LOC_ALTITUDE,
				// "" + location.getAltitude());
				editor.putString(Constant.MySP.STR_LOC_TIME, location.getTime());
				editor.commit();
			}
		}
	}

}