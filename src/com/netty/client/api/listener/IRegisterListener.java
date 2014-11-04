package com.netty.client.api.listener;

/**
 * 注册消息客户端监听
 * 
 * @类名称：IRegisterListener
 * @类描述：
 * @创建人：maofw
 * @创建时间：2014-10-30 上午11:21:05
 * 
 */
public interface IRegisterListener {
	public void receive(String regId);
}
