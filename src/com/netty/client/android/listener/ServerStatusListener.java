package com.netty.client.android.listener;

import java.util.ArrayList;

/**
 * 服务器连接成功 回調處理
 * 
 * @类名称：ServerStatusListener
 * @类描述：
 * @创建人：maofw
 * @创建时间：2014-10-20 上午9:31:31
 * 
 */
@SuppressWarnings("rawtypes")
public class ServerStatusListener implements INettyHandlerListener<Object> {
	private ArrayList<INettyHandlerListener> nettyHandlerListeners = new ArrayList<INettyHandlerListener>();

	public void registNettyHandlerListener(INettyHandlerListener nettyHandlerListener) {
		if (nettyHandlerListener != null && !nettyHandlerListeners.contains(nettyHandlerListener)) {
			nettyHandlerListeners.add(nettyHandlerListener);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void callback(Object t) {
		// 服务器连接成功后通知
		if (nettyHandlerListeners != null && !nettyHandlerListeners.isEmpty()) {
			for (INettyHandlerListener nettyHandlerListener : nettyHandlerListeners) {
				nettyHandlerListener.callback(t);
			}
		}
	}
	
	public void destory(){
		if(nettyHandlerListeners!=null){
			nettyHandlerListeners.clear() ;
			nettyHandlerListeners = null ;
		}
	}
}
