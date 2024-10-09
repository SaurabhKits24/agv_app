package com.reeman.agv.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.reeman.agv.R;
import com.reeman.points.model.custom.GenericPoint;
import com.reeman.points.model.custom.GenericPointsWithMap;

import java.util.List;

public class MapGroupAdapter extends RecyclerView.Adapter<MapGroupAdapter.TableGroupViewHolder> {

    private int selectedIndex = 0;
    private final List<GenericPointsWithMap> pointsWithMapList;

    public MapGroupAdapter(List<GenericPointsWithMap> pointsWithMapList, OnMapGroupItemClickListener listener) {
        this.pointsWithMapList = pointsWithMapList;
        this.onMapGroupItemClickListener = listener;
    }

    public List<GenericPoint> setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        notifyDataSetChanged();
        return pointsWithMapList.get(selectedIndex).getPointList();
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public String getSelectedMap(){return pointsWithMapList.get(selectedIndex).getAlias();}

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
        if (selectedIndex == position) {
            holder.tvTableGroup.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.bg_table_group_selected, context.getTheme()));
        } else {
            holder.tvTableGroup.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.bg_table_group_normal, context.getTheme()));
        }
        GenericPointsWithMap mapWithQRCodePoints = pointsWithMapList.get(position);
        holder.tvTableGroup.setText(mapWithQRCodePoints.getAlias());
        holder.tvTableGroup.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            setSelectedIndex(adapterPosition);
            if (onMapGroupItemClickListener != null) {
                onMapGroupItemClickListener.onMapGroupItemClick(adapterPosition,mapWithQRCodePoints.getAlias(), mapWithQRCodePoints.getPointList());
            }
        });
    }

    @Override
    public int getItemCount() {
        return pointsWithMapList.size();
    }

    static class TableGroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvTableGroup;

        public TableGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTableGroup = itemView.findViewById(R.id.tv_table_group);
        }
    }

    private final OnMapGroupItemClickListener onMapGroupItemClickListener;

    public interface OnMapGroupItemClickListener {
        void onMapGroupItemClick(int index, String mapAlias,List<GenericPoint> pointList);
    }
}
