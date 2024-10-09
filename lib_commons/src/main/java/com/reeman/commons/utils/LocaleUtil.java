package com.reeman.commons.utils;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Locale;

public class LocaleUtil {

    public static void changeAppLanguage(Resources resources, int currentLanguageType) {
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Locale locale;
        switch (currentLanguageType) {
            case 1:
                locale = Locale.CHINA;
                break;
            case 2:
                locale = Locale.JAPAN;
                break;
            case 3:
                locale = Locale.KOREA;
                break;
            case 4:
                locale = new Locale.Builder()
                        .setLanguage("es")
                        .build();
                break;
            case 5:
                locale = new Locale.Builder()
                        .setLanguage("th")
                        .build();
                break;
            case 6:
                locale = new Locale.Builder()
                        .setLanguage("zh")
                        .setRegion("HK")
                        .build();
                break;
            case 7:
                locale = new Locale.Builder()
                        .setLanguage("nl")
                        .build();
                break;
            case 8:
                locale = Locale.FRENCH;
                break;
            case 9:
                locale = new Locale.Builder()
                        .setLanguage("de")
                        .build();
                break;
            case 10:
                locale = new Locale.Builder()
                        .setLanguage("hu")
                        .build();
                break;
            case 11:
                locale = new Locale.Builder()
                        .setLanguage("ar")
                        .build();
                break;
            case 12:
                locale = new Locale.Builder()
                        .setLanguage("zh")
                        .setRegion("TW")
                        .build();
                break;
            case 13:
                locale = new Locale.Builder()
                        .setLanguage("vi")
                        .build();
                break;
            default:
                locale = Locale.ENGLISH;
                break;
        }
        configuration.locale = locale;
        resources.updateConfiguration(configuration, displayMetrics);
    }


    public static int getLocaleType() {
        String language = Locale.getDefault().toString();
        if (language.startsWith("ja")) {
            return 2;
        } else if (language.startsWith("ko")) {
            return 3;
        } else if (language.startsWith("es")) {
            return 4;
        } else if (language.startsWith("th")) {
            return 5;
        } else if (language.startsWith("zh_HK")) {
            return 6;
        } else if (language.startsWith("nl")) {
            return 7;
        } else if (language.startsWith("fr")) {
            return 8;
        } else if (language.startsWith("de")) {
            return 9;
        } else if (language.startsWith("hu")) {
            return 10;
        }  else if (language.startsWith("ar")) {
            return 11;
        } else if (language.startsWith("zh_TW")) {
            return 12;
        } else if (language.startsWith("vi")) {
            return 13;
        } else if (language.startsWith("zh")) {
            return 1;
        } else {
            return 0;
        }
    }

    public static String getLocaleType(int localeType) {
        switch (localeType) {
            case 1:
                return "中文简体";
            case 2:
                return "日本語";
            case 3:
                return "한국인";
            case 4:
                return "Español";
            case 5:
                return "ภาษาไทย";
            case 6:
                return "粤语";
            case 7:
                return "Nederlands";
            case 8:
                return "Français";
            case 9:
                return "Deutsch";
            case 10:
                return "Magyar";
            case 11:
                return "اللغة العربية";
            case 12:
                return "中文(繁體)";
            default:
                return "English";
        }

    }

    public static String getLanguage(int localeType) {
        switch (localeType) {
            case 1:
                return "cn-ZH";
            case 2:
                return "ja-JP";
            case 3:
                return "ko-KR";
            case 4:
                return "es-ES";
            case 5:
                return "th-TH";
            case 6:
                return "zh-HK";
            case 7:
                return "nl-NL";
            case 8:
                return "fr-FR";
            case 9:
                return "de-DE";
            case 10:
                return "hu-HU";
            case 11:
                return "ar-SA";
            case 12:
                return "zh-TW";
            case 13:
                return "vi-VN";
            default:
                return "en-US";
        }
    }

    public static String getVoice(int localeType) {
        switch (localeType) {
            case 1:
                return "zh-CN-XiaoxiaoNeural";
            case 2:
                return "ja-JP-NanamiNeural";
            case 3:
                return "ko-KR-SunHiNeural";
            case 4:
                return "es-ES-ElviraNeural";
            case 5:
                return "th-TH-AcharaNeural";
            case 6:
                return "zh-HK-HiuMaanNeural";
            case 7:
                return "nl-NL-ColetteNeural";
            case 8:
                return "fr-FR-DeniseNeural";
            case 9:
                return "de-DE-AmalaNeural";
            case 10:
                return "hu-HU-NoemiNeural";
            case 11:
                return "ar-SA-ZariyahNeural";
            case 12:
                return "zh-TW-HsiaoChenNeural";
            case 13:
                return "vi-VN-HoaiMyNeural";
            default:
                return "en-US-JennyNeural";
        }
    }

    public static String getAssetsPathByLanguage(int type) {
        String path;
        switch (type) {
            case 1:
                path = "zh/";
                break;
            case 2:
                path = "ja/";
                break;
            case 3:
                path = "ko/";
                break;
            case 4:
                path = "es/";
                break;
            case 5:
                path = "th/";
                break;
            case 6:
                path = "zh-hk/";
                break;
            case 7:
                path = "nl/";
                break;
            case 8:
                path = "fr/";
                break;
            case 9:
                path = "de/";
                break;
            case 10:
                path = "hu/";
                break;
            case 11:
                path = "ar/";
                break;
            case 12:
                path = "zh-tw/";
                break;
            case 13:
                path = "vi/";
                break;
            default:
                path = "en/";
                break;
        }
        return path;
    }
}

