package com.reeman.agv.activities

import android.app.Dialog
import android.content.DialogInterface
import android.os.Environment
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.reeman.agv.R
import com.reeman.agv.adapter.QRCodePointBindingAdapter
import com.reeman.agv.base.BaseActivity
import com.reeman.commons.constants.Constants
import com.reeman.commons.utils.FileMapUtils
import com.reeman.commons.utils.TimeUtil
import com.reeman.agv.contract.QRCodeCallingConfigContract
import com.reeman.agv.presenter.impl.QRCodeCallingConfigPresenter
import com.reeman.agv.utils.PackageUtils
import com.reeman.agv.utils.ScreenUtils
import com.reeman.agv.utils.ToastUtils
import com.reeman.agv.widgets.BaseDialog
import com.reeman.agv.widgets.CustomProgressDialog
import com.reeman.agv.widgets.EasyDialog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.util.Date

class QRCodeCallingConfigActivity : BaseActivity(), QRCodeCallingConfigContract.View {

    private var tvHostname: TextView? = null
    private var tvTime: TextView? = null
    private var tvBattery: TextView? = null
    private var presenter: QRCodeCallingConfigPresenter? = null
    private var progressDialog: CustomProgressDialog? = null
    private var callPoint: RecyclerView? = null
    private var qrCodePointBindingAdapter: QRCodePointBindingAdapter? = null
    private var pointList: MutableList<Pair<Pair<String, String>, Pair<String, String>>> =
        ArrayList()
    private val TAG = "QRCodeCallingConfigActivity"

    override fun getLayoutRes(): Int {
        return R.layout.activity_qrcode_calling_config
    }

    override fun initCustomView() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        pointList =
            intent.getSerializableExtra(Constants.TASK_TARGET) as MutableList<Pair<Pair<String, String>, Pair<String, String>>>

        callPoint = findViewById<RecyclerView>(R.id.gv_qrcode_call_points)

        qrCodePointBindingAdapter = QRCodePointBindingAdapter(pointList)
        callPoint!!.adapter = qrCodePointBindingAdapter
        callPoint!!.layoutManager = LinearLayoutManager(this)
        tvHostname = `$`(R.id.tv_hostname)
        tvTime = `$`(R.id.tv_time)
        tvBattery = `$`(R.id.tv_battery)
        val bind = findViewById<Button>(R.id.btn_call_bind)
        bind.setOnClickListener(this)
        val delete = findViewById<Button>(R.id.btn_delete_call_all)
        delete.setOnClickListener(this)
        delete.visibility = View.GONE
        val exit = findViewById<Button>(R.id.btn_call_exit)
        exit.setOnClickListener(this)

    }

    override fun initData() {
        presenter = QRCodeCallingConfigPresenter(this, this)
    }

    override fun onResume() {
        super.onResume()
        refreshState()
        presenter!!.startListen()
    }

    private fun refreshState() {
        tvHostname!!.text = robotInfo.ROSHostname
        tvTime!!.text = TimeUtil.formatHourAndMinute(Date())
        tvBattery!!.text = String.format("%s%%", robotInfo.powerLevel)
    }

    override fun onClick(view: View?) {
        super.onClick(view)
    }

    override fun onCustomClickResult(id: Int) {
        super.onCustomClickResult(id)
        when (id) {
            R.id.btn_call_bind -> {
                progressDialog = CustomProgressDialog(this@QRCodeCallingConfigActivity)
                progressDialog!!.setMessage(getString(R.string.text_waiting_for_key_pressed))
                progressDialog!!.setButton(
                    BaseDialog.BUTTON_NEGATIVE, getString(R.string.text_cancel)
                ) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    presenter!!.exitConfigMode()
                }
                presenter!!.enterConfigMode()
                progressDialog!!.show()
                Timber.tag(TAG)
                    .d("onCustomClickResult: %s", qrCodePointBindingAdapter!!.getPairList())
            }

            R.id.btn_delete_call_all -> {
                presenter!!.deleteAll()
            }

            R.id.btn_call_exit -> {
                exit()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            ScreenUtils.hideBottomUIMenu(this)
        }
    }

    override fun onPause() {
        super.onPause()
        presenter!!.stopListen()
        updateCallingMap()
    }

    override fun showNotFoundSerialDevice() {
        Timber.v("找不到呼叫模块串口")
        EasyDialog.getInstance(this).warn(
            getString(R.string.text_not_found_serial_device)
        ) { dialog: Dialog, id: Int ->
            dialog.dismiss()
            finish()
        }
    }

    override fun showOpenSerialDeviceFailed() {
        Timber.v("打开呼叫模块串口失败")
        EasyDialog.getInstance(this).warn(
            getString(R.string.text_open_serial_device_failed)
        ) { dialog: Dialog, id: Int ->
            dialog.dismiss()
            finish()
        }
    }

    override fun bind(key: String?) {
        Observable.create<Boolean?>(ObservableOnSubscribe<Boolean> { emitter: ObservableEmitter<Boolean> ->
            val name = Environment.getExternalStorageDirectory()
                .toString() + File.separator + PackageUtils.getAppName(this) + File.separator + Constants.KEY_BUTTON_MAP_WITH_QRCODE_TASK_PATH
            var newNum: String = gson.toJson(qrCodePointBindingAdapter!!.getPairList())
            if (TextUtils.isEmpty(FileMapUtils.get(name, key))) {
                FileMapUtils.put(name, key, newNum)
            } else {
                Timber.w("replace ")
                FileMapUtils.replace(name, key, newNum)
            }
            Timber.w("绑定呼叫按钮 : key : %s , point : %s", key, newNum)
            presenter!!.exitConfigMode()
            emitter.onNext(true)
        }).observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ aBoolean: Boolean? ->
                ToastUtils.showShortToast(getString(R.string.text_bind_success))
                if (progressDialog != null && progressDialog!!.isShowing) {
                    progressDialog!!.dismiss()
                }
            }) { throwable: Throwable ->
                Timber.w(throwable, "绑定呼叫按钮失败")
                presenter!!.exitConfigMode()
                if (progressDialog != null && progressDialog!!.isShowing) {
                    progressDialog!!.dismiss()
                }
                ToastUtils.showShortToast(getString(R.string.text_bind_failed, throwable.message))
            }
    }

    override fun showToast(msg: String?) {
        ToastUtils.showShortToast(msg)
    }


    fun exit() {
        if (EasyDialog.isShow())
            EasyDialog.getInstance().dismiss()
        this.finish()
    }
}