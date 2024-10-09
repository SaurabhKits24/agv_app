package com.reeman.commons.utils;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ScaleXSpan;
import android.text.style.StyleSpan;

public class StringUtils {

    /**
     * 判断是否是中文输入
     * @param c
     * @return
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS ||
                ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A;
    }


    public static String byteArrayToDecimalString(byte[] byteArray) {
        StringBuilder sb = new StringBuilder();
        for (byte b : byteArray) {
            int decimalValue = b & 0xFF;
            sb.append(decimalValue).append(" ");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public static void updateNumber(SpannableString str,int start,int length){
        str.setSpan(new ForegroundColorSpan(Color.BLUE), start, start + length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        str.setSpan(new AbsoluteSizeSpan(40), start, start + length, 0);
        str.setSpan(new StyleSpan(Typeface.BOLD), start, start + length, 0);
        str.setSpan(new ScaleXSpan(1.1f), start, start + length, 0);
    }
}
