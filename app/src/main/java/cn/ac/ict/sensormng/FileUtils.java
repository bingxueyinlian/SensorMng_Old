package cn.ac.ict.sensormng;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtils {
    private final String TAG = "FileUtils";
    private final String Encoding = "UTF-8";
    private String rootDirName = "SensorMng";
    private String configFileName = "config.txt";
    private String dirName = null;
    private String fileName = null;
    // 默认采样频率,时间单位秒，距离单位米
    // delay:延时执行时间，period：两次执行时间间隔，minTime：GPS最小更新时间，minDistance:GPS最小更新距离
    private final String defaultConfig = "gps:{minTime:60,minDistance:0}\r\n"
            + "gsm:{delay:1,period:5}\r\n"
            + "bluetooth:{delay:1,period:30}\r\n"
            + "wifi:{delay:1,period:30}\r\n";

    public FileUtils(String folderName, String fileName) {
        // 获取SD卡目录
        String sdPath = Environment.getExternalStorageDirectory() + File.separator;
        this.rootDirName = sdPath + this.rootDirName + File.separator;
        this.configFileName = this.rootDirName + this.configFileName;
        this.dirName = this.rootDirName + folderName + File.separator;
        String date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                .format(new Date());
        this.fileName = this.dirName + fileName + "_" + date + ".txt";// 以天为单位保存文件
        try {
            createSDDir(this.dirName);
            createSDFile(this.fileName);
            SaveDefaultConfig();// 保存默认配置，必须放在createSDDir后，否则没有根目录会出错
        } catch (Exception e) {
            Log.i(TAG, "FileUtils==>" + e);
        }
    }

    /**
     * 创建文件
     */
    private File createSDFile(String fName) throws Exception {
        File file = new File(fName);
        if (!file.exists()) {
            boolean success = file.createNewFile();
            if (!success) {
                Log.e(TAG, fName + ": create fail");
            }
        }
        return file;
    }

    /**
     * 创建目录
     *
     * @throws Exception
     */
    private File createSDDir(String dir) throws Exception {
        File file = new File(dir);
        if (!file.exists()) {
            boolean success = file.mkdirs();
            if (!success) {
                Log.e(TAG, dir + ": create fail");
            }
        }
        return file;
    }

    /**
     * 将msg追加到写入SD文件中,默认当前时间
     */
    public void appendLine(String msg) {
        String time = new SimpleDateFormat("yyyyMMddHHmmss,SSS",
                Locale.getDefault()).format(new Date());
        appendLine(time, msg);
    }

    /**
     * 将msg追加到写入SD文件中,指定时间
     */
    public void appendLine(String time, String msg) {
        msg = time + "," + msg + "\r\n";// 前置时间
        append(msg);
    }

    /**
     * 将msg追加到写入SD文件中
     */
    public void append(String msg) {
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(this.fileName,
                    true), Encoding);
            writer.write(msg);
        } catch (Exception e) {
            Log.i(TAG, "appendLine write==>" + e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e2) {
                Log.i(TAG, "appendLine close==>" + e2);
            }
        }
    }

    /**
     * 保存默认配置信息
     */
    private void SaveDefaultConfig() {
        File file = new File(this.configFileName);
        if (!file.exists()) {
            OutputStreamWriter writer = null;
            try {
                writer = new OutputStreamWriter(new FileOutputStream(file),
                        Encoding);
                writer.write(defaultConfig);
            } catch (Exception e) {
                Log.i(TAG, "SaveDefaultConfig write==>" + e);
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e) {
                    Log.i(TAG, "SaveDefaultConfig close==>" + e);
                }
            }
        }
    }

    /**
     * 获取指定标记的配置值
     *
     * @param tagName    标记名    ,如gps
     * @param configName 配置名 ，如period,同时获取多个配置时用英文逗号分隔开，如delay,period
     * @return 返回配置对应的值，configName有多个值时，返回值也以逗号分隔，结果与configName顺序对应
     */
    public String getConfigInfo(String tagName, String configName) {
        File file = new File(this.configFileName);
        if (!file.exists()) {
            return null;
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    file), Encoding));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(tagName)) {
                    line = line.substring(line.indexOf("{") + 1,
                            line.lastIndexOf("}"));
                    String[] tempArr = configName.split(",");
                    if (tempArr.length == 0) {
                        br.close();
                        return null;
                    }
                    String result = "";
                    for (int i = 0; i < tempArr.length; i++) {
                        if (i > 0) {
                            result += ",";
                        }
                        String item = tempArr[i];
                        if (item == null || item.length() == 0) {
                            continue;
                        }
                        int start = line.indexOf(item) + item.length() + 1;
                        int end = line.indexOf(",", start);
                        if (end == -1) {
                            result += line.substring(start);// 最后一配置信息
                        } else {
                            result += line.substring(start, end);
                        }

                    }

                    Log.i(TAG, "Find " + tagName + " :" + configName + "="
                            + result);
                    br.close();
                    return result;
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "getConfigInfo read==>" + e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {
                Log.i(TAG, "getConfigInfo close==>" + e);
            }

        }
        return null;
    }
}