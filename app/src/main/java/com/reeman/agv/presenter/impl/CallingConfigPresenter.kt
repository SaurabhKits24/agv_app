package com.reeman.agv.presenter.impl

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.reeman.agv.R
import com.reeman.agv.calling.CallingInfo
import com.reeman.agv.contract.CallingConfigContract
import com.reeman.agv.utils.PackageUtils
import com.reeman.agv.widgets.EasyDialog
import com.reeman.commons.constants.Constants
import com.reeman.commons.provider.SerialPortProvider
import com.reeman.commons.state.NavigationMode
import com.reeman.commons.state.RobotInfo
import com.reeman.commons.utils.ByteUtil
import com.reeman.commons.utils.FileMapUtils
import com.reeman.points.model.custom.GenericPoint
import com.reeman.points.model.custom.GenericPointsWithMap
import com.reeman.points.process.PointRefreshProcessor
import com.reeman.points.process.callback.RefreshPointDataCallback
import com.reeman.points.process.impl.DeliveryPointsRefreshProcessingStrategy
import com.reeman.points.process.impl.DeliveryPointsWithMapsRefreshProcessingStrategy
import com.reeman.points.process.impl.FixedDeliveryPointsRefreshProcessingStrategy
import com.reeman.points.process.impl.FixedDeliveryPointsWithMapsRefreshProcessingStrategy
import com.reeman.serialport.controller.SerialPortParser
import com.reeman.serialport.util.Parser
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.util.regex.Pattern

