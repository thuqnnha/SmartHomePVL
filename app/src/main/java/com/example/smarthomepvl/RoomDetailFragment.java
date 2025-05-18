package com.example.smarthomepvl;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Locale;

import android.Manifest;

public class RoomDetailFragment extends Fragment {

    private static final String ARG_ROOM_NAME = "room_name";
    private String roomName;
    private LinearLayout deviceContainer;
    private SpeechRecognizer speechRecognizer;

    public static RoomDetailFragment newInstance(String roomName) {
        RoomDetailFragment fragment = new RoomDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_NAME, roomName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            roomName = getArguments().getString(ARG_ROOM_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room_detail, container, false);

        TextView tvRoomName = view.findViewById(R.id.tvRoomName);
        tvRoomName.setText(roomName);

        deviceContainer = view.findViewById(R.id.deviceContainer);

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

        setupCameraAndControl(view, roomName);

        return view;
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

    private void setupCameraAndControl(View rootView, String roomName) {
        // Xóa thiết bị cũ nếu có
        deviceContainer.removeAllViews();

        if ("Phòng khách".equals(roomName)) {
            addDeviceSwitch("Tivi");
            addDeviceSwitch("Đèn trần");
        } else if ("Phòng ngủ 1".equals(roomName)) {
            addDeviceSwitch("Đèn ngủ");
            addDeviceSwitch("Quạt");
        } else if ("Phòng bếp".equals(roomName)) {
            addDeviceSwitch("Máy hút mùi");
            addDeviceSwitch("Đèn bếp");
        } else {
            // Nếu phòng chưa có thiết bị, thông báo tạm
            TextView tvNoDevice = new TextView(getContext());
            tvNoDevice.setText("Chưa có thiết bị điều khiển");
            deviceContainer.addView(tvNoDevice);
        }
    }

    private void addDeviceSwitch(String deviceName) {
        Switch deviceSwitch = new Switch(getContext());
        deviceSwitch.setText(deviceName);
        deviceSwitch.setTextSize(18);
        deviceSwitch.setPadding(0, 20, 0, 20);

        // Tạm thời chưa thêm sự kiện, sẽ bổ sung sau
        deviceContainer.addView(deviceSwitch);
    }
}
