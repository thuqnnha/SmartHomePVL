package com.example.smarthomepvl;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    public interface DeviceListener {
        void onDeviceSwitchChanged(Device device, boolean isOn);
        void onDeviceLongClick(Device device, View view);
        void onDeviceClick(Device device);
    }

    private List<Device> deviceList;
    private DeviceListener listener;


    public DeviceAdapter(List<Device> deviceList, DeviceListener listener) {
        this.deviceList = deviceList;
        this.listener = listener;
    }
    public void updateDeviceStatus(String macAddress, ChartEntry latestEntry) {
        for (int i = 0; i < deviceList.size(); i++) {
            Device device = deviceList.get(i);
            if (device.getDiaChiMAC().equals(macAddress)) {
                device.setLatestEntry(latestEntry);
                notifyItemChanged(i); // chỉ cập nhật item đó
                break;
            }
        }
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

        ChartEntry latest = device.getLatestEntry();
        if (latest != null) {
            holder.tvTimeRange.setText(latest.gettimeOn() + "\n" + latest.gettimeOff());

            String params = String.format(Locale.getDefault(), "%.2fA\n%.2fV\n%.2f°C",
                    latest.getCurrent(), latest.getVoltage(), latest.getTemperature());
            holder.tvParams.setText(params);

            String status = latest.getStatus();
            if ("OFF".equals(status)) {
                holder.btnOff.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
                holder.btnOn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F5F5F5")));
                holder.btnAuto.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F5F5F5")));
            } else if("ON".equals(status)) {
                holder.btnOn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                holder.btnOff.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F5F5F5")));
                holder.btnAuto.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F5F5F5")));
            }else if("ON/OFF".equals(status)) {
                holder.btnAuto.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
                holder.btnOn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F5F5F5")));
                holder.btnOff.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F5F5F5")));
            }
        } else {
            // nếu chưa có dữ liệu
            holder.tvParams.setText("Đang tải...");
            holder.tvTimeRange.setText("");
        }


        // btnOff click
        holder.btnOff.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), device.getTenThietBi() + ": OFF", Toast.LENGTH_SHORT).show();

        });
        // btnOn click
        holder.btnOn.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), device.getTenThietBi() + ": ON", Toast.LENGTH_SHORT).show();
        });

        // btnAuto click
        holder.btnAuto.setOnClickListener(v -> {
            boolean isVisible = holder.layoutAutoSettings.getVisibility() == View.VISIBLE;
            holder.layoutAutoSettings.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        });

        //longclick
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onDeviceLongClick(device, v);
            }
            return true;
        });

        //click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeviceClick(device);
            }
        });


    }


    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceName;
        MaterialButtonToggleGroup toggleGroup;
        LinearLayout layoutAutoSettings;
        TextView tvTimeRange, tvParams;
        MaterialButton btnOff, btnAuto, btnOn;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            toggleGroup = itemView.findViewById(R.id.toggleGroup);
            layoutAutoSettings = itemView.findViewById(R.id.layoutAutoSettings);
            btnOff = itemView.findViewById(R.id.btnOff);
            btnAuto = itemView.findViewById(R.id.btnAuto);
            btnOn = itemView.findViewById(R.id.btnOn);
            tvTimeRange = itemView.findViewById(R.id.tvTimeRange);
            tvParams = itemView.findViewById(R.id.tvParams);
        }
    }

}

