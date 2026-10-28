package kat.blasted.mainmenunotebook.model;

import java.util.UUID;

public class NoteTab {
    private String id;
    private String title;
    private String text;
    private int colorIndex;

    public NoteTab() {
        this.id = UUID.randomUUID().toString();
        this.title = "1";
        this.text = "";
        this.colorIndex = 0;
    }

    public NoteTab(String title, String text, int colorIndex) {
        this.id = UUID.randomUUID().toString();
        this.title = title != null ? title : "1";
        this.text = text != null ? text : "";
        this.colorIndex = colorIndex;
    }

    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title != null && !title.isEmpty() ? title : "1";
    }

    public void setTitle(String title) {
        this.title = title;
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
}
