package com.netty.client.api.listener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.xwtec.protoc.CommandProtoc;
import com.xwtec.protoc.CommandProtoc.Message;

/**
 * 默认消息通知监听实现
 * 
 * @类名称：DefaultNotificationListener
 * @类描述：
 * @创建人：maofw
 * @创建时间：2014-10-28 下午12:41:54
 * 
 */
public class DefaultNotificationListener implements IConnectionListener {
	private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);

	private NotificationManager notificationManager;

	private Context mContext;
	private int mIconId;
	private boolean mIsShowSysMsg = false ;

	public DefaultNotificationListener(Context context,boolean isShowSysMsg) {
		mContext = context;
		mIconId = context.getApplicationInfo().icon ;
		mIsShowSysMsg = isShowSysMsg ;
	}

	@Override
	public void receive(Message message) {
		Log.i(getClass().getName(), "receive:mContext:"+mContext.getClass().getName()+"-mIconId:"+mIconId);
		if(message.getType()==1|| (message.getType() == 0&&mIsShowSysMsg)){
			//系统消息并且需要展示的进行消息提醒 |用户消息必须提醒
			notification(mContext, mIconId, message);
		}
		

	}

	/**
	 * 消息通知
	 * 
	 * @param icon
	 * @param title
	 * @param content
	 */
	@SuppressWarnings("deprecation")
	private void notification(Context context, int icon, CommandProtoc.Message message) {
		if (notificationManager == null) {
			notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		int idx = message.getMsgId() == null ? 1 : Integer.parseInt(message.getMsgId());
		String title = message.getTitle();
		String content = message.getContent().toStringUtf8();
		PackageManager packageManager = context.getPackageManager();
		Intent appIntent = new Intent();
		appIntent = packageManager.getLaunchIntentForPackage(message.getAppPackage());
		appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		// appIntent.setPackage(message.getAppPackage());
		// appIntent.set
		appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, idx, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification notification = new Notification();
		try {
			notification.when = sdf.parse(message.getPushTime()).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
			notification.when = System.currentTimeMillis();
		}
		notification.defaults = Notification.DEFAULT_SOUND;
		notification.flags = Notification.FLAG_AUTO_CANCEL;

		// 使用系统默认样式
		notification.icon = icon;
		notification.tickerText = title;
		notification.setLatestEventInfo(context, title, content, pendingIntent);

		notificationManager.notify(idx, notification);
	}
}
