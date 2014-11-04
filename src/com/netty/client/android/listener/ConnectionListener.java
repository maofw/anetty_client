package com.netty.client.android.listener;

import android.content.Context;
import android.util.Log;

import com.netty.client.android.dao.Device;
import com.netty.client.android.service.RemoteService;
import com.netty.client.context.ApplicationContextClient;

/**
 * 服务器连接成功 回調處理
 * 
 * @类名称：ConnectionListener
 * @类描述：
 * @创建人：maofw
 * @创建时间：2014-10-20 上午9:31:31
 * 
 */
public class ConnectionListener implements INettyHandlerListener<Device> {
	private ApplicationContextClient applicationContextClient = RemoteService.getApplicationContextClient();

	private Device deviceInfo;

	private Context mContext;

	public ConnectionListener(Context context, String appKey, String appPackage) {
		Log.i(ApplicationContextClient.class.getName(), "ConnectionListener");
		mContext = context;
		// 获取设备缓存信息
		deviceInfo = applicationContextClient.makeDevice(context, appKey, appPackage);
	}

	@Override
	public void callback(Device t) {
		// 服务器连接成功后发送设备注册信息给服务器
		// 发送设备注册消息
		if (deviceInfo != null && deviceInfo.getIsOnline() == ApplicationContextClient.DEVICE_OFFLINE) {
			// 如果设备不在线 则发送注册请求
			applicationContextClient.sendRegistrationMessage(deviceInfo, new RegistrationResultListener(mContext, deviceInfo));
		}

	}
}
