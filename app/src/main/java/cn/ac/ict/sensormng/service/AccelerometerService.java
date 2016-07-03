package cn.ac.ict.sensormng.service;

import android.hardware.Sensor;

/**
 * Accelerometer
 */
public class AccelerometerService extends AbstractSensorService {

	@Override
	int getSensorType() {
		return Sensor.TYPE_ACCELEROMETER;
	}

}
