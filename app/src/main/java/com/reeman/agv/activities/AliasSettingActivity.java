package com.reeman.agv.activities;

import static com.reeman.agv.base.BaseApplication.mApp;

import android.app.Dialog;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputEditText;
import com.reeman.agv.R;
import com.reeman.agv.base.BaseActivity;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.utils.SpManager;
import com.reeman.commons.utils.StringUtils;
import com.reeman.agv.utils.ToastUtils;
import com.reeman.agv.widgets.EasyDialog;

import timber.log.Timber;

public class AliasSettingActivity extends BaseActivity {

    private TextInputEditText etAlias;

    private Button btnSkip;

    private boolean isGuide;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_alias_setting;
    }

    @Override
    protected void initCustomView() {
        isGuide = SpManager.getInstance().getBoolean(Constants.KEY_IS_ALIAS_GUIDE, false);
        etAlias = $(R.id.et_alias);
        etAlias.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (!Character.isLetterOrDigit(c) && c != '_' && c != '-' && !StringUtils.isChinese(c)) {
                    etAlias.setError(getString(R.string.text_alias_input_type));
                    return "";
                }
            }
            return null;
        }});
        if (isGuide){
            etAlias.setText(robotInfo.getRobotAlias());
        }
        btnSkip = $(R.id.btn_skip);
        btnSkip.setVisibility(isGuide ? View.GONE : View.VISIBLE);
        btnSkip.setOnClickListener(this);
        $(R.id.btn_save).setOnClickListener(this);
        $(R.id.btn_exit).setOnClickListener(this);
    }


    @Override
    protected void onCustomClickResult(int id) {
        super.onCustomClickResult(id);
        switch (id) {
            case R.id.btn_save:
                String s = etAlias.getText().toString();
                if (TextUtils.isEmpty(s)) {
                    EasyDialog.getInstance(this).confirm(getString(R.string.text_check_not_input_alias_will_use_hostname), new EasyDialog.OnViewClickListener() {
                        @Override
                        public void onViewClick(Dialog dialog, int id) {
                            dialog.dismiss();
                            if (id == R.id.btn_confirm) {
                                saveAlias(robotInfo.getROSHostname());
                            }
                        }
                    });
                    return;
                }
                if (s.length() < 5) {
                    etAlias.setError(getString(R.string.text_alias_input_type));
                    return;
                }
                saveAlias(s);
                break;
            case R.id.btn_skip:
                EasyDialog.getInstance(this).confirm(getString(R.string.text_skip_will_use_hostname), new EasyDialog.OnViewClickListener() {
                    @Override
                    public void onViewClick(Dialog dialog, int id) {
                        dialog.dismiss();
                        if (id == R.id.btn_confirm) {
                            SpManager.getInstance().edit().putBoolean(Constants.KEY_IS_ALIAS_GUIDE, true).apply();
                            BaseActivity.startupAndClearStack(AliasSettingActivity.this, MainActivity.class);
                            mApp.startCallingService();
                        }
                    }
                });
                break;
            case R.id.btn_exit:
                if (!isGuide) {
                    mApp.exit();
                    return;
                }
                finish();
                break;

        }
    }

    private void saveAlias(String alias) {
        Timber.w("设置别名为 : %s", alias);
        SpManager.getInstance().edit().putString(Constants.KEY_ROBOT_ALIAS, alias).apply();
        robotInfo.setRobotAlias(alias);
        ToastUtils.showShortToast(getString(R.string.text_set_alias, alias));
        if (!isGuide) {
            SpManager.getInstance().edit().putBoolean(Constants.KEY_IS_ALIAS_GUIDE, true).apply();
            BaseActivity.startupAndClearStack(AliasSettingActivity.this, MainActivity.class);
            mApp.startCallingService();
        }
    }
}
