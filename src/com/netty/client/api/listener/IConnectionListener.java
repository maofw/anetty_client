package com.netty.client.api.listener;

import com.xwtec.protoc.CommandProtoc;

/**
 * 客户端连接监听器
 * 
 * @类名称：IConnectionListener
 * @类描述：
 * @创建人：maofw
 * @创建时间：2014-10-28 下午12:41:04
 * 
 */
public interface IConnectionListener {
	public void receive(CommandProtoc.Message message);
}
