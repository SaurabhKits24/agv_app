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
import com.reeman.commons.utils.ClickRestrict;

import java.util.ArrayList;
import java.util.List;

import kotlin.Pair;

public class QRCodeCheckedPointsAdapter extends RecyclerView.Adapter<QRCodeCheckedPointsAdapter.ItemViewHolder> {
    private List<Pair<Pair<String, String>, Pair<String, String>>> qrCodePointPairList = new ArrayList<>();
    private final OnQRCodeViewClickListener listener;

    public QRCodeCheckedPointsAdapter(OnQRCodeViewClickListener listener) {
        this.listener = listener;
    }
    public void addItem(Pair<String, String> item) {
        if (qrCodePointPairList.isEmpty() || qrCodePointPairList.get(qrCodePointPairList.size() - 1).getSecond() != null) {
            qrCodePointPairList.add(new Pair<>(item, null));
            notifyItemInserted(qrCodePointPairList.size()-1);
        } else {
            int index = qrCodePointPairList.size() - 1;
            Pair<String, String> pair = qrCodePointPairList.get(index).getFirst();
            if (pair.getFirst().equals(item.getFirst()) && pair.getFirst().equals(item.getSecond())) {
                listener.onPointSameWarn();
                return;
            }
            qrCodePointPairList.set(index, new Pair<>(pair, item));
            notifyItemChanged(index);
        }
    }

    public void removeQRCodeModeItem(int position) {
        qrCodePointPairList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, qrCodePointPairList.size() - position);
    }

    public void clear() {
        this.qrCodePointPairList.clear();
        notifyDataSetChanged();
    }

    public List<Pair<Pair<String, String>, Pair<String, String>>> getPairList() {
        return qrCodePointPairList;
    }


    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_qrcode_point, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Pair<Pair<String, String>, Pair<String, String>> item = getItem(position);
        Pair<String, String> first = item.getFirst();
        if (!TextUtils.isEmpty(first.getFirst())) {
            holder.tvStartLocation.setText(String.format("%s - %s", first.getFirst(), first.getSecond()));
        } else {
            holder.tvStartLocation.setText(first.getSecond());
        }
        holder.tvStartLocation.setTag(first);
        Pair<String, String> second = item.getSecond();
        if (second == null) {
            holder.tvTargetLocation.setVisibility(View.INVISIBLE);
        } else {
            holder.tvTargetLocation.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(second.getFirst())) {
                holder.tvTargetLocation.setText(String.format("%s - %s", second.getFirst(), second.getSecond()));
            } else {
                holder.tvTargetLocation.setText(second.getSecond());
            }
            holder.tvTargetLocation.setTag(second);
        }
        holder.ivQRCodePoint.setOnClickListener(view -> {
            if (ClickRestrict.restrictFrequency(500)) return;
            if (position >= 0 && position < getItemCount())
                listener.onDeleteQRCode(position);
        });
    }

    @Override
    public int getItemCount() {
        if (qrCodePointPairList == null) return 0;
        return qrCodePointPairList.size();
    }

    private Pair<Pair<String, String>, Pair<String, String>> getItem(int position) {
        return qrCodePointPairList.get(position);
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStartLocation, tvTargetLocation;
        private final AppCompatImageView ivQRCodePoint;

        public ItemViewHolder(View itemView) {
            super(itemView);
            tvStartLocation = itemView.findViewById(R.id.tv_start_location);
            tvTargetLocation = itemView.findViewById(R.id.tv_target_location);
            ivQRCodePoint = itemView.findViewById(R.id.iv_delete_qrcode_point);
        }
    }

    public interface OnQRCodeViewClickListener {
        /**
         * 删除已选的顶升点位
         *
         * @param position
         */
        void onDeleteQRCode(int position);

        void onPointSameWarn();
    }
}
