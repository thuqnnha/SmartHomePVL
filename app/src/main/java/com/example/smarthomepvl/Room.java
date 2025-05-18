package com.example.smarthomepvl;

public class Room {
    private String name;
    private int iconResId;
    private int backgroundResId;

    public Room(String name, int iconResId, int backgroundResId) {
        this.name = name;
        this.iconResId = iconResId;
        this.backgroundResId = backgroundResId;
    }

    public String getName() { return name; }
    public int getIconResId() { return iconResId; }
    public int getBackgroundResId() { return backgroundResId; }
}

