package com.reeman.commons.model.request;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("code")
    public Integer code;
    @SerializedName("message")
    public String message;
    @SerializedName("data")
    public DataDTO data;

    public static class DataDTO {
        @SerializedName("result")
        public ResultDTO result;

        public static class ResultDTO {
            @SerializedName("accessToken")
            public String accessToken;
            @SerializedName("refreshToken")
            public String refreshToken;

            @Override
            public String toString() {
                return "ResultDTO{" +
                        "accessToken='" + accessToken + '\'' +
                        ", refreshToken='" + refreshToken + '\'' +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "DataDTO{" +
                    "result=" + result +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
