package com.example.smarthomepvl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smarthomepvl.Device;

import java.util.List;

public class RoomDetailFragment extends Fragment {

    private static final String ARG_ROOM_NAME = "room_name";
    private static final String ARG_ROOM_ID = "room_id";

    private String roomName;
    private int roomId;

    private LinearLayout deviceContainer;

    public static RoomDetailFragment newInstance(int id, String name) {
        RoomDetailFragment fragment = new RoomDetailFragment();
        Bundle args = new Bundle();
        args.putInt("room_id", id);
        args.putString("room_name", name);
        fragment.setArguments(args);
        return fragment;
    }


    public interface DeviceCallback {
        void onDevicesLoaded(List<Device> deviceList);
        void onError(String message);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            roomName = getArguments().getString(ARG_ROOM_NAME);
            roomId = getArguments().getInt(ARG_ROOM_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room_detail, container, false);

        TextView tvRoomName = view.findViewById(R.id.tvRoomName);
        tvRoomName.setText(roomName);

        deviceContainer = view.findViewById(R.id.deviceContainer);

        DatabaseHelper db = new DatabaseHelper(requireContext());

        db.loadDeviceInRoom(roomId, new DeviceCallback() {
            @Override
            public void onDevicesLoaded(List<Device> deviceList) {
                for (Device d : deviceList) {
                    addDeviceView(d);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void addDeviceView(Device device) {
        View deviceView = LayoutInflater.from(getContext()).inflate(R.layout.item_device, deviceContainer, false);

        TextView tvDeviceName = deviceView.findViewById(R.id.tvDeviceName);
        Switch switchDevice = deviceView.findViewById(R.id.switchDevice);

        tvDeviceName.setText(device.getTenThietBi());

        switchDevice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(getContext(), device.getTenThietBi() + ": " + (isChecked ? "Bật" : "Tắt"), Toast.LENGTH_SHORT).show();
            // Bạn có thể thêm xử lý thật ở đây
        });

        deviceContainer.addView(deviceView);
    }
}
