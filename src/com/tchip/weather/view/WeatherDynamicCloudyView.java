package com.tchip.weather.view;

import com.tchip.weather.MyApp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class WeatherDynamicCloudyView extends View implements Runnable {

	/**
	 * 要处理的图
	 */
	public static Bitmap bitmap;

	private int left;
	private int top;

	/**
	 * 图片移动频率
	 */
	private int dx = 1;
	private int dy = 1;

	private int sleepTime;

	/**
	 * 图片是否在移动
	 */
	public static boolean IsRunning = true;

	private Handler handler;

	public WeatherDynamicCloudyView(Context context, Bitmap bitmap, int left,
			int top, int sleepTime) {
		super(context);

		this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		this.bitmap = bitmap;
		this.left = left;
		this.top = top;
		this.sleepTime = sleepTime;

		handler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				WeatherDynamicCloudyView.this.invalidate();
			};
		};
	}

	public void move() {
		new Thread(this).start();
		// new Thread(new TimeThread()).start(); // 5s后取消动画
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		canvas.drawBitmap(bitmap, left, top, null);
	}

	@Override
	public void run() {

		while (WeatherDynamicCloudyView.IsRunning && MyApp.isActivityShowing) {
			if ((bitmap != null) && (left > (getWidth()))) {
				left = -bitmap.getWidth();
			}
			left = left + dx;
			handler.sendMessage(handler.obtainMessage());
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	final Handler timeHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				IsRunning = false;
			}
			super.handleMessage(msg);
		}
	};

	public class TimeThread implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(5000);
					Message message = new Message();
					message.what = 1;
					timeHandler.sendMessage(message);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
