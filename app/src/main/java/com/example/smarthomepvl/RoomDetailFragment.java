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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthomepvl.Device;

import java.util.List;

public class RoomDetailFragment extends Fragment {

    private static final String ARG_ROOM_NAME = "room_name";
    private static final String ARG_ROOM_ID = "room_id";

    private String roomName;
    private int roomId;
    private RecyclerView recyclerDevices;


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

        recyclerDevices = view.findViewById(R.id.recyclerDevices);
        recyclerDevices.setLayoutManager(new LinearLayoutManager(getContext())); // hoặc GridLayoutManager nếu bạn muốn

        TextView tvRoomName = view.findViewById(R.id.txtRoomName);
        tvRoomName.setText(roomName);

        DatabaseHelper db = new DatabaseHelper(requireContext());

        db.loadDeviceInRoom(roomId, new DeviceCallback() {
            @Override
            public void onDevicesLoaded(List<Device> deviceList) {
                requireActivity().runOnUiThread(() -> {
                    DeviceAdapter adapter = new DeviceAdapter(deviceList, new DeviceAdapter.DeviceListener() {
                        @Override
                        public void onDeviceSwitchChanged(Device device, boolean isOn) {
                            // Xử lý bật tắt thiết bị ở đây
                            Toast.makeText(getContext(), device.getTenThietBi() + ": " + (isOn ? "Bật" : "Tắt"), Toast.LENGTH_SHORT).show();
                        }
                    });
                    recyclerDevices.setAdapter(adapter);
                });
            }


            @Override
            public void onError(String message) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show()
                );
            }
        });

        return view;
    }

}
