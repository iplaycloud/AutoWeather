package com.tchip.weather.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class ProviderUtil {

	public ProviderUtil() {
	}

	public static final class Name { // Ctrl+Shift+X
		/** 后录录像状态:0-未录像，1-录像 */
		public static final String REC_BACK_STATE = "rec_back_state";

		/** 前录录像状态:0-未录像，1-录像 */
		public static final String REC_FRONT_STATE = "rec_front_state";

		/** 前录分辨率:720,1080 */
		public static final String REC_FRONT_SIZE = "rec_front_size";

		/** 前录分段:1,3 */
		public static final String REC_FRONT_TIME = "rec_front_time";

		/** 天气定位城市:深圳 */
		public static final String WEATHER_LOC_CITY = "weather_loc_city";

		/** 天气定位时间 */
		public static final String WEATHER_LOC_TIME = "weather_loc_time";

		/** 天气信息:晴 */
		public static final String WEATHER_INFO = "weather_info";

		/** 当日最低气温:15 */
		public static final String WEATHER_TEMP_LOW = "weather_temp_low";

		/** 当日最高气温:25 */
		public static final String WEATHER_TEMP_HIGH = "weather_temp_high";

		/** 音乐播放状态:0,1 */
		public static final String MUSIC_PLAY_STATE = "music_play_state";

		/** 正在播放歌曲名 */
		public static final String MUSIC_PLAY_NAME = "music_play_name";

		/** 蓝牙播放音乐状态:0,1 */
		public static final String BT_MUSIC_STATE = "bt_music_state";

		/** 蓝牙开关:0,1 */
		public static final String BT_ENABLE = "bt_enable";

		/** 蓝牙连接状态：0,1 */
		public static final String BT_CONNECT_STATE = "bt_connect_state";

		/** FM发射开关:0,1 */
		public static final String FM_TRANSMIT_STATE = "fm_transmit_state";

		/** FM发射频率: 8750-10800 */
		public static final String FM_TRANSMIT_FREQ = "fm_transmit_freq";

		/** 电子狗电源状态:0,1 */
		public static final String EDOG_POWER_STATE = "edog_power_state";

		/** 自动亮度开关:0,1 */
		public static final String SET_AUTO_LIGHT_STATE = "set_auto_light_state";

		/** 碰撞侦测开关:0,1 */
		public static final String SET_DETECT_CRASH_STATE = "set_detect_crash_state";

		/** 碰撞侦测灵敏度:1-低,2-中,3高 */
		public static final String SET_DETECT_CRASH_LEVEL = "set_detect_crash_level";

		/** 停车守卫开关:0,1 */
		public static final String SET_PARK_MONITOR_STATE = "set_park_monitor_state";

	}

	public static String getValue(Context context, String name) {
		String dbValue = "";
		Uri uri = Uri
				.parse("content://com.tchip.provider.AutoProvider/state/name/"
						+ name);
		ContentResolver contentResolver = context.getContentResolver();
		Cursor cursor = contentResolver.query(uri, null, null, null, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			dbValue = cursor.getString(cursor.getColumnIndex("value"));
			cursor.close();
		} else {
		}
		MyLog.v("[ProviderUtil]name:" + name + ",value:" + dbValue);
		return dbValue;
	}

	public static void setValue(Context context, String name, String value) {
		Uri uriUpdate = Uri
				.parse("content://com.tchip.provider.AutoProvider/state/name/"
						+ name);
		ContentResolver contentResolverUpdate = context.getContentResolver();
		ContentValues valuesUpdate = new ContentValues();
		valuesUpdate.put("value", value);
		int count = contentResolverUpdate.update(uriUpdate, valuesUpdate,
				"name=?", new String[] { name }); // Update
		MyLog.v("[ProviderUtil]Update count:" + count);
		if (count == 0) {
			Uri uriInsert = Uri
					.parse("content://com.tchip.provider.AutoProvider/state/");
			ContentResolver contentResolverInsert = context
					.getContentResolver();
			ContentValues valuesInsert = new ContentValues();
			valuesInsert.put("name", name);
			valuesInsert.put("value", value);
			contentResolverInsert.insert(uriInsert, valuesInsert); // Insert
		}
	}

}
