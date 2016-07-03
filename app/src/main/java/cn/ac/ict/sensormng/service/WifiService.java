package cn.ac.ict.sensormng.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;

import cn.ac.ict.sensormng.FileUtils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public class WifiService extends Service {
    private String TAG = "WIFIService";
    private final String dirName = "Wifi";
    private String fileName = "wifi";
    private FileUtils fileUtils = null;
    private WifiManager wifiManager = null;
    private Timer timer = null;
    private int cnt = 0;
    private String scanID = "";
    List<String> scanedList = null;

    @Override
    public void onCreate() {
        super.onCreate();

        fileUtils = new FileUtils(dirName, fileName);
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

        IntentFilter filter = new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mReceiver, filter);
        // Don't forget to unregister during onDestroy

        // delay:1,period:30
        String config = fileUtils.getConfigInfo("wifi", "delay,period");
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
                cnt++;
                String cntStr = String.format(Locale.getDefault(), "%04d", cnt);
                String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale
                        .getDefault()).format(new Date());
                scanID = time + cntStr;
                Log.i(TAG, scanID);
                if (!wifiManager.isWifiEnabled())
                    wifiManager.setWifiEnabled(true);
                scanedList = new ArrayList<String>();
                wifiManager.startScan();
            }
        }, Integer.parseInt(delay) * 1000, Integer.parseInt(period) * 1000);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent
                    .getAction())) {
                List<ScanResult> resultList = wifiManager.getScanResults();

                if (resultList != null && resultList.size() > 0) {
                    String time = new SimpleDateFormat("yyyyMMddHHmmss,SSS",
                            Locale.getDefault()).format(new Date());
                    StringBuffer sbResult = new StringBuffer();
                    for (ScanResult sr : resultList) {

                        boolean isExist = checkExist(sr.BSSID);
                        if (isExist) {
                            continue;
                        }
                        String msg = convertToString(sr);
                        msg = time + "," + msg + "," + scanID + "\r\n";
                        sbResult.append(msg);
                    }
                    //
                    fileUtils.append(sbResult.toString());
                    Log.i(TAG, sbResult.toString());
                }
            }
        }

        private boolean checkExist(String bSSID) {
            if (scanedList == null) {
                scanedList = new ArrayList<String>();
            }
            if (scanedList.contains(bSSID)) {
                return true;
            } else {
                scanedList.add(bSSID);
                return false;
            }
        }

        private String convertToString(ScanResult scanResult) {

            String ssid = scanResult.SSID;
            if (ssid == null || ssid.trim().length() == 0) {
                ssid = "null";
            } else {
                ssid = ssid.trim().replace(" ", "").replace(",", "")
                        .replace("\"", "");
            }
            String bssid = scanResult.BSSID;
            if (bssid == null || bssid.trim().length() == 0) {
                bssid = "null";
            } else {
                bssid = bssid.trim().replace(" ", "").replace(",", "")
                        .replace("\"", "");
            }
            String capabilities = scanResult.capabilities;
            if (capabilities == null || capabilities.trim().length() == 0) {
                capabilities = "null";
            } else {
                capabilities = capabilities.trim().replace(" ", "")
                        .replace(",", "").replace("\"", "");
            }
            String level = scanResult.level + "";
            String frequency = scanResult.frequency + "";
            String timestamp = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {//17
                timestamp = scanResult.timestamp + "";
            }
            ArrayList<String> dataList = new ArrayList<String>();
            dataList.add(ssid);
            dataList.add(bssid);
            dataList.add(capabilities);
            dataList.add(level);
            dataList.add(frequency);
            dataList.add(timestamp);

            return StringUtils.join(dataList, ",");
        }
    };
}
