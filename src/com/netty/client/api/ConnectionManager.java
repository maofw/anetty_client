package com.netty.client.api;

import java.util.ArrayList;
import java.util.Iterator;

import com.netty.client.api.listener.IConnectionListener;
import com.netty.client.api.listener.IRegisterListener;
import com.xwtec.protoc.CommandProtoc;

/**
 * 连接管理器
 * 
 * @类名称：ConnectionManager
 * @类描述：
 * @创建人：maofw
 * @创建时间：2014-10-28 下午12:46:21
 * 
 */
public class ConnectionManager {

	// private static ConnectionManager connectionManager = new ConnectionManager();

	private static ArrayList<IConnectionListener> connectionListeners = new ArrayList<IConnectionListener>();

	private static ArrayList<IRegisterListener> registerListeners = new ArrayList<IRegisterListener>();

	// private ConnectionManager() {
	// }
	//
	// public static ConnectionManager getInstance() {
	// return connectionManager;
	// }

	/**
	 * 添加 监听器
	 * 
	 * @param connectionListener
	 */
	public static void addConnectionListener(IConnectionListener connectionListener) {
		if (connectionListener != null && !connectionListeners.contains(connectionListener)) {
			connectionListeners.add(connectionListener);
		}
	}
	
	public static void addRegisterListener(IRegisterListener registerListener) {
		if (registerListener != null && !registerListeners.contains(registerListener)) {
			registerListeners.add(registerListener);
		}
	}

	/**
	 * 
	 */
	public static void notificationMessageListeners(CommandProtoc.Message message) {
		if (connectionListeners != null && !connectionListeners.isEmpty()) {
			Iterator<IConnectionListener> iterator = connectionListeners.iterator();
			while (iterator.hasNext()) {
				iterator.next().receive(message);
			}
		}
	}

	public static void notificationRegisterListeners(String regId) {
		if (registerListeners != null && !registerListeners.isEmpty()) {
			Iterator<IRegisterListener> iterator = registerListeners.iterator();
			while (iterator.hasNext()) {
				iterator.next().receive(regId);
			}
		}
	}
}
