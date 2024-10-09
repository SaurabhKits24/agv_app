package com.reeman.agv.fragments.main.listener;

import java.util.List;

import kotlin.Pair;

public interface ModeQRCodeClickListener {
        void onStart(List<Pair<Pair<String,String>,Pair<String,String>>> QRCodeModelList);
    }