package com.reeman.agv.widgets;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.reeman.agv.R;

import timber.log.Timber;


public class EasyDialog extends Dialog implements View.OnClickListener {
    private static EasyDialog dialog;
    private TextView tvContent;
    private Button btnCancel;
    private Button btnConfirm;
    private Button btnNeutral;
    private TextView tvTitle;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateTask;
    private int execCount;
    public ViewGroup root;
    private boolean isTiming;

    private boolean autoDismissEnable = true;

    public EasyDialog setAutoDismissEnable(boolean autoDismissEnable) {
        this.autoDismissEnable = autoDismissEnable;
        return dialog;
    }

    public boolean isAutoDismissEnable() {
        return autoDismissEnable;
    }

    private EasyDialog(@NonNull Context context) {
        this(context, R.layout.layout_easy_dialog);
    }

    private EasyDialog(Context context, int layoutId) {
        super(context, R.style.common_dialog_style);
        root = (ViewGroup) LayoutInflater.from(context).inflate(layoutId, null);
        setContentView(root);
        tvTitle = root.findViewById(R.id.tv_dialog_title);
        tvContent = root.findViewById(R.id.tv_content);
        btnCancel = root.findViewById(R.id.btn_cancel);
        btnConfirm = root.findViewById(R.id.btn_confirm);
        btnNeutral = root.findViewById(R.id.btn_neutral);
        btnCancel.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
        btnNeutral.setOnClickListener(this);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
        autoDismissEnable = true;
    }

    private EasyDialog(Context context, int layoutId, boolean custom) {
        super(context, R.style.common_dialog_style);
        root = (ViewGroup) LayoutInflater.from(context).inflate(layoutId, null);
        btnCancel = root.findViewById(R.id.btn_cancel);
        if (btnCancel != null) {
            btnCancel.setVisibility(View.GONE);
            btnCancel.setOnClickListener(this);
        }
        setContentView(this.root);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
        autoDismissEnable = true;
    }

    public static EasyDialog getInstance(Context context) {
        if (dialog == null) {
            dialog = new EasyDialog(context);
        }
        return dialog;
    }


    public static EasyDialog getInstance(Context context, @LayoutRes int layoutId) {
        if (dialog == null) {
            dialog = new EasyDialog(context, layoutId);
        }
        return dialog;
    }


    public static EasyDialog getInstance() {
        return dialog;
    }

    public static EasyDialog newCustomInstance(Context context, int layoutId) {
        if (dialog == null) {
            dialog = new EasyDialog(context, layoutId, true);
        }
        return dialog;
    }


    public static EasyDialog getLoadingInstance(Context context) {
        if (dialog == null) {
            dialog = new EasyDialog(context, R.layout.layout_loading_dialog_view, true);
        }
        return dialog;
    }

    public static EasyDialog getWaitingInstance(Context context) {
        if (dialog == null) {
            dialog = new EasyDialog(context, R.layout.layout_waiting_dialog, true);
        }
        return dialog;
    }

    public static EasyDialog getCancelableLoadingInstance(Context context) {
        if (dialog == null) {
            dialog = new EasyDialog(context, R.layout.layout_cancelable_loading_dialog_view, true);
        }
        return dialog;
    }

    public View getView(int id) {
        if (root != null) {
            return root.findViewById(id);
        }
        return null;
    }

    public static boolean isShow() {
        return dialog != null && dialog.isShowing();
    }

    public void warn(String positiveText, String content, OnViewClickListener onViewClickListener) {
        try {
            tvContent.setText(content);
            btnCancel.setText(R.string.text_cancel);
            btnConfirm.setText(positiveText);
            btnConfirm.setVisibility(View.VISIBLE);
            btnNeutral.setVisibility(View.GONE);
            btnCancel.setVisibility(View.INVISIBLE);
            this.onViewClickListener = onViewClickListener;
            show();
        } catch (Exception e) {
            dismiss();
        }
    }

    public void warn(String content, OnViewClickListener onViewClickListener) {
        try {
            tvContent.setText(content);
            btnCancel.setText(R.string.text_cancel);
            btnConfirm.setText(R.string.text_confirm);
            btnConfirm.setVisibility(View.VISIBLE);
            btnNeutral.setVisibility(View.GONE);
            btnCancel.setVisibility(View.INVISIBLE);
            this.onViewClickListener = onViewClickListener;
            show();
        } catch (Exception e) {
            dismiss();
        }
    }

