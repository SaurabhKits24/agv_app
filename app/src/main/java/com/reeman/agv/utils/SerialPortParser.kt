package com.reeman.agv.utils

import android.os.SystemClock
import com.aill.androidserialport.SerialPort
import com.reeman.serialport.BuildConfig
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class SerialPortParser(file: File, baudRate: Int, listener: OnDataResultListener) {

    private val inputStream: InputStream
    private val outputStream: OutputStream
    private val thread: Thread
    private var bytes: ByteArray
    @Volatile
    private var stopped = false
    private val serialPort: SerialPort
    private var listener: OnDataResultListener? = listener

    init {
        serialPort = SerialPort(file, baudRate, 0)
        inputStream = serialPort.inputStream
        outputStream = serialPort.outputStream
        bytes = ByteArray(1024)
        thread = Thread(ReadRunnable(), "serial-port-read-thread1")
    }

    fun start() {
        thread.start()
    }

    @Synchronized
    fun stop() {
        stopped = true
        listener = null
        try {
            inputStream.close()
            outputStream.close()
        } catch (e: IOException) {
            Timber.e(e, "Error closing streams")
        }
        serialPort.tryClose()
    }

    @Throws(IOException::class)
    fun sendCommand(bytes: ByteArray) {
        outputStream.write(bytes)
    }

    interface OnDataResultListener {
        fun onDataResult(bytes: ByteArray, len: Int)
    }

    private inner class ReadRunnable : Runnable {

        override fun run() {
            while (!stopped) {
                try {
                    if (inputStream.available() <= 0) {
                        SystemClock.sleep(10)
                        continue
                    }
                    val len: Int
                    if (inputStream.read(bytes).also { len = it } > 0) {
                        listener?.onDataResult(bytes, len)
                    }
                } catch (e: IOException) {
                    if (!stopped) {
                        e.printStackTrace()
                    }
                    break
                }
            }
            Timber.tag(BuildConfig.LOG_ROS).w("read thread finish")
            bytes = ByteArray(0)
        }
    }
}
