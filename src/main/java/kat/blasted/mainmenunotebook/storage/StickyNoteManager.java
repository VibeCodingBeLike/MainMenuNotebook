package kat.blasted.mainmenunotebook.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import kat.blasted.mainmenunotebook.model.StickyNote;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StickyNoteManager {
    private static final StickyNoteManager INSTANCE = new StickyNoteManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type NOTE_LIST_TYPE = new TypeToken<List<StickyNote>>() {}.getType();

    private final Path configPath;
    private final List<StickyNote> notes = new ArrayList<>();
    private boolean loaded = false;

    private StickyNoteManager() {
        this.configPath = FabricLoader.getInstance().getConfigDir().resolve("mainmenunotebook").resolve("notes.json");
    }

    public static StickyNoteManager getInstance() {
        return INSTANCE;
    }

    public synchronized void load() {
        if (loaded) return;
        loaded = true;
        notes.clear();

        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                List<StickyNote> loadedNotes = GSON.fromJson(reader, NOTE_LIST_TYPE);
                if (loadedNotes != null) {
                    notes.addAll(loadedNotes);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Default welcome note if no notes are stored yet
        if (notes.isEmpty()) {
            StickyNote welcome = new StickyNote(20, 20);
            welcome.setText("Welcome to Main Menu Notebook!\n\n• Ctrl + Click to create a note\n• Drag header to move\n• Click note body to type\n• Click ● to change color\n• Click – to fold / unfold\n• Drag ⇲ to resize\n• Click ✕ to delete");
            welcome.setWidth(150);
            welcome.setHeight(130);
            notes.add(welcome);
            save();
        }
    }

    public synchronized void save() {
        try {
            if (configPath.getParent() != null && !Files.exists(configPath.getParent())) {
                Files.createDirectories(configPath.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(notes, NOTE_LIST_TYPE, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<StickyNote> getNotes() {
        return notes;
    }

    public synchronized StickyNote createNote(int x, int y) {
        StickyNote note = new StickyNote(x, y);
        note.setColorIndex(notes.size() % 6);
        notes.add(note);
        save();
        return note;
    }

    public synchronized void removeNote(StickyNote note) {
        notes.remove(note);
        save();
    }

    public synchronized void bringToFront(StickyNote note) {
        if (notes.remove(note)) {
            notes.add(note);
            save();
        }
    }
}
