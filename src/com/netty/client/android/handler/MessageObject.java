package com.netty.client.android.handler;

public class MessageObject {
	private String appPackage;
	private Object obj;

	public MessageObject() {
	}

	public String getAppPackage() {
		return appPackage;
	}

	public void setAppPackage(String appPackage) {
		this.appPackage = appPackage;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}
}
