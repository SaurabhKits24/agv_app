package com.reeman.commons.utils;

import android.text.format.Time;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeUtil {
    public static String formatDay(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return formatter.format(date);
    }

    public static String formatHour(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH", Locale.getDefault());
        return formatter.format(date);
    }
    public static String formatMilliseconds(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    public static String formatMills(long timestamp){
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        return dateFormat.format(new Date(timestamp));
    }


    public static String formatHourAndMinute(Date startTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return formatter.format(startTime);
    }

    public static String formatHourAndMinuteAndSecond(Date startTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return formatter.format(startTime);
    }

    public static String formatHourAndMinute(long startTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return formatter.format(startTime);
    }

    public static String formatTimeHourMinSec(long timestamp) {
        long hours = TimeUnit.MILLISECONDS.toHours(timestamp);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timestamp) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timestamp) % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static long convertToTimestamp(String timeString) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = dateFormat.parse(timeString);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean isCurrentInTimeScope(String beginTimeStr,String endTimeStr) {
        String[] splitBeginTime = beginTimeStr.split(":");
        String[] splitEndTime = endTimeStr.split(":");
        boolean result = false;
        final long aDayInMillis = 1000 * 60 * 60 * 24;
        final long currentTimeMillis = System.currentTimeMillis();
        Time now = new Time();
        now.set(currentTimeMillis);
        Time startTime = new Time();
        startTime.set(currentTimeMillis);
        startTime.hour = Integer.parseInt(splitBeginTime[0]);
        startTime.minute = Integer.parseInt(splitBeginTime[1]);
        Time endTime = new Time();
        endTime.set(currentTimeMillis);
        endTime.hour = Integer.parseInt(splitEndTime[0]);
        endTime.minute = Integer.parseInt(splitEndTime[1]);
        if (!startTime.before(endTime)) {
            startTime.set(startTime.toMillis(true) - aDayInMillis);
            result = !now.before(startTime) && !now.after(endTime); // startTime <= now <= endTime
            Time startTimeInThisDay = new Time();
            startTimeInThisDay.set(startTime.toMillis(true) + aDayInMillis);
            if (!now.before(startTimeInThisDay)) {
                result = true;
            }
        } else {
            result = !now.before(startTime) && !now.after(endTime); // startTime <= now <= endTime
        }
        return result;
    }

}
