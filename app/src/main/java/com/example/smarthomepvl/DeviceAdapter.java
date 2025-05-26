package com.example.smarthomepvl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    public interface DeviceListener {
        void onDeviceSwitchChanged(Device device, boolean isOn);
    }

    private List<Device> deviceList;
    private DeviceListener listener;

    public DeviceAdapter(List<Device> deviceList, DeviceListener listener) {
        this.deviceList = deviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device device = deviceList.get(position);
        holder.tvDeviceName.setText(device.getTenThietBi());

        holder.switchDevice.setOnCheckedChangeListener(null); // reset listener tránh bị gọi lại khi tái sử dụng view
        holder.switchDevice.setChecked(false); // mặc định hoặc tùy theo trạng thái thật

        holder.switchDevice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onDeviceSwitchChanged(device, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceName;
        Switch switchDevice;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            switchDevice = itemView.findViewById(R.id.switchDevice);
        }
    }
}

