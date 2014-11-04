package com.netty.client.android.network;

public interface INetworkCallback {
	public void connectSuccess();

	public void connectFailed();

	public void setConnected(boolean isConnected);
}
