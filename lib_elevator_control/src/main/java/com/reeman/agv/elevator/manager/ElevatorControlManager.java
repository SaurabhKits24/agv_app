package com.reeman.agv.elevator.manager;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.reeman.agv.elevator.BuildConfig;
import com.reeman.agv.elevator.constants.Constants;
import com.reeman.agv.elevator.exception.CustomException;
import com.reeman.agv.elevator.mqtt.MqttClient;
import com.reeman.agv.elevator.request.ApiClient;
import com.reeman.agv.elevator.request.model.Ack;
import com.reeman.agv.elevator.request.model.ActivationResult;
import com.reeman.agv.elevator.request.model.CancelElevator;
import com.reeman.agv.elevator.request.model.ElevatorIdResult;
import com.reeman.agv.elevator.request.model.Message;
import com.reeman.agv.elevator.request.model.Passenger;
import com.reeman.agv.elevator.request.model.ReqRobotIdResult;
import com.reeman.agv.elevator.request.model.Response;
import com.reeman.agv.elevator.request.model.State;
import com.reeman.agv.elevator.request.model.TakeElevator;
import com.reeman.agv.elevator.request.model.TakeElevatorInside;
import com.reeman.agv.elevator.request.model.TaskInfo;
import com.reeman.agv.elevator.state.Code;
import com.reeman.agv.elevator.state.Step;
import com.reeman.commons.utils.SpManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import timber.log.Timber;


public class ElevatorControlManager implements MqttClient.CallBack {

    private final String TAG = BuildConfig.ELEVATOR_DIR;

    private static ElevatorControlManager elevatorControlManager;
    Gson gson = new Gson();
    private MqttClient client;
    private CallBack callBack;
    private String elevatorId;
    private String hostname;
    private String from;
    private String to;
    private String thingId;

    private ActivationResult activationResult;
    private boolean isReceiveEnter;
    private boolean isReceiveLeave;

    private Step currentStep = Step.IDLE;

    private long waitElevatorTime;

    private Disposable disposable;

    public long getWaitElevatorTime() {
        return waitElevatorTime;
    }

    public Step getCurrentStep() {
        return currentStep;
    }

    public String getElevatorId() {
        return elevatorId;
    }

    public String getThingId() {
        return thingId;
    }

    public void setElevatorId(String elevatorId) {
        this.elevatorId = elevatorId;
    }

    public void setThingId(String thingId) {
        this.thingId = thingId;
    }

    public static synchronized ElevatorControlManager getInstance() {
        if (elevatorControlManager == null) {
            elevatorControlManager = new ElevatorControlManager();
        }
        return elevatorControlManager;
    }

    public void init(String hostname, String from, String to, CallBack callBack) {
        this.callBack = callBack;
        this.hostname = hostname;
        this.from = from;
        this.to = to;
    }

