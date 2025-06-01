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

import java.util.List;

public class RoomDetailFragment extends Fragment {

    private static final String ARG_ROOM_NAME = "room_name";
    private static final String ARG_ROOM_ID = "room_id";

    private String roomName;
    private int roomId;

    private RecyclerView recyclerDevices;

    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private EZPlayer mEZPlayer;

    private boolean isSurfaceCreated = false;

    private String deviceSerial = "F69721360";
    private int cameraNo = 1;
    private String verifyCode = "Thuan2012";
    private Button btnPlay, btnStop;

    private final BroadcastReceiver oauthSuccessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            Log.d("HomeFragment", "OAUTH_SUCCESS_ACTION - Lấy AccessToken và tạo player");

            EZAccessToken tokenObj = EZGlobalSDK.getInstance().getEZAccessToken();
            if (tokenObj != null) {
                String accessToken = tokenObj.getAccessToken();
                Log.d("HomeFragment", "AccessToken: " + accessToken);
                EZGlobalSDK.getInstance().setAccessToken(accessToken);
            } else {
                Log.d("HomeFragment", "AccessToken object is null");
            }
        }
    };

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

        // Đăng ký receiver
        IntentFilter filter = new IntentFilter(Constant.OAUTH_SUCCESS_ACTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().registerReceiver(oauthSuccessReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            ContextCompat.registerReceiver(requireContext(), oauthSuccessReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        }

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
        recyclerDevices.setLayoutManager(new LinearLayoutManager(getContext()));

        TextView tvRoomName = view.findViewById(R.id.txtRoomName);
        tvRoomName.setText(roomName);

        loadDevicesAndCamera();

        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSurfaceView = view.findViewById(R.id.surfaceViewCamera);
        btnPlay = view.findViewById(R.id.btnPlay);

        EZAccessToken tokenObj = EZGlobalSDK.getInstance().getEZAccessToken();
        if (tokenObj != null) {
            String accessToken = tokenObj.getAccessToken();
            //Log.d("HomeFragment", "AccessToken: " + accessToken);
            EZGlobalSDK.getInstance().setAccessToken(accessToken);
        } else {
            //Log.d("HomeFragment", "AccessToken object is null");
        }

        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                isSurfaceCreated = true;
                if (mEZPlayer != null) {
                    mEZPlayer.setSurfaceHold(holder);
                    mEZPlayer.startRealPlay();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                isSurfaceCreated = false;
                if (mEZPlayer != null) {
                    mEZPlayer.stopRealPlay();
                    mEZPlayer.release();
                    mEZPlayer = null;
                }
            }
        });

        // Nút Play
        btnPlay.setOnClickListener(v -> {
            playLiveView();
            Toast.makeText(requireContext(), "Đang phát camera", Toast.LENGTH_SHORT).show();
        });
    }
    private void playLiveView() {
        if (mEZPlayer != null) {
            mEZPlayer.stopRealPlay();
            mEZPlayer.release();
            mEZPlayer = null;
        }

        mEZPlayer = EZGlobalSDK.getInstance().createPlayer(deviceSerial, cameraNo);

        if (mEZPlayer == null) {
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Không tạo được player", Toast.LENGTH_SHORT).show());
            return;
        }

        mEZPlayer.setPlayVerifyCode(verifyCode);

        if (isSurfaceCreated) {
            mEZPlayer.setSurfaceHold(mHolder);
            mEZPlayer.startRealPlay();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mEZPlayer != null) {
            mEZPlayer.stopRealPlay();
            mEZPlayer.release();
            mEZPlayer = null;
        }

        requireContext().unregisterReceiver(oauthSuccessReceiver);
    }

    private void loadDevicesAndCamera() {
        DatabaseHelper db = new DatabaseHelper(requireContext());

        db.loadDeviceInRoom(roomId, new DeviceCallback() {
            @Override
            public void onDevicesLoaded(List<Device> deviceList) {
                requireActivity().runOnUiThread(() -> {
                    DeviceAdapter adapter = new DeviceAdapter(deviceList, (device, isOn) -> {
                        Toast.makeText(getContext(), device.getTenThietBi() + ": " + (isOn ? "Bật" : "Tắt"), Toast.LENGTH_SHORT).show();
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
    }
}
