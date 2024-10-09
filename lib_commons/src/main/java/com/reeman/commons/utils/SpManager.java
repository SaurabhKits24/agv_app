package com.reeman.commons.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SpManager {
    private static SharedPreferences sSharedPreferences;

    public static void init(Context context, String spName) {
        sSharedPreferences = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
    }

    public static SharedPreferences getInstance() {
        return sSharedPreferences;
    }

}
