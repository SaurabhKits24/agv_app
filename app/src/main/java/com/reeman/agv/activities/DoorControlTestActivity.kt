package com.reeman.agv.activities

import android.app.Dialog
import android.os.Build
import android.text.InputFilter
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import com.reeman.agv.R
import com.reeman.agv.base.BaseActivity
import com.reeman.agv.controller.DoorController
import com.reeman.agv.utils.ToastUtils
import com.reeman.agv.widgets.EasyDialog
import com.reeman.commons.provider.SerialPortProvider
import com.reeman.commons.utils.TimeUtil
import timber.log.Timber

class DoorControlTestActivity : BaseActivity(), DoorController.OnAccessControlListener {

    private lateinit var etDoorNum: AppCompatEditText
    private lateinit var tvData: TextView
    private var doorController: DoorController? = null
    override fun getLayoutRes() =
        R.layout.activity_door_control_test


    override fun initCustomView() {
        etDoorNum = `$`(R.id.et_door_num)
        tvData = `$`(R.id.tv_data)
        `$`<AppCompatButton>(R.id.btn_open_door).setOnClickListener(this)
        `$`<AppCompatButton>(R.id.btn_close_door).setOnClickListener(this)
        `$`<AppCompatButton>(R.id.btn_exit).setOnClickListener(this)
        `$`<TextView>(R.id.tv_clean).setOnClickListener(this)
        tvData.movementMethod = ScrollingMovementMethod.getInstance()
        etDoorNum.filters = arrayOf(InputFilter.LengthFilter(4),
            InputFilter { source, start, end, dest, dstart, dend ->
                if (source.isEmpty()) {
                    return@InputFilter null
                }
                try {
                    val input = Integer.parseInt(dest.toString() + source.toString())
                    if (input <= 0) {
                        return@InputFilter ""
                    }
                } catch (nfe: NumberFormatException) {
                    return@InputFilter ""
                }
                null
            }
        )
    }

    override fun onResume() {
        super.onResume()
        try {
            doorController = DoorController.createInstance()
            doorController!!.init(this, SerialPortProvider.ofDoorControl(Build.PRODUCT))
        } catch (e: Exception) {
            Timber.w(e, "打开门控串口失败")
            EasyDialog.getInstance(this)
                .warn(getString(R.string.text_open_serial_device_failed)) { dialog: Dialog, id: Int ->
                    dialog.dismiss()
                    finish()
                }
        }
    }

    override fun onPause() {
        super.onPause()
        doorController?.unInit()
    }

    override fun onCustomClickResult(id: Int) {
        super.onCustomClickResult(id)

        fun getDoorNum(): Int? {
            val doorNumStr = etDoorNum.text.toString()
            if (doorNumStr.isBlank() || doorNumStr.length != 4) {
                etDoorNum.error = getString(R.string.text_please_input_door_num)
                return null
            }
            val doorNum = doorNumStr.toIntOrNull()
            if (doorNum == null) {
                etDoorNum.setText("")
                etDoorNum.error = getString(R.string.text_please_input_door_num)
                return null
            }
            return doorNum
        }
        when (id) {
            R.id.btn_open_door -> {
                getDoorNum()?.let {
                    doorController!!.openDoor(it.toString(), false)
                    updateLogData(getString(R.string.text_open_door_test_cmd_send_success, it))
                }
            }

            R.id.btn_close_door -> {
                getDoorNum()?.let {
                    doorController!!.closeDoor(it.toString(), false)
                    updateLogData(getString(R.string.text_close_door_test_cmd_send_success, it))
                }
            }

            R.id.btn_exit -> {
                finish()
            }

            R.id.tv_clean -> {
                tvData.text = ""
                tvData.scrollTo(0, 0)

            }
        }
    }

    private fun updateLogData(newData: String) {
        val data: String = tvData.text.toString()
        tvData.text = String.format("%s\n%s %s", data, TimeUtil.formatMilliseconds(), newData)
        val offset: Int = tvData.lineCount * tvData.lineHeight
        if (offset > tvData.height) {
            tvData.scrollTo(0, offset - tvData.height + 20)
        }
    }

    override fun onOpenDoorSuccess() {
        if(doorController!!.currentState == DoorController.State.OPENED)return
        doorController!!.currentState = DoorController.State.OPENED
        runOnUiThread { updateLogData(getString(R.string.text_open_door_test_success)) }
    }

    override fun onCloseDoorSuccess(currentClosingDoor: String) {
        if(doorController!!.currentState == DoorController.State.CLOSED)return
        doorController!!.currentState = DoorController.State.CLOSED
        runOnUiThread { updateLogData(getString(R.string.text_close_door_test_success)) }
    }

    override fun onThrowable(isOpenDoor: Boolean, throwable: Throwable) {
            runOnUiThread{ToastUtils.showShortToast(getString(if(isOpenDoor)R.string.exception_open_door_failed else R.string.exception_close_door_failed))}
    }
}