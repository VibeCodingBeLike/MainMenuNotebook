package kat.blasted.mainmenunotebook.mixin;

import kat.blasted.mainmenunotebook.client.gui.StickyNoteWidget;
import kat.blasted.mainmenunotebook.model.StickyNote;
import kat.blasted.mainmenunotebook.storage.StickyNoteManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Unique
    private final List<StickyNoteWidget> mainmenunotebook$noteWidgets = new ArrayList<>();

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void mainmenunotebook$init(CallbackInfo ci) {
        mainmenunotebook$noteWidgets.clear();
        StickyNoteManager manager = StickyNoteManager.getInstance();
        manager.load();

        // Add all existing notes exactly at their saved positions (no clamping onto screen)
        for (StickyNote note : manager.getNotes()) {
            mainmenunotebook$addNoteWidget(note);
        }
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void mainmenunotebook$renderStickyNotesAtFront(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        // Render notes at the very end of TitleScreen rendering, on top of title png, splash text, and realms news
        for (StickyNoteWidget widget : mainmenunotebook$noteWidgets) {
            if (widget.visible) {
                widget.extractRenderState(graphics, mouseX, mouseY, a);
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mainmenunotebook$onMouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        // 1. Hold Ctrl and Left-Click to create a new sticky note
        if (event.hasControlDown() && event.button() == 0) {
            for (StickyNoteWidget w : mainmenunotebook$noteWidgets) {
                w.unfocusEditBox();
            }

            int defaultW = 140;
            int defaultH = 110;

            // Strictly clamp spawn position so new notes never spawn off-screen
            int maxW = Math.max(0, this.width - defaultW);
            int maxH = Math.max(0, this.height - defaultH);
            int spawnX = Math.clamp((int) event.x() - 10, 0, maxW);
            int spawnY = Math.clamp((int) event.y() - 5, 0, maxH);

            StickyNote newNote = StickyNoteManager.getInstance().createNote(spawnX, spawnY);
            newNote.setWidth(defaultW);
            newNote.setHeight(defaultH);
            StickyNoteManager.getInstance().save();

            StickyNoteWidget widget = mainmenunotebook$addNoteWidget(newNote);
            this.setFocused(widget);

            AbstractWidget.playButtonClickSound(Minecraft.getInstance().getSoundManager());
            cir.setReturnValue(true);
            return;
        }

        // 2. Prevent clicking through notes to main menu buttons:
        // Iterate through notes in reverse order (top-most first)
        for (int i = mainmenunotebook$noteWidgets.size() - 1; i >= 0; i--) {
            StickyNoteWidget widget = mainmenunotebook$noteWidgets.get(i);
            if (widget.visible && widget.isMouseOver(event.x(), event.y())) {
                for (StickyNoteWidget other : mainmenunotebook$noteWidgets) {
                    if (other != widget) {
                        other.unfocusEditBox();
                    }
                }
                widget.mouseClicked(event, doubleClick);
                this.setFocused(widget);
                if (event.button() == 0) {
                    this.setDragging(true);
                }
                // Consume click so buttons underneath are never clicked
                cir.setReturnValue(true);
                return;
            }
        }

        // 3. Clicked outside all notes: unfocus all notes
        for (StickyNoteWidget widget : mainmenunotebook$noteWidgets) {
            widget.unfocusEditBox();
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        for (StickyNoteWidget widget : mainmenunotebook$noteWidgets) {
            if (widget.isEditing()) {
                widget.keyPressed(event);
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        for (StickyNoteWidget widget : mainmenunotebook$noteWidgets) {
            if (widget.isEditing()) {
                widget.charTyped(event);
                return true;
            }
        }
        return super.charTyped(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        for (StickyNoteWidget widget : mainmenunotebook$noteWidgets) {
            if (widget.isEditing()) {
                widget.keyReleased(event);
                return true;
            }
        }
        return super.keyReleased(event);
    }

    @Unique
    private StickyNoteWidget mainmenunotebook$addNoteWidget(StickyNote note) {
        int x = note.getX();
        int y = note.getY();

        StickyNoteWidget widget = new StickyNoteWidget(
            x, y, note.getWidth(), note.getHeight(),
            note, this.font, this.width, this.height,
            w -> {
                this.removeWidget(w);
                mainmenunotebook$noteWidgets.remove(w);
            },
            w -> {
                // Bring to front in both children and noteWidgets list
                this.removeWidget(w);
                this.addWidget(w);
                mainmenunotebook$noteWidgets.remove(w);
                mainmenunotebook$noteWidgets.add(w);
                StickyNoteManager.getInstance().bringToFront(note);
            }
        );

        mainmenunotebook$noteWidgets.add(widget);
        this.addWidget(widget);
        return widget;
    }
}
