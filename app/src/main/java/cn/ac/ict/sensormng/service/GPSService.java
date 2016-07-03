package cn.ac.ict.sensormng.service;

import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import cn.ac.ict.sensormng.FileUtils;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * GPS
 */
public class GPSService extends Service implements LocationListener {
	private String TAG = "GPSService";
	private final String dirName = "GPS";
	private String fileName = "gps";
	private FileUtils fileUtils = null;
	private LocationManager mLocationManager = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		fileUtils = new FileUtils(dirName, fileName);

		// minTime:30,minDistance:0
		String config = fileUtils.getConfigInfo("gps", "minTime,minDistance");
		String[] tempArr = config.split(",");
		String minTime = tempArr[0];
		String minDistance = tempArr[1];
		if (minTime == null || minTime.length() == 0) {
			minTime = "30";
		}
		if (minDistance == null || minDistance.length() == 0) {
			minDistance = "0";
		}

		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				Integer.parseInt(minTime) * 1000,
				Integer.parseInt(minDistance), this);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mLocationManager != null) {
			mLocationManager.removeUpdates(this);
		}
	}

	private void UpdateLocation(Location location) {
		if (location != null) {
			ArrayList<String> dataList = new ArrayList<String>();
			dataList.add(location.getLongitude() + "");
			dataList.add(location.getLatitude() + "");
			dataList.add(location.getAltitude() + "");
			dataList.add(location.getBearing() + "");
			dataList.add(location.getSpeed() + "");
			dataList.add(location.getAccuracy() + "");

			String msg = StringUtils.join(dataList, ",");
			fileUtils.appendLine(msg);

			Log.i(TAG, msg);
		}

	}

	@Override
	public void onLocationChanged(Location location) {
		UpdateLocation(location);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onProviderDisabled(String provider) {

	}
}
