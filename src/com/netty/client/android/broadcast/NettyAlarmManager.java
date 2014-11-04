package com.netty.client.android.broadcast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class NettyAlarmManager {
	private static AlarmManager alarmManager = null;

	private static PendingIntent sender = null;

	private static PendingIntent reconnectSender = null;
	// 心跳週期 默认30s
	public static long PERIOD = 30 * 1000L;

	// 间隔重连时间次数
	public static long reconnectTimes = 1L;
	public static long RECONNECT_PERIOD = 500L;

	private static AlarmManager getAlarmManager(Context context) {
		if (alarmManager == null) {
			alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		}
		return alarmManager;
	}

	/**
	 * 启动心跳监控
	 * 
	 * @param period
	 */
	public static void startHeart(Context context) {
		getAlarmManager(context);
		// 操作：发送一个广播，广播接收后Toast提示定时操作完成
		if (sender == null) {
			Intent intent = new Intent();
			intent.setAction(AlarmReceiver.ACTION);
			sender = PendingIntent.getBroadcast(context, 0, intent, 0);
		}
		// 开始时间
		long firstime = SystemClock.elapsedRealtime() + 10 * 1000L;
		Log.i(NettyAlarmManager.class.getName(), "startHeart");
		// 一个周期，不停的发送广播
		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime, PERIOD, sender);
	}

	/**
	 * 停止心跳監控
	 */
	public static void stopHeart() {
		Log.i(NettyAlarmManager.class.getName(), "stopHeart");
		if (alarmManager != null && sender != null) {
			alarmManager.cancel(sender);
		}
	}

	/**
	 * 启动重连
	 * 
	 * @param period
	 */
	public static void startReconnection(Context context) {
		getAlarmManager(context);
		// 操作：发送一个广播，广播接收后Toast提示定时操作完成
		if (reconnectSender == null) {
			Intent intent = new Intent();
			intent.setAction(AlarmReceiver.CONNECT_ACTION);
			reconnectSender = PendingIntent.getBroadcast(context, 0, intent, 0);
		}
		Log.i(NettyAlarmManager.class.getName(), "startReconnection");
		// 一个周期，不停的发送广播
		// 或者以下面方式简化
		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + reconnectTimes * RECONNECT_PERIOD, reconnectSender);
		increaseReconnectTimes();
		// alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime, PERIOD, sender);
	}

	/**
	 * 重置
	 */
	public static void resetReconnectTimes() {
		reconnectTimes = 1L;
		RECONNECT_PERIOD = 500L;
	}

	/**
	 * 重新计算重连间隔
	 */
	public static void increaseReconnectTimes() {
		// 以2的冪次進行递增
		// if (reconnectTimes > 0) {
		// if (reconnectTimes + 100L >= Long.MAX_VALUE) {
		// resetReconnectTimes();
		// } else {
		// reconnectTimes += (reconnectTimes++) * 100L;
		//
		// }
		// } else if (reconnectTimes < 0) {
		// resetReconnectTimes();
		// }

		if (((++reconnectTimes) * (RECONNECT_PERIOD + 500L)) >= Long.MAX_VALUE) {
			resetReconnectTimes();
		} else {
			RECONNECT_PERIOD += 500L;
		}
	}

	/**
	 * 停止重连監控
	 */
	public static void stopReconnection() {
		Log.i(NettyAlarmManager.class.getName(), "stopReconnection");
		if (alarmManager != null && reconnectSender != null) {
			alarmManager.cancel(reconnectSender);
		}
		resetReconnectTimes();
	}
}
