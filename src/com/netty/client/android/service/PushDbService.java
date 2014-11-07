package com.netty.client.android.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.netty.client.android.dao.Device;
import com.netty.client.android.dao.DeviceDao;
import com.netty.client.context.ApplicationContextClient;

import de.greenrobot.dao.query.QueryBuilder;

public class PushDbService {

	private DeviceDao deviceDao;

	private static PushDbService instance;

	public static PushDbService getInstance(Context context) {
		if (instance == null) {
			instance = new PushDbService();
			instance.deviceDao = RemoteService.getDaoSession(context).getDeviceDao();
		}
		return instance;
	}

	/**
	 * 从数据库中获取设备信息
	 * 
	 * @return
	 */
	public Map<String, Device> queryDevicesForMap() {
		QueryBuilder<Device> qb = this.deviceDao.queryBuilder();
		List<Device> list = qb.list();
		if (list != null && list.size() > 0) {
			Map<String, Device> map = new HashMap<String, Device>();
			for (Device device : list) {
				if (device != null) {
					// 设备从数据库中取出来默认为已经离线
					device.setIsOnline(ApplicationContextClient.DEVICE_OFFLINE);
					map.put(device.getAppPackage(), device);
				}
			}
			return map;
		}
		return null;
	}

	/**
	 * 保存设备信息到数据库中
	 * 
	 * @param device
	 * @return
	 */
	public int saveOrUpdateDevice(Device device) {
		if (device == null) {
			return -1;
		}
		long id = this.deviceDao.insertOrReplace(device);
		if (id > 0) {
			device.setId(id);
			return 1;
		}
		return 0;
	}

	public void deleteDevice(Device device) {
		if (device != null) {
			this.deviceDao.delete(device);
		}
	}

	public void saveOrUpdateDevices(List<Device> devices) {
		if (devices != null && devices.size() > 0) {
			this.deviceDao.insertInTx(devices);
		}

	}
}
