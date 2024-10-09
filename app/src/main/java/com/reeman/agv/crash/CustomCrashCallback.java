package com.reeman.agv.crash;

import static com.reeman.agv.base.BaseApplication.isFirstEnter;
import static com.reeman.agv.base.BaseApplication.mApp;
import static com.reeman.agv.base.BaseApplication.ros;

import android.content.Intent;
import android.util.Log;

import com.reeman.agv.BuildConfig;
import com.reeman.agv.activities.CrashActivity;
import com.reeman.agv.calling.mqtt.MqttClient;
import com.reeman.commons.state.RobotInfo;
import com.reeman.agv.widgets.FloatingCallingListView;
import com.reeman.agv.widgets.FloatingCountdown;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

import timber.log.Timber;
import xcrash.ICrashCallback;

public class CustomCrashCallback implements ICrashCallback {
    @Override
    public void onCrash(String logPath, String emergency) {
        String content = null;
        try {
            File file = new File(logPath);
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            content = new String(data, StandardCharsets.UTF_8);
            Timber.tag(BuildConfig.CRASH_LOG_DIR).e("Uncaught exception:\n %s", content);
            if (ros != null) {
                isFirstEnter = true;
                ros.stopListen();
            }
            file.delete();
            MqttClient mqttClient = MqttClient.getInstance();
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
            if (FloatingCountdown.isShow()) {
                FloatingCountdown.getInstance().close();
            }
            if (FloatingCallingListView.isShow()) {
                FloatingCallingListView.getInstance().close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Intent intent = new Intent(mApp, CrashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            if (content != null && content.length() > 0 && content.contains("logcat:")) {
                intent.putExtra("hostname", RobotInfo.INSTANCE.getROSHostname());
                intent.putExtra("stackTrace", content.split("logcat:")[0]);
            }
            mApp.startActivity(intent);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }
}
