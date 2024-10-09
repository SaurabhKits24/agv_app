package com.reeman.commons.utils;

public class ClickRestrict {
    private static long lastClickTimeMills;

    public static boolean restrictFrequency(long interval) {
        if (System.currentTimeMillis() - lastClickTimeMills < interval) {
            return true;
        }
        lastClickTimeMills = System.currentTimeMillis();
        return false;
    }


}
