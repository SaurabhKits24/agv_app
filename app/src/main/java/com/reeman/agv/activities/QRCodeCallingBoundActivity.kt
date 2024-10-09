package com.reeman.agv.activities

import android.app.Dialog
import android.view.View
import android.view.WindowManager
import android.widget.ExpandableListView
import android.widget.TextView
import com.reeman.agv.R
import com.reeman.agv.adapter.QRCodeCallingBoundAdapter
import com.reeman.agv.base.BaseActivity
import com.reeman.agv.calling.CallingInfo
import com.reeman.commons.utils.TimeUtil
import com.reeman.agv.contract.QRCodeCallingConfigContract
import com.reeman.agv.presenter.impl.QRCodeCallingConfigPresenter
import com.reeman.agv.utils.ScreenUtils
import com.reeman.agv.utils.ToastUtils
import com.reeman.agv.widgets.EasyDialog
import timber.log.Timber
import java.util.Date

class QRCodeCallingBoundActivity : BaseActivity(), QRCodeCallingConfigContract.View {

    private var tvHostname: TextView? = null
    private var tvTime: TextView? = null
    private var tvBattery: TextView? = null
    private var presenter: QRCodeCallingConfigPresenter? = null
    private var qRcodeCallingBoundAdapter: QRCodeCallingBoundAdapter? = null
    private var expandableListView: ExpandableListView? = null
    private var back: TextView? = null

    override fun getLayoutRes(): Int {
        return R.layout.activity_calling_bound
    }

    override fun initCustomView() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        tvHostname = `$`(R.id.tv_hostname)
        tvTime = `$`(R.id.tv_time)
        tvBattery = `$`(R.id.tv_battery)
        expandableListView = `$`(R.id.expand_calling_bound_list)
        back = `$`(R.id.tv_back)

        qRcodeCallingBoundAdapter = QRCodeCallingBoundAdapter(
            this,
            expandableListView!!,
            CallingInfo.callingButtonWithQRCodeModelTaskMap,
            object : QRCodeCallingBoundAdapter.OnItemClickListener {

                override fun onDeleteBoundKey(position: Int) {
                    val callingButtonWithQRCodeModelTaskMap =
                        CallingInfo.callingButtonWithQRCodeModelTaskMap.toMutableMap()
                    EasyDialog.getInstance(this@QRCodeCallingBoundActivity).confirm(
                        "${getString(R.string.text_is_delete_key)}${
                            callingButtonWithQRCodeModelTaskMap.keys.elementAt(position)
                        }"
                    ) { dialog: Dialog, id: Int ->
                        if (id == R.id.btn_confirm) {
                            presenter!!.deleteByKey(
                                callingButtonWithQRCodeModelTaskMap.keys.elementAt(position))
                            callingButtonWithQRCodeModelTaskMap.remove(callingButtonWithQRCodeModelTaskMap.keys.elementAt(position))
                            qRcodeCallingBoundAdapter!!.resetData(
                                callingButtonWithQRCodeModelTaskMap
                            )
                        }
                        dialog.dismiss()
                    }
                }

                override fun onDeletePoint(groupPosition: Int, childPosition: Int) {
                }

            })

        expandableListView!!.setIndicatorBounds(0, 50)
        expandableListView!!.setAdapter(qRcodeCallingBoundAdapter)

        back!!.setOnClickListener(this)
    }

    override fun initData() {
        presenter = QRCodeCallingConfigPresenter(this, this)
    }

    override fun onResume() {
        super.onResume()
        refreshState()
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
            R.id.tv_back -> {
                finish()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            ScreenUtils.hideBottomUIMenu(this)
        }
    }

    override fun showNotFoundSerialDevice() {
    }

    override fun showOpenSerialDeviceFailed() {
    }

    override fun bind(key: String?) {
    }

    override fun showToast(msg: String?) {
        ToastUtils.showShortToast(msg)
    }
}