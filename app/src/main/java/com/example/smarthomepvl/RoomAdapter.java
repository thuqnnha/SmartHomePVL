package com.example.smarthomepvl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<Room> roomList;
    private OnRoomClickListener listener;

    public interface OnRoomClickListener {
        void onRoomClick(Room room);
        void onRoomLongClick(View view, Room room);
    }

    public RoomAdapter(List<Room> roomList, OnRoomClickListener listener) {
        this.roomList = roomList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);
        holder.bind(room);
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    class RoomViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRoomIcon;
        TextView txtRoomName;
        View roomItemLayout;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRoomIcon = itemView.findViewById(R.id.imgRoomIcon);
            txtRoomName = itemView.findViewById(R.id.txtRoomName);
            roomItemLayout = itemView.findViewById(R.id.roomItemLayout);
        }

        public void bind(Room room) {
            txtRoomName.setText(room.getName());
            imgRoomIcon.setImageResource(room.getIconResId());

            // Nếu bạn muốn đổi màu/tùy biến nền theo room.getBackgroundResId()
            roomItemLayout.setBackgroundResource(room.getBackgroundResId());
            //click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRoomClick(room);
                }
            });
            //long click
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onRoomLongClick(v, room);
                }
                return true;
            });

        }
    }
}
