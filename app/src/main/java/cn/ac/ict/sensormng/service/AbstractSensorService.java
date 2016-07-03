package cn.ac.ict.sensormng.service;

import java.util.Locale;

import cn.ac.ict.sensormng.FileUtils;
import cn.ac.ict.sensormng.MyConstants;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

/**
 * AbstractSensorService
 */
public abstract class AbstractSensorService extends Service implements
		SensorEventListener {

	private String TAG = "";
	private int m_delay = MyConstants.SENSOR_DELAY;
	private String dirName = "";
	private String fileName = "";
	private FileUtils fileUtils = null;
	private SensorManager mSensorManager = null;
	private Sensor mSensor = null;

	abstract int getSensorType();

	@Override
	public void onCreate() {
		super.onCreate();
		TAG = this.getClass().getSimpleName();
		dirName = TAG.replace("Service", "");
		fileName = dirName.toLowerCase(Locale.getDefault());
		fileUtils = new FileUtils(dirName, fileName);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(getSensorType());
		mSensorManager.registerListener(this, mSensor, m_delay);

	}

	@Override
	public void onDestroy() {
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(this, mSensor);
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		StringBuffer sb = new StringBuffer();
		float[] values = event.values;
		for (int i = 0; i < values.length; i++) {
			float v = values[i];
			if (i > 0) {
				sb.append(",");
			}
			sb.append(v);
		}
		String msg = sb.toString().trim();
		fileUtils.appendLine(msg);

		Log.i(TAG, msg);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
