package com.tchip.weather;

public interface Constant {
	/**
	 * Debug：打印Log
	 */
	public static final boolean isDebug = true;

	/**
	 * 日志Tag
	 */
	public static final String TAG = "AZ";

	public static final class Module {

		/** 天气界面是否有动画 **/
		public static final boolean hasWeatherAnimation = false;
	}

	/** 广播 */
	public static final class Broadcast {
		/** TTS播报,Extra:content(String) */
		public static final String TTS_SPEAK = "tchip.intent.action.TTS_SPEAK";
	}

	public static final class MySP {
		/**
		 * SharedPreferences文件名字
		 */
		public static final String FILE_NAME = "Weather";

		/**
		 * [boolean:true]是否播报天气
		 */
		public static final String STR_AUTO_SPEARK_WEATHER = "autoSpeakWeather";

		/**
		 * [String:05:55:55]上次自动播报的天气发布时间
		 */
		public static final String STR_LAST_SPEAK_POST_TIME = "lastSpeakPostTime";

		/**
		 * [boolean:true]是否使用定位城市的天气
		 */
		public static final String STR_IS_USE_LOCATE = "isUseLocate";

		/**
		 * [String:null]手动设置的城市名，isUsingLocate为false时生效
		 */
		public static final String STR_MANUL_CITY = "manulCity";

		/**
		 * [String:未定位]定位的城市名
		 */
		public static final String STR_LOC_CITY_NAME = "locCityName";

		/**
		 * [String:未定位]较旧的定位城市名
		 */
		public static final String STR_LOC_CITY_NAME_OLD = "locCityNameOld";

		/**
		 * [String:]定位的纬度latitude
		 */
		public static final String STR_LOC_LATITUDE = "locLatitude";

		/**
		 * [String:]定位的经度longitude
		 */
		public static final String STR_LOC_LONGITUDE = "locLongitude";

		/**
		 * [String:]定位的海拔altitude
		 */
		public static final String STR_LOC_ALTITUDE = "locAltitude";

		/**
		 * [String:]定位的地址
		 */
		public static final String STR_LOC_ADDRESS = "locAddress";

		/**
		 * [String:]定位的时间
		 */
		public static final String STR_LOC_TIME = "locTime";

	}

	/**
	 * 路径
	 */
	public static final class Path {

		/** 字体目录 **/
		public static final String FONT = "fonts/";
	}

	/**
	 * SDK
	 */
	public static final class SDK {
		/**
		 * 讯飞语音APP ID
		 */
		public static final String XUNFEI_APP_ID = "5639c13a";

		/**
		 * 百度API KEY
		 */
		public static final String BAIDU_API_KEY = "1lUDOW9LjeWdPVLLYtu7job4";

		/**
		 * 百度安全码MCODE
		 */
		public static final String BAIDU_MCODE = "6B:40:B2:47:13:F5:6A:F7:40:6A:89:84:46:53:33:47:AD:DC:C1:0C;com.tchip.weather";
	}

}
