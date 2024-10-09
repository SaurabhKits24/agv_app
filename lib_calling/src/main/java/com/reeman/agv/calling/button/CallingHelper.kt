package com.reeman.agv.calling.button

import android.os.Build
import com.reeman.agv.calling.CallingInfo
import com.reeman.agv.calling.event.CallingButtonEvent
import com.reeman.agv.calling.event.QRCodeButtonEvent
import com.reeman.agv.calling.event.UnboundButtonEvent
import com.reeman.agv.calling.model.QRCodeModeTaskModel
import com.reeman.agv.calling.utils.CallingStateManager
import com.reeman.commons.provider.SerialPortProvider
import com.reeman.commons.state.RobotInfo
import com.reeman.commons.utils.ByteUtil
import com.reeman.serialport.controller.SerialPortParser
import com.reeman.serialport.util.Parser
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.util.regex.Pattern

object CallingHelper {
    private var parser: SerialPortParser? = null
    private var start = false
    private val mPattern = Pattern.compile("AA55")

    @Throws(Exception::class)
    fun start() {
        val path = SerialPortProvider.ofCallModule(Build.PRODUCT)
        val file = File(path)
        val files = file.listFiles()
        if (!file.exists() || files.isNullOrEmpty()) {
            throw FileNotFoundException()
        }
        val target = files.firstOrNull { it.name.startsWith("ttyUSB") } ?: throw FileNotFoundException()

        parser = SerialPortParser(
            File("/dev/${target.name}"),
            115200,
            object : SerialPortParser.OnDataResultListener {
                private val sb = StringBuilder()

                override fun onDataResult(bytes: ByteArray, len: Int) {
                    sb.append(ByteUtil.byteArr2HexString(bytes, len))
                    while (sb.isNotEmpty()) {
                        if (sb.length < 4) break
                        val matcher = mPattern.matcher(sb)
                        if (matcher.find()) {
                            try {
                                val start = matcher.start()
                                val startIndex = start + 4

                                if (startIndex + 2 >= sb.length)
                                    break

                                val dataSize = sb.substring(startIndex, startIndex + 2)
                                val intSize = ByteUtil.hexStringToInt(dataSize)

                                val dataLastIndex = startIndex + intSize * 2 + 2

                                if (dataLastIndex + 2 > sb.length)
                                    break

                                val dataHexSum = sb.substring(startIndex, dataLastIndex)
                                val checkSum = sb.substring(dataLastIndex, dataLastIndex + 2)
                                if (checkSum == Parser.checkXor(dataHexSum)) {
                                    val key = sb.substring(dataLastIndex - 6, dataLastIndex)
                                    if (RobotInfo.isElevatorMode) {
                                        if (CallingInfo.callingButtonMapWithElevator.containsKey(key)) {
                                            val pair = CallingInfo.callingButtonMapWithElevator[key]
                                            Timber.w("收到呼叫,key: $key, taskPoint: $pair")
                                            pair?.let {mPair->
                                                CallingStateManager.setCallingButtonEvent(CallingButtonEvent(key,mPair.first,mPair.second))
                                            }
                                        }else{
                                            Timber.d("unbound key : $key")
                                            CallingStateManager.setUnboundButtonEvent(UnboundButtonEvent(key))
                                        }
                                    } else {
                                        if (CallingInfo.isQRCodeTaskUseCallingButton && CallingInfo.callingButtonWithQRCodeModelTaskMap.containsKey(key)) {
                                            val qrCodeModelTask = CallingInfo.callingButtonWithQRCodeModelTaskMap[key]
                                            Timber.w("收到呼叫,key: $key, taskPoint: $qrCodeModelTask")
                                            qrCodeModelTask?.let { mQRCodeModelTask->
                                                CallingStateManager.setQRCodeButtonEvent(QRCodeButtonEvent(key,mQRCodeModelTask.map { Pair(QRCodeModeTaskModel(it.first.first,it.first.second),QRCodeModeTaskModel(it.second.first,it.second.second)) }))
                                            }
                                        } else if (CallingInfo.callingButtonMap.containsKey(key)) {
                                            val point = CallingInfo.callingButtonMap[key]
                                            Timber.w("收到呼叫,key: $key, taskPoint: $point")
                                            point?.let {mPoint->
                                                CallingStateManager.setCallingButtonEvent(CallingButtonEvent(key,"",mPoint))
                                            }
                                        } else {
                                            Timber.d("unbound key : $key")
                                            CallingStateManager.setUnboundButtonEvent(UnboundButtonEvent(key))
                                        }
                                    }
                                    sb.delete(0, dataLastIndex + 2)
                                } else if (matcher.find()) {
                                    Timber.w("数据解析失败1 $sb")
                                    sb.delete(0, matcher.start())
                                } else {
                                    Timber.w("数据解析失败2 $sb")
                                    sb.delete(0, sb.length)
                                }
                            } catch (e: Exception) {
                                Timber.w(e, "数据解析错误 $sb")
                                sb.delete(0, sb.length)
                            }
                        } else {
                            Timber.w("找不到协议头 $sb")
                            sb.delete(0, sb.length)
                        }
                    }
                }
            })
        parser?.start()
        start = true
    }

    fun stop() {
        parser?.stop()
        start = false
    }

    fun isStart(): Boolean {
        return start
    }
}
