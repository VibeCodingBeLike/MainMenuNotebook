package kat.blasted.mainmenunotebook.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StickyNote {
    private String id;
    private List<NoteTab> tabs = new ArrayList<>();
    private int activeTabIndex = 0;

    // For backwards compatibility with older saves
    private String text;
    private Integer colorIndex;
    private Double relX;
    private Double relY;

    private int x;
    private int y;
    private int width;
    private int height;
    private boolean minimized;
    private long createdAt;

    public StickyNote() {
        this.id = UUID.randomUUID().toString();
        this.x = 20;
        this.y = 20;
        this.width = 140;
        this.height = 110;
        this.minimized = false;
        this.createdAt = System.currentTimeMillis();
        ensureTabs();
    }

    public StickyNote(int x, int y) {
        this();
        this.x = x;
        this.y = y;
    }

    public void ensureTabs() {
        if (tabs == null) {
            tabs = new ArrayList<>();
        }
        if (tabs.isEmpty()) {
            int cIdx = (colorIndex != null) ? colorIndex : 0;
            NoteTab initialTab = new NoteTab("1", text != null ? text : "", cIdx);
            tabs.add(initialTab);
        }
        if (activeTabIndex < 0 || activeTabIndex >= tabs.size()) {
            activeTabIndex = 0;
        }
    }

    public List<NoteTab> getTabs() {
        ensureTabs();
        return tabs;
    }

    public NoteTab getActiveTab() {
        ensureTabs();
        return tabs.get(activeTabIndex);
    }

    public int getActiveTabIndex() {
        ensureTabs();
        return activeTabIndex;
    }

    public void setActiveTabIndex(int index) {
        ensureTabs();
        if (index >= 0 && index < tabs.size()) {
            this.activeTabIndex = index;
            // Keep top-level legacy fields in sync
            this.text = getActiveTab().getText();
            this.colorIndex = getActiveTab().getColorIndex();
        }
    }

    public NoteTab addTab() {
        ensureTabs();
        int nextNum = tabs.size() + 1;
        int nextColor = (getActiveTab().getColorIndex() + 1) % StickyNoteColor.values().length;
        NoteTab newTab = new NoteTab(String.valueOf(nextNum), "", nextColor);
        tabs.add(newTab);
        activeTabIndex = tabs.size() - 1;
        this.text = newTab.getText();
        this.colorIndex = newTab.getColorIndex();
        return newTab;
    }

    public boolean removeTab(int index) {
        ensureTabs();
        if (tabs.size() <= 1) {
            return false;
        }
        if (index >= 0 && index < tabs.size()) {
            tabs.remove(index);
            // Re-number tab titles if they are sequential numbers
            for (int i = 0; i < tabs.size(); i++) {
                try {
                    Integer.parseInt(tabs.get(i).getTitle());
                    tabs.get(i).setTitle(String.valueOf(i + 1));
                } catch (NumberFormatException ignored) {}
            }
            if (activeTabIndex >= tabs.size()) {
                activeTabIndex = tabs.size() - 1;
            }
            this.text = getActiveTab().getText();
            this.colorIndex = getActiveTab().getColorIndex();
            return true;
        }
        return false;
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

    public String getText() {
        return getActiveTab().getText();
    }

    public void setText(String text) {
        getActiveTab().setText(text);
        this.text = text;
    }

    public int getColorIndex() {
        return getActiveTab().getColorIndex();
    }

    public void setColorIndex(int colorIndex) {
        getActiveTab().setColorIndex(colorIndex);
        this.colorIndex = colorIndex;
    }

    public void cycleColor() {
        getActiveTab().cycleColor();
        this.colorIndex = getActiveTab().getColorIndex();
    }

    public StickyNoteColor getColor() {
        return getActiveTab().getColor();
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
