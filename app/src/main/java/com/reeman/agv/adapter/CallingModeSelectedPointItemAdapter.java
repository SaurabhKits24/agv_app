package com.reeman.agv.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.reeman.agv.R;

import java.util.ArrayList;
import java.util.List;

import kotlin.Pair;

public class CallingModeSelectedPointItemAdapter extends RecyclerView.Adapter<CallingModeSelectedPointItemAdapter.ViewHolder> {
    private final List<Pair<String, Pair<String, String>>> pointBoundInfoList = new ArrayList<>();
    private final OnDeleteClickListener listener;

    public CallingModeSelectedPointItemAdapter(List<Pair<String, Pair<String, String>>> pointBoundInfoList, OnDeleteClickListener listener) {
        this.pointBoundInfoList.addAll(pointBoundInfoList);
        this.listener = listener;
    }

    public void addItem(Pair<String, Pair<String, String>> item) {
        boolean isReplace = false;
        for (int i = 0; i < pointBoundInfoList.size(); i++) {
            if (pointBoundInfoList.get(i).getFirst().equals(item.getFirst())) {
                pointBoundInfoList.set(i, item);
                notifyItemChanged(i);
                isReplace = true;
                break;
            }
        }
        if (!isReplace) {
            pointBoundInfoList.add(item);
            notifyItemInserted(pointBoundInfoList.size() - 1);
        }
    }

    public void removeItem(int position) {
        pointBoundInfoList.remove(position);
        notifyItemRemoved(position);
        if (position != pointBoundInfoList.size()) {
            notifyItemRangeChanged(position, pointBoundInfoList.size() - position);
        }
    }

    public void removeAll() {
        pointBoundInfoList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewGroup root = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_bound_point, parent, false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pair<String, Pair<String, String>> pair = pointBoundInfoList.get(position);
        Pair<String, String> second = pair.getSecond();
        String point = second.getSecond();
        if (!TextUtils.isEmpty(second.getFirst())) {
            point = second.getFirst() + " - " + second.getSecond();
        }
        holder.tvButtonNum.setText(pair.getFirst());
        holder.tvPoint.setText(point);
        holder.ivBoundPointDelete.setVisibility(View.VISIBLE);
        holder.ivBoundPointDelete.setOnClickListener(v -> listener.onDeleteClick(position, pointBoundInfoList.get(position)));
    }

    @Override
    public int getItemCount() {
        return pointBoundInfoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvButtonNum;

        private final TextView tvPoint;

        private final AppCompatImageView ivBoundPointDelete;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvButtonNum = itemView.findViewById(R.id.tv_bound_first_point);
            tvPoint = itemView.findViewById(R.id.tv_bound_second_point);
            ivBoundPointDelete = itemView.findViewById(R.id.iv_bound_point_delete);
        }
    }

    public interface OnDeleteClickListener {

        void onDeleteClick(int position, Pair<String, Pair<String, String>> item);
    }


}
