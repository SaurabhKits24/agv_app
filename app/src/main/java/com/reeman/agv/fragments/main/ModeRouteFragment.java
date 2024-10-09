package com.reeman.agv.fragments.main;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.kyleduo.switchbutton.SwitchButton;
import com.reeman.agv.R;
import com.reeman.agv.base.BaseFragment;
import com.reeman.agv.calling.utils.PointCheckUtil;
import com.reeman.agv.contract.ModeRouteContract;
import com.reeman.agv.fragments.main.listener.OnGreenButtonClickListener;
import com.reeman.agv.presenter.impl.ModeRoutePresenter;
import com.reeman.agv.calling.exception.NoFindPointException;
import com.reeman.dao.repository.entities.PointsVO;
import com.reeman.dao.repository.entities.RouteWithPoints;
import com.reeman.agv.utils.VoiceHelper;
import com.reeman.agv.widgets.EasyDialog;
import com.reeman.points.model.custom.GenericPoint;
import com.reeman.points.utils.PointCacheInfo;

import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public class ModeRouteFragment extends BaseFragment implements ModeRouteContract.View, OnGreenButtonClickListener {
    private ModeRoutePresenter presenter;

    private AppCompatButton btnStart;

    private Fragment currentFragment;

    private List<RouteWithPoints> routeWithPointsList;

    private final ModeRouteClickListener modeRouteClickListener;

    private boolean isEditMode;

    public ModeRouteFragment(boolean isEditMode, ModeRouteClickListener modeRouteClickListener) {
        this.isEditMode = isEditMode;
        this.modeRouteClickListener = modeRouteClickListener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_mode_route;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter = new ModeRoutePresenter(this);
        presenter.getAllRoute(robotInfo.getNavigationMode());
        btnStart = $(R.id.btn_start);
        SwitchButton sbMode = $(R.id.sb_mode);
        sbMode.setChecked(!isEditMode);
        sbMode.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            initViewByMode(!isChecked);
            isEditMode = !isChecked;
        });
        btnStart.setOnClickListener(this);
    }

    private void initViewByMode(boolean isEditMode) {
        if (isEditMode) {
            initRouteEditMode();
            btnStart.setClickable(false);
            btnStart.setBackgroundResource(R.drawable.bg_common_button_inactive);
        } else {
            initRouteTaskMode();
            btnStart.setClickable(true);
            btnStart.setBackgroundResource(R.drawable.bg_common_button_active);
        }
    }

    private void initRouteEditMode() {
        currentFragment = new ModeRouteEditFragment(routeWithPointsList, modeRouteEditClickListener);
        getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim.custom_anim_enter, R.anim.custom_anim_exit).replace(R.id.route_fragment_view, currentFragment).commit();
    }

    private void initRouteTaskMode() {
        currentFragment = new ModeRouteTaskFragment(routeWithPointsList);
        getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim.custom_anim_enter, R.anim.custom_anim_exit).replace(R.id.route_fragment_view, currentFragment).commit();
    }

    private final ModeRouteEditFragment.ModeRouteEditClickListener modeRouteEditClickListener = new ModeRouteEditFragment.ModeRouteEditClickListener() {

        @Override
        public void onAddRouteTask(RouteWithPoints routeWithPoints) {
            modeRouteClickListener.onAddClick(routeWithPoints);
        }

        @Override
        public void onEditRouteTask(RouteWithPoints routeWithPoints) {
            modeRouteClickListener.onEditClick(routeWithPoints);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
    }

    @Override
    protected void onCustomClickResult(int id) {
        if (id == R.id.btn_start) {
            onStartClick();
        }
    }

    private void onStartClick() {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        if (currentFragment != null && currentFragment instanceof ModeRouteTaskFragment) {
            int selectedRouteIndex = ((ModeRouteTaskFragment) currentFragment).getSelectedRouteIndex();
            if (selectedRouteIndex < 0 || selectedRouteIndex >= routeWithPointsList.size()) {
                EasyDialog.getInstance(requireActivity()).warnError(getString(R.string.voice_please_select_route));
                VoiceHelper.play("voice_please_select_route");
                return;
            }
            RouteWithPoints routeWithPoints = routeWithPointsList.get(selectedRouteIndex);
            List<PointsVO> pointsVOList = routeWithPoints.getPointsVOList();
            if (pointsVOList.isEmpty()) {
                EasyDialog.getInstance(requireActivity()).warnError(getString(R.string.voice_please_choose_route_point_first));
                return;
            }
            try {
                PointCheckUtil.INSTANCE.filterNonExistentPathPoints(pointsVOList, PointCacheInfo.INSTANCE.getPointListByType(Arrays.asList( GenericPoint.PRODUCT, GenericPoint.DELIVERY)));
                modeRouteClickListener.onStart(routeWithPoints);
            } catch (NoFindPointException e) {
                Timber.w(e, "路线模式找不到点");
                EasyDialog.getInstance(requireContext()).warnError(getString(R.string.text_filter_not_exist_points, e.getPoints().toString()));
            }
        }
    }

    @Override
    public void onGetAllRouteFailed(Throwable throwable) {
        EasyDialog.getInstance(requireContext()).warn(getString(R.string.text_get_route_failed, throwable.getMessage()), new EasyDialog.OnViewClickListener() {
            @Override
            public void onViewClick(Dialog dialog, int id) {
                dialog.dismiss();
                modeRouteClickListener.onGetRouteFailed();
            }
        });
    }

    @Override
    public void onGetAllRouteSuccess(List<RouteWithPoints> routeWithPointsList) {
        this.routeWithPointsList = routeWithPointsList;
        initViewByMode(isEditMode);
    }

    @Override
    public void onKeyUpEvent() {
        if (isEditMode) return;
        onStartClick();
    }

    public interface ModeRouteClickListener {

        void onAddClick(RouteWithPoints routeWithPoints);

        void onEditClick(RouteWithPoints routeWithPoints);

        void onStart(RouteWithPoints routeWithPoints);

        void onGetRouteFailed();
    }
}
