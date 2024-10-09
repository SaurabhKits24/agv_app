package com.reeman.agv.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.reeman.agv.R;
import com.reeman.points.model.request.MapVO;

import java.util.List;

public class MapItemAdapter extends RecyclerView.Adapter<MapItemAdapter.ViewHolder> {
    private final List<MapVO> mapList;

    public MapItemAdapter(List<MapVO> maps) {
        this.mapList = maps;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewGroup root = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_map_item, parent, false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MapVO map = mapList.get(position);
        if (map.selected) {
            holder.root.setBackgroundColor(Color.parseColor("#cdcdcd"));
        } else {
            holder.root.setBackgroundColor(Color.WHITE);
        }
        holder.tvMapName.setText(TextUtils.isEmpty(map.alias)?map.name: map.alias);
        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                MapVO item = mapList.get(adapterPosition);
                for (int i = 0; i < mapList.size(); i++) {
                    MapVO temp = mapList.get(i);
                    if (item == temp) {
                        temp.selected = !temp.selected;
                    } else {
                        temp.selected = false;
                    }
                }
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mapList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvMapName;
        private final View root;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.root = itemView;
            tvMapName = this.itemView.findViewById(R.id.tv_spinner_item);
        }
    }
}
