package com.reeman.agv.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.reeman.agv.R;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.utils.SpManager;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

public class QRCodePairingDialog extends BaseDialog {

    public QRCodePairingDialog(Context context, Bitmap qrCode) {
        super(context);
        View root = LayoutInflater.from(context).inflate(R.layout.layout_dialog_qrcode_pairing, null);
        ImageView ivQRCode = root.findViewById(R.id.iv_qrcode);
        ivQRCode.setImageBitmap(qrCode);
        setContentView(root);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
    }


}
