package cn.ac.ict.sensormng.service;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.os.Build;

/**
 * MagneticFieldUncalibrated
 */
public class MagneticFieldUncalibratedService extends AbstractSensorService {

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	@Override
	int getSensorType() {
		return Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED;
	}
}
