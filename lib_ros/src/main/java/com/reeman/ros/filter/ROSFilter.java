package com.reeman.ros.filter;

import android.text.TextUtils;

public class ROSFilter {
    private static String coreData;
    private static int chargeState = -1;
    private static int scramState = -1;

    private static int antiFallState = -1;

    private static int level = -1;

    private static int wheelWorkState = -1;

    private static String lastSensorData;

    private static String lastHFLSVersionData;
    private static long lastTimestamps = -1;

    public static boolean isLastTimestampsDiff(Long data) {
        if (lastTimestamps == -1) {
            lastTimestamps = data;
            return true;
        }
        if (lastTimestamps == data) {
            return false;
        }
        lastTimestamps = data;
        return true;
    }

    public static boolean isHFLSVersionDataDiff(String data) {
        if (TextUtils.isEmpty(lastHFLSVersionData)) {
            lastHFLSVersionData = data;
            return true;
        }
        if (lastHFLSVersionData.equals(data)) {
            return false;
        }
        lastHFLSVersionData = data;
        return true;
    }

    public static boolean isSensorDataDiff(String data) {
        if (TextUtils.isEmpty(lastSensorData)) {
            lastSensorData = data;
            return true;
        }
        if (lastSensorData.equals(data)) {
            return false;
        }
        lastSensorData = data;
        return true;
    }

    public static boolean isWheelWorkStateDiff(int code) {
        if (wheelWorkState == -1) {
            wheelWorkState = code;
            return true;
        }
        if (wheelWorkState == code) {
            return false;
        }
        wheelWorkState = code;
        return true;
    }

    public static boolean isAntiFallStateDiff(int data) {
        if (antiFallState == -1) {
            antiFallState = data;
            return true;
        }
        if (antiFallState == data) {
            return false;
        }
        antiFallState = data;
        return true;
    }

    public static boolean isChargeStateDiff(int data) {
        if (chargeState == -1) {
            chargeState = data;
            return true;
        }
        if (chargeState == data) {
            return false;
        }
        chargeState = data;
        return true;
    }

    public static boolean isScramStateDiff(int data) {
        if (scramState == -1) {
            scramState = data;
            return true;
        }
        if (scramState == data) {
            return false;
        }
        scramState = data;
        return true;
    }

    public static boolean isLevelDiff(int data) {
        if (level == -1) {
            level = data;
            return true;
        }
        if (level == data) {
            return false;
        }
        level = data;
        return true;
    }

    public static boolean isCoreDataDiff(String data) {
        if (TextUtils.isEmpty(coreData)) {
            coreData = data;
            return true;
        }
        if (coreData.equals(data)) {
            return false;
        }
        coreData = data;
        return true;
    }
}
