package com.netty.client.context;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.netty.client.android.dao.Device;
import com.netty.client.android.handler.NettyProcessorHandler;
import com.netty.client.android.listener.INettyHandlerListener;
import com.netty.client.android.listener.RegistrationResultListener;
import com.netty.client.android.service.PushDbService;
import com.netty.client.consts.SystemConsts;
import com.netty.client.utils.Md5Util;
import com.xwtec.protoc.CommandProtoc;

/**
 * 客户端调用
 * 
 * @author maofw
 * 
 */
public class ApplicationContextClient {

	// 设备在线状态
	public static final int DEVICE_ONLINE = 1;
	public static final int DEVICE_OFFLINE = 0;

	// 是否关闭状态
	public static boolean isClosed = false;
	private ChannelHandlerContext ctx;
	private Map<String, Device> deviceInfos = new HashMap<String, Device>();

	// 保存handler 回调Listener
	@SuppressWarnings("rawtypes")
	private Map<String, Map<Integer, INettyHandlerListener>> nettyHandlerListeners = new HashMap<String, Map<Integer, INettyHandlerListener>>();

	private PushDbService pushDbService;
	private Context mContext;

	public ApplicationContextClient(Context context) {
		this.mContext = context;
		this.pushDbService = PushDbService.getInstance(context);
	}

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	public void writeAndFlush(CommandProtoc.PushMessage pushMessage) {
		if (this.ctx != null) {
			this.ctx.writeAndFlush(pushMessage);
		}
	}

	public Map<String, Device> getDeviceInfos() {
		if (deviceInfos == null || deviceInfos.isEmpty()) {
			Map<String, Device> map = this.pushDbService.queryDevicesForMap();
			if (map != null && map.size() > 0) {
				deviceInfos.putAll(map);
			}
		}
		return deviceInfos;
	}

	/**
	 * 根据app包名获取设备信息
	 * 
	 * @param appPackage
	 * @return
	 */
	public Device getDeviceInfoByAppPackage(String appPackage) {
		Map<String, Device> map = this.getDeviceInfos();
		if (map != null && !map.isEmpty()) {
			return map.get(appPackage);
		}
		return null;
	}

	/**
	 * 註冊設備
	 * 
	 * @param appKey
	 * @param appPackage
	 * @param deviceId
	 * @param imei
	 * @param regId
	 * @return
	 */
	public void saveOrUpdateDevice(Device device) {
		if (device != null) {
			deviceInfos.put(device.getAppPackage(), device);
			this.pushDbService.saveOrUpdateDevice(device);
		}
	}

	/**
	 * 生成Device对象
	 * 
	 * @param appKey
	 * @param appPackage
	 * @param deviceId
	 * @param imei
	 * @return
	 */

