package com.reeman.agv.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.reeman.agv.R;
import com.reeman.agv.calling.model.RemoteTaskModel;
import com.reeman.commons.state.TaskMode;
import com.reeman.commons.utils.TimeUtil;

import java.util.List;

public class RemoteTaskItemAdapter extends RecyclerView.Adapter<RemoteTaskItemAdapter.ViewHolder> {
    private Context context;
    private final List<RemoteTaskModel> remoteTaskModelList;

    public RemoteTaskItemAdapter(Context context, List<RemoteTaskModel> maps) {
        this.context = context;
        this.remoteTaskModelList = maps;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewGroup root = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_calling_point_item, parent, false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RemoteTaskModel remoteTaskModel = remoteTaskModelList.get(position);
        holder.tvPointInfo.setText(remoteTaskModel.getPoint());
        String mode = null;
        if (remoteTaskModel.getTaskMode() == TaskMode.MODE_CALLING) {
            mode = context.getString(R.string.text_mode_calling);
        } else if (remoteTaskModel.getTaskMode() == TaskMode.MODE_NORMAL) {
            mode = context.getString(R.string.text_mode_normal);
        } else if (remoteTaskModel.getTaskMode() == TaskMode.MODE_ROUTE) {
            mode = context.getString(R.string.text_mode_route);
        } else if (remoteTaskModel.getTaskMode() == TaskMode.MODE_QRCODE) {
            mode = context.getString(R.string.text_mode_qrcode);
        } else if (remoteTaskModel.getTaskMode() == TaskMode.MODE_CHARGE) {
            mode = context.getString(R.string.text_mode_charge);
        } else if (remoteTaskModel.getTaskMode() == TaskMode.MODE_START_POINT) {
            mode = context.getString(R.string.text_mode_return);
        }
        if (mode == null) {
            throw new NullPointerException("mode must not null");
        }
        holder.tvTaskMode.setText(mode);
        holder.tvFirstCallingTime.setText(TimeUtil.formatMills(remoteTaskModel.getFirstCallingTime()));
        holder.tvLastCallingTime.setText(TimeUtil.formatMills(remoteTaskModel.getLastCallingTime()));
        holder.tvCallingCount.setText(String.valueOf(remoteTaskModel.getCallingCount()));
    }

    @Override
    public int getItemCount() {
        return remoteTaskModelList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvPointInfo;

        private final TextView tvTaskMode;

        private final TextView tvFirstCallingTime;

        private final TextView tvLastCallingTime;

        private final TextView tvCallingCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPointInfo = itemView.findViewById(R.id.tv_point_info);
            tvTaskMode = itemView.findViewById(R.id.tv_task_mode);
            tvFirstCallingTime = itemView.findViewById(R.id.tv_first_calling_time);
            tvLastCallingTime = itemView.findViewById(R.id.tv_last_calling_time);
            tvCallingCount = itemView.findViewById(R.id.tv_calling_count);
        }
    }
}
