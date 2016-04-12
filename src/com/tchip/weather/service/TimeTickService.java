package com.tchip.weather.service;

import com.tchip.weather.model.TimeTickReceiver;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class TimeTickService extends Service {

	private TimeTickReceiver timeTickReceiver;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		timeTickReceiver = new TimeTickReceiver();
		IntentFilter timeTickfilter = new IntentFilter(Intent.ACTION_TIME_TICK);
		registerReceiver(timeTickReceiver, timeTickfilter);

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(timeTickReceiver);
		super.onDestroy();
	}

}
