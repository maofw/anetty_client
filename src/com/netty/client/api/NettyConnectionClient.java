package com.netty.client.api;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.netty.client.android.aidl.NettyServiceClient;
import com.netty.client.android.aidl.NettyServiceClient.Stub;

/**
 * 连接客户端jar
 * 
 * @类名称：NettyConnectionClient
 * @类描述：
 * @创建人：maofw
 * @创建时间：2014-10-20 上午11:08:32
 * 
 */
public class NettyConnectionClient {

	private Context mContext;
	private String mAppKey;

	private Intent aidlIntent = new Intent(NettyServiceClient.class.getName());

	public NettyConnectionClient(Context context, String appKey) {
		this.mContext = context;
		this.mAppKey = appKey;
		// 启动service
		// if(!isMyServiceExisted()){
		// 如果service不存在 则启动
		mContext.startService(aidlIntent);
		// }
	}

	ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i("NettyConnectionClient", "### aidl disconnected.");
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i("NettyConnectionClient", "### aidl onServiceConnected. service : " + service.getClass().getName());

			NettyServiceClient nettyServiceClient = Stub.asInterface(service);
			Log.i("NettyConnectionClient", "### after asInterface : " + nettyServiceClient.getClass().getName());
			try {
				nettyServiceClient.regist(mAppKey, mContext.getPackageName());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * 綁定service服务
	 * 
	 * @param host
	 * @param port
	 */
	public void bindNettyService() {
		// 服务端的action
		mContext.bindService(aidlIntent, mConnection, Context.BIND_AUTO_CREATE);
	}

	public void unbindNettyService() {
		mContext.unbindService(mConnection);
	}
}
