package com.example.smarthomepvl;

public class Room {
    private int id;
    private String name;
    private int iconResId;
    private int backgroundResId;

    public Room(int id, String name, int iconResId, int backgroundResId) {
        this.id = id;
        this.name = name;
        this.iconResId = iconResId;
        this.backgroundResId = backgroundResId;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getIconResId() { return iconResId; }
    public int getBackgroundResId() { return backgroundResId; }
}