    public void warn(String content, OnViewClickListener onViewClickListener, OnDismissListener dismissListener) {
        try {
            tvContent.setText(content);
            btnCancel.setText(R.string.text_cancel);
            btnConfirm.setText(R.string.text_confirm);
            btnConfirm.setVisibility(View.VISIBLE);
            btnNeutral.setVisibility(View.GONE);
            btnCancel.setVisibility(View.INVISIBLE);
            this.onViewClickListener = onViewClickListener;
            dialog.setOnDismissListener(dismissListener);
            show();
        } catch (Exception e) {
            dismiss();
        }
    }

    public void showInputPasswordDialog(OnViewClickListener listener) {
        try {
            Button btnConfirm = root.findViewById(R.id.btn_confirm);
            Button btnCancel = root.findViewById(R.id.btn_cancel);
            btnCancel.setVisibility(View.VISIBLE);
            btnConfirm.setOnClickListener(this);
            btnCancel.setOnClickListener(this);
            this.onViewClickListener = listener;
            show();
        } catch (Exception e) {
            dismiss();
        }
    }

    public void showInputPasswordDialog(String text, OnViewClickListener listener) {
        try {
            TextView tvDialogTitle = root.findViewById(R.id.tv_dialog_title);
            Button btnConfirm = root.findViewById(R.id.btn_confirm);
            Button btnCancel = root.findViewById(R.id.btn_cancel);
            tvDialogTitle.setText(text);
            btnCancel.setVisibility(View.VISIBLE);
            btnConfirm.setOnClickListener(this);
            btnCancel.setOnClickListener(this);
            this.onViewClickListener = listener;
            show();
        } catch (Exception e) {
            dismiss();
        }
    }

    public void showInputRouteNameDialog(OnViewClickListener listener) {
        try {
            Button btnConfirm = root.findViewById(R.id.btn_confirm);
            Button btnCancel = root.findViewById(R.id.btn_cancel);
            btnCancel.setVisibility(View.VISIBLE);
            btnConfirm.setOnClickListener(this);
            btnCancel.setOnClickListener(this);
            this.onViewClickListener = listener;
            show();
        } catch (Exception e) {
            dismiss();
        }
    }

    public void onlyCancel(String content, OnViewClickListener listener) {
        try {
            tvContent.setText(content);
            btnCancel.setText(R.string.text_cancel);
            btnConfirm.setText(R.string.text_confirm);
            btnConfirm.setVisibility(View.GONE);
            btnNeutral.setVisibility(View.GONE);
            btnCancel.setVisibility(View.VISIBLE);
            this.onViewClickListener = listener;
            show();
        } catch (Exception e) {
            dismiss();
        }
    }

    public void update(int viewId, String text) {
        TextView tvTextView = root.findViewById(viewId);
        if (tvTextView != null) tvTextView.setText(text);
    }

