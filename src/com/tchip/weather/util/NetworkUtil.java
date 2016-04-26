package com.tchip.weather.util;

import com.tchip.weather.R;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.widget.Toast;

public class NetworkUtil {

	public static int getNetworkType(Context context) {
		ConnectivityManager connectMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo info = connectMgr.getActiveNetworkInfo();
		if (info != null) {
			return info.getType();
		} else {
			return -1;
		}
	}

	public static void noNetworkHint(Context context) {
		String strNoNetwork = context.getResources().getString(
				R.string.hint_no_network);

		Toast.makeText(context, strNoNetwork, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 返回网络状态
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * 返回当前Wifi是否连接上
	 * 
	 * @param context
	 * @return true 已连接
	 */
	public static boolean isWifiConnected(Context context) {
		ConnectivityManager conMan = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = conMan.getActiveNetworkInfo();
		if (netInfo != null
				&& netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}
		return false;
	}

	/**
	 * 飞行模式是否打开
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isAirplaneModeOn(Context context) {
		return android.provider.Settings.System.getInt(
				context.getContentResolver(),
				android.provider.Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
	}

	/**
	 * 外接蓝牙模块是否打开
	 */
	public static boolean isExtBluetoothOn(Context context) {
		String btStatus = "";

		try {
			btStatus = Settings.System.getString(context.getContentResolver(),
					"bt_enable");

		} catch (Exception e) {

		}

		if ("1".equals(btStatus)) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 外接蓝牙模块是否连接
	 */
	public static boolean isExtBluetoothConnected(Context context) {
		String btStatus = "";

		try {
			btStatus = Settings.System.getString(context.getContentResolver(),
					"bt_connect");

		} catch (Exception e) {

		}

		if ("1".equals(btStatus)) {
			return true;
		} else {
			return false;
		}

	}

}
