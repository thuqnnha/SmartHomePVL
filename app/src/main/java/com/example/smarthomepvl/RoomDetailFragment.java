package com.example.smarthomepvl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RoomDetailFragment extends Fragment {

    private static final String ARG_ROOM_NAME = "room_name";
    private String roomName;
    private LinearLayout deviceContainer;

    public static RoomDetailFragment newInstance(String roomName) {
        RoomDetailFragment fragment = new RoomDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_NAME, roomName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            roomName = getArguments().getString(ARG_ROOM_NAME);
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

        setupCameraAndControl(view, roomName);

        return view;
    }

    private void setupCameraAndControl(View rootView, String roomName) {
        // Xóa thiết bị cũ nếu có
        deviceContainer.removeAllViews();

        if ("Phòng khách".equals(roomName)) {
            addDeviceSwitch("Tivi");
            addDeviceSwitch("Đèn trần");
        } else if ("Phòng ngủ 1".equals(roomName)) {
            addDeviceSwitch("Đèn ngủ");
            addDeviceSwitch("Quạt");
        } else if ("Phòng bếp".equals(roomName)) {
            addDeviceSwitch("Máy hút mùi");
            addDeviceSwitch("Đèn bếp");
        } else {
            // Nếu phòng chưa có thiết bị, thông báo tạm
            TextView tvNoDevice = new TextView(getContext());
            tvNoDevice.setText("Chưa có thiết bị điều khiển");
            deviceContainer.addView(tvNoDevice);
        }
    }

    private void addDeviceSwitch(String deviceName) {
        Switch deviceSwitch = new Switch(getContext());
        deviceSwitch.setText(deviceName);
        deviceSwitch.setTextSize(18);
        deviceSwitch.setPadding(0, 20, 0, 20);

        // Tạm thời chưa thêm sự kiện, sẽ bổ sung sau
        deviceContainer.addView(deviceSwitch);
    }
}
