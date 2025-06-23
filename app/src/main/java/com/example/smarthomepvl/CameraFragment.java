package com.example.smarthomepvl;

import static android.widget.Toast.*;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZGlobalSDK;
import com.videogo.openapi.EZPlayer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class CameraFragment extends Fragment {
    private final String deviceSerial = "F69721360";
    private final int cameraNo = 1;
    private final String verifyCode = "RVNRNT";
    private final int ptzSpeed = 2;
    private EZPlayer mEZPlayer;
    private String recordFilePath;
    private SurfaceView mSurfaceView;
    private ImageView imgRecord,imgAudio;
    private ImageButton btnMic;
    //--------------------------------Khai báo biến cờ------------------------------------
    private boolean isRecording = false;
    private boolean isSoundOn = false;
    private boolean isTalking = false;
    private boolean isPaused = false;
    private boolean isFullScreen = false;
    LinearLayout topBar;
    HorizontalScrollView horizontalNav;
    RelativeLayout buttonPanel;
    FrameLayout cameraFrame;
    ImageButton btnFullScreen;
    WindowInsetsControllerCompat controller;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // trong Activity.onCreate hoặc Fragment.onViewCreated
        Window window = requireActivity().getWindow();
        // Tắt auto-fitsystemwindows cho toàn bộ decor
        WindowCompat.setDecorFitsSystemWindows(window, false);
        // Lấy controller để ẩn/hiện status+nav bars
        controller = WindowCompat.getInsetsController(window, window.getDecorView());

        btnFullScreen.setOnClickListener(v -> {
            setFullScreen(!isFullScreen);
        });
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera_controller, container, false);
        //------------------------------------Ánh xạ layout--------------------------------------
        mSurfaceView = view.findViewById(R.id.surfaceView);
        buttonPanel = view.findViewById(R.id.buttonPanel);
        RelativeLayout ptzPanel = view.findViewById(R.id.ptzPanel);
        //------------------------------------Ánh xạ nút--------------------------------------
        LinearLayout btnPTZControl = view.findViewById(R.id.btnPTZControl);
        ImageButton btnUp = view.findViewById(R.id.btnUp);
        ImageButton btnLeft = view.findViewById(R.id.btnLeft);
        ImageButton btnRight = view.findViewById(R.id.btnRight);
        ImageButton btnDown = view.findViewById(R.id.btnDown);
        LinearLayout btnCapture = view.findViewById(R.id.btnCapture);
        imgRecord = view.findViewById(R.id.imgRecord);
        LinearLayout btnRecord = view.findViewById(R.id.btnRecord);
        LinearLayout btnAudio = view.findViewById(R.id.btnAudio);
        imgAudio = view.findViewById(R.id.imgAudio);
        LinearLayout btnFlip = view.findViewById(R.id.btnFlip);
        LinearLayout btnSetting = view.findViewById(R.id.btnSetting);
        //------------------------------------Ánh xạ nút hỗ trợ--------------------------------------
        ImageButton btnPause = view.findViewById(R.id.btnPause);
        ImageButton btnSound = view.findViewById(R.id.btnSound);
        ImageButton btnPTZ = view.findViewById(R.id.btnPTZ);
        btnMic = view.findViewById(R.id.btnMic);
        btnFullScreen = view.findViewById(R.id.btnFullScreen);
        //---------------------------------------------------------------------------------------
        horizontalNav = view.findViewById(R.id.horizontalNav);
        topBar = view.findViewById(R.id.topBar);
        cameraFrame = view.findViewById(R.id.cameraFrame);

        //---------------------------------Cấp quyền--------------------------------------------
        requestPermission();

        mSurfaceView.setDrawingCacheEnabled(true);

        loadCamera();

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
        //---------------------------------------------Nút chức năng-------------------------------------------------
        btnCapture.setOnClickListener(v -> {
            capManHinh();
        });
        btnRecord.setOnClickListener(v -> {
            quayManHinh();
        });
        btnAudio.setOnClickListener(v -> {
            amThanhHaiChieu();
        });
        btnFlip.setOnClickListener(v -> {
            latAnh();
        });

        //-------------------------------Xu li nut ho tro-------------------------------
        btnPause.setOnClickListener(v -> {
            if (mEZPlayer != null) {
                if (!isPaused) {
                    isPaused = true;
                    btnPause.setImageResource(R.drawable.ic_play);
                } else {
                    isPaused = false;
                    btnPause.setImageResource(R.drawable.ic_pause);
                }
            } else {
                makeText(requireContext(), "Chưa khởi tạo player", LENGTH_SHORT).show();
            }
        });
        btnSound.setOnClickListener(v -> {
            try {
                if (!isSoundOn) {
                    mEZPlayer.openSound();
                    isSoundOn = true;
                    btnSound.setImageResource(R.drawable.ic_sound_on);
                    makeText(requireContext(), "Âm thanh đã bật", LENGTH_SHORT).show();
                } else {
                    mEZPlayer.closeSound();
                    isSoundOn = false;
                    btnSound.setImageResource(R.drawable.ic_sound_off);
                    makeText(requireContext(), "Âm thanh đã tắt", LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace(); // Ghi log khi debug
                makeText(requireContext(), "Lỗi xử lý âm thanh: " + e.getMessage(), LENGTH_LONG).show();
            }
        });
        btnPTZ.setOnClickListener(v -> {
            if (ptzPanel.getVisibility() == View.GONE) {
                ptzPanel.setVisibility(View.VISIBLE);
            } else {
                ptzPanel.setVisibility(View.GONE); // nếu muốn ấn lần 2 để ẩn lại
            }
        });
        btnMic.setOnClickListener(v -> {
            amThanhHaiChieu();
        });
//        btnFullScreen.setOnClickListener(v -> {
//            setFullScreen(!isFullScreen);
//            mainContainer.requestApplyInsets();
//        });






        return view;
    }
    private void setFullScreen(boolean enable) {
        isFullScreen = enable;

        // Toolbar
        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setVisibility(enable ? View.GONE : View.VISIBLE);
        }

        // ActionBar
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            if (enable) actionBar.hide(); else actionBar.show();
        }

        // Các thành phần khác
        int visibility = enable ? View.GONE : View.VISIBLE;
        topBar.setVisibility(visibility);
        horizontalNav.setVisibility(visibility);

        requireActivity().findViewById(R.id.navigation_view).setVisibility(visibility);
        buttonPanel.setVisibility(View.VISIBLE);

        // Cập nhật UI hệ thống
        View decorView = requireActivity().getWindow().getDecorView();
        if (enable) {
            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            controller.hide(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());
        } else {
            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            controller.show(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());
        }

        // Cập nhật kích thước và padding/margin camera
        updateCameraPaddingAndMargin(enable);
        setCameraViewSize(enable
                ? ViewGroup.LayoutParams.MATCH_PARENT
                : (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 250, getResources().getDisplayMetrics()));
    }

    private void setCameraViewSize(int height) {
        ViewGroup.LayoutParams frameParams = cameraFrame.getLayoutParams();
        frameParams.height = height;
        cameraFrame.setLayoutParams(frameParams);

        ViewGroup.LayoutParams surfaceParams = mSurfaceView.getLayoutParams();
        surfaceParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        surfaceParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        mSurfaceView.setLayoutParams(surfaceParams);
    }
    private void updateCameraPaddingAndMargin(boolean isFullScreen) {
        // Thay đổi padding của cameraFrame
        int padding = isFullScreen ? 0 : (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        cameraFrame.setPadding(padding, padding, padding, padding);

        // Thay đổi marginTop của surfaceView
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mSurfaceView.getLayoutParams();
        params.topMargin = isFullScreen ? 0 : (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        mSurfaceView.setLayoutParams(params);
    }


    private void loadCamera()
    {
        mEZPlayer = EZGlobalSDK.getInstance().createPlayer(deviceSerial, cameraNo);

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

                    makeText(requireContext(), message, LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    private void capManHinh()
    {
        Bitmap capturedBitmap = mEZPlayer.capturePicture();
        if (capturedBitmap != null) {
            saveBitmapToGallery(capturedBitmap);
        } else {
            makeText(requireContext(), "Chụp màn hình thất bại", LENGTH_SHORT).show();
        }
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
            makeText(requireContext(), "Ảnh đã lưu vào thư viện", LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();

            // Xử lý lỗi chi tiết
            if (uri != null) {
                resolver.delete(uri, null, null);
            }
            makeText(requireContext(), "Lỗi khi lưu ảnh: " + e.getMessage(), LENGTH_LONG).show();
        } finally {
            bitmap.recycle(); // Giải phóng bộ nhớ
        }
    }
    private void quayManHinh()
    {
        if (!isRecording) {
            imgRecord.setImageResource(R.drawable.ic_videocam);
            // Tạo thư mục lưu file video
            String folderPath = Environment.getExternalStorageDirectory().getPath() + "/EzvizVideos";
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            recordFilePath = folderPath + "/record_" + System.currentTimeMillis() + ".mp4";

            boolean result = mEZPlayer.startLocalRecordWithFile(recordFilePath);
            if (result) {
                isRecording = true;
                makeText(requireContext(), "Đang quay video...", LENGTH_SHORT).show();
            } else {
                makeText(requireContext(), "Không thể bắt đầu quay video.", LENGTH_SHORT).show();
            }
        } else {
            mEZPlayer.stopLocalRecord();
            isRecording = false;
            imgRecord.setImageResource(R.drawable.ic_videocam_off);
            makeText(requireContext(), "Video đã lưu tại:\n" + recordFilePath, LENGTH_LONG).show();
        }
    }
    private void amThanhHaiChieu()
    {
        if (!isTalking) {
            if (mEZPlayer.startVoiceTalk()) {
                mEZPlayer.setVoiceTalkStatus(true);
                isTalking = true;
                btnMic.setImageResource(R.drawable.ic_mic_on);
                imgAudio.setImageResource(R.drawable.ic_mic_on);
            } else {
                makeText(requireContext(), "Không thể bắt đầu voice talk", LENGTH_SHORT).show();
            }
        } else {
            mEZPlayer.stopVoiceTalk();
            isTalking = false;
            btnMic.setImageResource(R.drawable.ic_mic_off);
            imgAudio.setImageResource(R.drawable.ic_mic_off);
        }
    }
    private void latAnh()
    {
        try {
            EZGlobalSDK.getInstance().controlVideoFlip(deviceSerial, cameraNo, EZConstants.EZPTZDisplayCommand.EZPTZDisplayCommandFlip);
        } catch (Exception e) {
            e.printStackTrace();
            makeText(getContext(), "Lỗi khi lật hình ảnh: " + e.getMessage(), LENGTH_SHORT).show();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        loadCamera();
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mEZPlayer != null) {
            mEZPlayer.stopRealPlay();
            mEZPlayer.release();
            mEZPlayer = null;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 100);
        } else {
            requestPermissions(new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 100);
        }
        //
        if (requireContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1);
        }

    }




}