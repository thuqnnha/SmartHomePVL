package com.example.smarthomepvl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.videogo.constant.Constant;
import com.videogo.openapi.EZGlobalSDK;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.bean.EZAccessToken;

import java.util.ArrayList;
import java.util.List;

public class RoomDetailFragment extends Fragment {

    private static final String ARG_ROOM_NAME = "room_name";
    private static final String ARG_ROOM_ID = "room_id";

    private String roomName;
    private int roomId;

    private RecyclerView recyclerDevices,recyclerCameras;
    private TextView txtCameraTitle;
    private TextView txtDeviceTitle;

    public RoomDetailFragment() {}
    public interface DeviceCallback {
        void onDevicesLoaded(List<Device> deviceList);
        void onError(String message);
    }

    public static RoomDetailFragment newInstance(int id, String name) {
        RoomDetailFragment fragment = new RoomDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ROOM_ID, id);
        args.putString(ARG_ROOM_NAME, name);
        fragment.setArguments(args);
        return fragment;
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

        //------------------------------------Ánh xạ view--------------------------------------
        recyclerDevices = view.findViewById(R.id.recyclerDevices);
        recyclerCameras = view.findViewById(R.id.recyclerCameras);
        txtCameraTitle = view.findViewById(R.id.txtCameraTitle);
        txtDeviceTitle = view.findViewById(R.id.txtDeviceTitle);

        recyclerDevices.setLayoutManager(new LinearLayoutManager(getContext()));

        TextView tvRoomName = view.findViewById(R.id.txtRoomName);
        tvRoomName.setText(roomName);


        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //------------------------------------Ánh xạ view--------------------------------------

        loadDevicesAndCamera();
    }

    private void loadDevicesAndCamera() {
        DatabaseHelper db = new DatabaseHelper(requireContext());

        db.loadDeviceInRoom(roomId, new DeviceCallback() {
            @Override
            public void onDevicesLoaded(List<Device> deviceList) {
                requireActivity().runOnUiThread(() -> {
                    List<Device> cameraList = new ArrayList<>();
                    List<Device> otherDevices = new ArrayList<>();

                    int listCam = 0;
                    int listDevice = 0;

                    // Phân loại thiết bị
                    for (Device device : deviceList) {
                        if (device.getLoaiThietBi() == 0) {
                            cameraList.add(device);
                        } else {
                            otherDevices.add(device);
                        }
                        listCam = cameraList.size();
                        listDevice = otherDevices.size();
                    }
                    txtCameraTitle.setText("Danh sách camera (" + listCam + ")");
                    txtDeviceTitle.setText("Thiết bị trong phòng (" + listDevice + ")");

                    // Adapter camera
                    CameraAdapter cameraAdapter = new CameraAdapter(cameraList, camera -> {
                        // Mở fragment cấu hình camera
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("camera", camera);

                        CameraControllerFragment fragment = new CameraControllerFragment();
                        fragment.setArguments(bundle);

                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
//                                .replace(R.id.fragment_container, fragment)
                                .add(R.id.fragment_container, fragment)
                                .hide(RoomDetailFragment.this)
                                .addToBackStack(null)
                                .commit();
                    });
                    recyclerCameras.setAdapter(cameraAdapter);

                    // Adapter thiết bị khác
                    DeviceAdapter deviceAdapter = new DeviceAdapter(otherDevices, (device, isOn) -> {
                        Toast.makeText(getContext(), device.getTenThietBi() + ": " + (isOn ? "Bật" : "Tắt"), Toast.LENGTH_SHORT).show();
                    });
                    recyclerDevices.setAdapter(deviceAdapter);
                });
            }

            @Override
            public void onError(String message) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

}
