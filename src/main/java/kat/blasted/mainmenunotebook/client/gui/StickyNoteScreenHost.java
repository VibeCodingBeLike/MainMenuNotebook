package kat.blasted.mainmenunotebook.client.gui;

import kat.blasted.mainmenunotebook.model.NoteScope;
import kat.blasted.mainmenunotebook.model.StickyNote;
import kat.blasted.mainmenunotebook.storage.StickyNoteManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class StickyNoteScreenHost {
    private final Screen screen;
    private final NoteScope scope;
    private final Consumer<AbstractWidget> widgetAdder;
    private final Consumer<GuiEventListener> widgetRemover;
    private final List<StickyNoteWidget> noteWidgets = new ArrayList<>();

    public StickyNoteScreenHost(
        Screen screen,
        NoteScope scope,
        Consumer<AbstractWidget> widgetAdder,
        Consumer<GuiEventListener> widgetRemover
    ) {
        this.screen = screen;
        this.scope = scope;
        this.widgetAdder = widgetAdder;
        this.widgetRemover = widgetRemover;
    }

    public void init() {
        noteWidgets.clear();
        StickyNoteManager manager = StickyNoteManager.get(scope);
        manager.load();

        for (StickyNote note : manager.getNotes()) {
            addNoteWidget(note);
        }
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        for (StickyNoteWidget widget : noteWidgets) {
            if (widget.visible) {
                widget.extractRenderState(graphics, mouseX, mouseY, a);
            }
        }
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        StickyNoteManager manager = StickyNoteManager.get(scope);

        // 1. Hold Ctrl and Left-Click to create a new sticky note
        if (event.hasControlDown() && event.button() == 0) {
            for (StickyNoteWidget w : noteWidgets) {
                w.unfocusEditBox();
            }

            int defaultW = 140;
            int defaultH = 110;

            int maxW = Math.max(0, screen.width - defaultW);
            int maxH = Math.max(0, screen.height - defaultH);
            int spawnX = Math.clamp((int) event.x() - 10, 0, maxW);
            int spawnY = Math.clamp((int) event.y() - 5, 0, maxH);

            StickyNote newNote = manager.createNote(spawnX, spawnY);
            newNote.setWidth(defaultW);
            newNote.setHeight(defaultH);
            manager.save();

            StickyNoteWidget widget = addNoteWidget(newNote);
            screen.setFocused(widget);

            AbstractWidget.playButtonClickSound(Minecraft.getInstance().getSoundManager());
            return true;
        }

        // 2. Prevent clicking through notes to underlying screen buttons:
        // Iterate through notes in reverse order (top-most first)
        for (int i = noteWidgets.size() - 1; i >= 0; i--) {
            StickyNoteWidget widget = noteWidgets.get(i);
            if (widget.visible && widget.isMouseOver(event.x(), event.y())) {
                for (StickyNoteWidget other : noteWidgets) {
                    if (other != widget) {
                        other.unfocusEditBox();
                    }
                }
                widget.mouseClicked(event, doubleClick);
                screen.setFocused(widget);
                if (event.button() == 0) {
                    screen.setDragging(true);
                }
                return true;
            }
        }

        // 3. Clicked outside all notes: unfocus all notes
        for (StickyNoteWidget widget : noteWidgets) {
            widget.unfocusEditBox();
        }
        return false;
    }

    public boolean keyPressed(KeyEvent event) {
        for (StickyNoteWidget widget : noteWidgets) {
            if (widget.isEditing()) {
                widget.keyPressed(event);
                return true;
            }
        }
        return false;
    }

    public boolean charTyped(CharacterEvent event) {
        for (StickyNoteWidget widget : noteWidgets) {
            if (widget.isEditing()) {
                widget.charTyped(event);
                return true;
            }
        }
        return false;
    }

    public boolean keyReleased(KeyEvent event) {
        for (StickyNoteWidget widget : noteWidgets) {
            if (widget.isEditing()) {
                widget.keyReleased(event);
                return true;
            }
        }
        return false;
    }

    private StickyNoteWidget addNoteWidget(StickyNote note) {
        StickyNoteManager manager = StickyNoteManager.get(scope);
        int x = note.getX();
        int y = note.getY();

        StickyNoteWidget widget = new StickyNoteWidget(
            x, y, note.getWidth(), note.getHeight(),
            note, Minecraft.getInstance().font, screen.width, screen.height,
            manager,
            w -> {
                widgetRemover.accept(w);
                noteWidgets.remove(w);
            },
            w -> {
                widgetRemover.accept(w);
                widgetAdder.accept(w);
                noteWidgets.remove(w);
                noteWidgets.add(w);
                manager.bringToFront(note);
            }
        );

        noteWidgets.add(widget);
        widgetAdder.accept(widget);
        return widget;
    }

    public List<StickyNoteWidget> getNoteWidgets() {
        return noteWidgets;
    }
}
