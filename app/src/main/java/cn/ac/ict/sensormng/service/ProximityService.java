package cn.ac.ict.sensormng.service;

import android.hardware.Sensor;

/**
 * Proximity
 */
public class ProximityService extends AbstractSensorService {

	@Override
	int getSensorType() {
		return Sensor.TYPE_PROXIMITY;
	}
}
