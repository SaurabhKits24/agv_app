package com.reeman.agv.elevator.request.model;

import com.google.gson.annotations.SerializedName;

public class Ack {

    public Ack(Message message, BodyDTO body) {
        this.requestId = message.requestId;
        this.heads = message.heads;
        this.body = body;
    }

    @SerializedName("requestId")
    public String requestId;
    @SerializedName("heads")
    public HeadsDTO heads;
    @SerializedName("body")
    public BodyDTO body;

    public static class BodyDTO {

        public BodyDTO(String statusCode, String status) {
            this.statusCode = statusCode;
            this.status = status;
        }

        @SerializedName("statusCode")
        public String statusCode;

        @SerializedName("status")
        public String status;
    }


}
