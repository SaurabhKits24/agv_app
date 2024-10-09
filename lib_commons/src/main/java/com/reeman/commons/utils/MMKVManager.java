package com.reeman.commons.utils;


import android.content.Context;

import com.tencent.mmkv.MMKV;

import java.util.HashSet;
import java.util.Set;


public class MMKVManager {
    private static MMKV mmkv;

    public static void init(Context context) {
        MMKV.initialize(context);
        mmkv = MMKV.defaultMMKV();
    }

    public static void sync(){
        if (mmkv != null){
            mmkv.sync();
        }
    }
    public static MMKV getInstance() {
        return mmkv;
    }

    public static String decodeString(String key) {
        return mmkv.getString(key, "");
    }

    public static String decodeString(String key, String defaultValue) {
        return mmkv.getString(key, defaultValue);
    }

    public static void encode(String key, String value) {
        mmkv.edit().putString(key, value).apply();
    }

    public static Boolean decodeBool(String key) {
        return mmkv.getBoolean(key, false);
    }

    public static Boolean decodeBool(String key, boolean defaultValue) {
        return mmkv.getBoolean(key, defaultValue);
    }

    public static void encode(String key, boolean value) {
        mmkv.edit().putBoolean(key, value).apply();
    }

    public static Integer decodeInt(String key) {
        return mmkv.getInt(key, 0);
    }

    public static Integer decodeInt(String key, int defaultValue) {
        return mmkv.getInt(key, defaultValue);
    }

    public static void encode(String key, int value) {
        mmkv.edit().putInt(key, value).apply();
    }

    public static long decodeLong(String key) {
        return mmkv.getLong(key, 0L);
    }

    public static Long decodeLong(String key, long defaultValue) {
        return mmkv.getLong(key, defaultValue);
    }

    public static void encode(String key, long value) {
        mmkv.edit().putLong(key, value).apply();
    }

    public static Float decodeFloat(String key) {
        return mmkv.getFloat(key, 0.0F);
    }

    public static Float decodeFloat(String key, float defaultValue) {
        return mmkv.getFloat(key, defaultValue);
    }

    public static void encode(String key, float value) {
        mmkv.edit().putFloat(key, value).apply();
    }

    public static Set<String> decodeStringSet(String key) {
        return mmkv.getStringSet(key, null);
    }

    public static Set<String> decodeStringSet(String key, Set<String> defaultValue) {
        return mmkv.getStringSet(key, defaultValue);
    }

    public static void encode(String key, HashSet<String> value) {
        mmkv.edit().putStringSet(key, value).apply();
    }
}
