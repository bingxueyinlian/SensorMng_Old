package cn.ac.ict.sensormng.service;

import android.hardware.Sensor;

/**
 * Pressure
 */
public class PressureService extends AbstractSensorService {

	@Override
	int getSensorType() {
		return Sensor.TYPE_PRESSURE;
	}
}
