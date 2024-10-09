package com.reeman.agv.fragments.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.reeman.agv.R;
import com.reeman.agv.activities.LanguageSelectActivity;
import com.reeman.agv.base.BaseActivity;
import com.reeman.agv.base.BaseFragment;

public class LanguageSettingFragment extends BaseFragment implements View.OnClickListener {
    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_language_setting;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayout llSwitchLanguage = $(R.id.ll_switch_language);
        llSwitchLanguage.setOnClickListener(this);
    }

    @Override
    protected void onCustomClickResult(int id) {
        if (id == R.id.ll_switch_language) {
            BaseActivity.startup(requireContext(), LanguageSelectActivity.class);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }
}
