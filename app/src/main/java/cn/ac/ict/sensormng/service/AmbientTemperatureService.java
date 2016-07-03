package cn.ac.ict.sensormng.service;

import android.hardware.Sensor;

/**
 * AmbientTemperature
 */
public class AmbientTemperatureService extends AbstractSensorService {

	@Override
	int getSensorType() {
		return Sensor.TYPE_AMBIENT_TEMPERATURE;
	}
}
