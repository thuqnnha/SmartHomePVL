package com.example.smarthomepvl;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerRooms;
    private FloatingActionButton btnAddRoom;
    private TextView txtVoiceResult;
    private DatabaseHelper db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private SpeechRecognizer speechRecognizer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        //----------------------------Ánh xạ view--------------------------------------
        recyclerRooms = view.findViewById(R.id.recyclerRooms);
        btnAddRoom = view.findViewById(R.id.btnAddRoom);
        txtVoiceResult = view.findViewById(R.id.txtVoiceResult);
        ImageButton btnMicro = view.findViewById(R.id.btnMicro);

        recyclerRooms.setLayoutManager(new GridLayoutManager(getContext(), 2));


        loadRooms();

        btnAddRoom.setOnClickListener(v -> {
            showAddRoomDialog();
        });

        //Xử lý sự kiện khi bấm nút micro
        btnMicro.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                return;
            }

            if (!SpeechRecognizer.isRecognitionAvailable(getContext())) {
                Toast.makeText(getContext(), "Thiết bị không hỗ trợ giọng nói", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(getContext(), "Đang chờ giọng nói...", Toast.LENGTH_SHORT).show();
            startSpeechRecognition();
        });

        return view;
    }
    private void loadRooms() {
        db = new DatabaseHelper(getContext());
        db.loadRoom(new DatabaseHelper.RoomCallback() {
            @Override
            public void onRoomsLoaded(List<Room> rooms) {
                RoomAdapter adapter = new RoomAdapter(rooms, new RoomAdapter.OnRoomClickListener() {
                    @Override
                    public void onRoomClick(Room room) {
                        // Mở chi tiết phòng khi click
                        RoomDetailFragment fragment = RoomDetailFragment.newInstance(room.getId(), room.getName());
                        getParentFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    }

                    @Override
                    public void onRoomLongClick(View view, Room room) {
                        // Hiện popup menu khi nhấn giữ
                        PopupMenu popup = new PopupMenu(view.getContext(), view);
                        popup.getMenuInflater().inflate(R.menu.room_options_menu, popup.getMenu());
                        popup.setOnMenuItemClickListener(item -> {
                            if (item.getItemId() == R.id.menu_edit) {
                                // Gọi sửa phòng
                                showEditRoomDialog(room);
                                return true;
                            } else if (item.getItemId() == R.id.menu_delete) {
                                deleteRoom(room);
                                return true;
                            }
                            return false;
                        });
                        popup.show();
                    }
                });
                recyclerRooms.setAdapter(adapter);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showEditRoomDialog(Room room) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_room, null);

        TextInputEditText etRoomName = dialogView.findViewById(R.id.etRoomName);

        new AlertDialog.Builder(requireContext())
                .setTitle("Chỉnh sửa phòng")
                .setView(dialogView)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String tenPhong = etRoomName.getText().toString().trim();

                    if (tenPhong.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Vui lòng điền đủ tất cả thông tin.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    executorService.execute(() -> {
                        boolean success = DatabaseHelper.updateRoom(tenPhong, room.getId());

                        // Sau khi xong, đưa kết quả lên UI Thread
                        requireActivity().runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(requireContext(),
                                        "Chỉnh sửa thành công!",
                                        Toast.LENGTH_SHORT).show();
                                loadRooms();
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
    private void deleteRoom(Room room) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc muốn xoá phòng: \"" + room.getName() + "\" không?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    // Thực hiện xoá trên background thread
                    executorService.execute(() -> {
                        boolean success = DatabaseHelper.deleteRoom(room.getId());

                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                if (success) {
                                    Toast.makeText(requireContext(),
                                            "Xoá phòng thành công!",
                                            Toast.LENGTH_SHORT).show();
                                    loadRooms();
                                } else {
                                    Toast.makeText(requireContext(),
                                            "Xoá phòng thất bại. Vui lòng thử lại.",
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

    private void showAddRoomDialog() {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_room, null);

        TextInputEditText etRoomName = dialogView.findViewById(R.id.etRoomName);

        new AlertDialog.Builder(requireContext())
                .setTitle("Thêm phòng mới")
                .setView(dialogView)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String tenPhong = etRoomName.getText().toString().trim();

                    if (tenPhong.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Vui lòng điền đủ tất cả thông tin.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    executorService.execute(() -> {
                        boolean success = DatabaseHelper.insertRoom(tenPhong);

                        // Sau khi xong, đưa kết quả lên UI Thread
                        requireActivity().runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(requireContext(),
                                        "Thêm phòng thành công!",
                                        Toast.LENGTH_SHORT).show();
                                loadRooms();
                            } else {
                                Toast.makeText(requireContext(),
                                        "Thêm phòng thất bại. Vui lòng thử lại.",
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
    private void startSpeechRecognition() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override public void onReadyForSpeech(Bundle params) {}
                @Override public void onBeginningOfSpeech() {}
                @Override public void onRmsChanged(float rmsdB) {}
                @Override public void onBufferReceived(byte[] buffer) {}
                @Override public void onEndOfSpeech() {}
                @Override public void onError(int error) {
                    Toast.makeText(getContext(), "Lỗi nhận giọng nói", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String spokenText = matches.get(0);
                        //Toast.makeText(getContext(), "Ghi âm: " + spokenText, Toast.LENGTH_LONG).show();
                        // TODO: Xử lí
                        txtVoiceResult.setText(spokenText);

                        txtVoiceResult.postDelayed(() -> txtVoiceResult.setText("Nói gì đó..."), 5000);
                    }
                }
                @Override
                public void onPartialResults(Bundle partialResults) {
                    ArrayList<String> partial = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (partial != null && !partial.isEmpty()) {
                        String preview = partial.get(0);
                        //Toast.makeText(getContext(), "Bạn đang nói: " + preview, Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onEvent(int eventType, Bundle params) {}
            });
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

//        // Giảm độ trễ phản hồi
//        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1000);
//        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000);
//        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2500);

        speechRecognizer.startListening(intent);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}


