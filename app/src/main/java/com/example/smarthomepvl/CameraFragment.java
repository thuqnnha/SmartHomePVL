package com.example.smarthomepvl;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.videogo.constant.Constant;
import com.videogo.openapi.EZGlobalSDK;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.bean.EZAccessToken;
import android.Manifest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CameraFragment extends Fragment {

    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private EZPlayer mEZPlayer;

    //private Button btnPlay, btnStop;
    private FloatingActionButton fabCapture ;
    private ImageButton btnSleep,btnRecord,btnTalk;

    private String deviceSerial = "F69721360";
    private int cameraNo = 1;
    private String verifyCode = "RVNRNT";
    private boolean isSurfaceCreated = false;

    public CameraFragment() {
        // Required empty public constructor
    }

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //------------------------------------Ánh xạ view--------------------------------------
        mSurfaceView = view.findViewById(R.id.surfaceView);
        fabCapture = view.findViewById(R.id.fabCapture);
        btnSleep = view.findViewById(R.id.btnSleep);
        btnRecord = view.findViewById(R.id.btnRecord);
        btnTalk = view.findViewById(R.id.btnTalk);
        //-------------------------------------------------------------------------------------

        //-----------------------------------Cấp quyền-----------------------------------------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 100);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 100);
        }
        //-------------------------------------------------------------------------------------

        mHolder = mSurfaceView.getHolder();

//        EZAccessToken tokenObj = EZGlobalSDK.getInstance().getEZAccessToken();
//        if (tokenObj != null) {
//            String accessToken = tokenObj.getAccessToken();
//            Log.d("HomeFragment", "AccessToken: " + accessToken);
//            EZGlobalSDK.getInstance().setAccessToken(accessToken);
//        } else {
//            Log.d("HomeFragment", "AccessToken object is null");
//        }

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
        //cap màn hình
        fabCapture.setOnClickListener(v -> {
            Bitmap capturedBitmap = mEZPlayer.capturePicture();
            if (capturedBitmap != null) {
                saveBitmapToGallery(capturedBitmap);
            } else {
                Toast.makeText(requireContext(), "Chụp màn hình thất bại", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void saveBitmapToGallery(Bitmap bitmap) {
        ContentValues values = new ContentValues();
        String fileName = "SCREENSHOT_" + System.currentTimeMillis() + ".jpg";

        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        // Xử lý đường dẫn theo version Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES+"/EZVIZ");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        } else {
            values.put(MediaStore.Images.Media.DATA,
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES+"/EZVIZ") + fileName);
        }

        ContentResolver resolver = requireActivity().getContentResolver();
        Uri uri = null;

        try {
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (uri == null) {
                throw new IOException("Failed to create MediaStore entry");
            }

            try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                if (outputStream == null) {
                    throw new IOException("Failed to open output stream");
                }

                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                    throw new IOException("Failed to compress bitmap");
                }
            }

            // Cập nhật trạng thái cho Android 10+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                resolver.update(uri, values, null, null);
            }

            // Thông báo thành công
            Toast.makeText(requireContext(), "Ảnh đã lưu vào thư viện", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();

            // Xử lý lỗi chi tiết
            if (uri != null) {
                resolver.delete(uri, null, null);
            }
            Toast.makeText(requireContext(), "Lỗi khi lưu ảnh: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            bitmap.recycle(); // Giải phóng bộ nhớ
        }
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
    }
}
