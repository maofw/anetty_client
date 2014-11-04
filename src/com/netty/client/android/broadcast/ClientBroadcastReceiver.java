package com.netty.client.android.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.netty.client.api.ConnectionManager;
import com.netty.client.consts.SystemConsts;
import com.netty.client.context.ApplicationContextClient;
import com.xwtec.protoc.CommandProtoc;

/**
 * 消息通知广播接收器
 * 
 * @类名称：NotificationReceiver
 * @类描述：
 * @创建人：maofw
 * @创建时间：2014-10-24 上午10:33:48
 * 
 */
public class ClientBroadcastReceiver extends BroadcastReceiver {
	// 消息通知
	public static final String NOTIFICATION_ACTION = "com.xwtec.netty.NOTIFICATION";
	// 注册成功广播通知
	public static final String REG_ACTION = "com.xwtec.netty.REG";

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Log.i(ApplicationContextClient.class.getName(), "NotificationReceiver:" + intent.getAction() + ":" + context);
		} catch (Exception e) {
			System.out.println("NotificationReceiver");
		}
		if (REG_ACTION.equals(intent.getAction())) {
			// 接收广播通知 展示notification
			CommandProtoc.RegistrationResult message = (CommandProtoc.RegistrationResult) intent.getSerializableExtra(SystemConsts.REGISTRATION_MESSAGE);
			// 通知所有监听器
			if (message != null) {
				// 获取本应用AppPackage名称
				if (isSelfContext(context, message.getAppPackage())) {
					ConnectionManager.notificationRegisterListeners(message.getRegistrationId());
				}
			}

		} else if (NOTIFICATION_ACTION.equals(intent.getAction())) {
			// 接收广播通知 展示notification
			CommandProtoc.Message message = (CommandProtoc.Message) intent.getSerializableExtra(SystemConsts.NOTIFICATION_MESSAGE);
			// 通知所有监听器
			if (message != null) {
				// 获取本应用AppPackage名称
				if (isSelfContext(context, message.getAppPackage())) {
					ConnectionManager.notificationMessageListeners(message);
				}
			}
		}
	}

	/**
	 * 判断是否是属于本应用
	 * 
	 * @param context
	 * @param appPackage
	 * @return
	 */
	private boolean isSelfContext(Context context, String appPackage) {
		boolean b = false;
		if (appPackage != null && appPackage.equals(context.getPackageName())) {
			b = true;
		}
		return b;
	}

}
