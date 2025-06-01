package com.example.smarthomepvl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.videogo.constant.Constant;
import com.videogo.openapi.EZGlobalSDK;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.bean.EZAccessToken;

public class CameraFragment extends Fragment {

    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private EZPlayer mEZPlayer;

    //private Button btnPlay, btnStop;

    private String deviceSerial = "F69721360";
    private int cameraNo = 1;
    private String verifyCode = "RVNRNT";
    private boolean isSurfaceCreated = false;

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

    public CameraFragment() {
        // Required empty public constructor
    }

    public static CameraFragment newInstance() {
        return new CameraFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ view
        mSurfaceView = view.findViewById(R.id.surfaceView);

        mHolder = mSurfaceView.getHolder();

        EZAccessToken tokenObj = EZGlobalSDK.getInstance().getEZAccessToken();
        if (tokenObj != null) {
            String accessToken = tokenObj.getAccessToken();
            Log.d("HomeFragment", "AccessToken: " + accessToken);
            EZGlobalSDK.getInstance().setAccessToken(accessToken);
        } else {
            Log.d("HomeFragment", "AccessToken object is null");
        }

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
       playLiveView();
//
//        // Nút Stop
//        btnStop.setOnClickListener(v -> {
//            if (mEZPlayer != null) {
//                mEZPlayer.stopRealPlay();
//                Toast.makeText(requireContext(), "Đã dừng phát", Toast.LENGTH_SHORT).show();
//            }
//        });
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
}
