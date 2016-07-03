package cn.ac.ict.sensormng.service;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.os.Build;

/**
 * GyroscopeUncalibrated
 */
public class GyroscopeUncalibratedService extends AbstractSensorService {

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	@Override
	int getSensorType() {
		return Sensor.TYPE_GYROSCOPE_UNCALIBRATED;
	}
}
