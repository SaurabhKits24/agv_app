package com.reeman.agv.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.reeman.agv.R;

import java.util.List;

public class WifiItemAdapter extends RecyclerView.Adapter<WifiItemAdapter.ViewHolder> {

    private List<ScanResult> result;

    public void setResult(List<ScanResult> result) {
        this.result = result;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_wifi_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ScanResult scanResult = result.get(position);
        holder.mWifiName.setText(scanResult.SSID);
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(result.get(position));
            }
        });
        int level = calculateSignalLevel(scanResult.level, 5);
        Context context = holder.mWifiStrength.getContext();
        int drawable = context.getResources().getIdentifier("icon_wifi_" + level, "drawable", context.getPackageName());
        holder.mWifiStrength.setImageResource(drawable);
    }

    public static int calculateSignalLevel(int rssi, int numLevels) {
        if (rssi <= -100) {
            return 0;
        } else if (rssi >= -55) {
            return numLevels - 1;
        } else {
            float inputRange = 45;
            float outputRange = (numLevels - 1);
            return (int) ((float) (rssi + 100) * outputRange / inputRange);
        }
    }

    @Override
    public int getItemCount() {
        return result == null ? 0 : result.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mWifiName;
        private final ImageView mWifiStrength;
        private final View itemView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            mWifiName = itemView.findViewById(R.id.tv_wifi_ssid);
            mWifiStrength = itemView.findViewById(R.id.iv_wifi_strength);
        }

    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(ScanResult scanResult);
    }
}
