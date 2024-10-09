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

import java.util.List;

public class PageGroupAdapter extends RecyclerView.Adapter<PageGroupAdapter.TableGroupViewHolder> {

    private int selectedIndex;
    private List<Integer> pageList;

    public PageGroupAdapter(OnPageGroupItemClickListener listener) {
        this.onPageGroupItemClickListener = listener;
    }

    public void setPageList(List<Integer> pageList) {
        selectedIndex = 0;
        this.pageList = pageList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TableGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_page_item, parent, false);
        return new TableGroupViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull TableGroupViewHolder holder, int position) {

    }

    @Override
    public void onBindViewHolder(@NonNull TableGroupViewHolder holder, int position, @NonNull List<Object> payloads) {
        Context context = holder.itemView.getContext();
        if (selectedIndex == position) {
            holder.tvPage.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.bg_config_button_active, context.getTheme()));
        } else {
            holder.tvPage.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.bg_config_button_inactive, context.getTheme()));
        }
        int page = pageList.get(position);
        holder.tvPage.setText(String.valueOf(page));
        holder.tvPage.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (selectedIndex == adapterPosition)return;
            selectedIndex = adapterPosition;
            notifyDataSetChanged();
            if (onPageGroupItemClickListener != null) {
                onPageGroupItemClickListener.onMapGroupItemClick(adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pageList.size();
    }

    static class TableGroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvPage;

        public TableGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPage = itemView.findViewById(R.id.tv_page);
        }
    }

    private final OnPageGroupItemClickListener onPageGroupItemClickListener;

    public interface OnPageGroupItemClickListener {
        void onMapGroupItemClick(int index);
    }
}
