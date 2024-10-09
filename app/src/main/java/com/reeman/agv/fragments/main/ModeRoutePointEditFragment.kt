package com.reeman.agv.fragments.main

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.reeman.agv.R
import com.reeman.agv.adapter.RoutePointsAdapter
import com.reeman.agv.base.BaseFragment
import com.reeman.agv.utils.ToastUtils
import com.reeman.agv.widgets.CallingTaskChoosePointDialog
import com.reeman.agv.widgets.EasyDialog
import com.reeman.commons.utils.TimeUtil
import com.reeman.dao.repository.entities.PointsVO
import com.reeman.dao.repository.entities.RouteWithPoints
import com.reeman.points.model.custom.GenericPoint
import com.reeman.points.utils.PointCacheInfo
import java.util.Calendar
import java.util.Date

class ModeRoutePointEditFragment(
    val routeWithPoints: RouteWithPoints,
    private val modeRoutePointEditListener: OnModeRoutePointEditListener
) : BaseFragment(), RoutePointsAdapter.OnRoutePointsAdapterClickListener {

    private lateinit var btnAddPoint: AppCompatButton
    private lateinit var tvPointName: TextView
    private lateinit var rgWaitingTimeControlSwitch: RadioGroup
    private lateinit var tvWaitingTimeSetting: TextView
    private lateinit var tvWaitingTime: TextView
    private lateinit var ivEditWaitingTime: ImageView
    private lateinit var layoutEditPoint: ConstraintLayout
    private lateinit var tvNotChoosePointShow: TextView
    private lateinit var routePointsAdapter: RoutePointsAdapter


    override fun getLayoutRes() = R.layout.fragment_mode_route_edit_point

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rvTaskPoint = `$`<RecyclerView>(R.id.rv_task_point)
        routePointsAdapter = RoutePointsAdapter(
            requireContext(),
            routeWithPoints.pointsVOList.map { PointsVO(it) }.toMutableList(),
            this
        )
        rvTaskPoint.adapter = routePointsAdapter
        rvTaskPoint.layoutManager = LinearLayoutManager(requireContext())
        btnAddPoint = `$`(R.id.btn_add)
        layoutEditPoint = `$`(R.id.layout_edit_point)
        tvNotChoosePointShow = `$`(R.id.tv_not_choose_point_show)
        tvPointName = `$`(R.id.tv_point_name)
        rgWaitingTimeControlSwitch = `$`(R.id.rg_waiting_time_switch)
        tvWaitingTimeSetting = `$`(R.id.tv_waiting_time_setting)
        tvWaitingTime = `$`(R.id.tv_waiting_time)
        ivEditWaitingTime = `$`(R.id.iv_edit_waiting_time)
        btnAddPoint.setOnClickListener(this)
        ivEditWaitingTime.setOnClickListener(this)
        `$`<AppCompatButton>(R.id.btn_return).setOnClickListener(this)
        `$`<RadioButton>(R.id.rb_open).setOnClickListener(this)
        `$`<RadioButton>(R.id.rb_close).setOnClickListener(this)
        updateView(-1)
    }

    private fun updateView(position: Int, pointsVO: PointsVO? = null) {
        if (position == -1) {
            layoutEditPoint.visibility = View.GONE
            tvNotChoosePointShow.visibility = View.VISIBLE
        } else if (pointsVO != null) {
            layoutEditPoint.visibility = View.VISIBLE
            tvNotChoosePointShow.visibility = View.GONE
            tvPointName.text = pointsVO.point
            rgWaitingTimeControlSwitch.check(
                if (pointsVO.isOpenWaitingTime) {
                    R.id.rb_open
                } else {
                    R.id.rb_close
                }
            )
            updateWaitingTimeVisibility(
                if (pointsVO.isOpenWaitingTime) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            )
            tvWaitingTime.text = TimeUtil.formatTimeHourMinSec(pointsVO.waitingTime * 1000L)
        }
    }


    override fun onCustomClickResult(id: Int) {
        when (id) {
            R.id.btn_add -> {
                val pointList = PointCacheInfo.getPointListByType(
                    listOf(
                        GenericPoint.PRODUCT,
                        GenericPoint.DELIVERY
                    )
                ).map { it.name }
                CallingTaskChoosePointDialog(
                    requireContext(),
                    pointList.toMutableList(),
                    null
                ) { selectedPoint: Pair<String, String>? ->
                    selectedPoint?.let {
                        routePointsAdapter.addItem(PointsVO().getDefault(it.second))
                    }
                }.show()
            }

            R.id.btn_return -> {
                val oldPointsVOList = routeWithPoints.pointsVOList
                val newPointsVOList = routePointsAdapter.pointsVOList
                if (oldPointsVOList.equals(newPointsVOList)) {
                    modeRoutePointEditListener.onReturnClick(routeWithPoints)
                    return
                }
                EasyDialog.getInstance(requireContext())
                    .neutral(
                        getString(R.string.text_save_and_return),
                        getString(R.string.text_only_exit),
                        getString(R.string.text_cancel),
                        getString(R.string.text_will_not_save_change_after_confirm)
                    ) { dialog, mId ->
                        dialog.dismiss()
                        if (mId == R.id.btn_confirm) {
                            routeWithPoints.pointsVOList = routePointsAdapter.pointsVOList
                            ToastUtils.showShortToast(getString(R.string.text_save_success))
                            modeRoutePointEditListener.onReturnClick(routeWithPoints)
                        } else if (mId == R.id.btn_neutral) {
                            modeRoutePointEditListener.onReturnClick(routeWithPoints)
                        }
                    }
            }

            R.id.rb_open -> {
                routePointsAdapter.getCurrentItem()?.let {
                    if (it.isOpenWaitingTime) return
                    updateWaitingTimeVisibility(View.VISIBLE)
                    it.isOpenWaitingTime = true
                    routePointsAdapter.updateItem(it)
                }
            }

            R.id.rb_close -> {
                routePointsAdapter.getCurrentItem()?.let {
                    if (!it.isOpenWaitingTime) return
                    updateWaitingTimeVisibility(View.GONE)
                    it.isOpenWaitingTime = false
                    routePointsAdapter.updateItem(it)
                }
            }

            R.id.iv_edit_waiting_time -> {
                routePointsAdapter.getCurrentItem()?.let {
                    showTimePicker(it)
                }
            }
        }
    }

    private fun updateWaitingTimeVisibility(visibility: Int) {
        tvWaitingTimeSetting.visibility = visibility
        tvWaitingTime.visibility = visibility
        ivEditWaitingTime.visibility = visibility
    }

    private fun showTimePicker(pointsVO: PointsVO) {
        val date = Calendar.getInstance()
        val hours = (pointsVO.waitingTime / 3600)
        date[Calendar.HOUR_OF_DAY] = hours
        val minutes = ((pointsVO.waitingTime - hours * 3600) / 60)
        date[Calendar.MINUTE] = minutes
        date[Calendar.SECOND] = (pointsVO.waitingTime - hours * 3600 - minutes * 60)
        val timePicker = TimePickerBuilder(
            requireContext()
        ) { date1: Date, v: View? ->
            val newTime = date1.seconds + date1.minutes * 60 + date1.hours * 60 * 60
            if (newTime == 0) {
                ToastUtils.showShortToast(getString(R.string.text_waiting_time_cannot_be_zero))
                return@TimePickerBuilder
            }
            val cycleTime =
                TimeUtil.formatHourAndMinuteAndSecond(date1)
            tvWaitingTime.text = cycleTime
            pointsVO.waitingTime = newTime
            routePointsAdapter.updateItem(pointsVO)
        }
            .setSubmitText(getString(R.string.text_confirm))
            .setCancelText(getString(R.string.text_cancel))
            .setType(booleanArrayOf(false, false, false, true, true, true))
            .setLabel("", "", "", "h", "m", "s")
            .setTitleSize(20)
            .setDate(date)
            .isCyclic(true)
            .isDialog(true)
            .setSubCalSize(24)
            .setContentTextSize(24)
            .setItemVisibleCount(9) //若设置偶数，实际值会加1（比如设置6，则最大可见条目为7）
            .setLineSpacingMultiplier(2.0f)
            .isAlphaGradient(true)
            .setOutSideCancelable(false)
            .build()
        timePicker.show()
    }

    interface OnModeRoutePointEditListener {
        fun onReturnClick(routeWithPoints: RouteWithPoints)
    }

    override fun onDeleteClick(position: Int, pointsVO: PointsVO) {
        EasyDialog.getInstance(requireContext())
            .confirm(
                getString(
                    R.string.text_confirm_to_delete_point,
                    pointsVO.point
                )
            ) { dialog, id ->
                dialog.dismiss()
                if (id == R.id.btn_confirm) {
                    routePointsAdapter.remove(position)
                }
            }
    }

    override fun onItemClick(position: Int, pointsVO: PointsVO?) {
        updateView(position, pointsVO)
    }

}