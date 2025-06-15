package com.example.smarthomepvl;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZGlobalSDK;
import com.videogo.openapi.EZPlayer;

import java.util.List;

public class CameraAdapter extends RecyclerView.Adapter<CameraAdapter.CameraViewHolder> {

    private List<Device> cameraList;
    private OnCameraClickListener listener;

    public interface OnCameraClickListener {
        void onCameraClick(Device camera);
    }

    public CameraAdapter(List<Device> cameraList, OnCameraClickListener listener) {
        this.cameraList = cameraList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CameraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_camera, parent, false);
        return new CameraViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CameraViewHolder holder, int position) {
        holder.isCameraReady = false;

        Device camera = cameraList.get(position);
        holder.txtCameraName.setText(camera.getTenThietBi());

        String diaChiMAC = camera.getDiaChiMAC();
        String[] parts = diaChiMAC.split(" ");

        if (parts.length == 3) {
            holder.deviceSerial = parts[0];
            try {
                holder.cameraNo = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                Log.e("CameraAdapter", "Lỗi chuyển cameraNo", e);
                holder.cameraNo = 1; // fallback
            }
            holder.verifyCode = parts[2];
        } else {
            Log.e("CameraAdapter", "Định dạng MAC không hợp lệ: " + diaChiMAC);
            holder.deviceSerial = null;
            holder.verifyCode = null;
            holder.cameraNo = 1;
        }

        // Nếu Surface đã được tạo thì bắt đầu phát
        if (holder.isSurfaceCreated) {
            holder.playLiveView();
        }
        // Sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (holder.deviceSerial != null && holder.verifyCode != null && holder.isCameraReady) {
                if (listener != null) {
                    listener.onCameraClick(camera);
                    Log.d("CameraAdapter", "isCameraReady: " + holder.isCameraReady);

                    // Gọi fragment hiển thị camera
                    Fragment fragment = CameraControllerFragment.newInstance(
                            holder.deviceSerial,
                            holder.cameraNo,
                            holder.verifyCode
                    );

                    ((AppCompatActivity) v.getContext()).getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment) // Thay bằng ID layout thực tế
                            .addToBackStack(null)
                            .commit();
                }

            } else {
                Log.d("CameraAdapter", "isCameraReady: " + holder.isCameraReady);
                Toast.makeText(v.getContext(), "Camera không khả dụng", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return cameraList.size();
    }

    class CameraViewHolder extends RecyclerView.ViewHolder {
        TextView txtCameraName;
        SurfaceView mSurfaceView;
        SurfaceHolder mHolder;
        EZPlayer mEZPlayer;
        boolean isSurfaceCreated = false;
        boolean isCameraReady = false;

        String deviceSerial;
        int cameraNo;
        String verifyCode;

        public CameraViewHolder(@NonNull View itemView) {
            super(itemView);

            txtCameraName = itemView.findViewById(R.id.txtCameraName);
            mSurfaceView = itemView.findViewById(R.id.surfaceViewCamera);
            mHolder = mSurfaceView.getHolder();

            mHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    isSurfaceCreated = true;
                    playLiveView();
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    isSurfaceCreated = false;
                    if (mEZPlayer != null) {
                        mEZPlayer.stopRealPlay();
                        mEZPlayer.release();
                        mEZPlayer = null;
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
            });
        }

        private void playLiveView() {
            if (deviceSerial == null || verifyCode == null) {
                isCameraReady = false;
                return;
            }

            if (mEZPlayer != null) {
                mEZPlayer.stopRealPlay();
                mEZPlayer.release();
                mEZPlayer = null;
            }

            mEZPlayer = EZGlobalSDK.getInstance().createPlayer(deviceSerial, cameraNo);
            if (mEZPlayer == null) {
                isCameraReady = false;
                return;
            }

            mEZPlayer.setPlayVerifyCode(verifyCode);

            // Đăng ký handler để nhận sự kiện lỗi phát
            mEZPlayer.setHandler(new android.os.Handler(msg -> {
                switch (msg.what) {
                    case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_FAIL:
                        Log.e("EZPlayer", "Phát camera lỗi");
                        isCameraReady = false;
                        break;
                    case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_SUCCESS:
                        isCameraReady = true;
                        break;
                }
                return true;
            }));

            if (isSurfaceCreated) {
                mEZPlayer.setSurfaceHold(mHolder);
                mEZPlayer.startRealPlay();
            } else {
                isCameraReady = false;
            }
        }

    }
}


