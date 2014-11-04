/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\android_workspace\\anetty_client\\src\\com\\netty\\client\\android\\aidl\\NettyServiceClient.aidl
 */
package com.netty.client.android.aidl;
public interface NettyServiceClient extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.netty.client.android.aidl.NettyServiceClient
{
private static final java.lang.String DESCRIPTOR = "com.netty.client.android.aidl.NettyServiceClient";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.netty.client.android.aidl.NettyServiceClient interface,
 * generating a proxy if needed.
 */
public static com.netty.client.android.aidl.NettyServiceClient asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.netty.client.android.aidl.NettyServiceClient))) {
return ((com.netty.client.android.aidl.NettyServiceClient)iin);
}
return new com.netty.client.android.aidl.NettyServiceClient.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_regist:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
this.regist(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_deviceOffline:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.deviceOffline(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.netty.client.android.aidl.NettyServiceClient
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void regist(java.lang.String appKey, java.lang.String appPackage) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(appKey);
_data.writeString(appPackage);
mRemote.transact(Stub.TRANSACTION_regist, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void deviceOffline(java.lang.String appPackage) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(appPackage);
mRemote.transact(Stub.TRANSACTION_deviceOffline, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_regist = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_deviceOffline = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public void regist(java.lang.String appKey, java.lang.String appPackage) throws android.os.RemoteException;
public void deviceOffline(java.lang.String appPackage) throws android.os.RemoteException;
}
