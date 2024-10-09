package com.reeman.commons.board;

import android.os.Build;

public class BoardConstants {
    public static final int WIFI_CONNECT_THRESHOLD = Build.PRODUCT.startsWith("YF") ? 1 : 2;
}
