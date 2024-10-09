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

import java.util.LinkedList;
import java.util.List;

import kotlin.Pair;

public class DeliveryCheckedPointsAdapter extends RecyclerView.Adapter<DeliveryCheckedPointsAdapter.ItemViewHolder> {
    private final List<Pair<String,String>> pointList = new LinkedList<>();

    public DeliveryCheckedPointsAdapter() {
    }

    public void removeDeliveryPointItem(int position) {
        pointList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, pointList.size() - position);
    }

    public void addDeliveryPointList(Pair<String,String> deliveryPoint) {
        pointList.add(deliveryPoint);
        notifyItemInserted(pointList.size()-1);
    }

    public List<Pair<String,String>> getDeliveryPointList() {
        return pointList;
    }

    public void clear() {
        int itemCount = pointList.size();
        pointList.clear();
        notifyItemRangeRemoved(0, itemCount);
    }


    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_delivery_point, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Pair<String,String> item = getItem(position);
        if (!TextUtils.isEmpty(item.getFirst())){
            holder.tvDeliveryPoint.setText(String.format("%s - %s", item.getFirst(), item.getSecond()));
        }else {
            holder.tvDeliveryPoint.setText(item.getSecond());
        }
        holder.ivDeleteDeliveryPoint.setOnClickListener(view -> {
            if (position >= 0 && position < getDeliveryPointList().size())
                removeDeliveryPointItem(position);
        });
    }

    @Override
    public int getItemCount() {
        return pointList.size();
    }

    private Pair<String,String> getItem(int position) {
        return pointList.get(position);
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDeliveryPoint;
        private final AppCompatImageView ivDeleteDeliveryPoint;

        public ItemViewHolder(View itemView) {
            super(itemView);
            tvDeliveryPoint = itemView.findViewById(R.id.tv_delivery_point);
            ivDeleteDeliveryPoint = itemView.findViewById(R.id.iv_delete_delivery_point);
        }
    }
}