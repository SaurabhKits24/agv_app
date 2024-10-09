package com.reeman.agv.elevator.mqtt;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import com.hivemq.client.rx.FlowableWithSingle;
import com.reeman.agv.elevator.BuildConfig;
import com.reeman.agv.elevator.exception.CustomException;
import com.reeman.agv.elevator.state.Code;
import com.reeman.agv.elevator.state.Step;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import timber.log.Timber;


public class MqttClient {

    private final String host;
    private final String clientId;
    private final String username;
    private final String password;
    private Mqtt3BlockingClient client;
    private final String topic;
    private int reconnectCount;
    private boolean isManualDisconnect;
    private CallBack callBack;

    private boolean isAlreadyReceiveLeave;

    public void setAlreadyReceiveLeave(boolean alreadyReceiveLeave) {
        isAlreadyReceiveLeave = alreadyReceiveLeave;
    }

    public boolean isManualDisconnect() {
        return isManualDisconnect;
    }

    public MqttClient(String host, String clientId, String username, String password, String topic, CallBack callBack) {
        this.host = host;
        this.clientId = clientId;
        this.username = username;
        this.password = password;
        this.topic = topic;
        this.callBack = callBack;
    }

    public Observable<Integer> connect() {
        return Observable.create(emitter -> {
            isManualDisconnect = false;
            isAlreadyReceiveLeave = false;
            reconnectCount = 0;
            String[] split = host.replace("tcp://", "").split(":");
            if (split.length != 2)
                throw new CustomException(Step.CONNECT_MQTT, Code.PARSE_MQTT_HOST_FAILED);
            Timber.tag(BuildConfig.ELEVATOR_DIR).w(" host : " + split[0] + " , port : " + split[1]);
            Mqtt3Connect connect = Mqtt3Connect.builder()
                    .keepAlive(10)
                    .build();
            client = com.hivemq.client.mqtt.MqttClient.builder()
                    .useMqttVersion3()
                    .identifier(clientId)
                    .serverHost(split[0])
                    .serverPort(Integer.parseInt(split[1]))
                    .simpleAuth()
                    .username(username)
                    .password(password.getBytes())
                    .applySimpleAuth()
                    .addConnectedListener(context -> {
                        if (reconnectCount != 0) {
                            Timber.tag(BuildConfig.ELEVATOR_DIR).w("重新连接成功");
                            reconnectCount = 0;
                            return;
                        }
                        Timber.tag(BuildConfig.ELEVATOR_DIR).w("连接成功");
                        emitter.onNext(0);
                    })
                    .addDisconnectedListener(context -> {
                        Timber.tag(BuildConfig.ELEVATOR_DIR).w(context.getCause(),"连接断开");
                        if (isManualDisconnect || isAlreadyReceiveLeave || callBack == null) return;
                        if (reconnectCount < 3) {
                            Timber.tag(BuildConfig.ELEVATOR_DIR).w("断开连接,正在重连 : %s", reconnectCount);
                            context.getReconnector()
                                    .reconnect(true)
                                    .delay(++reconnectCount * 2L, TimeUnit.SECONDS);
                            return;
                        }
                        Timber.tag(BuildConfig.ELEVATOR_DIR).w("断开连接,超过最大重连次数");
                        callBack.onThrows(new CustomException(Step.CONNECT_MQTT, Code.CONNECT_MQTT_FAILED));
                    })
                    .buildBlocking();
            client.connect(connect);
        });


    }

    public void publish(String topic,String msg){
        client.publishWith().topic(topic).qos(MqttQos.AT_LEAST_ONCE).payload(msg.getBytes()).send();
    }


    public FlowableWithSingle<Mqtt3Publish, Mqtt3SubAck> subscribeToTopic() {
        return client.toRx().subscribePublishesWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .applySubscribe();
    }

    public void disconnect() {
        callBack = null;
        isManualDisconnect = true;
        if (client != null) {
            try {
                Mqtt3AsyncClient mqtt3AsyncClient = client.toAsync();
                mqtt3AsyncClient.unsubscribeWith().topicFilter(topic).send();
                mqtt3AsyncClient.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            client = null;
        }
    }

    public interface CallBack {

        void onThrows(Throwable throwable);
    }

}
