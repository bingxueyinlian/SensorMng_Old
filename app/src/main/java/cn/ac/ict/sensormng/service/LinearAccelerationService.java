package cn.ac.ict.sensormng.service;

import android.hardware.Sensor;

/**
 * LinearAcceleration
 */
public class LinearAccelerationService extends AbstractSensorService {

	@Override
	int getSensorType() {
		return Sensor.TYPE_LINEAR_ACCELERATION;
	}
}
