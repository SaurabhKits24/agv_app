package com.reeman.agv.elevator.request.model;

import com.google.gson.annotations.SerializedName;

public class Message{


    @SerializedName("requestId")
    public String requestId;
    @SerializedName("heads")
    public HeadsDTO heads;
    @SerializedName("body")
    public String body;
}