	public Device makeDevice(Context context, String appKey, String appPackage) {

		Device deviceInfo = this.getDeviceInfoByAppPackage(appPackage);
		if (deviceInfo == null || (deviceInfo.getAppKey() != null && !deviceInfo.getAppKey().equals(appKey))) {
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			String imei = tm.getDeviceId();

			String macAddress = null;
			WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());
			if (null != info) {
				macAddress = info.getMacAddress();
			}

			String deviceId = Md5Util.toMD5(SystemConsts.CHANNEL + appKey + macAddress + imei);
			if (deviceInfo == null) {
				deviceInfo = new Device();
			}
			deviceInfo.setAppKey(appKey);
			deviceInfo.setAppPackage(appPackage);
			deviceInfo.setDeviceId(deviceId);
			deviceInfo.setImei(imei);
			deviceInfo.setIsOnline(DEVICE_OFFLINE);
		}
		return deviceInfo;
	}

	/**
	 * 删除设备
	 * 
	 * @param appPackage
	 */
	public void deleteDeviceInfo(Device deviceInfo) {
		if (deviceInfo != null) {
			this.pushDbService.deleteDevice(deviceInfo);
			// 删除缓存内容
			deviceInfos.remove(deviceInfo.getAppPackage());
		}
	}

	public void offlineAllDevices() {
		if (deviceInfos != null && !deviceInfos.isEmpty()) {
			List<Device> list = new ArrayList<Device>();
			Iterator<Map.Entry<String, Device>> iterator = deviceInfos.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Device> entry = iterator.next();
				Device device = entry.getValue();
				if (device != null) {
					device.setIsOnline(DEVICE_OFFLINE);
					list.add(device);
				}
			}
			this.pushDbService.saveOrUpdateDevices(list);
			list = null;
		}

	}

	@SuppressWarnings("rawtypes")
	public void registListener(String appPackage, Integer type, INettyHandlerListener listener) {
		if (listener != null) {
			Map<Integer, INettyHandlerListener> mNettyHandlerListeners = nettyHandlerListeners.get(appPackage);
			INettyHandlerListener nettyHandlerListener = null;
			if (mNettyHandlerListeners == null) {
				mNettyHandlerListeners = new HashMap<Integer, INettyHandlerListener>();
				nettyHandlerListeners.put(appPackage, mNettyHandlerListeners);
			} else {
				nettyHandlerListener = mNettyHandlerListeners.get(type);
				if (nettyHandlerListener != null) {
					nettyHandlerListener = null;
				}
			}
			mNettyHandlerListeners.put(type, listener);
		}
	}

	@SuppressWarnings("rawtypes")
	public INettyHandlerListener getNettyHandlerListener(String appPackage, Integer type) {
		if (appPackage != null && type != null && nettyHandlerListeners.containsKey(appPackage)) {
			Map<Integer, INettyHandlerListener> mNettyHandlerListeners = nettyHandlerListeners.get(appPackage);
			INettyHandlerListener nettyHandlerListener = mNettyHandlerListeners == null ? null : mNettyHandlerListeners.get(type);
			return nettyHandlerListener;
		}
		return null;
	}

	/**
	 * 心跳请求
	 * 
	 * @param listener
	 */
	public void sendHeartBeatMessage() {
		try {
			Log.i(ApplicationContextClient.class.getName(), "sendHeartBeatMessage");
		} catch (Exception e) {
			System.out.println("sendHeartBeatMessage");
		}
		if (ctx != null) {
			// 心跳请求
			CommandProtoc.PushMessage.Builder builder = this.createCommandPushMessage(CommandProtoc.PushMessage.Type.HEART_BEAT);
			ctx.writeAndFlush(builder.build());
		}
	}

	/**
	 * 设备注册请求
	 * 
	 * @param ctx
	 * @param imei
	 * @param deviceId
	 * @param appKey
	 * @param appPackage
	 */
	public void sendRegistrationMessage(Device deviceInfo, INettyHandlerListener<CommandProtoc.RegistrationResult> listener) {
		try {
			Log.i(ApplicationContextClient.class.getName(), "sendRegistrationMessage");
		} catch (Exception e) {
			System.out.println("sendRegistrationMessage");
		}
		// Log.i(ApplicationContextClient.class.getName(),"sendRegistrationMessage");
		// 激活后发送设备注册请求
		if (deviceInfo != null) {
			if (listener == null && this.getNettyHandlerListener(deviceInfo.getAppPackage(), NettyProcessorHandler.REGISTRATION_RESULT) == null) {
				this.registListener(deviceInfo.getAppPackage(), NettyProcessorHandler.REGISTRATION_RESULT, new RegistrationResultListener(mContext, deviceInfo));
			} else {
				this.registListener(deviceInfo.getAppPackage(), NettyProcessorHandler.REGISTRATION_RESULT, listener);
			}
			CommandProtoc.PushMessage pushMessage = this.createCommandRegistration(deviceInfo.getImei(), deviceInfo.getDeviceId(), deviceInfo.getAppKey(),
					deviceInfo.getAppPackage());
			ctx.writeAndFlush(pushMessage);
		}
	}

	/**
	 * 发送设备上线消息
	 * 
	 * @param ctx
	 * @param deviceId
	 */
	public void sendDeviceOnlineMessage(Device deviceInfo, INettyHandlerListener<CommandProtoc.DeviceOnoffResult> listener) {
		try {
			Log.i(ApplicationContextClient.class.getName(), "sendDeviceOnlineMessage");
		} catch (Exception e) {
			System.out.println("sendDeviceOnlineMessage");
		}
		if (deviceInfo != null) {
			this.registListener(deviceInfo.getAppPackage(), NettyProcessorHandler.DEVICE_ONLINE_RESULT, listener);
			CommandProtoc.PushMessage pushMessage = this.createCommandDeviceOnline(deviceInfo.getDeviceId());
			ctx.writeAndFlush(pushMessage);
		}
	}

	/**
	 * 发送设备下线消息
	 * 
	 * @param ctx
	 * @param deviceId
	 */
	public void sendDeviceOfflineMessage(Device deviceInfo, INettyHandlerListener<CommandProtoc.DeviceOnoffResult> listener) {
		try {
			Log.i(ApplicationContextClient.class.getName(), "sendDeviceOfflineMessage");
		} catch (Exception e) {
			System.out.println("sendDeviceOfflineMessage");
		}
		if (deviceInfo != null) {
			this.registListener(deviceInfo.getAppPackage(), NettyProcessorHandler.DEVICE_OFFLINE_RESULT, listener);
			CommandProtoc.PushMessage pushMessage = this.createCommandDeviceOffline(deviceInfo.getDeviceId());
			ctx.writeAndFlush(pushMessage);
		}
	}

	/**
	 * 发送消息确认回执消息
	 * 
	 * @param ctx
	 * @param appKey
	 * @param msgId
	 */
	public void sendReceiptMessage(Device deviceInfo, String msgId) {
		try {
			Log.i(ApplicationContextClient.class.getName(), "sendReceiptMessage");
		} catch (Exception e) {
			System.out.println("sendReceiptMessage");
		}
		if (deviceInfo != null) {
			CommandProtoc.PushMessage pushMessage = this.createCommandMessageReceipt(deviceInfo.getAppKey(), deviceInfo.getRegId(), msgId);
			ctx.writeAndFlush(pushMessage);
		}
	}

	/**
	 * 创建Registration对象
	 * 
	 * @param type
	 * @return
	 */
	public CommandProtoc.PushMessage createCommandRegistration(String imei, String deviceId, String appKey, String appPackage) {
		CommandProtoc.Registration.Builder builder = CommandProtoc.Registration.newBuilder();
		builder.setImei(imei);
		builder.setDeviceId(deviceId);
		builder.setAppKey(appKey);
		builder.setAppPackage(appPackage);
		builder.setChannel(SystemConsts.CHANNEL);
		CommandProtoc.Registration commandProtoc = builder.build();

		// 创建消息对象
		CommandProtoc.PushMessage.Builder messageBuilder = this.createCommandPushMessage(CommandProtoc.PushMessage.Type.REGISTRATION);
		messageBuilder.setRegistration(commandProtoc);
		return messageBuilder.build();
	}

	/**
	 * 创建DeviceOnline对象
	 * 
	 * @param type
	 * @return
	 */
	public CommandProtoc.PushMessage createCommandDeviceOnline(String deviceId) {
		CommandProtoc.DeviceOnline.Builder builder = CommandProtoc.DeviceOnline.newBuilder();
		builder.setDeviceId(deviceId);
		CommandProtoc.DeviceOnline commandProtoc = builder.build();

		// 创建消息对象
		CommandProtoc.PushMessage.Builder messageBuilder = this.createCommandPushMessage(CommandProtoc.PushMessage.Type.DEVICE_ONLINE);
		messageBuilder.setDeviceOnline(commandProtoc);
		return messageBuilder.build();
	}

	/**
	 * 创建DeviceOffline对象
	 * 
	 * @param type
	 * @return
	 */
	public CommandProtoc.PushMessage createCommandDeviceOffline(String deviceId) {
		CommandProtoc.DeviceOffline.Builder builder = CommandProtoc.DeviceOffline.newBuilder();
		builder.setDeviceId(deviceId);
		CommandProtoc.DeviceOffline commandProtoc = builder.build();

		// 创建消息对象
		CommandProtoc.PushMessage.Builder messageBuilder = this.createCommandPushMessage(CommandProtoc.PushMessage.Type.DEVICE_OFFLINE);
		messageBuilder.setDeviceOffline(commandProtoc);
		return messageBuilder.build();
	}

	/**
	 * 创建MessageReceipt对象
	 * 
	 * @param type
	 * @return
	 */
	public CommandProtoc.PushMessage createCommandMessageReceipt(String appKey, String registrationId, String msgId) {
		CommandProtoc.MessageReceipt.Builder builder = CommandProtoc.MessageReceipt.newBuilder();
		builder.setAppKey(appKey);
		builder.setRegistrationId(registrationId);
		builder.setMsgId(msgId);
		CommandProtoc.MessageReceipt commandProtoc = builder.build();
		// 创建消息对象
		CommandProtoc.PushMessage.Builder messageBuilder = this.createCommandPushMessage(CommandProtoc.PushMessage.Type.MESSAGE_RECEIPT);
		messageBuilder.setMessageReceipt(commandProtoc);
		return messageBuilder.build();
	}

	/**
	 * 创建发送消息对象
	 * 
	 * @param type
	 * @return
	 */
	private CommandProtoc.PushMessage.Builder createCommandPushMessage(CommandProtoc.PushMessage.Type type) {
		CommandProtoc.PushMessage.Builder builder = CommandProtoc.PushMessage.newBuilder();
		builder.setType(type);
		return builder;
	}

	public void destory() {
		isClosed = true;
		if (this.deviceInfos != null) {
			this.deviceInfos.clear();
			this.deviceInfos = null;
		}

		if (nettyHandlerListeners != null) {
			nettyHandlerListeners.clear();
			nettyHandlerListeners = null;
		}

		pushDbService = null;
		if (ctx != null) {
			ctx.close();
			ctx = null;
		}
	}
}
