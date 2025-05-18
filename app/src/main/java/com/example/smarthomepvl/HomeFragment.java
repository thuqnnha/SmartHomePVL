package com.example.smarthomepvl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class HomeFragment extends Fragment {

    private RecyclerView recyclerRooms;
    private FloatingActionButton btnAddRoom;
    //private List<String> rooms = new ArrayList<>(Arrays.asList("Phòng khách", "Phòng ngủ 1", "Phòng ngủ 2", "Phòng bếp"));


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerRooms = view.findViewById(R.id.recyclerRooms);
        btnAddRoom = view.findViewById(R.id.btnAddRoom);

        List<Room> roomList = new ArrayList<>();
        roomList.add(new Room("Phòng khách", R.drawable.ic_living_room, R.drawable.room_item_background));
        roomList.add(new Room("Phòng ngủ 1", R.drawable.ic_bedroom, R.drawable.room_item_background));
        roomList.add(new Room("Phòng bếp", R.drawable.ic_kitchen, R.drawable.room_item_background));

        recyclerRooms.setLayoutManager(new LinearLayoutManager(getContext()));
        RoomAdapter adapter = new RoomAdapter(roomList, roomName -> {
            // Mở RoomDetailFragment với roomName
            RoomDetailFragment fragment = RoomDetailFragment.newInstance(roomName);

            // Thay fragment container trong Activity hoặc Fragment cha
            FragmentManager fm = getParentFragmentManager(); // Hoặc getActivity().getSupportFragmentManager();
            fm.beginTransaction()
                    .replace(R.id.fragment_container, fragment) // fragment_container là id FrameLayout chứa fragment
                    .addToBackStack(null)
                    .commit();
        });
        recyclerRooms.setAdapter(adapter);


        btnAddRoom.setOnClickListener(v -> {
//            // TODO: Xử lý thêm phòng mới
//            rooms.add("Phòng mới " + (rooms.size() + 1));
//            adapter.notifyItemInserted(rooms.size() - 1);
        });

        return view;
    }

}


