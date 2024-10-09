package com.reeman.agv.utils

import android.content.ContentResolver
import android.os.SystemClock
import android.provider.Settings

object TimeSettingUtils {
    fun disableAutoTime(contentResolver :ContentResolver){
        Settings.Global.putInt(contentResolver,Settings.Global.AUTO_TIME,0)
    }

    fun setCurrentTimeMillis(timestamp:Long){
        SystemClock.setCurrentTimeMillis(timestamp)
    }
}