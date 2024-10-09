package com.reeman.agv.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.reeman.agv.R;
import com.reeman.agv.adapter.MapItemAdapter;
import com.reeman.points.model.request.MapVO;
import com.reeman.commons.state.RobotInfo;

import java.util.List;

public class MapChooseDialog extends BaseDialog {

    private final RecyclerView rvMapList;
    private int current = 0;

    public MapChooseDialog(@NonNull Context context, List<MapVO> mapList, boolean autoSelectCurrentMap,boolean checkChargingPile, OnMapListItemSelectedListener onMapListItemSelectedListener) {
        super(context);
        String map = RobotInfo.INSTANCE.getCurrentMapEvent().getMap();
        MapVO currentMap;
        for (int i = 0; i < mapList.size(); i++) {
            currentMap = mapList.get(i);
            if (autoSelectCurrentMap) {
                if (currentMap.name.equals(map)) {
                    currentMap.selected = true;
                    current = i;
                    break;
                }
            } else if (currentMap.selected) {
                current = i;
            }
        }
        setCanceledOnTouchOutside(true);
        View root = LayoutInflater.from(context).inflate(R.layout.layout_map_choose_dialog, null);
        rvMapList = root.findViewById(R.id.lv_map_list);
        Button btnConfirm = root.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onMapListItemSelectedListener != null) {
                    for (MapVO map : mapList) {
                        if (map.selected) {
                            onMapListItemSelectedListener.onMapListItemSelected(MapChooseDialog.this, map,checkChargingPile);
                            return;
                        }
                    }
                    onMapListItemSelectedListener.onNoMapSelected(checkChargingPile);
                }
            }
        });

        rvMapList.setLayoutManager(new LinearLayoutManager(context));
        rvMapList.setAdapter(new MapItemAdapter(mapList));

        setContentView(root);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
    }

    @Override
    public void show() {
        super.show();
        rvMapList.smoothScrollToPosition(current);
    }

    public interface OnMapListItemSelectedListener {
        void onMapListItemSelected(MapChooseDialog mapChooseDialog, MapVO mapVO,boolean checkChargingPile);

        void onNoMapSelected(boolean checkChargingPile);
    }
}
