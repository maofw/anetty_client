package com.netty.client.android;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import android.content.Context;
import android.util.Log;

import com.google.protobuf.ExtensionRegistry;
import com.netty.client.android.broadcast.NettyAlarmManager;
import com.netty.client.android.handler.NettyProcessorHandler;
import com.netty.client.android.listener.INettyHandlerListener;
import com.netty.client.android.listener.ReConnectionListener;
import com.netty.client.context.ApplicationContextClient;
import com.netty.client.handler.PushMessageHandler;
import com.xwtec.protoc.CommandProtoc;

/**
 * 客户端连接类
 * 
 * @author maofw
 * 
 */
public class NettyServerManager {

	// 心跳監控週期 30s
	private EventLoopGroup group;
	// private ChannelFuture channelFuture;

	// private ApplicationContextClient applicationContextClient;

	private static NettyProcessorHandler mNettyProcessorHandler;

	private static NettyServerManager connectionManager = null;

	private static Context mContext;
	private String mHost;
	private int mPort;

	// private INetworkCallback mNetworkCallback;

	/**
	 * 连接状态常量
	 */
	// 初始化
	public static final int CONNECT_INIT = 0;
	// 正在处理中
	public static final int CONNECT_PROCESSORING = 1;
	// 連接成功
	public static final int CONNECT_SUCCESS = 2;
	// 連接失敗
	public static final int CONNECT_FAILED = -1;
	// 連接关闭
	public static final int CONNECT_CLOSED = -2;
	// 連接超時
	public static final int CONNECT_TIMEOUT = -3;
	// 連接异常
	public static final int CONNECT_EXCEPTION = -4;
	// 连接状态
	private int connectState = CONNECT_INIT;

	public int getConnectState() {
		return connectState;
	}

	public void setConnectState(int connectState) {
		this.connectState = connectState;
	}

	/**
	 * 构造方法传递Context和 appKey
	 * 
	 * @param context
	 * @param appKey
	 */
	private NettyServerManager(Context context, NettyProcessorHandler nettyProcessorHandler) {
		mContext = context;
		// mNetworkCallback = new DefaultNetworkCallback(context);
		mNettyProcessorHandler = nettyProcessorHandler;
	}

	public static NettyServerManager getInstance(Context context, NettyProcessorHandler nettyProcessorHandler) {
		if (connectionManager == null) {
			connectionManager = new NettyServerManager(context, nettyProcessorHandler);
		}
		return connectionManager;
	}

	/**
	 * 重连
	 */
	public static void reconnect() throws Exception {
		if (connectionManager == null) {
			connectionManager = new NettyServerManager(mContext, mNettyProcessorHandler);
		}
		// connectionManager.mNetworkCallback.setConnected(false);
		connectionManager.reconnection();
	}

	private void reconnection() throws Exception {
		this.connect(mHost, mPort, new ReConnectionListener());
	}

