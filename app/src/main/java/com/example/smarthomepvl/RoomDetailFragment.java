package com.example.smarthomepvl;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.videogo.constant.Constant;
import com.videogo.openapi.EZGlobalSDK;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.bean.EZAccessToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoomDetailFragment extends Fragment {

    private static final String ARG_ROOM_NAME = "room_name";
    private static final String ARG_ROOM_ID = "room_id";

    private String roomName;
    private int roomId;

    private RecyclerView recyclerDevices,recyclerCameras;
    private TextView txtCameraTitle;
    private TextView txtDeviceTitle;
    private FloatingActionButton fabAddDevice;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

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
        fabAddDevice = view.findViewById(R.id.fabAddDevice);

        recyclerDevices.setLayoutManager(new LinearLayoutManager(getContext()));

        TextView tvRoomName = view.findViewById(R.id.txtRoomName);
        tvRoomName.setText(roomName);


        fabAddDevice.setOnClickListener(v->{
            showAddDeviceDialog();
        });
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //------------------------------------Ánh xạ view--------------------------------------

        loadDevicesAndCamera();
    }

    private void showAddDeviceDialog() {
        final CharSequence[] options = {"Thêm Camera", "Thêm Thiết Bị Khác"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Chọn loại thiết bị");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showAddCameraDialog();
                } else {
                    showAddOtherDeviceDialog();
                }
            }
        });
        builder.show();
    }
    private void showAddCameraDialog() {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_camera, null);

        TextInputEditText etCameraName = dialogView.findViewById(R.id.etCameraName);
        TextInputEditText etSerial = dialogView.findViewById(R.id.etSerial);
        TextInputEditText etCameraNo = dialogView.findViewById(R.id.etCameraNo);
        TextInputEditText etVerifyCode = dialogView.findViewById(R.id.etVerifyCode);

        new AlertDialog.Builder(requireContext())
                .setTitle("Thêm Camera mới")
                .setView(dialogView)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String tenThietBi = etCameraName.getText().toString().trim();
                    String maThietBi = etSerial.getText().toString().trim();
                    String soCamera = etCameraNo.getText().toString().trim();
                    String maXacNhan = etVerifyCode.getText().toString().trim();

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
//                    Toast.makeText(requireContext(),
//                            diaChiMAC,
//                            Toast.LENGTH_SHORT).show();
                    executorService.execute(() -> {
                        boolean success = DatabaseHelper.insertCamera(diaChiMAC, tenThietBi, roomId);

                        // Sau khi xong, đưa kết quả lên UI Thread
                        requireActivity().runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(requireContext(),
                                        "Thêm camera thành công!",
                                        Toast.LENGTH_SHORT).show();
                                loadDevicesAndCamera();
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
    private void showAddOtherDeviceDialog() {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_device, null);

        TextInputEditText etDeviceName = dialogView.findViewById(R.id.etDeviceName);
        TextInputEditText etMacAddress = dialogView.findViewById(R.id.etMacAddress);

        new AlertDialog.Builder(requireContext())
                .setTitle("Thêm thiết bị mới")
                .setView(dialogView)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String tenThietBi = etDeviceName.getText().toString().trim();
                    String diaChiMAC = etMacAddress.getText().toString().trim();

                    if (tenThietBi.isEmpty() || diaChiMAC.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Vui lòng điền đủ tất cả thông tin.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(requireContext(),
                            diaChiMAC,
                            Toast.LENGTH_SHORT).show();
                    executorService.execute(() -> {
                        boolean success = DatabaseHelper.insertDevice(diaChiMAC, tenThietBi, roomId);

                        // Sau khi xong, đưa kết quả lên UI Thread
                        requireActivity().runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(requireContext(),
                                        "Thêm thiết bị thành công!",
                                        Toast.LENGTH_SHORT).show();
                                loadDevicesAndCamera();
                            } else {
                                Toast.makeText(requireContext(),
                                        "Thêm thiết bị thất bại. Vui lòng thử lại.",
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
                    CameraAdapter cameraAdapter = new CameraAdapter(cameraList, new CameraAdapter.OnCameraClickListener() {
                        @Override
                        public void onCameraClick(Device camera) {
                            // Xử lý khi click (mở camera controller)
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("camera", camera);

                            CameraControllerFragment fragment = new CameraControllerFragment();
                            fragment.setArguments(bundle);

                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .add(R.id.fragment_container, fragment)
                                    .hide(RoomDetailFragment.this)
                                    .addToBackStack(null)
                                    .commit();
                        }
                        @Override
                        public void onCameraLongClick(Device camera, View view) {
                            // Hiện popup menu khi nhấn giữ
                            PopupMenu popup = new PopupMenu(view.getContext(), view);
                            popup.getMenuInflater().inflate(R.menu.device_options_menu, popup.getMenu());
                            popup.setOnMenuItemClickListener(item -> {
                                if (item.getItemId() == R.id.menu_edit) {
                                    showEditCameraDialog(camera);
                                    return true;
                                } else if (item.getItemId() == R.id.menu_delete) {
                                    deleteCamera(camera);
                                    return true;
                                }
                                return false;
                            });
                            popup.show();
                        }
                    });

                    recyclerCameras.setAdapter(cameraAdapter);

                    // Adapter thiết bị khác
                    DeviceAdapter deviceAdapter = new DeviceAdapter(otherDevices, new DeviceAdapter.DeviceListener() {
                        @Override
                        public void onDeviceSwitchChanged(Device device, boolean isOn) {
                            Toast.makeText(getContext(), device.getTenThietBi() + ": " + (isOn ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onDeviceLongClick(Device device, View view) {
                            PopupMenu popup = new PopupMenu(getContext(), view);
                            popup.getMenuInflater().inflate(R.menu.device_options_menu, popup.getMenu());
                            popup.setOnMenuItemClickListener(item -> {
                                if (item.getItemId() == R.id.menu_edit) {
                                    showEditOtherDeviceDialog(device);
                                    return true;
                                } else if (item.getItemId() == R.id.menu_delete) {
                                    deleteOtherDevice(device);
                                    return true;
                                }
                                return false;
                            });
                            popup.show();
                        }
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
    private void showEditCameraDialog(Device camera) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_camera, null);

        TextInputEditText etCameraName = dialogView.findViewById(R.id.etCameraName);
        TextInputEditText etSerial = dialogView.findViewById(R.id.etSerial);
        TextInputEditText etCameraNo = dialogView.findViewById(R.id.etCameraNo);
        TextInputEditText etVerifyCode = dialogView.findViewById(R.id.etVerifyCode);

        String[] parts = camera.getDiaChiMAC().split(" ");

        etCameraName.setText(camera.getTenThietBi());
        if (parts.length == 3) {
            etSerial.setText(parts[0]);
            etCameraNo.setText(parts[1]);
            etVerifyCode.setText(parts[2]);

            new AlertDialog.Builder(requireContext())
                    .setTitle("Chỉnh sửa phòng")
                    .setView(dialogView)
                    .setPositiveButton("Xác nhận", (dialog, which) -> {
                        String tenCamera = etCameraName.getText().toString().trim();
                        String maCam = etSerial.getText().toString().trim();
                        String soCam = etCameraNo.getText().toString().trim();
                        String maXacNhan = etVerifyCode.getText().toString().trim();

                        if (tenCamera.isEmpty() || maCam.isEmpty() || soCam.isEmpty() || maXacNhan.isEmpty()) {
                            Toast.makeText(requireContext(),
                                    "Vui lòng điền đủ tất cả thông tin.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        executorService.execute(() -> {
                            String DiaChiMAC = maCam + " " + soCam + " " + maXacNhan;
                            boolean success = DatabaseHelper.updateCamera(DiaChiMAC, tenCamera, camera.getId());

                            // Sau khi xong, đưa kết quả lên UI Thread
                            requireActivity().runOnUiThread(() -> {
                                if (success) {
                                    Toast.makeText(requireContext(),
                                            "Chỉnh sửa thành công!",
                                            Toast.LENGTH_SHORT).show();
                                    loadDevicesAndCamera();
                                } else {
                                    Toast.makeText(requireContext(),
                                            "Chỉnh sửa thất bại. Vui lòng thử lại.",
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
    }
    private void deleteCamera(Device camera) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc muốn xoá camera: \"" + camera.getTenThietBi() + "\" không?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    // Thực hiện xoá trên background thread
                    executorService.execute(() -> {
                        boolean success = DatabaseHelper.DeleteCamera(camera.getId());

                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                if (success) {
                                    Toast.makeText(requireContext(),
                                            "Xoá camera thành công!",
                                            Toast.LENGTH_SHORT).show();
                                    loadDevicesAndCamera();
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

    private void showEditOtherDeviceDialog(Device device) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_device, null);

        TextInputEditText etDeviceName = dialogView.findViewById(R.id.etDeviceName);
        TextInputEditText etMacAddress = dialogView.findViewById(R.id.etMacAddress);

        etDeviceName.setText(device.getTenThietBi());
        etMacAddress.setText(device.getDiaChiMAC());

        new AlertDialog.Builder(requireContext())
                .setTitle("Chỉnh sửa thiết bị")
                .setView(dialogView)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String tenThietBi = etDeviceName.getText().toString().trim();
                    String MACThietBi = etMacAddress.getText().toString().trim();

                    if (tenThietBi.isEmpty() || MACThietBi.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Vui lòng điền đủ tất cả thông tin.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    executorService.execute(() -> {
                        boolean success = DatabaseHelper.updateCamera(MACThietBi, tenThietBi, device.getId());

                        // Sau khi xong, đưa kết quả lên UI Thread
                        requireActivity().runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(requireContext(),
                                        "Chỉnh sửa thành công!",
                                        Toast.LENGTH_SHORT).show();
                                loadDevicesAndCamera();
                            } else {
                                Toast.makeText(requireContext(),
                                        "Chỉnh sửa thất bại. Vui lòng thử lại.",
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
    private void deleteOtherDevice(Device device) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc muốn xoá thiết bị: \"" + device.getTenThietBi() + "\" không?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    // Thực hiện xoá trên background thread
                    executorService.execute(() -> {
                        boolean success = DatabaseHelper.DeleteCamera(device.getId());

                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                if (success) {
                                    Toast.makeText(requireContext(),
                                            "Xoá thiết bị thành công!",
                                            Toast.LENGTH_SHORT).show();
                                    loadDevicesAndCamera();
                                } else {
                                    Toast.makeText(requireContext(),
                                            "Xoá thiết bị thất bại. Vui lòng thử lại.",
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

}
