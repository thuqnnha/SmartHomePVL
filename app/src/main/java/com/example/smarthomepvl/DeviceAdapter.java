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
//                notifyItemChanged(i); // chỉ cập nhật item đó
                notifyItemChanged(i, "updateDataOnly");

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
            // Tối ưu so sánh TimeRange
            String newTimeRange = latest.gettimeOn() + "\n" + latest.gettimeOff();
            CharSequence currentTimeRange = holder.tvTimeRange.getText();
            if (!newTimeRange.contentEquals(currentTimeRange)) {
                holder.tvTimeRange.setText(newTimeRange);
            }

            // Tối ưu so sánh Params
            String newParams = String.format(Locale.getDefault(), "%.2fA\n%.2fV\n%.2f°C",
                    latest.getCurrent(), latest.getVoltage(), latest.getTemperature());
            CharSequence currentParams = holder.tvParams.getText();
            if (!newParams.contentEquals(currentParams)) {
                holder.tvParams.setText(newParams);
            }

            // Tối ưu cập nhật màu status
            String status = latest.getStatus();

            final ColorStateList red = ColorStateList.valueOf(Color.parseColor("#F44336"));
            final ColorStateList green = ColorStateList.valueOf(Color.parseColor("#4CAF50"));
            final ColorStateList orange = ColorStateList.valueOf(Color.parseColor("#FF9800"));
            final ColorStateList gray = ColorStateList.valueOf(Color.parseColor("#F5F5F5"));

            // Cập nhật màu từng nút theo trạng thái nếu khác
            if ("OFF".equals(status)) {
                if (!red.equals(holder.btnOff.getBackgroundTintList()))
                    holder.btnOff.setBackgroundTintList(red);
                if (!gray.equals(holder.btnOn.getBackgroundTintList()))
                    holder.btnOn.setBackgroundTintList(gray);
                if (!gray.equals(holder.btnAuto.getBackgroundTintList()))
                    holder.btnAuto.setBackgroundTintList(gray);
            } else if ("ON".equals(status)) {
                if (!green.equals(holder.btnOn.getBackgroundTintList()))
                    holder.btnOn.setBackgroundTintList(green);
                if (!gray.equals(holder.btnOff.getBackgroundTintList()))
                    holder.btnOff.setBackgroundTintList(gray);
                if (!gray.equals(holder.btnAuto.getBackgroundTintList()))
                    holder.btnAuto.setBackgroundTintList(gray);
            } else if ("ON/OFF".equals(status)) {
                if (!orange.equals(holder.btnAuto.getBackgroundTintList()))
                    holder.btnAuto.setBackgroundTintList(orange);
                if (!gray.equals(holder.btnOn.getBackgroundTintList()))
                    holder.btnOn.setBackgroundTintList(gray);
                if (!gray.equals(holder.btnOff.getBackgroundTintList()))
                    holder.btnOff.setBackgroundTintList(gray);
            }
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

