package cn.ac.ict.sensormng.service;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.os.Build;

/**
 * GeoMagneticRotationVector
 */
public class GeoMagneticRotationVectorService extends AbstractSensorService {

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	int getSensorType() {
		return Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR;
	}
}
