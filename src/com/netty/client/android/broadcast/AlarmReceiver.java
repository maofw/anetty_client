package com.netty.client.android.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.netty.client.android.NettyServerManager;
import com.netty.client.android.service.RemoteService;
import com.netty.client.context.ApplicationContextClient;

public class AlarmReceiver extends BroadcastReceiver {
	public static String ACTION = "HEART_BEAT";
	public static String CONNECT_ACTION = "CONNECT_ACTION";
	private ConnectivityManager manager = null;//
	private ApplicationContextClient applicationContextClient = RemoteService.getApplicationContextClient();

	private void connectivityManager(Context context) {
		if (manager == null) {
			manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Log.i(ApplicationContextClient.class.getName(), "AlarmReceiver:" + intent.getAction());
		} catch (Exception e) {
			System.out.println("AlarmReceiver");
		}
		if (ACTION.equals(intent.getAction())) {
			// 心跳广播
			applicationContextClient.sendHeartBeatMessage();
		} else if (CONNECT_ACTION.equals(intent.getAction())) {
			// 重连广播监听
			try {
				NettyServerManager.reconnect();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
			// 网络状态监听广播接收
			connectivityManager(context);
			NetworkInfo gprs = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (!gprs.isConnected() && !wifi.isConnected()) {
				// 網絡斷開 了
				// 停止心跳消息发送
				NettyAlarmManager.stopHeart();
				// 停止重连
				NettyAlarmManager.stopReconnection();
			} else {
				// 启动重连
				NettyAlarmManager.startReconnection(context);
			}
		}
	}
}
