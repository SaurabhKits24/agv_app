package com.reeman.agv.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.reeman.agv.R;
import com.reeman.dao.repository.entities.RouteWithPoints;

import java.util.List;

public class RouteListAdapter extends RecyclerView.Adapter<RouteListAdapter.ViewHolder> {


    private int currentIndex = -1;
    private List<RouteWithPoints> list;

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setList(List<RouteWithPoints> list, int currentIndex) {
        this.list = list;
        if (list != null && !list.isEmpty()) {
            this.currentIndex = currentIndex;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView root = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_route_task_mode, parent, false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView textView = (TextView) holder.itemView;
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == currentIndex) {
                    textView.setTextColor(Color.parseColor("#707070"));
                    textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_route_item_normal, 0, 0, 0);
                    currentIndex = -1;
                } else {
                    textView.setTextColor(Color.parseColor("#008EFB"));
                    textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_route_item_selected, 0, 0, 0);
                    notifyItemChanged(currentIndex);
                    currentIndex = position;
                }
            }
        });
        textView.setText(list.get(position).getRouteName());
        if (position == currentIndex) {
            textView.setTextColor(Color.parseColor("#008EFB"));
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_route_item_selected, 0, 0, 0);
        } else {
            textView.setTextColor(Color.parseColor("#707070"));
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_route_item_normal, 0, 0, 0);
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
