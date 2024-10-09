package com.reeman.agv.fragments.main;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentResultListener;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.view.TimePickerView;
import com.google.android.material.textfield.TextInputEditText;
import com.reeman.agv.R;
import com.reeman.agv.base.BaseFragment;
import com.reeman.agv.contract.ModeRouteContract;
import com.reeman.agv.presenter.impl.ModeRoutePresenter;
import com.reeman.agv.widgets.AutoWrapTextViewGroup;
import com.reeman.commons.constants.Constants;
import com.reeman.dao.repository.entities.PointsVO;
import com.reeman.commons.utils.SpManager;
import com.reeman.commons.utils.TimeUtil;
import com.reeman.dao.repository.entities.RouteWithPoints;
import com.reeman.agv.utils.ToastUtils;
import com.reeman.agv.widgets.EasyDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ModeRouteAttributesEditFragment extends BaseFragment implements ModeRouteContract.View {

    private ModeRoutePresenter presenter;
    private EditText etRouteName;
    private TextView tvTaskAgainInterval;

    private TextView tvNoPoint;
    private AutoWrapTextViewGroup awtvgPoints;
    private final ModeRouteAttributesEditClickListener clickListener;
    private final RouteWithPoints routeWithPoints;

    public ModeRouteAttributesEditFragment(RouteWithPoints routeWithPoints, ModeRouteAttributesEditClickListener clickListener) {
        this.routeWithPoints = routeWithPoints;
        this.clickListener = clickListener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_mode_route_attributes_edit;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter = new ModeRoutePresenter(this);
        $(R.id.tv_only_return).setOnClickListener(this);
        $(R.id.tv_return_and_save).setOnClickListener(this);
        $(R.id.iv_task_aging_interval).setOnClickListener(this);
        $(R.id.iv_edit_point).setOnClickListener(this);
        tvTaskAgainInterval = $(R.id.tv_task_again_interval);
        tvNoPoint = $(R.id.tv_no_point);
        awtvgPoints = $(R.id.awtvg_points);
        etRouteName = $(R.id.et_route_name);
        RadioGroup rgFinishAction = $(R.id.rg_finish_action);
        RadioGroup rgTaskCycleSwitch = $(R.id.rg_task_cycle_switch);
        LinearLayout layoutTaskAgainInterval = $(R.id.layout_task_again_interval);
        LinearLayout layoutTaskCycleSwitch = $(R.id.layout_task_cycle_switch);
        etRouteName.setText(routeWithPoints.getRouteName());
        etRouteName.setSelection(routeWithPoints.getRouteName().length());
        int taskFinishAction = routeWithPoints.getTaskFinishAction();
        switch (taskFinishAction) {
            case 1:
                rgFinishAction.check(R.id.rb_return_product_point);
                break;
            case 2:
                rgFinishAction.check(R.id.rb_start_route_cruising_again);
                break;
            case 3:
                rgFinishAction.check(R.id.rb_return_charge_point);
                break;
        }
        rgTaskCycleSwitch.check(routeWithPoints.isExecuteAgainSwitch() ? R.id.rb_open_task_cycle : R.id.rb_close_task_cycle);
        layoutTaskAgainInterval.setVisibility(routeWithPoints.isExecuteAgainSwitch() ? View.VISIBLE : View.GONE);
        layoutTaskCycleSwitch.setVisibility(taskFinishAction == 2 ? View.GONE : View.VISIBLE);
        if (taskFinishAction == 2) {
            layoutTaskAgainInterval.setVisibility(View.GONE);
        }
        tvTaskAgainInterval.setText(TimeUtil.formatTimeHourMinSec(routeWithPoints.getExecuteAgainTime() * 1000L));
        List<String> pointList = new ArrayList<>();
        for (PointsVO pointsVO : routeWithPoints.getPointsVOList()) {
            pointList.add(pointsVO.getPoint());
        }
        if (pointList.isEmpty()) {
            tvNoPoint.setVisibility(View.VISIBLE);
            awtvgPoints.setVisibility(View.GONE);
        } else {
            tvNoPoint.setVisibility(View.GONE);
            awtvgPoints.setVisibility(View.VISIBLE);
        }
        awtvgPoints.setData(pointList);
        rgFinishAction.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_return_product_point:
                    routeWithPoints.setTaskFinishAction(1);
                    layoutTaskCycleSwitch.setVisibility(View.VISIBLE);
                    layoutTaskAgainInterval.setVisibility(routeWithPoints.isExecuteAgainSwitch() ? View.VISIBLE : View.GONE);
                    break;
                case R.id.rb_start_route_cruising_again:
                    routeWithPoints.setTaskFinishAction(2);
                    layoutTaskCycleSwitch.setVisibility(View.GONE);
                    layoutTaskAgainInterval.setVisibility(View.GONE);
                    break;
                case R.id.rb_return_charge_point:
                    routeWithPoints.setTaskFinishAction(3);
                    layoutTaskCycleSwitch.setVisibility(View.VISIBLE);
                    layoutTaskAgainInterval.setVisibility(routeWithPoints.isExecuteAgainSwitch() ? View.VISIBLE : View.GONE);
                    break;
            }
        });
        rgTaskCycleSwitch.setOnCheckedChangeListener((group, checkedId) -> {
            routeWithPoints.setExecuteAgainSwitch(checkedId == R.id.rb_open_task_cycle);
            layoutTaskAgainInterval.setVisibility(routeWithPoints.isExecuteAgainSwitch() ? View.VISIBLE : View.GONE);
        });

        int languageType = SpManager.getInstance().getInt(Constants.KEY_LANGUAGE_TYPE, Constants.DEFAULT_LANGUAGE_TYPE);
        rgFinishAction.setOrientation(languageType == 1 ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
    }

    @Override
    protected void onCustomClickResult(int id) {
        super.onCustomClickResult(id);
        switch (id) {
            case R.id.tv_only_return:
                EasyDialog.getInstance(requireContext()).confirm(getString(R.string.text_confirm_only_exit_route_attributes_edit), (dialog, id1) -> {
                    dialog.dismiss();
                    if (id1 == R.id.btn_confirm) {
                        clickListener.onReturnClick();
                    }
                });
                break;
            case R.id.tv_return_and_save:
                String routeName = etRouteName.getText().toString();
                if (routeName.isEmpty()) {
                    etRouteName.setError(getString(R.string.text_route_name_cannot_be_null));
                    return;
                }
                if (routeWithPoints.getTaskFinishAction() != 2 && routeWithPoints.isExecuteAgainSwitch() && routeWithPoints.getExecuteAgainTime() < 1) {
                    ToastUtils.showShortToast(getString(R.string.text_task_execute_again_time_must_bigger_than_one_seconds));
                    return;
                }
                List<PointsVO> pointsVOList = routeWithPoints.getPointsVOList();
                if (pointsVOList == null || pointsVOList.isEmpty()){
                    ToastUtils.showShortToast(getString(R.string.text_please_select_task_point_first));
                    return;
                }
                routeWithPoints.setRouteName(routeName);
                EasyDialog.getLoadingInstance(requireContext()).loading(getString(R.string.text_is_saving));
                presenter.getAllRoute(robotInfo.getNavigationMode());
                break;
            case R.id.iv_task_aging_interval:
                String[] time = tvTaskAgainInterval.getText().toString().split(":");
                int hour = Integer.parseInt(time[0]);
                int minute = Integer.parseInt(time[1]);
                int second = Integer.parseInt(time[2]);
                showTimePicker(hour * 3600L + minute * 60L + second);
                break;
            case R.id.iv_edit_point:
                clickListener.onEditPoints(routeWithPoints);
                break;

        }
    }

    private void showTimePicker(long seconds) {
        Calendar date = Calendar.getInstance();
        int hours = (int) (seconds / 3600);
        date.set(Calendar.HOUR_OF_DAY, hours);
        int minutes = (int) ((seconds - hours * 3600) / 60);
        date.set(Calendar.MINUTE, minutes);
        date.set(Calendar.SECOND, (int) (seconds - hours * 3600 - minutes * 60));
        TimePickerView timePicker = new TimePickerBuilder(requireContext(),
                (date1, v) -> {
                    String cycleTime = TimeUtil.formatHourAndMinuteAndSecond(date1);
                    tvTaskAgainInterval.setText(cycleTime);
                    routeWithPoints.setExecuteAgainTime(date1.getSeconds() + date1.getMinutes() * 60 + date1.getHours() * 60 * 60);
                })
                .setSubmitText(getString(R.string.text_confirm))
                .setCancelText(getString(R.string.text_cancel))
                .setType(new boolean[]{false, false, false, true, true, true})
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
                .build();
        timePicker.show();
    }

    @Override
    public void onUpdateRouteSuccess() {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        ToastUtils.showShortToast(getString(R.string.text_save_success));
        clickListener.onReturnClick();
    }

    @Override
    public void onUpdateRouteFailed(Throwable throwable) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        EasyDialog.getInstance(requireContext()).warnError(getString(R.string.text_save_route_failed, throwable.getMessage()));
    }

    @Override
    public void onAddRouteSuccess() {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        ToastUtils.showShortToast(getString(R.string.text_save_success));
        clickListener.onReturnClick();
    }

    @Override
    public void onAddRouteFailed(Throwable throwable) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        EasyDialog.getInstance(requireContext()).warnError(getString(R.string.text_save_route_failed, throwable.getMessage()));
    }

    @Override
    public void onGetAllRouteFailed(Throwable throwable) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        EasyDialog.getInstance(requireContext()).warnError(getString(R.string.text_save_route_failed, throwable.getMessage()));
    }

    @Override
    public void onGetAllRouteSuccess(List<RouteWithPoints> routeWithPointsList) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        if (routeWithPointsList.isEmpty()) {
            if (routeWithPoints.getId() > 1) {
                routeWithPoints.setId(0);
            }
            presenter.addRoute(routeWithPoints);
            return;
        }
        for (RouteWithPoints route : routeWithPointsList) {
            if (route.getRouteName().equals(routeWithPoints.getRouteName())) {
                if (route.getId() != routeWithPoints.getId()) {
                    ToastUtils.showShortToast(getString(R.string.text_route_name_cannot_same));
                    return;
                }
            }
        }
        if (routeWithPoints.getId() == 0) {
            presenter.addRoute(routeWithPoints);
        } else {
            presenter.updateRoute(routeWithPoints);
        }
    }

    public interface ModeRouteAttributesEditClickListener {

        void onEditPoints(RouteWithPoints routeWithPoints);

        void onReturnClick();
    }
}
