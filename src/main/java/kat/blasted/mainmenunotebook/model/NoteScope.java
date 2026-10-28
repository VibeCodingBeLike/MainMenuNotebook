package kat.blasted.mainmenunotebook.model;

public enum NoteScope {
    MAIN_MENU("main_menu_notes.json", "notes.json", "Main Menu"),
    PAUSE_MENU("pause_menu_notes.json", null, "Pause Menu");

    private final String fileName;
    private final String legacyFileName;
    private final String displayName;

    NoteScope(String fileName, String legacyFileName, String displayName) {
        this.fileName = fileName;
        this.legacyFileName = legacyFileName;
        this.displayName = displayName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getLegacyFileName() {
        return legacyFileName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
