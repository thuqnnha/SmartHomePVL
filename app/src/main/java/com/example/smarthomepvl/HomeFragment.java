package com.example.smarthomepvl;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerRooms;
    private FloatingActionButton btnAddRoom;
    private DatabaseHelper db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

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


}


