package com.netty.client.android.listener;

import android.content.Context;
import android.content.Intent;

import com.netty.client.android.broadcast.ClientBroadcastReceiver;
import com.netty.client.android.dao.Device;
import com.netty.client.android.service.RemoteService;
import com.netty.client.consts.SystemConsts;
import com.netty.client.context.ApplicationContextClient;
import com.xwtec.protoc.CommandProtoc;

/**
 * 系统默认的注册listener实现
 * 
 * @author maofw
 * 
 */
public class RegistrationResultListener implements INettyHandlerListener<CommandProtoc.RegistrationResult> {
	private ApplicationContextClient applicationContextClient = RemoteService.getApplicationContextClient();

	private Device deviceInfo;
	private Context mContext;

	public RegistrationResultListener(Context context, Device deviceInfo) {
		this.deviceInfo = deviceInfo;
		this.mContext = context;
	}

	@Override
	public void callback(CommandProtoc.RegistrationResult t) {
		if (t != null && t.getRegistrationId() != null) {
			deviceInfo.setRegId(t.getRegistrationId());
			// 注册成功 缓存设备信息
			applicationContextClient.saveOrUpdateDevice(deviceInfo);
			// 注册成功了 设备上线消息处理
			applicationContextClient.sendDeviceOnlineMessage(deviceInfo, null);

			if (mContext != null) {
				// 发送广播给客户端
				Intent intent = new Intent();
				intent.setAction(ClientBroadcastReceiver.REG_ACTION);
				intent.putExtra(SystemConsts.REGISTRATION_MESSAGE, t);
				mContext.sendBroadcast(intent);
			}
		}
	}
}
