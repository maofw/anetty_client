package com.netty.client.android.listener;

import java.util.Iterator;
import java.util.Map;

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
public class ReConnectionListener implements INettyHandlerListener<Device> {
	private ApplicationContextClient applicationContextClient = RemoteService.getApplicationContextClient();

	@Override
	public void callback(Device t) {
		Device deviceInfo = null;
		// 服务器连接成功后发送设备注册信息给服务器
		Map<String, Device> devices = applicationContextClient.getDeviceInfos();
		if (devices != null && !devices.isEmpty()) {
			// 循环发送设备注册消息
			Iterator<Map.Entry<String, Device>> iterator = devices.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Device> entry = iterator.next();
				if (entry != null) {
					// 獲取設備信息
					deviceInfo = entry.getValue();
					applicationContextClient.sendRegistrationMessage(deviceInfo, null);
				}
			}
		}
	}
}
