package cn.ac.ict.sensormng.service;

import android.hardware.Sensor;

/**
 * Gravity
 */
public class GravityService extends AbstractSensorService {

	@Override
	int getSensorType() {
		return Sensor.TYPE_GRAVITY;
	}

}
