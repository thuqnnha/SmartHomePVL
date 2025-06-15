package com.example.smarthomepvl;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.videogo.exception.BaseException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZGlobalSDK;
import com.videogo.openapi.EZPlayer;

public class CameraControllerFragment extends Fragment {

    private static final String ARG_DEVICE_SERIAL = "device_serial";
    private static final String ARG_CAMERA_NO = "camera_no";
    private static final String ARG_VERIFY_CODE = "verify_code";

    private String deviceSerial;
    private int cameraNo;
    private String verifyCode;
    private int ptzSpeed = 2;

    public CameraControllerFragment() {
        // Required empty public constructor
    }

    public static CameraControllerFragment newInstance(String deviceSerial, int cameraNo, String verifyCode) {
        CameraControllerFragment fragment = new CameraControllerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DEVICE_SERIAL, deviceSerial);
        args.putInt(ARG_CAMERA_NO, cameraNo);
        args.putString(ARG_VERIFY_CODE, verifyCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deviceSerial = getArguments().getString(ARG_DEVICE_SERIAL);
            cameraNo = getArguments().getInt(ARG_CAMERA_NO);
            verifyCode = getArguments().getString(ARG_VERIFY_CODE);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera_controller, container, false);
        //------------------------------------Ánh xạ layout--------------------------------------
        SurfaceView mSurfaceView = view.findViewById(R.id.surfaceView);
        RelativeLayout buttonPanel = view.findViewById(R.id.buttonPanel);
        RelativeLayout ptzPanel = view.findViewById(R.id.ptzPanel);
        //------------------------------------Ánh xạ nút--------------------------------------
        LinearLayout btnPTZControl = view.findViewById(R.id.btnPTZControl);
        ImageButton btnUp = view.findViewById(R.id.btnUp);
        ImageButton btnLeft = view.findViewById(R.id.btnLeft);
        ImageButton btnRight = view.findViewById(R.id.btnRight);
        ImageButton btnDown = view.findViewById(R.id.btnDown);

        EZPlayer mEZPlayer = EZGlobalSDK.getInstance().createPlayer(deviceSerial, cameraNo);

        if (mEZPlayer != null) {
            mEZPlayer.setPlayVerifyCode(verifyCode);

            // Đợi surfaceView sẵn sàng mới phát
            mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    mEZPlayer.setSurfaceHold(holder);
                    mEZPlayer.startRealPlay();
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    mEZPlayer.stopRealPlay();
                    mEZPlayer.release();
                }
            });
        }
        mSurfaceView.setOnClickListener(v -> {
            if (buttonPanel.getVisibility() == View.GONE) {
                buttonPanel.setVisibility(View.VISIBLE);
            } else {
                buttonPanel.setVisibility(View.GONE); // nếu muốn ấn lần 2 để ẩn lại
            }
        });
        //---------------------------------------------Nút điều hướng-------------------------------------------------
        btnPTZControl.setOnClickListener(v -> {
            if (ptzPanel.getVisibility() == View.GONE) {
                ptzPanel.setVisibility(View.VISIBLE);
            } else {
                ptzPanel.setVisibility(View.GONE); // nếu muốn ấn lần 2 để ẩn lại
            }
        });
        // TouchListener cho nút LÊN
        btnUp.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setPressed(true); // Kích hoạt hiệu ứng nhấn
                    handlePtzMovement(event, EZConstants.EZPTZCommand.EZPTZCommandUp);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setPressed(false); // Trả lại trạng thái
                    handlePtzMovement(event, EZConstants.EZPTZCommand.EZPTZCommandUp);
                    break;
            }
            return true; // vẫn trả về true để xử lý PTZ
        });
        // TouchListener cho nút XUỐNG
        btnDown.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setPressed(true); // Kích hoạt hiệu ứng nhấn
                    handlePtzMovement(event, EZConstants.EZPTZCommand.EZPTZCommandDown);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setPressed(false); // Trả lại trạng thái
                    handlePtzMovement(event, EZConstants.EZPTZCommand.EZPTZCommandDown);
                    break;
            }
            return true; // vẫn trả về true để xử lý PTZ
        });
        // TouchListener cho nút TRÁI
        btnLeft.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setPressed(true); // Kích hoạt hiệu ứng nhấn
                    handlePtzMovement(event, EZConstants.EZPTZCommand.EZPTZCommandLeft);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setPressed(false); // Trả lại trạng thái
                    handlePtzMovement(event, EZConstants.EZPTZCommand.EZPTZCommandLeft);
                    break;
            }
            return true; // vẫn trả về true để xử lý PTZ
        });
        // TouchListener cho nút PHẢI
        btnRight.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setPressed(true); // Kích hoạt hiệu ứng nhấn
                    handlePtzMovement(event, EZConstants.EZPTZCommand.EZPTZCommandRight);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setPressed(false); // Trả lại trạng thái
                    handlePtzMovement(event, EZConstants.EZPTZCommand.EZPTZCommandRight);
                    break;
            }
            return true; // vẫn trả về true để xử lý PTZ
        });
        //---------------------------------------------Nút điều hướng-------------------------------------------------

        return view;
    }
    private void handlePtzMovement(MotionEvent event, EZConstants.EZPTZCommand command) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Bắt đầu di chuyển khi nhấn nút
                controlPTZ(command, EZConstants.EZPTZAction.EZPTZActionSTART);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Dừng di chuyển khi thả nút
                controlPTZ(command, EZConstants.EZPTZAction.EZPTZActionSTOP);
                break;
        }
    }

    private void controlPTZ(EZConstants.EZPTZCommand command, EZConstants.EZPTZAction action) {
        new Thread(() -> {
            try {
                boolean result = EZGlobalSDK.getInstance().controlPTZ(
                        deviceSerial,
                        1,
                        command,
                        action,
                        ptzSpeed
                );

                Log.d("PTZ_CONTROL", "Command: " + command + " | Action: " + action + " | Speed: " + ptzSpeed + " | Result: " + result);

            } catch (Exception e) {
                e.printStackTrace();

                requireActivity().runOnUiThread(() -> {
                    String message = "Lỗi điều khiển: " + e.getMessage();

                    if (e instanceof BaseException) {
                        String msg = e.getMessage(); // ví dụ: "The PTZ rotation reaches the left limit"

                        if (msg != null) {
                            if (msg.contains("left limit")) {
                                message = "Đã đạt giới hạn quay trái";
                            } else if (msg.contains("right limit")) {
                                message = "Đã đạt giới hạn quay phải";
                            } else if (msg.contains("upper-limit") || msg.contains("up limit")) {
                                message = "Đã đạt giới hạn quay lên";
                            } else if (msg.contains("bottom limit") || msg.contains("down limit")) {
                                message = "Đã đạt giới hạn quay xuống";
                            }
                        }
                    }

                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }




}