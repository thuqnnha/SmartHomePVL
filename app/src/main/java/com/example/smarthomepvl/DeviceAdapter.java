package com.example.smarthomepvl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    public interface DeviceListener {
        void onDeviceSwitchChanged(Device device, boolean isOn);
        void onDeviceLongClick(Device device, View view);
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

        holder.toggleGroup.clearChecked(); // reset tránh tái sử dụng view lỗi

        // Lắng nghe sự kiện chọn
        holder.toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnOff || checkedId == R.id.btnOn) {
                    holder.layoutAutoSettings.setVisibility(View.GONE);
                    Toast.makeText(group.getContext(), device.getTenThietBi() + ": " + (checkedId == R.id.btnOff ? "OFF" : "ON"), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Gắn click listener riêng cho nút AUTO
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

    }


    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceName;
        MaterialButtonToggleGroup toggleGroup;
        LinearLayout layoutAutoSettings;
        private MaterialButton btnAuto;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            toggleGroup = itemView.findViewById(R.id.toggleGroup);
            layoutAutoSettings = itemView.findViewById(R.id.layoutAutoSettings);
            btnAuto = itemView.findViewById(R.id.btnAuto);
        }
    }

}