	/**
	 * 连接方法
	 * 
	 * @param host
	 * @param port
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public void connect(final String host, final int port, final INettyHandlerListener connectionListener) throws Exception {
		Log.i(getClass().getName(), "connect come in!connectState=" + connectState);
		if (isConnected() || connectState == CONNECT_PROCESSORING) {
			// 連接成功 停止重连
			NettyAlarmManager.stopReconnection();
			return;
		}
		Log.i(getClass().getName(), "connect come in!CONNECT_PROCESSORING!");
		connectState = CONNECT_PROCESSORING;
		mHost = host;
		mPort = port;
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.net.preferIPv6Addresses", "false");
		ChannelFuture channelFuture = null;
		group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group);
			b.channel(NioSocketChannel.class);
			b.option(ChannelOption.SO_KEEPALIVE, true);
			b.option(ChannelOption.TCP_NODELAY, true);
			b.remoteAddress(new InetSocketAddress(mHost, mPort));
			// 有连接到达时会创建一个channel
			final ExtensionRegistry registry = ExtensionRegistry.newInstance();
			CommandProtoc.registerAllExtensions(registry);
			b.handler(new ChannelInitializer<SocketChannel>() {
				public void initChannel(SocketChannel ch) throws Exception {					
					ChannelPipeline pipeline = ch.pipeline();
					pipeline.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
					pipeline.addLast("protobufDecoder", new ProtobufDecoder(CommandProtoc.PushMessage.getDefaultInstance(), registry));
					pipeline.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
					pipeline.addLast("protobufEncoder", new ProtobufEncoder());
					pipeline.addLast(new PushMessageHandler(connectionManager, mNettyProcessorHandler));
				}
			});
			channelFuture = b.connect().sync();
			channelFuture.addListener(new ChannelFutureListener() {
				@SuppressWarnings("unchecked")
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isSuccess()) {
						SocketAddress sa = future.channel().remoteAddress();								
						if(sa!=null){
							Log.i(getClass().getName(), "netty server connected success! host:" + sa);				
							// 連接成功
							connectState = CONNECT_SUCCESS;
							if (connectionListener != null) {
								connectionListener.callback(null);
							}
							// 启动心跳程序
							NettyAlarmManager.startHeart(mContext);
							// 連接成功 停止重连
							NettyAlarmManager.stopReconnection();
						}else{
							Log.i(getClass().getName(), "netty server connected failed! host:" + sa);
							// 連接失敗
							connectState = CONNECT_FAILED;
							// 連接 失敗 啟動重連
							future.cause().printStackTrace();
							future.channel().close();
						}						
					} else {
						Log.i(getClass().getName(), "netty server attemp failed! host:" + future.channel().remoteAddress());
						// 連接失敗
						connectState = CONNECT_FAILED;
						// 連接 失敗 啟動重連
						future.cause().printStackTrace();
						future.channel().close();
						// NettyAlarmManager.startReconnection(mContext);
						// if (mNetworkCallback != null) {
						// mNetworkCallback.connectFailed();
						// }
					}
				}
			});
			// Wait until the connection is closed.
			// channelFuture.channel().closeFuture().sync();
		} catch (Exception e) {
			Log.i(getClass().getName(), e.getMessage());
			connectState = CONNECT_EXCEPTION;
			// 连接关闭后启动重连
			NettyAlarmManager.startReconnection(mContext);
		} finally {
			Log.i(getClass().getName(), "connect finally!connectState=" + connectState);
			disconnect(channelFuture);
		}
	}

	// /**
	// * 设备上线消息请求
	// *
	// * @return
	// */
	// public void sendDeviceOnlineMessage(INettyHandlerListener<CommandProtoc.DeviceOnoffResult> listener) {
	// this.applicationContextClient.sendDeviceOnlineMessage(listener);
	// }
	//
	// /**
	// * 设备上线消息请求
	// *
	// * @return
	// */
	// public void sendDeviceOfflineMessage(INettyHandlerListener<CommandProtoc.DeviceOnoffResult> listener) {
	// this.applicationContextClient.sendDeviceOfflineMessage(listener);
	// }
	//
	// /**
	// * 添加用户自定义消息接收器
	// *
	// * @param listener
	// */
	// public void addMessageListener(INettyHandlerListener<CommandProtoc.Message> listener) {
	// this.applicationContextClient.registListener(NettyProcessorHandler.MESSAGE, listener);
	// }

	/**
	 * 断开连接
	 */
	public void disconnect(ChannelFuture channelFuture) throws Exception {
		if (channelFuture != null) {
			channelFuture.channel().closeFuture().addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture channelFuture) throws Exception {
					if (channelFuture != null) {
						connectState = CONNECT_CLOSED;
						if (!group.isShutdown() && !group.isShuttingDown()) {
							group.shutdownGracefully();// .sync();
						}
						// 已经关闭了 关闭心跳监控
						NettyAlarmManager.stopHeart();
						if (ApplicationContextClient.isClosed) {
							// 主动关闭行为
							// 关闭重新连接定时任务
							NettyAlarmManager.stopReconnection();
							Log.i(getClass().getName(), "stop netty server connection!" + channelFuture.channel().remoteAddress());
						} else {
							// 被动关闭行为
							// 连接关闭后启动重连
							NettyAlarmManager.startReconnection(mContext);
							Log.i(getClass().getName(), "netty server closed success!" + channelFuture.channel().remoteAddress());
						}
					}
				}
			});
		}
	}

	/**
	 * 是否連接
	 * 
	 * @return
	 */
	public boolean isConnected() {
		return connectState == CONNECT_SUCCESS ? true : false;
	}

	/**
	 * 是否連接正在进行中
	 * 
	 * @return
	 */
	public boolean isConnecting() {
		boolean b = false;
		switch (connectState) {
		case CONNECT_INIT:
		case CONNECT_PROCESSORING:
			b = true;
			break;
		default:
			break;
		}
		return b;
	}

	public static void main(String[] args) {
		try {
			new NettyServerManager(null, null).connect("127.0.0.1", 6319, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
