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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerRooms;
    private FloatingActionButton btnAddRoom;
    private DatabaseHelper db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private SpeechRecognizer speechRecognizer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerRooms = view.findViewById(R.id.recyclerRooms);
        btnAddRoom = view.findViewById(R.id.btnAddRoom);

        recyclerRooms.setLayoutManager(new LinearLayoutManager(getContext()));

        loadRooms();

        btnAddRoom.setOnClickListener(v -> {
            showAddRoomDialog();
        });

        //Xử lý sự kiện khi bấm nút micro
        ImageButton btnMicro = view.findViewById(R.id.btnMicro);
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
                RoomAdapter adapter = new RoomAdapter(rooms, roomName -> {
                    RoomDetailFragment fragment = RoomDetailFragment.newInstance(roomName);
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                });
                recyclerRooms.setAdapter(adapter);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thêm Phòng Mới");

        final EditText input = new EditText(getContext());
        input.setHint("Nhập tên phòng");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String roomName = input.getText().toString().trim();
            if (!roomName.isEmpty()) {
                executorService.execute(() -> {
                    boolean success = DatabaseHelper.insertRoom(roomName);
                    if (success) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(getContext(), "Thêm phòng thành công", Toast.LENGTH_SHORT).show();
                            loadRooms(); // Load lại danh sách phòng sau khi thêm
                        });
                    } else {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(getContext(), "Lỗi khi thêm phòng", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            } else {
                Toast.makeText(getContext(), "Tên phòng không được để trống", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
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
                        Toast.makeText(getContext(), "Ghi âm: " + spokenText, Toast.LENGTH_LONG).show();
                        // TODO: Ở đây bạn có thể xử lý logic theo lệnh giọng nói
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


