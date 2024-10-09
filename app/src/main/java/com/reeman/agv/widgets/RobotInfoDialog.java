package com.reeman.agv.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.reeman.agv.R;

public class RobotInfoDialog extends BaseDialog{


    public RobotInfoDialog(@NonNull Context context,String ROSWiFi,String ROSIP,String androidWiFi,String androidIP,String ROSVer,String powerBoardVer,String appVer,String currentMap) {
        super(context);
        View root = LayoutInflater.from(context).inflate(R.layout.layout_dialog_robot_info, null);
        TextView tvROSWiFi = root.findViewById(R.id.tv_ros_wifi);
        TextView tvROSIP = root.findViewById(R.id.tv_ros_ip);
        TextView tvAndroidWiFi = root.findViewById(R.id.tv_android_wifi);
        TextView tvAndroidIP = root.findViewById(R.id.tv_android_ip);
        TextView tvROSVer = root.findViewById(R.id.tv_ros_ver);
        TextView tvPowerBoardVer = root.findViewById(R.id.tv_power_board_ver);
        TextView tvAppVer = root.findViewById(R.id.tv_app_ver);
        TextView tvCurrentMap = root.findViewById(R.id.tv_current_map);
        tvROSWiFi.setText(context.getString(R.string.text_ros_wifi,ROSWiFi));
        tvROSIP.setText(context.getString(R.string.text_ros_ip,ROSIP));
        tvAndroidWiFi.setText(context.getString(R.string.text_android_wifi,androidWiFi));
        tvAndroidIP.setText(context.getString(R.string.text_android_ip,androidIP));
        tvROSVer.setText(context.getString(R.string.text_ros_version,ROSVer));
        tvPowerBoardVer.setText(context.getString(R.string.text_power_board_version,powerBoardVer));
        tvAppVer.setText(context.getString(R.string.text_app_ver,appVer));
        tvCurrentMap.setText(context.getString(R.string.text_current_map,currentMap));
        Button btnConfirm = root.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(view -> dismiss());
        setContentView(root);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
    }
}