class CallingConfigPresenter(
    private val view: CallingConfigContract.View,
    private val context: Context
) : CallingConfigContract.Presenter {
    private var instance: SerialPortParser? = null

    private var configMode = false
    private val mPattern = Pattern.compile("AA55")

    override fun startListen() {
        val path = SerialPortProvider.ofCallModule(Build.PRODUCT)
        val file = File(path)
        val files = file.listFiles()

        if (!file.exists() || files.isNullOrEmpty()) {
            view.showNotFoundSerialDevice()
            return
        }

        val target = files.firstOrNull { it.name.startsWith("ttyUSB") } ?: throw FileNotFoundException()

        try {
            instance = SerialPortParser(
                File("/dev/${target.name}"),
                115200,
                object : SerialPortParser.OnDataResultListener {
                    private val stringBuilder = StringBuilder()
                    override fun onDataResult(bytes: ByteArray, len: Int) {
                        stringBuilder.append(ByteUtil.byteArr2HexString(bytes, len))
                        while (stringBuilder.isNotEmpty()) {
                            if (stringBuilder.length < 4) break
                            val matcher = mPattern.matcher(stringBuilder)
                            if (matcher.find()) {
                                try {
                                    val start = matcher.start()
                                    val startIndex = start + 4
                                    if (startIndex + 2 >= stringBuilder.length) break
                                    val dataSize =
                                        stringBuilder.substring(startIndex, startIndex + 2)
                                    val intSize = ByteUtil.hexStringToInt(dataSize)
                                    val dataLastIndex = startIndex + intSize * 2 + 2
                                    if (dataLastIndex + 2 > stringBuilder.length) break
                                    val dataHexSum =
                                        stringBuilder.substring(startIndex, dataLastIndex)
                                    val checkSum =
                                        stringBuilder.substring(dataLastIndex, dataLastIndex + 2)
                                    if (checkSum == Parser.checkXor(dataHexSum)) {
                                        Timber.d("calling button: $stringBuilder")
                                        val key = stringBuilder.substring(
                                            dataLastIndex - 6,
                                            dataLastIndex
                                        )
                                        if (configMode) {
                                            view.bind(key)
                                        } else {
                                            keyPress(key)
                                        }
                                        stringBuilder.delete(0, dataLastIndex + 2)
                                    } else if (matcher.find()) {
                                        Timber.w("数据解析失败1 %s", stringBuilder.toString())
                                        stringBuilder.delete(0, matcher.start())
                                    } else {
                                        Timber.w("数据解析失败2 %s", stringBuilder.toString())
                                        stringBuilder.clear()
                                    }
                                } catch (e: Exception) {
                                    Timber.w(e, "数据解析错误 %s", stringBuilder.toString())
                                    stringBuilder.clear()
                                }
                            } else {
                                Timber.w("找不到协议头 %s", stringBuilder.toString())
                                stringBuilder.clear()
                            }
                        }
                    }
                })
            instance!!.start()
            view.openSerialDeviceSuccess()
        } catch (e: Exception) {
            view.showOpenSerialDeviceFailed()
        }
    }

    override fun stopListen() {
        instance?.stop()
        instance = null
    }

    @SuppressLint("CheckResult")
    private fun keyPress(key: String) {
        Observable.create { emitter: ObservableEmitter<String> ->
            var table = FileMapUtils.get(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + PackageUtils.getAppName(
                    context
                ) + File.separator + if (RobotInfo.isElevatorMode) Constants.KEY_BUTTON_MAP_WITH_ELEVATOR_PATH else Constants.KEY_BUTTON_MAP_PATH,
                key
            )
            if (RobotInfo.isElevatorMode && !TextUtils.isEmpty(table)) {
                val (first, second) = Gson().fromJson<Pair<String, String>>(
                    table,
                    object : TypeToken<Pair<String, String>>() {}.type
                )
                table = "$first : $second"
            }
            emitter.onNext(table)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { table: String? ->
                    view.showToast(
                        context.getString(
                            if (TextUtils.isEmpty(table)) R.string.text_please_bind_key_first else R.string.text_key_pressed,
                            table
                        )
                    )
                }
            ) { throwable: Throwable ->
                Timber.w(throwable, "查找按键对应的编号失败")
                view.showToast(context.getString(R.string.text_get_key_failed, throwable.message))
            }
    }

    override fun deleteByKey(position: Int, key: String) {
        Observable.create { emitter: ObservableEmitter<Boolean> ->
            if (RobotInfo.isElevatorMode) {
                FileMapUtils.deleteByKey(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + PackageUtils.getAppName(
                        context
                    ) + File.separator + Constants.KEY_BUTTON_MAP_WITH_ELEVATOR_PATH, key
                )
                CallingInfo.callingButtonMapWithElevator.remove(key)
            } else {
                FileMapUtils.deleteByKey(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + PackageUtils.getAppName(
                        context
                    ) + File.separator + Constants.KEY_BUTTON_MAP_PATH, key
                )
                CallingInfo.callingButtonMap.remove(key)
            }
            emitter.onNext(true)
            Timber.w("删除成功")
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ o: Boolean? ->
                view.onDeleteByKeySuccess(position)
            }) { throwable: Throwable ->
                Timber.w(throwable, "删除按键失败")
                view.showToast(context.getString(R.string.text_delete_failed, throwable.message))
            }
    }

    @SuppressLint("CheckResult")
    override fun deleteAll() {
        Observable.create { emitter: ObservableEmitter<Boolean> ->
            if (RobotInfo.isElevatorMode) {
                FileMapUtils.clear(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + PackageUtils.getAppName(
                        context
                    ) + File.separator + Constants.KEY_BUTTON_MAP_WITH_ELEVATOR_PATH
                )
                CallingInfo.callingButtonMapWithElevator.clear()
            } else {
                FileMapUtils.clear(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + PackageUtils.getAppName(
                        context
                    ) + File.separator + Constants.KEY_BUTTON_MAP_PATH
                )
                CallingInfo.callingButtonMap.clear()
            }
            emitter.onNext(true)
            Timber.w("删除成功")
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ o: Boolean? ->
                view.onDeleteAllSuccess()
            }) { throwable: Throwable ->
                Timber.w(throwable, "删除按键失败")
                view.showToast(context.getString(R.string.text_delete_failed, throwable.message))
            }
    }

    private val pointRefreshProcessingStrategy =
        if (RobotInfo.isElevatorMode) {
            if (RobotInfo.navigationMode == NavigationMode.autoPathMode) {
                DeliveryPointsWithMapsRefreshProcessingStrategy()
            } else {
                FixedDeliveryPointsWithMapsRefreshProcessingStrategy()
            }
        } else {
            if (RobotInfo.navigationMode == NavigationMode.autoPathMode) {
                DeliveryPointsRefreshProcessingStrategy()
            } else {
                FixedDeliveryPointsRefreshProcessingStrategy()
            }
        }

    override fun refreshCallingModePoints(context: Context) {
        EasyDialog.getLoadingInstance(context)
            .loading(context.getString(R.string.text_refresh_calling_mode_info))
        PointRefreshProcessor(
            pointRefreshProcessingStrategy,
            object : RefreshPointDataCallback {
                override fun onPointsLoadSuccess(pointList: List<GenericPoint>) {
                    view.onCallingModePointsDataLoadSuccess(pointList.map { it.name })
                }

                override fun onPointsWithMapsLoadSuccess(pointsWithMapList: List<GenericPointsWithMap>) {
                    view.onCallingModeMapsWithPointsDataLoadSuccess(pointsWithMapList.map {
                        Pair(
                            it.alias,
                            it.pointList.map { it.name })
                    })
                }

                override fun onThrowable(throwable: Throwable) {
                    view.onDataLoadFailed(throwable)
                }
            }).process(
            RobotInfo.ROSIPAddress,
            false,
            RobotInfo.supportEnterElevatorPoint(),
            listOf(
                GenericPoint.DELIVERY,
                GenericPoint.PRODUCT,
                GenericPoint.CHARGE
            )
        )
    }

    override fun exitConfigMode() {
        Timber.v("退出配置模式")
        configMode = false
    }

    override fun enterConfigMode() {
        Timber.v("进入配置模式")
        configMode = true
    }
}