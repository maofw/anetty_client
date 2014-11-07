package com.netty.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.netty.client.android.NettyServerManager;
import com.netty.client.android.dao.Device;
import com.netty.client.android.handler.MessageObject;
import com.netty.client.android.handler.NettyProcessorHandler;
import com.netty.client.android.service.RemoteService;
import com.netty.client.context.ApplicationContextClient;
import com.xwtec.protoc.CommandProtoc;
import com.xwtec.protoc.CommandProtoc.PushMessage;

/**
 * 客户端消息处理Handler
 * 
 * @author maofw
 * 
 */
public class PushMessageHandler extends SimpleChannelInboundHandler<CommandProtoc.PushMessage> {
	private ApplicationContextClient applicationContextClient;

	private NettyServerManager mConnectionManager;
	private Handler mHandler;

	public PushMessageHandler(NettyServerManager connectionManager, Handler handler) {
		this.mConnectionManager = connectionManager;
		this.mHandler = handler;
		this.applicationContextClient = RemoteService.getApplicationContextClient();
	}

	/**
	 * 此方法会在连接到服务器后被调用
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// 激活后发送设备注册请求
		applicationContextClient.setCtx(ctx);
		// applicationContextClient.sendRegistrationMessage(null);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, PushMessage t) throws Exception {
		try {
			Log.i(getClass().getName(), "channelRead0:" + t.toString());
		} catch (Exception e) {
			System.out.println("channelRead0:======================\n" + t);
		}

	}

	private void channelReadMe(ChannelHandlerContext ctx, PushMessage t) {
		try {
			Log.i(getClass().getName(), "channelReadMe:" + t.toString());
		} catch (Exception e) {
			System.out.println("channelReadMe:======================\n" + t);
		}
		if (t != null) {
			CommandProtoc.PushMessage.Type type = t.getType();
			int what = -1;
			Object obj = null;
			String appPackage = null;
			switch (type) {
			case REGISTRATION_RESULT:
				// 註冊結果消息
				CommandProtoc.RegistrationResult registrationResult = t.getRegistrationResult();
				what = NettyProcessorHandler.REGISTRATION_RESULT;
				obj = registrationResult;
				appPackage = registrationResult.getAppPackage();
				break;
			case DEVICE_ONOFFLINE_RESULT:
				// 上下线结果消息
				CommandProtoc.DeviceOnoffResult deviceOnoffResult = t.getDeviceOnoffResult();
				if (deviceOnoffResult != null) {
					CommandProtoc.Message.UserStatus userStatus = deviceOnoffResult.getUserStatus();
					switch (userStatus) {
					case ONLINE:
						// 上线结果消息
						what = NettyProcessorHandler.DEVICE_ONLINE_RESULT;
						obj = deviceOnoffResult.getResCode();
						appPackage = deviceOnoffResult.getAppPackage();
						if (deviceOnoffResult.getResCode() == CommandProtoc.DeviceOnoffResult.ResultCode.SUCCESS) {
							// 更新设备信息
							Device device = applicationContextClient.getDeviceInfoByAppPackage(appPackage);
							if (device != null) {
								device.setIsOnline(ApplicationContextClient.DEVICE_ONLINE);
								//applicationContextClient.saveOrUpdateDevice(device);
							}
						}
						break;
					case OFFLINE:
						// 下线结果消息
						what = NettyProcessorHandler.DEVICE_OFFLINE_RESULT;
						obj = deviceOnoffResult.getResCode();
						appPackage = deviceOnoffResult.getAppPackage();
						if (deviceOnoffResult.getResCode() == CommandProtoc.DeviceOnoffResult.ResultCode.SUCCESS) {
							// 更新设备信息
							Device device = applicationContextClient.getDeviceInfoByAppPackage(appPackage);
							if (device != null) {
								// 删除设备信息 不会发送消息了 （客户端中设备 在线状态与服务端设备状态时不一样的：客户端设备下线标识需要向发送登陆消息 ，上线成功后不需要重复发送登陆请求，而不需要发送消息的时候需要将客户端service数据缓存清除
								// ，服务端设备上下线仅更新状态内容，不会删除信息）
								applicationContextClient.deleteDeviceInfo(device);
								// device.setIsOnline(ApplicationContextClient.DEVICE_OFFLINE);
								// applicationContextClient.saveOrUpdateDevice(device);
							}
						}
						break;
					default:
						break;
					}
				}
				break;
			case MESSAGE:
				// 消息处理
				CommandProtoc.Message commandMessage = t.getMessage();
				if (commandMessage != null) {
					// 判断是否需要发送回执消息
					if (commandMessage.getIsReceipt()) {
						Device deviceInfo = applicationContextClient.getDeviceInfoByAppPackage(commandMessage.getAppPackage());
						// 收到消息 需要通知服务器 发送消息回执内容
						applicationContextClient.sendReceiptMessage(deviceInfo, commandMessage.getMsgId());
					}
					what = NettyProcessorHandler.MESSAGE;
					obj = commandMessage;
					appPackage = commandMessage.getAppPackage();
				}
				break;
			default:
				break;
			}
			if (mHandler != null) {
				MessageObject mo = new MessageObject();
				mo.setAppPackage(appPackage);
				mo.setObj(obj);
				// handler sendMessage
				Message message = mHandler.obtainMessage();
				message.what = what;
				message.obj = mo;
				mHandler.sendMessage(message);
			}
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
		if (obj != null && obj instanceof CommandProtoc.PushMessage) {
			channelReadMe(ctx, (CommandProtoc.PushMessage) obj);
		}
	}

	/**
	 * 捕捉到异常
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		Log.i(getClass().getName(), cause.getMessage());
		cause.printStackTrace();
		/**
		 * 异常关闭连接启动重连操作
		 */
		// 首先更新连接状态
		mConnectionManager.setConnectState(NettyServerManager.CONNECT_EXCEPTION);
		// 更新设备状态 信息为离线
		applicationContextClient.offlineAllDevices();
		ctx.close();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Log.i(getClass().getName(), "channelInactive");
		/**
		 * 异常关闭连接启动重连操作
		 */
		// 首先更新连接状态
		mConnectionManager.setConnectState(NettyServerManager.CONNECT_CLOSED);
		// 更新设备状态 信息为离线
		applicationContextClient.offlineAllDevices();
		ctx.close();
	}

}
