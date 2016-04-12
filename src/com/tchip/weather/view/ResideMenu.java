package com.tchip.weather.view;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.tchip.weather.Constant;
import com.tchip.weather.MyApp;
import com.tchip.weather.R;
import com.tchip.weather.adapter.WheelArrayWheelAdapter;
import com.tchip.weather.model.WheelCityModel;
import com.tchip.weather.model.WheelDistrictModel;
import com.tchip.weather.model.WheelProvinceModel;
import com.tchip.weather.model.WheelXmlParserHandler;
import com.tchip.weather.util.MyLog;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class ResideMenu extends FrameLayout {

	public static final int DIRECTION_LEFT = 0;
	public static final int DIRECTION_RIGHT = 1;
	private static final int PRESSED_MOVE_HORIZANTAL = 2;
	private static final int PRESSED_DOWN = 3;
	private static final int PRESSED_DONE = 4;
	private static final int PRESSED_MOVE_VERTICAL = 5;

	private ImageView imageViewShadow;
	private ImageView imageViewBackground;
	private LinearLayout layoutLeftMenu, layoutRightMenu, layoutInfo;
	private RelativeLayout leftMenu, rightMenu, scrollViewMenu, layoutHint;

	/** the activity that view attach to */
	private Activity activity;

	/** the decorview of the activity */
	private ViewGroup viewDecor;

	/** the viewgroup of the activity */
	private TouchDisableView viewActivity;

	/** the flag of menu open status */
	private boolean isOpened;

	private float shadowAdjustScaleX;
	private float shadowAdjustScaleY;
	/** the view which don't want to intercept touch event */
	private List<View> ignoredViews;
	private List<ResideMenuItem> leftMenuItems;
	private List<ResideMenuItem> rightMenuItems;
	private DisplayMetrics displayMetrics = new DisplayMetrics();
	private OnMenuListener menuListener;
	private float lastRawX;
	private boolean isInIgnoredView = false;
	private int scaleDirection = DIRECTION_LEFT;
	private int pressedState = PRESSED_DOWN;
	private List<Integer> disabledSwipeDirection = new ArrayList<Integer>();

	// valid scale factor is between 0.0f and 1.0f.
	private float mScaleValue = 0.5f;

	private RelativeLayout layoutManul; // 手动设置城市布局
	private TextView textManulCity; // 手动设置城市
	private SwitchButton switchUseLocate;
	private ImageView imageManulEdit; // 编辑城市
	private ImageView imageManulConfirm; // 确认城市

	private SharedPreferences sharedPreferences;
	private Editor editor;

	// 省市联动
	private WheelView wheelProvince;
	private WheelView wheelCity;
	private WheelView wheelDistrict;
	protected String[] mProvinceDatas; // 所有省
	// key - 省 value - 市
	protected Map<String, String[]> mCitisDatasMap = new HashMap<String, String[]>();
	// key - 市 values - 区
	protected Map<String, String[]> mDistrictDatasMap = new HashMap<String, String[]>();
	// key - 区 values - 邮编
	protected Map<String, String> mZipcodeDatasMap = new HashMap<String, String>();

	protected String mCurrentProviceName; // 当前省的名称
	protected String mCurrentCityName; // 当前市的名称
	protected String mCurrentDistrictName = ""; // 当前区的名称
	protected String mCurrentZipCode = ""; // 当前区的邮政编码

	private LinearLayout layoutWheel;

	private Context context;

	/** 天气后台播报 **/
	private SwitchButton switchSpeakWeather;

	public ResideMenu(Context context) {
		super(context);
		this.context = context;

		sharedPreferences = context.getSharedPreferences(
				Constant.MySP.FILE_NAME, Context.MODE_PRIVATE);
		editor = sharedPreferences.edit();

		initViews(context);
	}

	private void initViews(Context context) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.residemenu, this);
		leftMenu = (RelativeLayout) findViewById(R.id.rl_left_menu);
		rightMenu = (RelativeLayout) findViewById(R.id.rl_right_menu);
		imageViewShadow = (ImageView) findViewById(R.id.iv_shadow);
		layoutLeftMenu = (LinearLayout) findViewById(R.id.layout_left_menu);
		layoutRightMenu = (LinearLayout) findViewById(R.id.layout_right_menu);
		imageViewBackground = (ImageView) findViewById(R.id.iv_background);

		layoutInfo = (LinearLayout) findViewById(R.id.layout_info);

		layoutHint = (RelativeLayout) findViewById(R.id.layoutHint);

		// 天气自动播报
		MyApp.autoSpeakWeather = sharedPreferences.getBoolean(
				Constant.MySP.STR_AUTO_SPEARK_WEATHER, true);
		switchSpeakWeather = (SwitchButton) findViewById(R.id.switchSpeakWeather);
		switchSpeakWeather.setChecked(MyApp.autoSpeakWeather);
		switchSpeakWeather
				.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						MyApp.autoSpeakWeather = isChecked;
						editor.putBoolean(
								Constant.MySP.STR_AUTO_SPEARK_WEATHER,
								isChecked);
						editor.commit();
					}
				});

		// 手动设置城市
		layoutManul = (RelativeLayout) findViewById(R.id.layoutManul);
		textManulCity = (TextView) findViewById(R.id.textManulCity);
		switchUseLocate = (SwitchButton) findViewById(R.id.switchUseLocate);

		imageManulEdit = (ImageView) findViewById(R.id.imageManulEdit);
		imageManulEdit.setOnClickListener(new MyOnClickListener());
		imageManulConfirm = (ImageView) findViewById(R.id.imageManulConfirm);
		imageManulConfirm.setOnClickListener(new MyOnClickListener());

		MyApp.isUseLocate = sharedPreferences.getBoolean(
				Constant.MySP.STR_IS_USE_LOCATE, true);

		setLayoutManulVisibility(!MyApp.isUseLocate);
		switchUseLocate.setChecked(MyApp.isUseLocate);
		switchUseLocate
				.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						MyApp.isUseLocate = isChecked;
						setLayoutManulVisibility(!MyApp.isUseLocate);
						editor.putBoolean(Constant.MySP.STR_IS_USE_LOCATE,
								isChecked);
						editor.commit();
					}
				});

		// 城市联动
		wheelProvince = (WheelView) findViewById(R.id.wheelProvince);
		wheelProvince.addChangingListener(new MyOnWheelChangedListener());
		wheelCity = (WheelView) findViewById(R.id.wheelCity);
		wheelCity.addChangingListener(new MyOnWheelChangedListener());

		wheelDistrict = (WheelView) findViewById(R.id.wheelDistrict);
		wheelDistrict.addChangingListener(new MyOnWheelChangedListener());

		initProvinceDatas();

		wheelProvince.setViewAdapter(new WheelArrayWheelAdapter<String>(
				context, mProvinceDatas));
		// 设置可见条目数量
		wheelProvince.setVisibleItems(7);
		wheelCity.setVisibleItems(7);
		wheelDistrict.setVisibleItems(7);
		updateCities();
		updateAreas();

		layoutWheel = (LinearLayout) findViewById(R.id.layoutWheel);
		setLayoutWheelVisibility(isLayoutWheelShow);
	}

	/**
	 * 是否显示手动设置城市布局
	 * 
	 * @param isShow
	 */
	private void setLayoutManulVisibility(boolean isShow) {
		layoutManul.setVisibility(isShow ? View.VISIBLE : View.GONE);

		if (isShow) {
			String locateCity = sharedPreferences.getString(
					Constant.MySP.STR_LOC_CITY_NAME, "北京市");
			String manulCity = sharedPreferences.getString(
					Constant.MySP.STR_MANUL_CITY, locateCity);

			setTextManulCity(manulCity);
		}
	}

	private void setTextManulCity(String manulCity) {
		MyLog.v("[ResideMenu]setTextManulCity:" + manulCity);
		textManulCity.setText("自定义城市:\n" + manulCity);

		editor.putString(Constant.MySP.STR_MANUL_CITY, manulCity);
		editor.commit();

	}

	private boolean isLayoutWheelShow = false;

	private class MyOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.imageManulEdit:
				setLayoutWheelVisibility(true);
				break;

			case R.id.imageManulConfirm:
				setLayoutWheelVisibility(false);
				setTextManulCity(mCurrentCityName);
				break;

			default:
				break;
			}
		}
	}

	private void setLayoutWheelVisibility(boolean isShow) {

		if (isShow) {
			layoutWheel.setVisibility(View.VISIBLE);
			isLayoutWheelShow = true;
			imageManulEdit.setVisibility(View.GONE);
			imageManulConfirm.setVisibility(View.VISIBLE);
		} else {
			layoutWheel.setVisibility(View.GONE);
			isLayoutWheelShow = false;
			imageManulEdit.setVisibility(View.VISIBLE);
			imageManulConfirm.setVisibility(View.GONE);
		}
	}

	private class MyOnWheelChangedListener implements
			WheelOnWheelChangedListener {

		@Override
		public void onChanged(WheelView wheel, int oldValue, int newValue) {
			if (wheel == wheelProvince) {
				updateCities();
			} else if (wheel == wheelCity) {
				updateAreas();
			}
		}

	}

	/**
	 * 解析省市区的XML数据
	 */
	protected void initProvinceDatas() {
		List<WheelProvinceModel> provinceList = null;
		AssetManager asset = context.getAssets();
		try {
			InputStream input = asset.open("province_data.xml");
			// 创建一个解析xml的工厂对象
			SAXParserFactory spf = SAXParserFactory.newInstance();
			// 解析xml
			SAXParser parser = spf.newSAXParser();
			WheelXmlParserHandler handler = new WheelXmlParserHandler();
			parser.parse(input, handler);
			input.close();
			// 获取解析出来的数据
			provinceList = handler.getDataList();
			// 初始化默认选中的省、市、区
			if (provinceList != null && !provinceList.isEmpty()) {
				mCurrentProviceName = provinceList.get(0).getName();
				List<WheelCityModel> cityList = provinceList.get(0)
						.getCityList();
				if (cityList != null && !cityList.isEmpty()) {
					mCurrentCityName = cityList.get(0).getName();
					List<WheelDistrictModel> districtList = cityList.get(0)
							.getDistrictList();
					// mCurrentDistrictName = districtList.get(0).getName();
					// mCurrentZipCode = districtList.get(0).getZipcode();
				}
			}
			//
			mProvinceDatas = new String[provinceList.size()];
			for (int i = 0; i < provinceList.size(); i++) {
				// 遍历所有省的数据
				mProvinceDatas[i] = provinceList.get(i).getName();
				List<WheelCityModel> cityList = provinceList.get(i)
						.getCityList();
				String[] cityNames = new String[cityList.size()];
				for (int j = 0; j < cityList.size(); j++) {
					// 遍历省下面的所有市的数据
					cityNames[j] = cityList.get(j).getName();
					List<WheelDistrictModel> districtList = cityList.get(j)
							.getDistrictList();
					String[] distrinctNameArray = new String[districtList
							.size()];
					WheelDistrictModel[] distrinctArray = new WheelDistrictModel[districtList
							.size()];
					for (int k = 0; k < districtList.size(); k++) {
						// 遍历市下面所有区/县的数据
						WheelDistrictModel districtModel = new WheelDistrictModel(
								districtList.get(k).getName(), districtList
										.get(k).getZipcode());
						// 区/县对于的邮编，保存到mZipcodeDatasMap
						mZipcodeDatasMap.put(districtList.get(k).getName(),
								districtList.get(k).getZipcode());
						distrinctArray[k] = districtModel;
						distrinctNameArray[k] = districtModel.getName();
					}
					// 市-区/县的数据，保存到mDistrictDatasMap
					mDistrictDatasMap.put(cityNames[j], distrinctNameArray);
				}
				// 省-市的数据，保存到mCitisDatasMap
				mCitisDatasMap.put(provinceList.get(i).getName(), cityNames);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {

		}
	}

	/**
	 * 根据当前的市，更新区WheelView的信息
	 */
	private void updateAreas() {
		int pCurrent = wheelCity.getCurrentItem();
		mCurrentCityName = mCitisDatasMap.get(mCurrentProviceName)[pCurrent];
		String[] areas = mDistrictDatasMap.get(mCurrentCityName);

		if (areas == null) {
			areas = new String[] { "" };
		}
		wheelDistrict.setViewAdapter(new WheelArrayWheelAdapter<String>(
				context, areas));
		wheelDistrict.setCurrentItem(0);
	}

	/**
	 * 根据当前的省，更新市WheelView的信息
	 */
	private void updateCities() {
		int pCurrent = wheelProvince.getCurrentItem();
		mCurrentProviceName = mProvinceDatas[pCurrent];
		String[] cities = mCitisDatasMap.get(mCurrentProviceName);
		if (cities == null) {
			cities = new String[] { "" };
		}
		wheelCity.setViewAdapter(new WheelArrayWheelAdapter<String>(context,
				cities));
		wheelCity.setCurrentItem(0);
		updateAreas();
	}

	/**
	 * use the method to set up the activity which residemenu need to show;
	 * 
	 * @param activity
	 */
	public void attachToActivity(Activity activity) {
		initValue(activity);
		setShadowAdjustScaleXByOrientation();
		viewDecor.addView(this, 0);
		setViewPadding();
	}

	private void initValue(Activity activity) {
		this.activity = activity;
		leftMenuItems = new ArrayList<ResideMenuItem>();
		rightMenuItems = new ArrayList<ResideMenuItem>();
		ignoredViews = new ArrayList<View>();
		viewDecor = (ViewGroup) activity.getWindow().getDecorView();
		viewActivity = new TouchDisableView(this.activity);

		View mContent = viewDecor.getChildAt(0);
		viewDecor.removeViewAt(0);
		viewActivity.setContent(mContent);
		addView(viewActivity);
	}

	private void setShadowAdjustScaleXByOrientation() {
		int orientation = getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			shadowAdjustScaleX = 0.034f;
			shadowAdjustScaleY = 0.12f;
		} else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			shadowAdjustScaleX = 0.06f;
			shadowAdjustScaleY = 0.07f;
		}
	}

	/**
	 * set the menu background picture;
	 * 
	 * @param imageResrouce
	 */
	public void setBackground(int imageResrouce) {
		imageViewBackground.setImageResource(imageResrouce);
	}

	/**
	 * the visiblity of shadow under the activity view;
	 * 
	 * @param isVisible
	 */
	public void setShadowVisible(boolean isVisible) {
		if (isVisible)
			imageViewShadow.setImageResource(R.drawable.shadow);
		else
			imageViewShadow.setImageBitmap(null);
	}

	/**
	 * 添加用户信息
	 * 
	 * @param menuInfo
	 */
	public void addMenuInfo(ResideMenuInfo menuInfo) {
		layoutInfo.addView(menuInfo);
	}

	/**
	 * add a single items to left menu;
	 * 
	 * @param menuItem
	 */
	@Deprecated
	public void addMenuItem(ResideMenuItem menuItem) {
		this.leftMenuItems.add(menuItem);
		layoutLeftMenu.addView(menuItem);
	}

	/**
	 * add a single items;
	 * 
	 * @param menuItem
	 * @param direction
	 */
	public void addMenuItem(ResideMenuItem menuItem, int direction) {
		if (direction == DIRECTION_LEFT) {
			this.leftMenuItems.add(menuItem);
			layoutLeftMenu.addView(menuItem);
		} else {
			this.rightMenuItems.add(menuItem);
			layoutRightMenu.addView(menuItem);
		}
	}

	/**
	 * set the menu items by array list to left menu;
	 * 
	 * @param menuItems
	 */
	@Deprecated
	public void setMenuItems(List<ResideMenuItem> menuItems) {
		this.leftMenuItems = menuItems;
		rebuildMenu();
	}

	/**
	 * set the menu items by array list;
	 * 
	 * @param menuItems
	 * @param direction
	 */
	public void setMenuItems(List<ResideMenuItem> menuItems, int direction) {
		if (direction == DIRECTION_LEFT)
			this.leftMenuItems = menuItems;
		else
			this.rightMenuItems = menuItems;
		rebuildMenu();
	}

	private void rebuildMenu() {
		layoutLeftMenu.removeAllViews();
		layoutRightMenu.removeAllViews();
		for (int i = 0; i < leftMenuItems.size(); i++)
			layoutLeftMenu.addView(leftMenuItems.get(i), i);
		for (int i = 0; i < rightMenuItems.size(); i++)
			layoutRightMenu.addView(rightMenuItems.get(i), i);
	}

	/**
	 * get the left menu items;
	 * 
	 * @return
	 */
	@Deprecated
	public List<ResideMenuItem> getMenuItems() {
		return leftMenuItems;
	}

	/**
	 * get the menu items;
	 * 
	 * @return
	 */
	public List<ResideMenuItem> getMenuItems(int direction) {
		if (direction == DIRECTION_LEFT)
			return leftMenuItems;
		else
			return rightMenuItems;
	}

	/**
	 * if you need to do something on the action of closing or opening menu, set
	 * the listener here.
	 * 
	 * @return
	 */
	public void setMenuListener(OnMenuListener menuListener) {
		this.menuListener = menuListener;
	}

	public OnMenuListener getMenuListener() {
		return menuListener;
	}

	/**
	 * we need the call the method before the menu show, because the padding of
	 * activity can't get at the moment of onCreateView();
	 */
	private void setViewPadding() {
		this.setPadding(viewActivity.getPaddingLeft(),
				viewActivity.getPaddingTop(), viewActivity.getPaddingRight(),
				viewActivity.getPaddingBottom());
	}

	/**
	 * show the reside menu;
	 */
	public void openMenu(int direction) {

		setScaleDirection(direction);

		isOpened = true;
		AnimatorSet scaleDown_activity = buildScaleDownAnimation(viewActivity,
				mScaleValue, mScaleValue);
		AnimatorSet scaleDown_shadow = buildScaleDownAnimation(imageViewShadow,
				mScaleValue + shadowAdjustScaleX, mScaleValue
						+ shadowAdjustScaleY);
		AnimatorSet alpha_menu = buildMenuAnimation(scrollViewMenu, 1.0f);
		scaleDown_shadow.addListener(animationListener);
		scaleDown_activity.playTogether(scaleDown_shadow);
		scaleDown_activity.playTogether(alpha_menu);
		scaleDown_activity.start();
	}

	/**
	 * close the reslide menu;
	 */
	public void closeMenu() {

		isOpened = false;
		AnimatorSet scaleUp_activity = buildScaleUpAnimation(viewActivity,
				1.0f, 1.0f);
		AnimatorSet scaleUp_shadow = buildScaleUpAnimation(imageViewShadow,
				1.0f, 1.0f);
		AnimatorSet alpha_menu = buildMenuAnimation(scrollViewMenu, 0.0f);
		scaleUp_activity.addListener(animationListener);
		scaleUp_activity.playTogether(scaleUp_shadow);
		scaleUp_activity.playTogether(alpha_menu);
		scaleUp_activity.start();
	}

	@Deprecated
	public void setDirectionDisable(int direction) {
		disabledSwipeDirection.add(direction);
	}

	public void setSwipeDirectionDisable(int direction) {
		disabledSwipeDirection.add(direction);
	}

	private boolean isInDisableDirection(int direction) {
		return disabledSwipeDirection.contains(direction);
	}

	private void setScaleDirection(int direction) {

		int screenWidth = getScreenWidth();
		float pivotX;
		float pivotY = getScreenHeight() * 0.5f;

		if (direction == DIRECTION_LEFT) {
			scrollViewMenu = leftMenu;
			pivotX = screenWidth * 1.5f;
		} else {
			scrollViewMenu = rightMenu;
			pivotX = screenWidth * -0.5f;
		}

		ViewHelper.setPivotX(viewActivity, pivotX);
		ViewHelper.setPivotY(viewActivity, pivotY);
		ViewHelper.setPivotX(imageViewShadow, pivotX);
		ViewHelper.setPivotY(imageViewShadow, pivotY);
		scaleDirection = direction;
	}

	/**
	 * return the flag of menu status;
	 * 
	 * @return
	 */
	public boolean isOpened() {
		return isOpened;
	}

	private OnClickListener viewActivityOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			if (isOpened())
				closeMenu();
		}
	};

	private Animator.AnimatorListener animationListener = new Animator.AnimatorListener() {
		@Override
		public void onAnimationStart(Animator animation) {
			if (isOpened()) {
				scrollViewMenu.setVisibility(VISIBLE);
				if (menuListener != null)
					menuListener.openMenu();
			}
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			// reset the view;
			if (isOpened()) {
				viewActivity.setTouchDisable(true);
				viewActivity.setOnClickListener(viewActivityOnClickListener);
			} else {
				viewActivity.setTouchDisable(false);
				viewActivity.setOnClickListener(null);
				scrollViewMenu.setVisibility(GONE);
				if (menuListener != null)
					menuListener.closeMenu();
			}
		}

		@Override
		public void onAnimationCancel(Animator animation) {

		}

		@Override
		public void onAnimationRepeat(Animator animation) {

		}
	};

	/**
	 * a helper method to build scale down animation;
	 * 
	 * @param target
	 * @param targetScaleX
	 * @param targetScaleY
	 * @return
	 */
	private AnimatorSet buildScaleDownAnimation(View target,
			float targetScaleX, float targetScaleY) {

		AnimatorSet scaleDown = new AnimatorSet();
		scaleDown.playTogether(
				ObjectAnimator.ofFloat(target, "scaleX", targetScaleX),
				ObjectAnimator.ofFloat(target, "scaleY", targetScaleY));

		scaleDown.setInterpolator(AnimationUtils.loadInterpolator(activity,
				android.R.anim.decelerate_interpolator));
		scaleDown.setDuration(250);
		return scaleDown;
	}

	/**
	 * a helper method to build scale up animation;
	 * 
	 * @param target
	 * @param targetScaleX
	 * @param targetScaleY
	 * @return
	 */
	private AnimatorSet buildScaleUpAnimation(View target, float targetScaleX,
			float targetScaleY) {

		AnimatorSet scaleUp = new AnimatorSet();
		scaleUp.playTogether(
				ObjectAnimator.ofFloat(target, "scaleX", targetScaleX),
				ObjectAnimator.ofFloat(target, "scaleY", targetScaleY));

		scaleUp.setDuration(250);
		return scaleUp;
	}

	private AnimatorSet buildMenuAnimation(View target, float alpha) {

		AnimatorSet alphaAnimation = new AnimatorSet();
		alphaAnimation.playTogether(ObjectAnimator.ofFloat(target, "alpha",
				alpha));

		alphaAnimation.setDuration(250);
		return alphaAnimation;
	}

	/**
	 * if there ware some view you don't want reside menu to intercept their
	 * touch event,you can use the method to set.
	 * 
	 * @param v
	 */
	public void addIgnoredView(View v) {
		ignoredViews.add(v);
	}

	/**
	 * remove the view from ignored view list;
	 * 
	 * @param v
	 */
	public void removeIgnoredView(View v) {
		ignoredViews.remove(v);
	}

	/**
	 * clear the ignored view list;
	 */
	public void clearIgnoredViewList() {
		ignoredViews.clear();
	}

	/**
	 * if the motion evnent was relative to the view which in ignored view
	 * list,return true;
	 * 
	 * @param ev
	 * @return
	 */
	private boolean isInIgnoredView(MotionEvent ev) {
		Rect rect = new Rect();
		for (View v : ignoredViews) {
			v.getGlobalVisibleRect(rect);
			if (rect.contains((int) ev.getX(), (int) ev.getY()))
				return true;
		}
		return false;
	}

	private void setScaleDirectionByRawX(float currentRawX) {
		if (currentRawX < lastRawX)
			setScaleDirection(DIRECTION_RIGHT);
		else
			setScaleDirection(DIRECTION_LEFT);
	}

	private float getTargetScale(float currentRawX) {
		float scaleFloatX = ((currentRawX - lastRawX) / getScreenWidth()) * 0.75f;
		scaleFloatX = scaleDirection == DIRECTION_RIGHT ? -scaleFloatX
				: scaleFloatX;

		float targetScale = ViewHelper.getScaleX(viewActivity) - scaleFloatX;
		targetScale = targetScale > 1.0f ? 1.0f : targetScale;
		targetScale = targetScale < 0.5f ? 0.5f : targetScale;
		return targetScale;
	}

	private float lastActionDownX, lastActionDownY;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		float currentActivityScaleX = ViewHelper.getScaleX(viewActivity);
		if (currentActivityScaleX == 1.0f)
			setScaleDirectionByRawX(ev.getRawX());

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			lastActionDownX = ev.getX();
			lastActionDownY = ev.getY();
			isInIgnoredView = isInIgnoredView(ev) && !isOpened();
			pressedState = PRESSED_DOWN;
			break;

		case MotionEvent.ACTION_MOVE:
			if (isInIgnoredView || isInDisableDirection(scaleDirection))
				break;

			if (pressedState != PRESSED_DOWN
					&& pressedState != PRESSED_MOVE_HORIZANTAL)
				break;

			int xOffset = (int) (ev.getX() - lastActionDownX);
			int yOffset = (int) (ev.getY() - lastActionDownY);

			if (pressedState == PRESSED_DOWN) {
				if (yOffset > 25 || yOffset < -25) {
					pressedState = PRESSED_MOVE_VERTICAL;
					break;
				}
				if (xOffset < -50 || xOffset > 50) {
					pressedState = PRESSED_MOVE_HORIZANTAL;
					ev.setAction(MotionEvent.ACTION_CANCEL);
				}
			} else if (pressedState == PRESSED_MOVE_HORIZANTAL) {
				if (currentActivityScaleX < 0.95)
					scrollViewMenu.setVisibility(VISIBLE);

				float targetScale = getTargetScale(ev.getRawX());
				ViewHelper.setScaleX(viewActivity, targetScale);
				ViewHelper.setScaleY(viewActivity, targetScale);
				ViewHelper.setScaleX(imageViewShadow, targetScale
						+ shadowAdjustScaleX);
				ViewHelper.setScaleY(imageViewShadow, targetScale
						+ shadowAdjustScaleY);
				ViewHelper.setAlpha(scrollViewMenu, (1 - targetScale) * 2.0f);

				lastRawX = ev.getRawX();
				return true;
			}

			break;

		case MotionEvent.ACTION_UP:

			if (isInIgnoredView)
				break;
			if (pressedState != PRESSED_MOVE_HORIZANTAL)
				break;

			pressedState = PRESSED_DONE;
			if (isOpened()) {
				if (currentActivityScaleX > 0.56f)
					closeMenu();
				else
					openMenu(scaleDirection);
			} else {
				if (currentActivityScaleX < 0.94f) {
					openMenu(scaleDirection);
				} else {
					closeMenu();
				}
			}

			break;

		}
		lastRawX = ev.getRawX();
		return super.dispatchTouchEvent(ev);
	}

	public int getScreenHeight() {
		activity.getWindowManager().getDefaultDisplay()
				.getMetrics(displayMetrics);
		return displayMetrics.heightPixels;
	}

	public int getScreenWidth() {
		activity.getWindowManager().getDefaultDisplay()
				.getMetrics(displayMetrics);
		return displayMetrics.widthPixels;
	}

	public void setScaleValue(float scaleValue) {
		this.mScaleValue = scaleValue;
	}

	public interface OnMenuListener {

		/**
		 * the method will call on the finished time of opening menu's
		 * animation.
		 */
		public void openMenu();

		/**
		 * the method will call on the finished time of closing menu's animation
		 * .
		 */
		public void closeMenu();
	}

}
