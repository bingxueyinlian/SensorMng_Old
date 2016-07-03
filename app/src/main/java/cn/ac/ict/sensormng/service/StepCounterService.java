package cn.ac.ict.sensormng.service;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.os.Build;

/**
 * StepCounter
 */
public class StepCounterService extends AbstractSensorService {

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	int getSensorType() {
		return Sensor.TYPE_STEP_COUNTER;
	}
}
