package kat.blasted.mainmenunotebook.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import kat.blasted.mainmenunotebook.model.NoteScope;
import kat.blasted.mainmenunotebook.model.NoteTab;
import kat.blasted.mainmenunotebook.model.StickyNote;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class StickyNoteManager {
    private static final Map<NoteScope, StickyNoteManager> INSTANCES = new EnumMap<>(NoteScope.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type NOTE_LIST_TYPE = new TypeToken<List<StickyNote>>() {}.getType();

    private final NoteScope scope;
    private final Path configPath;
    private final Path legacyPath;
    private final List<StickyNote> notes = new ArrayList<>();
    private boolean loaded = false;

    private StickyNoteManager(NoteScope scope) {
        this.scope = scope;
        Path modDir = FabricLoader.getInstance().getConfigDir().resolve("mainmenunotebook");
        this.configPath = modDir.resolve(scope.getFileName());
        this.legacyPath = scope.getLegacyFileName() != null ? modDir.resolve(scope.getLegacyFileName()) : null;
    }

    public static synchronized StickyNoteManager get(NoteScope scope) {
        return INSTANCES.computeIfAbsent(scope, StickyNoteManager::new);
    }

    public static StickyNoteManager getInstance() {
        return get(NoteScope.MAIN_MENU);
    }

    public NoteScope getScope() {
        return scope;
    }

    public synchronized void load() {
        if (loaded) return;
        loaded = true;
        notes.clear();

        Path pathTarget = configPath;
        boolean migrated = false;

        if (!Files.exists(configPath) && legacyPath != null && Files.exists(legacyPath)) {
            pathTarget = legacyPath;
            migrated = true;
        }

        if (Files.exists(pathTarget)) {
            try (Reader reader = Files.newBufferedReader(pathTarget)) {
                List<StickyNote> loadedNotes = GSON.fromJson(reader, NOTE_LIST_TYPE);
                if (loadedNotes != null) {
                    for (StickyNote note : loadedNotes) {
                        note.ensureTabs();
                        notes.add(note);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (migrated && !notes.isEmpty()) {
            save(); // Save immediately to new scope file (e.g. main_menu_notes.json)
        }

        // Default welcome note if no notes are stored yet
        if (notes.isEmpty()) {
            StickyNote welcome = new StickyNote(20, 20);
            welcome.setWidth(150);
            welcome.setHeight(130);

            if (scope == NoteScope.MAIN_MENU) {
                welcome.setText("Welcome to Main Menu Notebook!\n\n• Ctrl + Click to create a note\n• Drag header to move\n• Click note body to type\n• Click [+] to add a tab\n• Click ● to change color\n• Click – to fold / unfold\n• Drag ⇲ to resize\n• Click ✕ to delete");
                NoteTab tab2 = new NoteTab("2", "Tabs let you stack notes!\n\n• Click tab numbers to switch\n• Click [+] to stack notes\n• Right-click a tab to close\n• Each tab has its own color!", 1);
                welcome.getTabs().add(tab2);
            } else {
                welcome.setText("Welcome to In-Game Notes!\n\n• Ctrl + Click to create a note\n• Drag header to move\n• Click note body to type\n• Click [+] to add a tab\n• Click ● to change color\n• Click – to fold / unfold\n• Drag ⇲ to resize\n• Click ✕ to delete");
                NoteTab tab2 = new NoteTab("2", "In-Game Notes:\n\n• These notes stay in your pause menu\n• Independent from main menu notes\n• Perfect for coordinates & to-do lists!", 2);
                welcome.getTabs().add(tab2);
            }
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
