package cn.ac.ict.sensormng;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.ac.ict.sensormng.service.AccelerometerService;
import cn.ac.ict.sensormng.service.AmbientTemperatureService;
import cn.ac.ict.sensormng.service.BluetoothService;
import cn.ac.ict.sensormng.service.GPSService;
import cn.ac.ict.sensormng.service.GSMService;
import cn.ac.ict.sensormng.service.GameRotationVectorService;
import cn.ac.ict.sensormng.service.GeoMagneticRotationVectorService;
import cn.ac.ict.sensormng.service.GravityService;
import cn.ac.ict.sensormng.service.GyroscopeService;
import cn.ac.ict.sensormng.service.GyroscopeUncalibratedService;
import cn.ac.ict.sensormng.service.LightService;
import cn.ac.ict.sensormng.service.LinearAccelerationService;
import cn.ac.ict.sensormng.service.MagneticFieldService;
import cn.ac.ict.sensormng.service.MagneticFieldUncalibratedService;
import cn.ac.ict.sensormng.service.OrientationService;
import cn.ac.ict.sensormng.service.PressureService;
import cn.ac.ict.sensormng.service.ProximityService;
import cn.ac.ict.sensormng.service.RelativeHumidityService;
import cn.ac.ict.sensormng.service.RotationVectorService;
import cn.ac.ict.sensormng.service.SignificantMotionService;
import cn.ac.ict.sensormng.service.StepCounterService;
import cn.ac.ict.sensormng.service.StepDetectorService;
import cn.ac.ict.sensormng.service.TemperatureService;
import cn.ac.ict.sensormng.service.WifiService;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();
    private String appPackageName = null;
    private ListView listView = null;
    private MyAdapter myAdapter;
    private final int TYPE_GPS = 100;
    private final int TYPE_GSM = 200;
    private final int TYPE_BLUETOOTH = 300;
    private final int TYPE_WIFI = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        appPackageName = getPackageName();
        Log.i(TAG, appPackageName);
        listView = (ListView) findViewById(R.id.list_view);
        this.setTitle(getString(R.string.app_name_title));
    }

    @Override
    protected void onResume() {
        super.onResume();
        FillListView();
        setServiceRunningStatus();
        listView.setAdapter(myAdapter);
    }

    private void FillListView() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> fullList = sensorManager.getSensorList(Sensor.TYPE_ALL);

        ArrayList<Map<String, Object>> dataList = new ArrayList<>();
        // GPS
        Map<String, Object> map = new HashMap<>();
        map.put("SensorName", "GPS");
        map.put("SensorType", TYPE_GPS);
        map.put("IsRunning", false);
        dataList.add(map);
        // GSM
        map = new HashMap<>();
        map.put("SensorName", "GSM");
        map.put("SensorType", TYPE_GSM);
        map.put("IsRunning", false);
        dataList.add(map);
        // BLUETOOTH
        map = new HashMap<>();
        map.put("SensorName", "Bluetooth");
        map.put("SensorType", TYPE_BLUETOOTH);
        map.put("IsRunning", false);
        dataList.add(map);
        // TYPE_WIFI
        map = new HashMap<>();
        map.put("SensorName", "WIFI");
        map.put("SensorType", TYPE_WIFI);
        map.put("IsRunning", false);
        dataList.add(map);

        // Sensor
        for (Sensor sensor : fullList) {
            map = new HashMap<>();
            map.put("SensorName", sensor.getName());
            map.put("SensorType", sensor.getType());
            map.put("IsRunning", false);
            dataList.add(map);
        }
        myAdapter = new MyAdapter(this, dataList);
    }

    private void setServiceRunningStatus() {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> list = activityManager
                .getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : list) {
            ComponentName service = runningServiceInfo.service;
            String packageName = service.getPackageName();
            if (appPackageName.equals(packageName)) {
                String className = service.getShortClassName().replace(".service.", "");
                updateRunningStatus(className);
            }
        }

    }

    private void updateRunningStatus(String serviceClassName) {
        serviceClassName = serviceClassName.replace("Service", "").toLowerCase(Locale.getDefault());
        for (int i = 0; i < myAdapter.getCount(); i++) {
            Map<String, Object> map = myAdapter.getItem(i);
            String sensorName = (String) map.get("SensorName");
            sensorName = sensorName.replace(" ", "").toLowerCase(Locale.getDefault());
            if (sensorName.contains(serviceClassName)) {
                myAdapter.updateRunningStatus(i, true);
                break;
            }
        }
    }

    private boolean setItemStatus(int position, boolean isStart) {
        Map<String, Object> map = myAdapter.getItem(position);
        int sensorType = (Integer) map.get("SensorType");
        return changeService(sensorType, isStart);
    }

    @SuppressWarnings("deprecation")
    private boolean changeService(int sensorType, boolean isStart) {
        Class<?> serviceClass = null;
        switch (sensorType) {
            case TYPE_GPS:
                if (isStart) {
                    LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    if (locManager == null) {
                        Toast.makeText(this, "No GPS!", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Toast.makeText(this, "Please Open GPS！", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, TYPE_GPS);
                        return false;
                    }
                }
                serviceClass = GPSService.class;
                break;
            case TYPE_GSM:
                serviceClass = GSMService.class;
                break;
            case TYPE_BLUETOOTH:
                if (isStart) {
                    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (btAdapter == null) {
                        Toast.makeText(this, "No Bluetooth!", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (!btAdapter.isEnabled()) {
                        boolean success = btAdapter.enable();
                        if (!success) {
                            Toast.makeText(this, "enable Bluetooth fail", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }
                }
                serviceClass = BluetoothService.class;
                break;
            case TYPE_WIFI:
                if (isStart) {
                    WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                    if (wifiManager == null) {
                        Toast.makeText(this, "No Wifi!", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    boolean success = wifiManager.setWifiEnabled(true);
                    if (!success) {
                        Toast.makeText(this, "enable Wifi fail", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                serviceClass = WifiService.class;
                break;
            case Sensor.TYPE_ACCELEROMETER:
                serviceClass = AccelerometerService.class;
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                serviceClass = AmbientTemperatureService.class;
                break;
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                serviceClass = GameRotationVectorService.class;
                break;
            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                serviceClass = GeoMagneticRotationVectorService.class;
                break;
            case Sensor.TYPE_GRAVITY:
                serviceClass = GravityService.class;
                break;
            case Sensor.TYPE_GYROSCOPE:
                serviceClass = GyroscopeService.class;
                break;
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                serviceClass = GyroscopeUncalibratedService.class;
                break;
            case Sensor.TYPE_LIGHT:
                serviceClass = LightService.class;
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                serviceClass = LinearAccelerationService.class;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                serviceClass = MagneticFieldService.class;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                serviceClass = MagneticFieldUncalibratedService.class;
                break;
            case Sensor.TYPE_ORIENTATION:
                serviceClass = OrientationService.class;
                break;
            case Sensor.TYPE_PRESSURE:
                serviceClass = PressureService.class;
                break;
            case Sensor.TYPE_PROXIMITY:
                serviceClass = ProximityService.class;
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                serviceClass = RelativeHumidityService.class;
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                serviceClass = RotationVectorService.class;
                break;
            case Sensor.TYPE_SIGNIFICANT_MOTION:
                serviceClass = SignificantMotionService.class;
                break;
            case Sensor.TYPE_STEP_COUNTER:
                serviceClass = StepCounterService.class;
                break;
            case Sensor.TYPE_STEP_DETECTOR:
                serviceClass = StepDetectorService.class;
                break;
            case Sensor.TYPE_TEMPERATURE:
                serviceClass = TemperatureService.class;
                break;
            default:
                break;
        }
        if (serviceClass != null) {
            Intent intent = new Intent(this, serviceClass);
            if (isStart) {
                startService(intent);
            } else {
                stopService(intent);
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*
         * super.onActivityResult(requestCode, resultCode, data); if (resultCode
		 * != RESULT_OK) { return; } Class<?> serviceClass = null; switch
		 * (requestCode) { case TYPE_GPS: serviceClass = GPSService.class;
		 * break; case TYPE_BLUETOOTH: serviceClass = BluetoothService.class;
		 * break; case TYPE_WIFI: serviceClass = WifiService.class; break;
		 * default: break; }
		 *
		 * if (serviceClass != null) { Intent intent = new Intent(this,
		 * serviceClass); startService(intent); }
		 */
    }

    private class MyAdapter extends BaseAdapter {

        private ArrayList<Map<String, Object>> mData;
        private LayoutInflater mInflater;

        public MyAdapter(Context context, ArrayList<Map<String, Object>> data) {
            mData = data;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void updateRunningStatus(int position, boolean isRunning) {
            mData.get(position).put("IsRunning", isRunning);
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Map<String, Object> getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = mInflater.inflate(R.layout.listitem, null);
            }
            bindView(position, v);
            return v;
        }

        private void bindView(int position, View v) {
            Map<String, Object> dataSet = mData.get(position);
            if (dataSet == null) {
                return;
            }
            String sName = dataSet.get("SensorName").toString();
            TextView tvSensorName = (TextView) v
                    .findViewById(R.id.tvSensorName);
            Switch switchItem = (Switch) v.findViewById(R.id.switchItem);
            tvSensorName.setText(sName);

            boolean isRunning = (Boolean) dataSet.get("IsRunning");
            switchItem.setOnCheckedChangeListener(null);
            switchItem.setChecked(isRunning);
            switchItem.setOnCheckedChangeListener(new SwitchCheckedChangeListener(position));
        }

        private class SwitchCheckedChangeListener implements
                CompoundButton.OnCheckedChangeListener {
            private int position;

            public SwitchCheckedChangeListener(int pos) {
                position = pos;
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean res = setItemStatus(position, isChecked);
                if (res) {
                    updateRunningStatus(position, isChecked);
                }
            }
        }
    }
}

