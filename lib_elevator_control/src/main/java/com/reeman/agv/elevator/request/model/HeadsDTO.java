package com.reeman.agv.elevator.request.model;

import com.google.gson.annotations.SerializedName;

public class HeadsDTO {
        @SerializedName("instructionId")
        public String instructionId;
        @SerializedName("thingId")
        public String thingId;
    }