package com.netty.client.android.network;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.netty.client.android.broadcast.AlarmReceiver;

public class DefaultNetworkCallback implements INetworkCallback {
	// 是否正在处理网络重连操作
	private boolean isConnected = false;
	private boolean ISDEALING = false;
	private AlarmManager alarmManager = null;

	private PendingIntent sender;
	// 重连时间 10s
	private static final long RECONN_TIMESOUT = 10 * 1000L;

	// 重连次数限制 3
	private static final int RECONN_MAXCNT = 3;

	// 重连次数
	private int reconnCnt = 0;

	private Context context;

	public DefaultNetworkCallback(Context context) {
		this.context = context;
	}

	@Override
	public void connectFailed() {
		if (!isConnected && !ISDEALING) {
			ISDEALING = true;
			if (reconnCnt >= RECONN_MAXCNT) {
				// 已经达到了规定限制 则停止一切活动运行
				stopAlarm();
				reconnCnt = 0;
				return;
			}

			reconnCnt++;

			if (alarmManager == null) {
				alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			}
			// 操作：发送一个广播，广播接收后Toast提示定时操作完成
			Intent intent = new Intent();
			intent.setAction(AlarmReceiver.CONNECT_ACTION);
			sender = PendingIntent.getBroadcast(context, 0, intent, 0);
			// 15s后重新发送连接请求
			ISDEALING = false;
			alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + RECONN_TIMESOUT, sender);
		}

	}

	@Override
	public void connectSuccess() {
		isConnected = true;
		ISDEALING = false;
		stopAlarm();
		reconnCnt = 0;
	}

	/**
	 * 停止定时时钟
	 */
	private void stopAlarm() {
		if (alarmManager != null && sender != null) {
			alarmManager.cancel(sender);
		}
	}

	@Override
	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}
}
