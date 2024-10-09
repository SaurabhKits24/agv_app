package com.reeman.agv.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.reeman.agv.R;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.utils.LocaleUtil;
import com.reeman.agv.utils.ScreenUtils;
import com.reeman.commons.utils.SpManager;
import com.reeman.agv.utils.ToastUtils;

import timber.log.Timber;

/**
 * 创建日期：2017/9/13.
 *
 * @author kevin
 */

public class FloatEditorActivity extends Activity implements View.OnClickListener {
    public static final String KEY_EDITOR_HOLDER = "editor_holder";
    public static final String KEY_EDITOR_CHECKER = "editor_checker";
    private TextView cancel;
    private TextView submit;
    private EditText etContent;
    private static EditorCallback mEditorCallback;
    private EditorHolder holder;
    private InputCheckRule checkRule;
    private boolean isClicked;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int languageType = SpManager.getInstance().getInt(Constants.KEY_LANGUAGE_TYPE, Constants.DEFAULT_LANGUAGE_TYPE);
        Timber.tag(getClass().getSimpleName()).w("语言 %d", languageType);
        LocaleUtil.changeAppLanguage(getResources(), languageType);
        holder = (EditorHolder) getIntent().getSerializableExtra(KEY_EDITOR_HOLDER);
        checkRule = (InputCheckRule) getIntent().getSerializableExtra(KEY_EDITOR_CHECKER);
        if (holder == null) {
            throw new RuntimeException("EditorHolder params not found!");
        }
        setContentView(holder.layoutResId);
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        getWindow().setBackgroundDrawableResource(R.drawable.bg_text_content);
        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        mEditorCallback.onAttached(decorView);
        initView();
        setEvent();
    }

    private void initView() {
        cancel = findViewById(holder.cancelViewId);
        submit = findViewById(holder.submitViewId);
        etContent = findViewById(holder.editTextId);
        cancel.setText(getString(R.string.text_try_listen));
        submit.setText(getString(R.string.text_confirm));
    }


    private void setEvent() {
        if (cancel != null)
            cancel.setOnClickListener(this);

        submit.setOnClickListener(this);
    }

    public static void openEditor(Context context, EditorHolder holder, EditorCallback editorCallback) {
        openEditor(context, editorCallback, holder, null);
    }

    public static void openEditor(Context context, EditorCallback editorCallback, EditorHolder holder, InputCheckRule checkRule) {
        Intent intent = new Intent(context, FloatEditorActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(KEY_EDITOR_HOLDER, holder);
        intent.putExtra(KEY_EDITOR_CHECKER, checkRule);
        mEditorCallback = editorCallback;
        context.startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == holder.cancelViewId) {
            if (TextUtils.isEmpty(etContent.getText().toString())) {
                ToastUtils.showShortToast(getString(R.string.text_input_try_listen_text_content));
                return;
            }
            mEditorCallback.onTryListen(this, etContent.getText().toString(), cancel, submit);
        } else if (id == holder.submitViewId) {
            if (TextUtils.isEmpty(etContent.getText().toString())) {
                ToastUtils.showShortToast(getString(R.string.text_input_try_listen_text_content));
                return;
            }
            /*if (checkRule != null && !(checkRule.minLength == 0 && checkRule.maxLength == 0)) {
                if (!illegal()) {
                    isClicked = true;
                    mEditorCallback.onConfirm(this, etContent.getText().toString(), cancel, submit);
                    finish();
                }

                return;
            }*/
            mEditorCallback.onConfirm(this, etContent.getText().toString(), cancel, submit);
        }
        isClicked = true;
    }

   /* private boolean illegal() {
        String content = etContent.getText().toString();
        if (TextUtils.isEmpty(content) || content.length() < checkRule.minLength) {
            Toast.makeText(this, getString(R.string.view_component_limit_min_warn, checkRule.minLength), Toast.LENGTH_SHORT).show();
            return true;
        }

        if (content.length() > checkRule.maxLength) {
            Toast.makeText(this, getString(R.string.view_component_limit_max_warn, checkRule.maxLength), Toast.LENGTH_SHORT).show();
            return true;
        }

        if (!TextUtils.isEmpty(checkRule.regxRule)) {
            Pattern pattern = Pattern.compile(checkRule.regxRule);
            Matcher matcher = pattern.matcher(content);
            if (!matcher.matches()) {
                Toast.makeText(this, getString(checkRule.regxWarn), Toast.LENGTH_SHORT).show();
                return true;
            }
        }

        return false;
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isClicked) {
            // mEditorCallback.onTryListen(cancel, submit);
        }
        if (Build.PRODUCT.startsWith("YF"))
            ScreenUtils.hideBottomUIMenu(this);
        mEditorCallback.onFinish();
        mEditorCallback = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return super.onTouchEvent(event);
    }
}
