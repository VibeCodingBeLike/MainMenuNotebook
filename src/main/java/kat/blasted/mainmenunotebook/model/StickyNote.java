package kat.blasted.mainmenunotebook.model;

import java.util.UUID;

public class StickyNote {
    private String id;
    private String text;
    private int colorIndex;
    private int x;
    private int y;
    // For backwards compatibility
    private Double relX;
    private Double relY;
    private int width;
    private int height;
    private boolean minimized;
    private long createdAt;

    public StickyNote() {
        this.id = UUID.randomUUID().toString();
        this.text = "";
        this.colorIndex = 0;
        this.x = 20;
        this.y = 20;
        this.width = 140;
        this.height = 110;
        this.minimized = false;
        this.createdAt = System.currentTimeMillis();
    }

    public StickyNote(int x, int y) {
        this();
        this.x = x;
        this.y = y;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text != null ? text : "";
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
    }

    public void cycleColor() {
        this.colorIndex = (this.colorIndex + 1) % StickyNoteColor.values().length;
    }

    public StickyNoteColor getColor() {
        return StickyNoteColor.fromIndex(this.colorIndex);
    }

    public int getX() {
        if (x <= 0 && relX != null && relX > 0) {
            x = (int) (relX * 854);
            relX = null;
        }
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        if (y <= 0 && relY != null && relY > 0) {
            y = (int) (relY * 480);
            relY = null;
        }
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width > 0 ? width : 140;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height > 0 ? height : 110;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isMinimized() {
        return minimized;
    }

    public void setMinimized(boolean minimized) {
        this.minimized = minimized;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
