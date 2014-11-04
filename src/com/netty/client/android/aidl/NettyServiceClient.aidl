package com.netty.client.android.aidl;
interface NettyServiceClient {
    void regist(String appKey,String appPackage);
    
    void deviceOffline(String appPackage);
}