    public void updateLoadingMessage(String prompt) {
        if (isShowing()) {
            try {
                ((TextView) root.findViewById(R.id.tv_loading_prompt)).setText(prompt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void warnWithScheduledUpdateDetail(String content, int positiveText, int negativeText, OnViewClickListener onClickListener, OnTimeStampListener listener, int rate, int total) {
        try {
            tvContent.setText(content);
            btnCancel.setText(negativeText);
            btnConfirm.setText(positiveText);
            btnConfirm.setVisibility(View.VISIBLE);
            btnNeutral.setVisibility(View.GONE);
            btnCancel.setVisibility(View.VISIBLE);
            execCount = 0;
            isTiming = true;
            updateTask = new Runnable() {
                @Override
                public void run() {
                    if (listener == null) return;
                    listener.onTimestamp(tvTitle, tvContent, btnCancel, btnNeutral, btnConfirm, execCount);
                    if (execCount < total / rate) {
                        execCount++;
                        handler.postDelayed(updateTask, rate);
                    } else {
                        isTiming = false;
                        listener.onTimeOut(dialog);
                    }
                }
            };
            handler.postDelayed(updateTask, rate);
            this.onViewClickListener = new OnViewClickListener() {
                @Override
                public void onViewClick(Dialog dialog, int id) {
                    handler.removeCallbacks(updateTask);
                    if (id == R.id.btn_cancel) isTiming = false;
                    if (onClickListener == null) return;
                    onClickListener.onViewClick(dialog, id);
                }
            };
            show();
        } catch (Exception e) {
            e.printStackTrace();
            Timber.e("倒计时弹窗打开失败");
            dismiss();
        }
    }

    public void warnWithScheduledUpdate(String content, OnViewClickListener onClickListener, OnTimeStampListener listener, int rate, int total) {
        warnWithScheduledUpdateDetail(content, R.string.text_confirm, R.string.text_cancel, onClickListener, listener, rate, total);
    }

    public void warnError(String content) {
        warn(content, (dialog, id) -> dialog.dismiss());
    }

    public boolean isTiming() {
        return isTiming;
    }

    public void confirm(String positiveTxt, String negativeTxt, String content, OnViewClickListener onViewClickListener) {
        try {
            tvContent.setText(content);
            btnCancel.setText(negativeTxt);
            btnConfirm.setText(positiveTxt);
            btnConfirm.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
            btnNeutral.setVisibility(View.GONE);
            this.onViewClickListener = onViewClickListener;
            show();
        } catch (Exception e) {
            dismiss();
        }
    }

    public void confirm(String content, OnViewClickListener onViewClickListener) {
        Context context = getContext();
        confirm(context.getString(R.string.text_confirm), context.getString(R.string.text_cancel), content, onViewClickListener);
    }

    public void confirmCareful(String content, String positiveTxt, String negativeTxt, OnViewClickListener onViewClickListener) {
        confirm(positiveTxt, negativeTxt, content, onViewClickListener);
        btnConfirm.setBackgroundResource(R.drawable.bg_common_button_warn);
    }

    public void updateMessage(CharSequence message) {
        if (tvContent != null) {
            tvContent.setText(message);
        }
    }

    public void neutral(String positiveTxt, String neutralTxt, String negativeTxt, String content, OnViewClickListener onViewClickListener) {
        try {
            tvContent.setText(content);
            btnCancel.setText(negativeTxt);
            btnNeutral.setText(neutralTxt);
            btnConfirm.setText(positiveTxt);
            btnConfirm.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
            btnNeutral.setVisibility(View.VISIBLE);
            this.onViewClickListener = onViewClickListener;
            show();
        } catch (Exception e) {
            dismiss();
        }
    }

    public void updateNeutralVisibility(int visibility) {
        if (btnNeutral != null) btnNeutral.setVisibility(visibility);
    }

    @Override
    public void show() {
        show(true);
    }

    public void show(boolean hideBottom) {
        Window window = this.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        super.show();
        int uiOptions;
        if (hideBottom) {
            uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | 0x00004000;
        } else {
            uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        window.getDecorView().setSystemUiVisibility(uiOptions);
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    public void loading(String prompt) {
        try {
            ((TextView) root.findViewById(R.id.tv_loading_prompt)).setText(prompt);
            show(false);
        } catch (Exception e) {
            dismiss();
        }
    }

    public void waiting(String positiveTxt, String negativeTxt, String content, OnViewClickListener listener) {
        try {
            tvContent = root.findViewById(R.id.tv_content);
            btnCancel = root.findViewById(R.id.btn_cancel);
            btnCancel.setVisibility(View.VISIBLE);
            btnConfirm = root.findViewById(R.id.btn_confirm);
            btnCancel.setOnClickListener(this);
            btnConfirm.setOnClickListener(this);
            tvContent.setText(content);
            btnConfirm.setText(positiveTxt);
            btnCancel.setText(negativeTxt);
            this.onViewClickListener = listener;
            show(false);
        } catch (Exception e) {
            dismiss();
        }
    }

    public void loadingCancelable(String prompt, long timeoutMills, String cancelText, OnViewClickListener listener) {
        try {
            ((TextView) root.findViewById(R.id.tv_loading_prompt)).setText(prompt);
            handler.postDelayed(() -> {
                if (btnCancel != null) {
                    btnCancel.setText(cancelText);
                    btnCancel.setVisibility(View.VISIBLE);
                }
            }, timeoutMills);
            this.onViewClickListener = listener;
            show();
        } catch (Exception e) {
            dismiss();
        }
    }

    public void loadingCancelable(String prompt, String cancelText, OnViewClickListener listener, OnDismissListener dismissListener) {
        try {
            ((TextView) root.findViewById(R.id.tv_loading_prompt)).setText(prompt);
            if (btnCancel != null) {
                btnCancel.setText(cancelText);
                btnCancel.setVisibility(View.VISIBLE);
            }
            this.onViewClickListener = listener;
            dialog.setOnDismissListener(dismissListener);
            show();
        } catch (Exception e) {
            dismiss();
        }
    }

    @Override
    public void dismiss() {
        try {
            super.dismiss();
            dialog = null;
            handler.removeCallbacks(updateTask);
        } catch (Exception e) {
            dialog = null;
            handler.removeCallbacks(updateTask);
            e.printStackTrace();
        }
    }

    private OnViewClickListener onViewClickListener;

    @Override
    public void onClick(View v) {
        if (onViewClickListener != null) {
            onViewClickListener.onViewClick(dialog, v.getId());
        }
    }

    public interface OnViewClickListener {
        void onViewClick(Dialog dialog, @IdRes int id);
    }

    public interface OnTimeStampListener {
        void onTimestamp(TextView title, TextView content, Button cancelBtn, Button neutralBtn, Button confirmBtn, int current);

        void onTimeOut(EasyDialog dialog);
    }
}