    public void release() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        currentStep = Step.IDLE;
        if (client != null) {
            client.disconnect();
            client = null;
        }
        waitElevatorTime = 0;
        callBack = null;
        hostname = "";
        from = "";
        to = "";
        isReceiveEnter = false;
        isReceiveLeave = false;
        if (!TextUtils.isEmpty(thingId) && !TextUtils.isEmpty(elevatorId)) {
            cancelElevator(elevatorId, thingId, "finish")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(integer -> Timber.tag(TAG).w("取消乘梯成功"), throwable -> Timber.tag(TAG).w("取消乘梯失败"));
        }
        thingId = "";
        elevatorId = "";
        elevatorControlManager = null;
    }

    public void takeElevatorInside() {
        if (client != null) {
            client.disconnect();
            client = null;
        }
        reqRobotId(hostname)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(v -> onThingIdReceived())
                .observeOn(Schedulers.io())
                .flatMap(v -> activationInstance())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(v -> onActivationSuccess())
                .observeOn(Schedulers.io())
                .flatMap(v -> connectMqtt())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(v -> onMqttConnectSuccess())
                .observeOn(Schedulers.io())
                .flatMap(v -> subscribeToTopic())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(v -> onSubscribeToTopicSuccess())
                .observeOn(Schedulers.io())
                .flatMap(v -> online())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(v -> onDeviceOnlineSuccess())
                .observeOn(Schedulers.io())
                .flatMap(v -> takeElevatorInside(new TakeElevatorInside(thingId, to)))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(v -> onElevatorInsideRequestSuccess())
                .onErrorResumeNext(this::handleError)
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(@NonNull Integer integer) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        onWorkflowError(e);
                    }

                    @Override
                    public void onComplete() {
                        onWorkflowSuccess();
                        disposable = null;
                    }
                });
    }

    /**
     * 梯外乘梯
     * 获取实例id
     * 激活
     * 订阅
     * 上线
     * 呼梯
     */
    public void takeElevator() {
        if (client != null) {
            client.disconnect();
            client = null;
        }
        reqRobotId(hostname)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(v -> onThingIdReceived())
                .observeOn(Schedulers.io())
                .flatMap(v -> checkElevatorState(SpManager.getInstance().getString(Constants.KEY_ELEVATOR_ID, null)))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(this::onCheckElevatorStateSuccess)
                .observeOn(Schedulers.io())
                .flatMap(v -> activationInstance())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(v -> onActivationSuccess())
                .observeOn(Schedulers.io())
                .flatMap(v -> connectMqtt())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(v -> onMqttConnectSuccess())
                .observeOn(Schedulers.io())
                .flatMap(v -> subscribeToTopic())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(v -> onSubscribeToTopicSuccess())
                .observeOn(Schedulers.io())
                .flatMap(v -> online())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(v -> onDeviceOnlineSuccess())
                .observeOn(Schedulers.io())
                .flatMap(v -> takeElevator(new TakeElevator(thingId, from, to)))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(v -> onElevatorRequestSuccess())
                .onErrorResumeNext(this::handleError)
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(@NonNull Integer integer) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        onWorkflowError(e);
                    }

                    @Override
                    public void onComplete() {
                        onWorkflowSuccess();
                        disposable = null;
                    }
                });
    }

    /**
     * 获取实例id
     *
     * @param hostname
     * @return
     */
    private Observable<Integer> reqRobotId(String hostname) {
        currentStep = Step.REQUEST_ID;
        return performApiCall(Step.REQUEST_ID, ApiClient.getInstance().getApiService().requestRobotId(hostname));
    }

    public Observable<Integer> checkElevatorState(String elevatorId) {
        if (elevatorId == null) {
            return Observable.create(emitter -> emitter.onNext(0));
        }
        currentStep = Step.CHECK_ELEVATOR_STATE;
        return performApiCall(Step.CHECK_ELEVATOR_STATE, ApiClient.getInstance().getApiService().getElevatorStatus(elevatorId));
    }

    /**
     * 激活
     *
     * @return
     */
    private Observable<Integer> activationInstance() {
        currentStep = Step.ACTIVATION;
        return performApiCall(Step.ACTIVATION, ApiClient.getInstance().getApiService().activationInstance(thingId, new JsonObject()));
    }

    /**
     * 设备上线
     *
     * @return
     */
    private Observable<Integer> online() {
        currentStep = Step.ONLINE;
        return performApiCall(Step.ONLINE, ApiClient.getInstance().getApiService().online(thingId, new JsonObject()));
    }

    /**
     * 乘梯
     *
     * @param takeElevator
     * @return
     */
    private Observable<Integer> takeElevator(TakeElevator takeElevator) {
        currentStep = Step.CALL_ELEVATOR;
        waitElevatorTime = System.currentTimeMillis();
        return performApiCall(Step.CALL_ELEVATOR, ApiClient.getInstance().getApiService().takeElevator(takeElevator));
    }

    /**
     * 梯内乘梯
     *
     * @param takeElevatorInside
     * @return
     */
    private Observable<Integer> takeElevatorInside(TakeElevatorInside takeElevatorInside) {
        currentStep = Step.CALL_ELEVATOR_INSIDE;
        waitElevatorTime = System.currentTimeMillis();
        return performApiCall(Step.CALL_ELEVATOR_INSIDE, ApiClient.getInstance().getApiService().takeElevatorInside(takeElevatorInside));
    }

    /**
     * 取消乘梯
     *
     * @param elevatorId
     * @param passenger
     * @param cause
     * @return
     */
    private Observable<Integer> cancelElevator(String elevatorId, String passenger, String cause) {
        currentStep = Step.CANCEL_ELEVATOR;
        return performApiCall(Step.CANCEL_ELEVATOR, ApiClient.getInstance().getApiService().cancelTakeElevatorExecute(elevatorId, new CancelElevator(passenger, cause)));

    }

    public Observable<Response> cancelTakeElevator(String elevatorId, String passenger, String cause) {
        return ApiClient.getInstance().getApiService().cancelTakeElevator(elevatorId, new CancelElevator(passenger, cause));
    }

    private Observable<Integer> performApiCall(Step step, Call<Response> call) {
        return Observable.create(emitter -> {
            try {
                retrofit2.Response<Response> execute = call.execute();
                if (callBack == null || disposable == null || disposable.isDisposed()) return;
                if (execute.isSuccessful()) {
                    Response body = execute.body();
                    if (body != null) {
                        if (body.code.equals(Code.SUCCESS)) {
                            handleSuccess(step, body, emitter);
                        } else {
                            if (disposable != null && !disposable.isDisposed())
                                emitter.onError(new CustomException(step, body.code, body.msg));
                        }
                    } else {
                        if (disposable != null && !disposable.isDisposed())
                            emitter.onError(new CustomException(step, Code.RESPONSE_BODY_NULL));
                    }
                } else {
                    if (disposable != null && !disposable.isDisposed())
                        emitter.onError(new CustomException(step, Code.REQUEST_FAIL));
                }
            } catch (IOException e) {
                Timber.tag(TAG).e(e, step.name());
                if (disposable != null && !disposable.isDisposed())
                    emitter.onError(new CustomException(step, Code.IO_EXCEPTION));
            } catch (JsonSyntaxException e) {
                Timber.tag(TAG).e(e, step.name());
                if (disposable != null && !disposable.isDisposed())
                    emitter.onError(new CustomException(step, Code.JSON_SYNTAX_EXCEPTION));
            } catch (Exception e) {
                Timber.tag(TAG).e(e, step.name());
                if (disposable != null && !disposable.isDisposed())
                    emitter.onError(new CustomException(step, Code.UNKNOWN_EXCEPTION));
            }
        });
    }

    private void handleSuccess(Step step, Response body, ObservableEmitter<Integer> emitter) {
        if (callBack == null || disposable == null || disposable.isDisposed()) return;
        switch (step) {
            case REQUEST_ID:
                handleRequestIdSuccess(body, emitter);
                break;
            case CHECK_ELEVATOR_STATE:
                handleCheckElevatorState(body, emitter);
                break;
            case ACTIVATION:
                handleActivationSuccess(body, emitter);
                break;
            case ONLINE:
                handleOnlineSuccess(body, emitter);
                break;
            case CALL_ELEVATOR_INSIDE:
            case CALL_ELEVATOR:
                handleCallElevator(body, emitter);
                break;
            case CANCEL_ELEVATOR:
                handleCancelElevator(body, emitter);
                break;
        }
    }

    private void handleRequestIdSuccess(Response body, ObservableEmitter<Integer> emitter) {
        if (callBack == null || disposable == null || disposable.isDisposed()) return;
        ReqRobotIdResult reqRobotIdResult = gson.fromJson(gson.toJson(body.result), ReqRobotIdResult.class);
        if (reqRobotIdResult != null && !TextUtils.isEmpty(reqRobotIdResult.result)) {
            thingId = reqRobotIdResult.result;
            emitter.onNext(0);
        } else {
            Timber.w("获取实例id失败");
            emitter.onError(new CustomException(Step.REQUEST_ID, Code.ELEVATOR_NOT_FOUND));
        }
    }

    private void handleCheckElevatorState(Response body, ObservableEmitter<Integer> emitter) {
        if (callBack == null || disposable == null || disposable.isDisposed()) return;
        Object result = body.result;
        Gson gson = new Gson();
        State state = gson.fromJson(gson.toJson(result), State.class);
        Object requests = state.requests;
        JsonObject jsonRequestObject = JsonParser.parseString(gson.toJson(requests)).getAsJsonObject();
        if (jsonRequestObject.size() > 0) {
            for (Map.Entry<String, JsonElement> entry : jsonRequestObject.entrySet()) {
                String thingIdTakeElevator = entry.getKey();
                if (!thingId.equals("") && thingIdTakeElevator.equals(thingId)) {
                    elevatorId = SpManager.getInstance().getString(Constants.KEY_ELEVATOR_ID, "");
                    emitter.onError(new CustomException(Step.CHECK_ELEVATOR_STATE, Code.REQUEST_ALREADY_EXIST));
                } else {
                    emitter.onNext(1);
                }
                Timber.tag(TAG).w(thingIdTakeElevator);
                Timber.tag(TAG).w(gson.fromJson(entry.getValue(), TaskInfo.class).toString());
                break;
            }
        } else {
            emitter.onNext(0);
        }
    }

    private void handleActivationSuccess(Response body, ObservableEmitter<Integer> emitter) {
        if (callBack == null || disposable == null || disposable.isDisposed()) return;
        List<ActivationResult> resultList = gson.fromJson(gson.toJson(body.result), new TypeToken<List<ActivationResult>>() {
        }.getType());
        if (resultList.size() == 1) {
            activationResult = resultList.get(0);
            emitter.onNext(0);
        } else {
            emitter.onError(new CustomException(Step.ACTIVATION, body.code, body.msg));
        }
    }

    private void handleOnlineSuccess(Response body, ObservableEmitter<Integer> emitter) {
        if (callBack == null || disposable == null || disposable.isDisposed()) return;
        emitter.onNext(0);
    }

    private void handleCallElevator(Response body, ObservableEmitter<Integer> emitter) {
        if (callBack == null || disposable == null || disposable.isDisposed()) return;
        ElevatorIdResult result = gson.fromJson(gson.toJson(body.result), ElevatorIdResult.class);
        this.elevatorId = result.elevatorId.toString();
        SpManager.getInstance().edit().putString(Constants.KEY_ELEVATOR_ID, elevatorId).apply();
        emitter.onNext(0);
        emitter.onComplete();
    }

    private void handleCancelElevator(Response body, ObservableEmitter<Integer> emitter) {
        if (callBack == null || disposable == null || disposable.isDisposed()) return;
        emitter.onNext(0);
    }

    /**
     * 连接mqtt并订阅
     *
     * @return
     */
    private Observable<Integer> connectMqtt() {
        ActivationResult.ChannelDataDTO.PropertiesDTO properties = activationResult.channelData.properties;
        client = new MqttClient(
                properties.host,
                properties.clientId,
                properties.username,
                properties.password,
                properties.topic.sub.command,
                this
        );
        currentStep = Step.CONNECT_MQTT;
        return client.connect();
    }

    private Observable<Integer> subscribeToTopic() {
        currentStep = Step.SUBSCRIBE;
        isReceiveEnter = false;
        isReceiveLeave = false;
        return Observable.create(emitter -> client.subscribeToTopic()
                .doOnSingle(mqtt3SubAck -> emitter.onNext(0))
                .subscribe(publish -> {
                    if (callBack == null || disposable == null || disposable.isDisposed()) return;
                    if (callBack == null || client == null || client.isManualDisconnect()) return;
                    Timber.tag(TAG).w(
                            "Received publish" + ", topic: " + publish.getTopic() + ", QoS: " + publish.getQos() +
                                    ", payload: " + new String(publish.getPayloadAsBytes()));
                    String msg = new String(publish.getPayloadAsBytes());
                    Message messageFromJson = gson.fromJson(msg, Message.class);
                    if (messageFromJson.body.equals("enter")) {
                        if (isReceiveEnter) return;
                        isReceiveEnter = true;
                        ack(0, messageFromJson, true);
                    } else if (messageFromJson.body.equals("leave")) {
                        if (isReceiveLeave) return;
                        if (client != null) {
                            client.setAlreadyReceiveLeave(true);
                        }
                        isReceiveLeave = true;
                        ack(0, messageFromJson, false);
                    }
                }, throwable -> {
                    if (client == null || client.isManualDisconnect()) return;
                    Timber.tag(TAG).e(throwable, "mqtt订阅异常");
                    emitter.onError(new CustomException(Step.SUBSCRIBE, Code.SUBSCRIBE_FAILED));
                }));
    }

    private void ack(int count, Message message, boolean enterElevator) {
        if (callBack == null || disposable == null || disposable.isDisposed()) return;
        if (client == null || client.isManualDisconnect()) return;
        currentStep = enterElevator ? Step.ENTER_ELEVATOR_ACK : Step.LEAVE_ELEVATOR_ACK;
        int retryCount = count + 1;
        publish(gson.toJson(new Ack(message, new Ack.BodyDTO("1", "Success"))))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (callBack == null || disposable == null || disposable.isDisposed()) return;
                    if (enterElevator) {
                        callBack.onArriveCurrentFloor();
                    } else {
                        callBack.onArriveTargetFloor(to);
                    }
                }, throwable -> {
                    if (callBack == null || disposable == null || disposable.isDisposed()) return;
                    if (client == null || client.isManualDisconnect() || callBack == null) return;
                    Timber.tag(TAG).w(throwable, "响应梯控平台消息失败");
                    if (retryCount > 3) {
                        if (enterElevator) {
                            callBack.onError(new CustomException(Step.ENTER_ELEVATOR_ACK, ""));
                        } else {
                            callBack.onErrorAfterReceiveLeaveElevator(new CustomException(Step.LEAVE_ELEVATOR_ACK, ""));
                        }
                        return;
                    }
                    ack(retryCount, message, enterElevator);
                });
    }

    /**
     * 完成进梯/出梯
     *
     * @param enterElevator
     */
    public void complete(int count, boolean enterElevator) {
        currentStep = enterElevator ? Step.ENTER_ELEVATOR_COMPLETE : Step.LEAVE_ELEVATOR_COMPLETE;
        int retryCount = count + 1;
        ApiClient.getInstance()
                .getApiService()
                .complete(elevatorId, new Passenger(thingId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (callBack == null || disposable == null || disposable.isDisposed()) return;
                    if (response.code.equals(Code.SUCCESS)) {
                        Timber.tag(TAG).w(enterElevator ? "进梯成功" : "出梯成功");
                        if (!enterElevator) {
                            thingId = "";
                            elevatorId = "";
                            currentStep = Step.IDLE;
                            if (client != null) {
                                client.disconnect();
                                client = null;
                            }
                            callBack.onFinish();
                        } else {
                            callBack.onEnterElevatorComplete();
                            waitElevatorTime = System.currentTimeMillis();
                        }
                    } else {
                        Timber.tag(TAG).w((enterElevator ? "进梯" : "出梯") + "失败 : code : " + response.code + " , msg : " + response.msg);
                        if (Code.REQUEST_FINISHED_NOT_ALLOWED.equals(response.code)) {
                            if (enterElevator) {
                                callBack.onError(new CustomException(Step.ENTER_ELEVATOR_COMPLETE, Code.REQUEST_FINISHED_NOT_ALLOWED));
                            } else {
                                callBack.onErrorAfterReceiveLeaveElevator(new CustomException(Step.LEAVE_ELEVATOR_COMPLETE, Code.REQUEST_FINISHED_NOT_ALLOWED));
                            }
                        } else {
                            if (enterElevator) {
                                callBack.onError(new CustomException(Step.ENTER_ELEVATOR_COMPLETE, Code.REQUEST_FAIL));
                            } else {
                                callBack.onError(new CustomException(Step.LEAVE_ELEVATOR_COMPLETE, Code.REQUEST_FAIL));
                            }
                        }
                    }
                }, throwable -> {
                    if (callBack == null || disposable == null || disposable.isDisposed()) return;
                    Timber.tag(TAG).e(throwable, (enterElevator ? "进梯失败" : "出梯失败"));
                    if (retryCount > 3) {
                        if (enterElevator) {
                            callBack.onError(new CustomException(Step.ENTER_ELEVATOR_COMPLETE, Code.IO_EXCEPTION));
                        } else {
                            callBack.onErrorAfterReceiveLeaveElevator(new CustomException(Step.LEAVE_ELEVATOR_COMPLETE, Code.IO_EXCEPTION));
                        }
                        return;
                    }
                    complete(retryCount, enterElevator);
                });
    }

    private Observable<Integer> publish(String msg) {
        return Observable.create(emitter -> {
            client.publish(activationResult.channelData.properties.topic.pub.ack, msg);
            emitter.onNext(0);
        });
    }

    private void onThingIdReceived() {
        Timber.tag(TAG).w("获取实例id成功 : %s", thingId);
    }

    private void onCheckElevatorStateSuccess(int code) {
        if (callBack == null || disposable == null || disposable.isDisposed()) return;
        if (code == 0) {
            callBack.onElevatorFree();
            Timber.tag(TAG).w("查询电梯信息完成,电梯空闲");
        } else if (code == 1) {
            callBack.onQueue();
            if (disposable != null && !disposable.isDisposed())
                disposable.dispose();
            Timber.tag(TAG).w("查询到电梯被占用,等待");
        }
    }

    private void onActivationSuccess() {
        Timber.tag(TAG).w("激活成功 : \n%s", activationResult.toString());
    }

    private void onMqttConnectSuccess() {
        Timber.tag(TAG).w("连接mqtt成功");
    }

    private void onSubscribeToTopicSuccess() {
        Timber.tag(TAG).w("mqtt订阅成功");
    }

    private void onDeviceOnlineSuccess() {
        Timber.tag(TAG).w("设备上线成功");
    }

    private void onElevatorRequestSuccess() {
        Timber.tag(TAG).w("发送乘梯请求成功");
        callBack.onCallElevatorSuccess();
    }

    private void onElevatorInsideRequestSuccess() {
        Timber.tag(TAG).w("发送梯内乘梯请求成功");
        callBack.onCallElevatorInsideSuccess();
    }

    private Observable<Integer> handleError(Throwable throwable) {
        if (callBack == null) return Observable.empty();
        if (client != null) {
            client.disconnect();
            client = null;
        }
        if (throwable instanceof CustomException) {
            CustomException customException = (CustomException) throwable;
            String code = customException.code;
            Timber.tag(TAG).w("step : " + customException.step + " , code : " + code + (TextUtils.isEmpty(customException.msg) ? "" : " , msg :" + customException.msg));
            if (customException.code.equals(Code.REQUEST_ALREADY_EXIST)) {
                String msg = customException.msg;
                if (!TextUtils.isEmpty(msg)) {
                    Pattern pattern = Pattern.compile("\\(id=(\\d+)\\).*\\【(\\d+)\\】");
                    Matcher matcher = pattern.matcher(msg);
                    if (matcher.find()) {
                        try {
                            String group1 = matcher.group(1);
                            String group2 = matcher.group(2);
                            thingId = group1;
                            elevatorId = group2;
                        } catch (Exception e) {
                            Timber.tag(TAG).w(e, "解析thingId和elevatorId错误");
                        } finally {
                            callBack.onErrorTaskExist(elevatorId, thingId);
                        }
                    }
                }
                if (!TextUtils.isEmpty(elevatorId) && !TextUtils.isEmpty(thingId)) {
                    return cancelElevator(elevatorId, thingId, "retry : workflow warn")
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnNext(v -> {
                                Timber.tag(TAG).w("准备重试...");
                                elevatorId = "";
                                thingId = "";
                            })
                            .doOnError(throwable1 -> {
                                Timber.tag(TAG).w(throwable1);
                                elevatorId = "";
                                thingId = "";
                                callBack.onError(new CustomException(Step.CANCEL_ELEVATOR, Code.UNKNOWN_EXCEPTION));
                            })
                            .delay(3, TimeUnit.SECONDS)
                            .subscribeOn(Schedulers.io())
                            .doOnNext(v -> takeElevator());
                }
            }
        } else {
            Timber.tag(TAG).w(throwable, "流程中出现未知错误");
        }
        if (!TextUtils.isEmpty(elevatorId) && !TextUtils.isEmpty(thingId)) {
            cancelElevator(elevatorId, thingId, "finish")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(integer -> {
                        Timber.tag(TAG).w("已取消本次乘梯");
                        thingId = "";
                        elevatorId = "";
                    }, throwable12 -> Timber.tag(TAG).w(throwable12, "未知错误"));

        }
        callBack.onError((Exception) throwable);
        return Observable.empty();
    }

    private void onWorkflowSuccess() {
        Timber.tag(TAG).w("请求乘梯完成,等待电梯到达");
    }

    private void onWorkflowError(Throwable throwable) {
        Timber.tag(TAG).e(throwable, "乘梯流程错误");
    }

    @Override
    public void onThrows(Throwable throwable) {
        if (currentStep == Step.IDLE) return;
        if (currentStep == Step.LEAVE_ELEVATOR_ACK || currentStep == Step.LEAVE_ELEVATOR_COMPLETE) {
            callBack.onErrorAfterReceiveLeaveElevator(new CustomException(currentStep, Code.IO_EXCEPTION));
            return;
        }
        Timber.tag(TAG).e(throwable, "断开连接,超过最大重连次数");
        handleError(throwable);
    }

    public interface CallBack {

        /**
         * 乘梯完成
         */
        void onFinish();

        /**
         * 呼梯成功
         */
        void onCallElevatorSuccess();

        /**
         * 梯内呼梯成功
         */
        void onCallElevatorInsideSuccess();

        /**
         * 电梯到达机器人所在楼层
         */
        void onArriveCurrentFloor();

        void onEnterElevatorComplete();

        /**
         * 电梯到达目标楼层
         */
        void onArriveTargetFloor(String floor);

        /**
         * 无法处理的报错,结束任务
         *
         * @param exception
         */
        void onError(Exception exception);

        /**
         * 乘梯任务存在
         */
        void onErrorTaskExist(String elevatorId, String thingId);

        /**
         * 收到出梯消息后的报错,不影响机器任务正常执行
         *
         * @param exception
         */
        void onErrorAfterReceiveLeaveElevator(Exception exception);

        /**
         * 查询到其他机器正在使用本电梯,排队
         */
        void onQueue();

        /**
         * 电梯空闲
         */
        void onElevatorFree();
    }

}
