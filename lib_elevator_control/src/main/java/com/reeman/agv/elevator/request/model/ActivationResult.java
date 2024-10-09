package com.reeman.agv.elevator.request.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ActivationResult implements Serializable {

    @SerializedName("thingId")
    public String thingId;
    @SerializedName("resourceGroups")
    public List<?> resourceGroups;
    @SerializedName("channelData")
    public ChannelDataDTO channelData;

    public static class ChannelDataDTO {
        @SerializedName("type")
        public String type;
        @SerializedName("properties")
        public PropertiesDTO properties;

        public static class PropertiesDTO {
            @SerializedName("general")
            public GeneralDTO general;
            @SerializedName("password")
            public String password;
            @SerializedName("clientId")
            public String clientId;
            @SerializedName("host")
            public String host;
            @SerializedName("lwt")
            public LwtDTO lwt;
            @SerializedName("topic")
            public TopicDTO topic;
            @SerializedName("tls")
            public TlsDTO tls;
            @SerializedName("username")
            public String username;

            public static class GeneralDTO {
                @SerializedName("mqttVersion")
                public String mqttVersion;
                @SerializedName("cleansession")
                public Boolean cleansession;
                @SerializedName("keepavlie")
                public Integer keepavlie;
            }

            public static class LwtDTO {
                @SerializedName("payload")
                public PayloadDTO payload;
                @SerializedName("topic")
                public String topic;
                @SerializedName("enabled")
                public Boolean enabled;

                public static class PayloadDTO {
                    @SerializedName("clientId")
                    public String clientId;
                }
            }

            public static class TopicDTO {
                @SerializedName("sub")
                public SubDTO sub;
                @SerializedName("pub")
                public PubDTO pub;

                public static class SubDTO {
                    @SerializedName("command")
                    public String command;
                }

                public static class PubDTO {
                    @SerializedName("data")
                    public String data;
                    @SerializedName("ack")
                    public String ack;
                }
            }

            public static class TlsDTO {
                @SerializedName("tlsVersion")
                public String tlsVersion;
                @SerializedName("certificate")
                public CertificateDTO certificate;
                @SerializedName("enableServerCertAuth")
                public Boolean enableServerCertAuth;
                @SerializedName("enabled")
                public Boolean enabled;
                @SerializedName("tlsType")
                public String tlsType;

                public static class CertificateDTO {
                    @SerializedName("cert")
                    public String cert;
                    @SerializedName("ca")
                    public String ca;
                    @SerializedName("key")
                    public String key;
                }
            }

            @Override
            public String toString() {
                return "PropertiesDTO{" +
                        "general=" + general +
                        ", password='" + password + '\'' +
                        ", clientId='" + clientId + '\'' +
                        ", host='" + host + '\'' +
                        ", lwt=" + lwt +
                        ", topic=" + topic +
                        ", tls=" + tls +
                        ", username='" + username + '\'' +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "ChannelDataDTO{" +
                    "type='" + type + '\'' +
                    ", properties=" + properties +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ActivationResult{" +
                "thingId='" + thingId + '\'' +
                ", resourceGroups=" + resourceGroups +
                ", channelData=" + channelData +
                '}';
    }
}
