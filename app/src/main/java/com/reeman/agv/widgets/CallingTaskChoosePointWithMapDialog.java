package com.reeman.agv.widgets;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.reeman.agv.R;
import com.reeman.agv.adapter.PointsAdapter;

import java.util.List;

import kotlin.Pair;

public class CallingTaskChoosePointWithMapDialog extends BaseDialog  {

    public CallingTaskChoosePointWithMapDialog(@NonNull Context context, List<Pair<String,List<String>>> allPoints, Pair<String, String> selectedPoint, OnPointChooseResultListener listener) {
        super(context);
        View root = LayoutInflater.from(context).inflate(R.layout.layout_dialog_choose_point_with_map, null);
        Pair<String, List<String>> pair = allPoints.get(0);
        PointsAdapter pointsAdapter = new PointsAdapter(pair.getSecond(), selectedPoint,pair.getFirst());
        RecyclerView rvMapGroup = root.findViewById(R.id.rv_map_group);
        MapGroupAdapter mapGroupAdapter = new MapGroupAdapter(allPoints, (map, points) -> pointsAdapter.updateData(points,pointsAdapter.getSelectedPoints(),map));
        rvMapGroup.setAdapter(mapGroupAdapter);
        rvMapGroup.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false) {
            @Override
            public boolean canScrollHorizontally() {
                return true;
            }
        });
        GridView gvPoints = root.findViewById(R.id.gv_points);
        gvPoints.setNumColumns(5);
        gvPoints.setHorizontalSpacing(10);
        gvPoints.setVerticalSpacing(5);
        gvPoints.setAdapter(pointsAdapter);
        root.findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                listener.onPointChooseResult(pointsAdapter.getSelectedPoints());
            }
        });
        setContentView(root);

        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        window.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        params.width = (int) (screenWidth * 0.8);
        params.height = (int) (screenHeight * 0.8);
        window.setAttributes(params);
    }

    public interface OnPointChooseResultListener {
        void onPointChooseResult(Pair<String, String> points);
    }

     static class MapGroupAdapter extends RecyclerView.Adapter<MapGroupAdapter.TableGroupViewHolder> {

        private int index = 0;
        private final List<Pair<String, List<String>>> mapList;

        private OnMapGroupItemClickListener listener;

        public MapGroupAdapter(List<Pair<String, List<String>>> mapList, OnMapGroupItemClickListener listener) {
            this.mapList = mapList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public TableGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_table_group, parent, false);
            return new TableGroupViewHolder(root);
        }

        @Override
        public void onBindViewHolder(@NonNull TableGroupViewHolder holder, int position) {

        }

        @Override
        public void onBindViewHolder(@NonNull TableGroupViewHolder holder, int position, @NonNull List<Object> payloads) {
            Context context = holder.itemView.getContext();
            if (index == position) {
                holder.tvTableGroup.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.bg_table_group_selected, context.getTheme()));
                holder.tvTableGroup.setTextColor(Color.WHITE);
            } else {
                holder.tvTableGroup.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.bg_table_group_normal, context.getTheme()));
                holder.tvTableGroup.setTextColor(Color.parseColor("#FF666666"));
            }
            Pair<String, List<String>> pair = mapList.get(position);
            holder.tvTableGroup.setText(pair.getFirst());
            holder.tvTableGroup.setOnClickListener(v -> {
                int oldIndex = index;
                index = position;
                notifyItemChanged(oldIndex);
                notifyItemChanged(index);
                if (listener != null){
                    listener.onMapGroupItemClick(pair.getFirst(),pair.getSecond());
                }
            });
        }

        @Override
        public int getItemCount() {
            return mapList.size();
        }

         class TableGroupViewHolder extends RecyclerView.ViewHolder {
            TextView tvTableGroup;

            public TableGroupViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTableGroup = itemView.findViewById(R.id.tv_table_group);
            }
        }

         public interface OnMapGroupItemClickListener {
            void onMapGroupItemClick(String map, List<String> points);
        }
    }
}

