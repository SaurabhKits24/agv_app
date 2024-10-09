package com.reeman.agv.calling.mqtt;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.reeman.agv.calling.event.MqttConnectionEvent;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.eventbus.EventBus;
import com.reeman.commons.utils.AESUtil;
import com.reeman.commons.utils.MMKVManager;
import com.reeman.commons.utils.WIFIUtils;


import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;


public class MqttClient {

    private static MqttClient instance;

    private final static String TAG = "CallingMqttClient";

    private final String host = "mqtt.rmbot.cn";
    private String clientId;

    private String hostname;
    private final String username;
    private final String password;
    private Context context;
    private Mqtt5BlockingClient client;
    private String topic;
    private int reconnectCount;

    private boolean isConnecting;

    private OnMqttPayloadCallback callback;

    private boolean disconnectManually = false;


    public boolean isConnected() {
        return client != null && client.toAsync().getState().isConnected();
    }

    public boolean isConnecting(){
        return isConnecting;
    }

    public void setCallback(OnMqttPayloadCallback callback) {
        this.callback = callback;
    }
    public static MqttClient getInstance() {
        if (instance == null) {
            synchronized (MqttClient.class) {
                if (instance == null) {
                    instance = new MqttClient();
                }
            }
        }
        return instance;
    }

    public MqttClient() {
        username = Constants.DEFAULT_MQTT_USERNAME;
        password = Constants.DEFAULT_MQTT_PASSWORD;
    }

    public Observable<Integer> connect(String hostname, Context mContext) {
        this.hostname = hostname;
        this.clientId = hostname + "_" + SystemClock.uptimeMillis();
        this.topic = "reeman/calling/phone/" + hostname + "/v2/#";
        this.context = mContext;
        isConnecting = true;
        return Observable.create(emitter -> {
            reconnectCount = 0;
            Mqtt5Connect connect = Mqtt5Connect.builder()
                    .keepAlive(10)
                    .build();
            client = com.hivemq.client.mqtt.MqttClient.builder()
                    .useMqttVersion5()
                    .identifier(clientId)
                    .serverHost(host)
                    .serverPort(1883)
                    .simpleAuth()
                    .username(username)
                    .password(password.getBytes())
                    .applySimpleAuth()
                    .addConnectedListener(context -> {
                       EventBus.INSTANCE.sendEvent(MqttConnectionEvent.class,new MqttConnectionEvent(true));
                        isConnecting =false;
                        if (reconnectCount != 0) {
                            Timber.tag(TAG).w("重新连接成功");
                            reconnectCount = 0;
                            return;
                        }
                        Timber.tag(TAG).w("连接成功");
                        emitter.onNext(0);
                    })
                    .addDisconnectedListener(context -> {
                       EventBus.INSTANCE.sendEvent(MqttConnectionEvent.class,new MqttConnectionEvent(false));
                        isConnecting = true;
                        Timber.w(context.getCause(), "连接断开");
                        if (disconnectManually)return;
                        if (reconnectCount > 3) reconnectCount = 0;
                        Timber.tag(TAG).w("断开连接,正在重连");
                        context.getReconnector()
                                .reconnect(true)
                                .delay(++reconnectCount * 2L, TimeUnit.SECONDS);
                    })
                    .buildBlocking();
            client.connect(connect);
            disconnectManually = false;
        });
    }

    public Observable<Boolean> publish(String topic, String msg) {
        return Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
                    client.publishWith().topic(topic).qos(MqttQos.AT_LEAST_ONCE).payload(msg.getBytes()).send();
                    emitter.onNext(true);
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void publishSync(String topic, String msg) {
        client.publishWith().topic(topic).qos(MqttQos.AT_LEAST_ONCE).payload(msg.getBytes()).send();
    }


    @SuppressLint("CheckResult")
    public Observable<Boolean> subscribeToTopic() {
        return Observable.create(emitter -> {
            unsubscribeTopics();
            client.toRx().subscribePublishesWith()
                    .topicFilter(topic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .applySubscribe()
                    .doOnSingle(mqtt3SubAck -> emitter.onNext(true))
                    .subscribe(mqtt5Publish -> {
                        String subscribe = mqtt5Publish.getTopic().toString();
                        String payload = new String(mqtt5Publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                        Timber.tag("subscribe").v("topic %s , payload %s", subscribe, payload);
                        if (callback !=null){
                            callback.onMqttPayload(subscribe,payload);
                        }
                    }, throwable -> {
                        Timber.w(throwable, "mqtt订阅异常");
                        if (client == null || !WIFIUtils.isNetworkConnected(context))
                            return;
                        emitter.onError(throwable);
                    });
        });
    }

    public void unsubscribeTopics() {
        Timber.d("取消订阅");
        if (client != null && topic != null) {
            try {
                Mqtt5AsyncClient mqtt5AsyncClient = client.toAsync();
                mqtt5AsyncClient.unsubscribeWith().topicFilter(topic).send();
            } catch (Exception e) {
                Timber.w(e, "取消订阅失败");
            }
        }
    }

    public void disconnect() {
        if (client != null) {
            try {
                disconnectManually = true;
                unsubscribeTopics();
                client.toAsync().disconnect();
            } catch (Exception e) {
                Timber.w(e, "断开连接失败");
            }
            isConnecting = false;
            client = null;
        }
    }

    public interface OnMqttPayloadCallback{

        void onMqttPayload(String topic,String payload);
    }

}
