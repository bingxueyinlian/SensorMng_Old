package cn.ac.ict.sensormng.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;

import cn.ac.ict.sensormng.FileUtils;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

/**
 * Bluetooth
 */
public class BluetoothService extends Service {
    private String TAG = "BluetoothService";
    private final String dirName = "Bluetooth";
    private String fileName = "bluetooth";
    private FileUtils fileUtils = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private Timer timer = null;
    private int cnt = 0;
    private String scanID = "";
    private ArrayList<String> arrDevices = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        fileUtils = new FileUtils(dirName, fileName);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
        // Don't forget to unregister during onDestroy

        // delay:1,period:30
        String config = fileUtils.getConfigInfo("bluetooth", "delay,period");
        String[] tempArr = config.split(",");
        String delay = tempArr[0];
        String period = tempArr[1];
        if (delay == null || delay.length() == 0) {
            delay = "1";
        }
        if (period == null || period.length() == 0) {
            period = "30";
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!mBluetoothAdapter.isDiscovering()) {
                    cnt++;
                    String cntStr = String.format(Locale.getDefault(), "%04d",
                            cnt);
                    String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale
                            .getDefault()).format(new Date());
                    scanID = time + cntStr;
                    Log.i(TAG, scanID);
                    mBluetoothAdapter.startDiscovery();
                } else {
                    Log.i(TAG, "before scan is running");
                }
            }
        }, Integer.parseInt(delay) * 1000, Integer.parseInt(period) * 1000);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothAdapter = null;
        }
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                arrDevices = new ArrayList<String>();
                Log.i(TAG, "ACTION_DISCOVERY_STARTED");
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.i(TAG, "ACTION_FOUND");
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    ArrayList<String> dataList = new ArrayList<String>();

                    String time = new SimpleDateFormat("yyyyMMddHHmmss,SSS",
                            Locale.getDefault()).format(new Date());
                    String deviceName = device.getName();
                    if (deviceName == null || deviceName.trim().length() == 0) {
                        deviceName = "null";
                    } else {
                        deviceName = deviceName.trim().replace(" ", "")
                                .replace(",", "").replace("\"", "");
                    }
                    dataList.add(time);
                    dataList.add(deviceName);
                    dataList.add(device.getAddress());
                    dataList.add(device.getBondState() + "");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {//18
                        dataList.add(device.getType() + "");
                    } else {
                        dataList.add("");
                    }
                    BluetoothClass bc = device.getBluetoothClass();
                    if (bc != null) {
                        dataList.add(bc.toString());
                        dataList.add(bc.getDeviceClass() + "");
                        dataList.add(bc.getMajorDeviceClass() + "");
                    } else {
                        dataList.add("");
                        dataList.add("");
                        dataList.add("");
                    }
                    String msg = StringUtils.join(dataList, ",");
                    boolean isExists = false;
                    for (String item : arrDevices) {
                        String temp = msg.replace(time, "");// ignore time
                        if (item.endsWith(temp)) {
                            isExists = true;
                            break;
                        }
                    }
                    if (!isExists) {
                        arrDevices.add(msg);
                    }
                    Log.i(TAG, "DATA:" + msg);

                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                Log.i(TAG, "ACTION_DISCOVERY_FINISHED");
                if (arrDevices != null && arrDevices.size() > 0) {
                    StringBuffer sbDevices = new StringBuffer();
                    for (String item : arrDevices) {
                        sbDevices.append(item + "," + scanID + "\r\n");
                    }
                    String data = sbDevices.toString();
                    fileUtils.append(data);
                    Log.i(TAG, "DATA:" + data);
                }
            }
        }
    };
}
