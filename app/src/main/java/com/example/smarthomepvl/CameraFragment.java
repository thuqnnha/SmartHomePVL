package com.example.smarthomepvl;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZGlobalSDK;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.bean.EZDeviceInfo;

import android.Manifest;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.IDN;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraFragment extends Fragment {
    //--------------------------------Khai báo biến------------------------------------
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private EZPlayer mEZPlayer;
    private FloatingActionButton fabCapture ;
    private ImageButton btnUp, btnLeft, btnRight, btnDown, btnAdd,btnEdit,btnDelete;
    private TextView tvStatus, tvDateTime;
    private Spinner spinnerCamera;
    BottomNavigationView bottomNavigationView;
    private ArrayAdapter<String> cameraAdapter;
    private List<Device> cameraList = new ArrayList<>();

    //--------------------------------Khai báo biến toàn cục------------------------------------
    private String deviceSerial;
    private int cameraNo = -1;
    private String verifyCode;
    private int ptzSpeed = 2;
    private String recordFilePath;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private int idCam = -1;
    private String tenCam;

    //--------------------------------Khai báo biến cờ------------------------------------
    private boolean isSurfaceCreated = false;
    private boolean isRecording = false;
    private boolean isSoundOn = false;
    private boolean isTalking = false;
    private boolean isSleep = false;
    //-------------------------------------------------------------------------------------

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //------------------------------------Ánh xạ view--------------------------------------
        mSurfaceView = view.findViewById(R.id.surfaceView);
        btnUp = view.findViewById(R.id.btnUp);
        btnRight = view.findViewById(R.id.btnRight);
        btnLeft = view.findViewById(R.id.btnLeft);
        btnDown = view.findViewById(R.id.btnDown);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvDateTime = view.findViewById(R.id.tvDateTime);
        spinnerCamera = view.findViewById(R.id.spinnerCameraList);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnDelete = view.findViewById(R.id.btnDelete);
        bottomNavigationView = view.findViewById(R.id.bottomNavigation);

        //-----------------------------------Cấp quyền-----------------------------------------
        requestPermission();
        //-------------------------------------------------------------------------------------
        updateDateTime();
        loadCameraList();

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

//
//        // Nút Stop
//        btnStop.setOnClickListener(v -> {
//            if (mEZPlayer != null) {
//                mEZPlayer.stopRealPlay();
//                Toast.makeText(requireContext(), "Đã dừng phát", Toast.LENGTH_SHORT).show();
//            }
//        });

        //bottom navi
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_capture) {
                capManHinh();
                return true;
            } else if (id == R.id.nav_record) {
                quayManHinh();
                return true;
            } else if (id == R.id.nav_talk) {
                amThanhHaiChieu();
                return true;
            } else if (id == R.id.nav_sleep) {
                cheDoNgu();
                return true;
            } else if (id == R.id.nav_flip) {
                latAnh();
                return true;
            }
            item.setChecked(false);

            return true;
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

        btnAdd.setOnClickListener(v -> showAddCameraDialog());

        btnEdit.setOnClickListener(v -> {
            if (idCam != -1) {
                showEditCameraDialog();
            } else {
                Toast.makeText(requireContext(),
                        "Vui lòng chọn một camera để sửa.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnDelete.setOnClickListener(v -> {
            if (idCam != -1) {
                showDeleteConfirmationDialog();
            } else {
                Toast.makeText(requireContext(),
                        "Vui lòng chọn một camera để xoá.",
                        Toast.LENGTH_SHORT).show();
            }
        });


    }
    private void capManHinh()
    {
        Bitmap capturedBitmap = mEZPlayer.capturePicture();
        if (capturedBitmap != null) {
            saveBitmapToGallery(capturedBitmap);
        } else {
            Toast.makeText(requireContext(), "Chụp màn hình thất bại", Toast.LENGTH_SHORT).show();
        }
    }
    private void quayManHinh()
    {
        if (!isRecording) {
            // Tạo thư mục lưu file video
            bottomNavigationView.getMenu().findItem(R.id.nav_record).setIcon(R.drawable.ic_videocam);
            String folderPath = Environment.getExternalStorageDirectory().getPath() + "/EzvizVideos";
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            recordFilePath = folderPath + "/record_" + System.currentTimeMillis() + ".mp4";

            boolean result = mEZPlayer.startLocalRecordWithFile(recordFilePath);
            if (result) {
                isRecording = true;
                Toast.makeText(requireContext(), "Đang quay video...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Không thể bắt đầu quay video.", Toast.LENGTH_SHORT).show();
            }
        } else {
            mEZPlayer.stopLocalRecord();
            isRecording = false;
            bottomNavigationView.getMenu().findItem(R.id.nav_record).setIcon(R.drawable.ic_videocam_off);
            Toast.makeText(requireContext(), "Video đã lưu tại:\n" + recordFilePath, Toast.LENGTH_LONG).show();
        }
    }
    private void amThanhMotChieu()
    {
        try {
            if (!isSoundOn) {
                mEZPlayer.openSound();
                isSoundOn = true;
                bottomNavigationView.getMenu().findItem(R.id.nav_talk).setIcon(R.drawable.ic_talk_on);
                Toast.makeText(requireContext(), "Âm thanh đã bật", Toast.LENGTH_SHORT).show();
            } else {
                mEZPlayer.closeSound();
                isSoundOn = false;
                bottomNavigationView.getMenu().findItem(R.id.nav_talk).setIcon(R.drawable.ic_talk_off);
                Toast.makeText(requireContext(), "Âm thanh đã tắt", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace(); // Ghi log khi debug
            Toast.makeText(requireContext(), "Lỗi xử lý âm thanh: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private void amThanhHaiChieu()
    {
        if (!isTalking) {
            if (mEZPlayer.startVoiceTalk()) {
                mEZPlayer.setVoiceTalkStatus(false);  // Bật mic: nói cho thiết bị nghe
                isTalking = true;
                bottomNavigationView.getMenu().findItem(R.id.nav_talk).setIcon(R.drawable.ic_mic_on);
            } else {
                Toast.makeText(requireContext(), "Không thể bắt đầu voice talk", Toast.LENGTH_SHORT).show();
            }
        } else {
            mEZPlayer.stopVoiceTalk();
            isTalking = false;
            bottomNavigationView.getMenu().findItem(R.id.nav_talk).setIcon(R.drawable.ic_mic_off);
        }
    }
    private void cheDoNgu()
    {
        if(!isSleep)
        {
            // Tạm dừng hoặc tắt stream camera
            mEZPlayer.stopPlayback();
            // Tắt âm thanh
            mEZPlayer.closeSound();
            isSleep = true;
            Toast.makeText(requireContext(), "Đã bật chế độ ngủ", Toast.LENGTH_SHORT).show();
        }
        else
        {
            playLiveView();
            isSleep = false;
            Toast.makeText(requireContext(), "Bật camera", Toast.LENGTH_SHORT).show();
        }
    }
    private void latAnh()
    {
        try {
            EZGlobalSDK.getInstance().controlVideoFlip(deviceSerial, cameraNo, EZConstants.EZPTZDisplayCommand.EZPTZDisplayCommandFlip);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi khi lật hình ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc muốn xoá camera \"" + tenCam + "\" không?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    // Thực hiện xoá trên background thread
                    executorService.execute(() -> {
                        boolean success = DatabaseHelper.DeleteCamera(idCam);

                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                if (success) {
                                    Toast.makeText(requireContext(),
                                            "Xoá camera thành công!",
                                            Toast.LENGTH_SHORT).show();
                                    // Reset lại biến và reload danh sách để Spinner cập nhật
                                    idCam = -1;
                                    deviceSerial = null;
                                    cameraNo     = -1;
                                    verifyCode   = null;
                                    loadCameraList();
                                } else {
                                    Toast.makeText(requireContext(),
                                            "Xoá camera thất bại. Vui lòng thử lại.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                })
                .setNegativeButton("Huỷ", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
    private void showEditCameraDialog() {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_camera, null);
        // Lưu ý chúng ta đang reuse layout dialog_add_camera.xml với 4 EditText:
        // edtDeviceName, edtDeviceCode, edtCameraNumber, edtVerificationCode

        EditText edtDeviceName     = dialogView.findViewById(R.id.edtDeviceName);
        EditText edtDeviceCode     = dialogView.findViewById(R.id.edtDeviceCode);
        EditText edtCameraNumber   = dialogView.findViewById(R.id.edtCameraNumber);
        EditText edtVerificationCode = dialogView.findViewById(R.id.edtVerificationCode);

        edtDeviceName.setText(tenCam);

        if (idCam != -1) {
            if (deviceSerial != null && cameraNo != -1 && verifyCode != null ) {
                edtDeviceCode.setText(deviceSerial);
                edtCameraNumber.setText(String.valueOf(cameraNo));
                edtVerificationCode.setText(verifyCode);
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Sửa thông tin Camera")
                .setView(dialogView)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    String newTenThietBi   = edtDeviceName.getText().toString().trim();
                    String newMaThietBi    = edtDeviceCode.getText().toString().trim();
                    String newSoCamera     = edtCameraNumber.getText().toString().trim();
                    String newMaXacNhan    = edtVerificationCode.getText().toString().trim();

                    if (newTenThietBi.isEmpty() ||
                            newMaThietBi.isEmpty() ||
                            newSoCamera.isEmpty() ||
                            newMaXacNhan.isEmpty()) {

                        Toast.makeText(requireContext(),
                                "Vui lòng điền đủ tất cả thông tin.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Ghép diaChiMAC mới
                    String newDiaChiMAC = newMaThietBi + " " + newSoCamera + " " + newMaXacNhan;

                    // Thực hiện update trên luồng nền
                    executorService.execute(() -> {
                        boolean success = DatabaseHelper.updateCamera(
                                newDiaChiMAC,
                                newTenThietBi,
                                idCam);

                        // Quay về UI Thread để show Toast và có thể refresh spinner
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                if (success) {
                                    Toast.makeText(requireContext(),
                                            "Cập nhật camera thành công!", Toast.LENGTH_SHORT).show();

                                    // Nếu muốn load lại spinner để thấy tên mới, bạn có thể gọi loadCameraList()
                                    loadCameraList();
                                } else {
                                    Toast.makeText(requireContext(),
                                            "Cập nhật camera thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });

                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executorService.shutdownNow(); // Dọn dẹp executor khi Fragment bị destroy
    }
    private void showAddCameraDialog() {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_camera, null);

        EditText edtDeviceName = dialogView.findViewById(R.id.edtDeviceName);
        EditText edtDeviceCode = dialogView.findViewById(R.id.edtDeviceCode);
        EditText edtCameraNumber = dialogView.findViewById(R.id.edtCameraNumber);
        EditText edtVerificationCode = dialogView.findViewById(R.id.edtVerificationCode);

        new AlertDialog.Builder(requireContext())
                .setTitle("Thêm Camera mới")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String tenThietBi = edtDeviceName.getText().toString().trim();
                    String maThietBi = edtDeviceCode.getText().toString().trim();
                    String soCamera = edtCameraNumber.getText().toString().trim();
                    String maXacNhan = edtVerificationCode.getText().toString().trim();

                    if (tenThietBi.isEmpty() ||
                            maThietBi.isEmpty() ||
                            soCamera.isEmpty() ||
                            maXacNhan.isEmpty()) {

                        Toast.makeText(requireContext(),
                                "Vui lòng điền đủ tất cả thông tin.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String diaChiMAC = maThietBi + " " + soCamera + " " + maXacNhan;
                    Toast.makeText(requireContext(),
                            diaChiMAC,
                            Toast.LENGTH_SHORT).show();
                    executorService.execute(() -> {
                        boolean success = DatabaseHelper.insertCamera(diaChiMAC, tenThietBi);

                        // Sau khi xong, đưa kết quả lên UI Thread
                        requireActivity().runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(requireContext(),
                                        "Thêm camera thành công!",
                                        Toast.LENGTH_SHORT).show();
                                loadCameraList();
                            } else {
                                Toast.makeText(requireContext(),
                                        "Thêm camera thất bại. Vui lòng thử lại.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    });

                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    private void loadCameraList() {
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        dbHelper.loadCameraList(new DeviceCallback() {
            @Override
            public void onDevicesLoaded(List<Device> deviceList) {
                List<String> tenList = new ArrayList<>();
                for (Device d : deviceList) {
                    tenList.add(d.getTenThietBi());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, tenList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCamera.setAdapter(adapter);

                spinnerCamera.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String diaChiMAC = deviceList.get(position).getDiaChiMAC();
                        //Log.d("MAC_Selected", diaChiMAC);

                        idCam = deviceList.get(position).getId();
                        tenCam = deviceList.get(position).getTenThietBi();
                        String[] parts = diaChiMAC.split(" ");

                        if (parts.length == 3) {
                            deviceSerial = parts[0];
                            cameraNo = Integer.parseInt(parts[1]);;
                            verifyCode = parts[2];

//                            Log.d("DeviceSerial", deviceSerial);
//                            Log.d("CameraNo", parts[1]);
//                            Log.d("VerifyCode", verifyCode);

                            playLiveView();
                            checkStatusCamera();

                        } else {
                            Log.e("MAC_Parse_Error", "Định dạng MAC không hợp lệ: " + diaChiMAC);
                        }
                    }


                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface DeviceCallback {
        void onDevicesLoaded(List<Device> deviceList);
        void onError(String message);
    }

    private void updateDateTime() {
        // Lấy thời gian hiện tại
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDateTime = sdf.format(new Date());

        // Gán vào TextView
        tvDateTime.setText(currentDateTime);
    }

    private void checkStatusCamera()
    {
        new Thread(() -> {
            try {
                EZDeviceInfo deviceInfo = EZGlobalSDK.getInstance().getDeviceInfo(deviceSerial);

                if (deviceInfo != null) {
                    int status = deviceInfo.getStatus(); // 0: Offline, 1: Online
                    String statusText;
                    int color;

                    if (status == 1) {
                        statusText = "● Online";
                        color = Color.parseColor("#22c55e"); // Màu xanh lá
                    } else {
                        statusText = "● Offline";
                        color = Color.parseColor("#ef4444"); // Màu đỏ
                    }

                    requireActivity().runOnUiThread(() -> {
                        tvStatus.setText(statusText);
                        tvStatus.setTextColor(color);
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        tvStatus.setText("● Unknown");
                        tvStatus.setTextColor(Color.GRAY);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    tvStatus.setText("● Lỗi");
                    tvStatus.setTextColor(Color.DKGRAY);
                });
            }
        }).start();

    }

    // Xử lý chung cho PTZ
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

    // Hàm điều khiển PTZ (sửa lại cho chạy trong Thread)
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
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Lỗi điều khiển: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
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
