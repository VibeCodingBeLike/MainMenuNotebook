package kat.blasted.mainmenunotebook.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import kat.blasted.mainmenunotebook.model.StickyNote;
import kat.blasted.mainmenunotebook.model.StickyNoteColor;
import kat.blasted.mainmenunotebook.storage.StickyNoteManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class StickyNoteWidget extends AbstractWidget {
    public static final int HEADER_HEIGHT = 16;
    public static final int PADDING = 4;
    public static final int MIN_WIDTH = 90;
    public static final int MIN_HEIGHT = 55;
    public static final int MAX_WIDTH = 340;
    public static final int MAX_HEIGHT = 280;
    public static final int BTN_SIZE = 12;

    private final StickyNote note;
    private final Font font;
    private final int screenWidth;
    private final int screenHeight;
    private final Consumer<StickyNoteWidget> onRemove;
    private final Consumer<StickyNoteWidget> onBringToFront;

    private MultiLineEditBox editBox;
    private boolean isDraggingHeader = false;
    private double dragStartX, dragStartY;
    private int initialWidgetX, initialWidgetY;

    private boolean isResizing = false;
    private int initialWidth, initialHeight;

    public StickyNoteWidget(
        int x,
        int y,
        int width,
        int height,
        StickyNote note,
        Font font,
        int screenWidth,
        int screenHeight,
        Consumer<StickyNoteWidget> onRemove,
        Consumer<StickyNoteWidget> onBringToFront
    ) {
        super(x, y, width, height, Component.literal("Sticky Note"));
        this.note = note;
        this.font = font;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.onRemove = onRemove;
        this.onBringToFront = onBringToFront;

        rebuildEditBox();
    }

    private void rebuildEditBox() {
        boolean wasFocused = this.editBox != null && this.editBox.isFocused();
        String currentText = this.editBox != null ? this.editBox.getValue() : note.getText();

        int boxX = getX() + PADDING;
        int boxY = getY() + HEADER_HEIGHT + PADDING;
        int boxW = Math.max(10, getWidth() - PADDING * 2);
        int boxH = Math.max(10, getHeight() - HEADER_HEIGHT - PADDING * 2);

        this.editBox = MultiLineEditBox.builder()
            .setX(boxX)
            .setY(boxY)
            .setPlaceholder(Component.literal("Write a note..."))
            .setShowBackground(false)
            .setShowDecorations(false)
            .setTextColor(note.getColor().getTextColor())
            .setTextShadow(false)
            .setCursorColor(0xFF333333)
            .build(font, boxW, boxH, Component.literal("Note content"));

        // Hide scrollbar on the edit box
        if (this.editBox instanceof ScrollbarHidable hidable) {
            hidable.mainmenunotebook$setHideScrollbar(true);
        }

        this.editBox.setValue(currentText);
        this.editBox.setValueListener(text -> {
            note.setText(text);
            StickyNoteManager.getInstance().save();
        });

        if (wasFocused) {
            this.editBox.setFocused(true);
        }
    }

    // Button hitboxes: 12x12 each, 2px top margin, 2px right margin, 2px gap between buttons
    private boolean isOverColor(double mx, double my) {
        int right = getX() + getWidth();
        return mx >= right - 42 && mx < right - 30 && my >= getY() + 2 && my < getY() + 14;
    }

    private boolean isOverMinimize(double mx, double my) {
        int right = getX() + getWidth();
        return mx >= right - 28 && mx < right - 16 && my >= getY() + 2 && my < getY() + 14;
    }

    private boolean isOverClose(double mx, double my) {
        int right = getX() + getWidth();
        return mx >= right - 14 && mx < right - 2 && my >= getY() + 2 && my < getY() + 14;
    }

    private boolean isOverResize(double mx, double my) {
        if (note.isMinimized()) return false;
        int rx = getX() + getWidth();
        int ry = getY() + getHeight();
        return mx >= rx - 10 && mx <= rx && my >= ry - 10 && my <= ry;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (!this.visible) return false;
        int curH = note.isMinimized() ? HEADER_HEIGHT : getHeight();
        return mouseX >= this.getX() && mouseX < this.getX() + this.getWidth() &&
               mouseY >= this.getY() && mouseY < this.getY() + curH;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!isMouseOver(event.x(), event.y())) {
            if (this.editBox.isFocused()) {
                this.editBox.setFocused(false);
            }
            return false;
        }

        if (onBringToFront != null) {
            onBringToFront.accept(this);
        }

        // Close button (rightmost)
        if (isOverClose(event.x(), event.y())) {
            AbstractWidget.playButtonClickSound(Minecraft.getInstance().getSoundManager());
            unfocusEditBox();
            StickyNoteManager.getInstance().removeNote(note);
            if (onRemove != null) {
                onRemove.accept(this);
            }
            return true;
        }

        // Minimize / Expand button (middle)
        if (isOverMinimize(event.x(), event.y())) {
            AbstractWidget.playButtonClickSound(Minecraft.getInstance().getSoundManager());
            note.setMinimized(!note.isMinimized());
            StickyNoteManager.getInstance().save();
            if (note.isMinimized()) {
                unfocusEditBox();
            }
            return true;
        }

        // Color cycle button (left of - and x)
        if (isOverColor(event.x(), event.y())) {
            AbstractWidget.playButtonClickSound(Minecraft.getInstance().getSoundManager());
            note.cycleColor();
            StickyNoteManager.getInstance().save();
            rebuildEditBox();
            return true;
        }

        // Resize handle
        if (isOverResize(event.x(), event.y())) {
            this.isResizing = true;
            this.dragStartX = event.x();
            this.dragStartY = event.y();
            this.initialWidth = this.getWidth();
            this.initialHeight = this.getHeight();
            return true;
        }

        // Header dragging
        if (event.y() < getY() + HEADER_HEIGHT && event.button() == 0) {
            if (activeEditingWidget == this) {
                activeEditingWidget = null;
            }
            if (this.editBox != null) {
                this.editBox.setFocused(false);
            }
            this.isDraggingHeader = true;
            this.dragStartX = event.x();
            this.dragStartY = event.y();
            this.initialWidgetX = this.getX();
            this.initialWidgetY = this.getY();
            return true;
        }

        // Content area editing
        if (!note.isMinimized() && event.y() >= getY() + HEADER_HEIGHT) {
            this.editBox.mouseClicked(event, doubleClick);
            this.editBox.setFocused(true);
            this.setFocused(true);
            activeEditingWidget = this;
            return true;
        }

        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.isDraggingHeader) {
            int newX = (int) (initialWidgetX + (event.x() - dragStartX));
            int newY = (int) (initialWidgetY + (event.y() - dragStartY));
            int curH = note.isMinimized() ? HEADER_HEIGHT : getHeight();

            newX = Math.clamp(newX, 0, Math.max(0, screenWidth - getWidth()));
            newY = Math.clamp(newY, 0, Math.max(0, screenHeight - curH));

            this.setX(newX);
            this.setY(newY);

            this.editBox.setX(newX + PADDING);
            this.editBox.setY(newY + HEADER_HEIGHT + PADDING);

            note.setX(newX);
            note.setY(newY);
            StickyNoteManager.getInstance().save();
            return true;
        }

        if (this.isResizing) {
            int newW = (int) (initialWidth + (event.x() - dragStartX));
            int newH = (int) (initialHeight + (event.y() - dragStartY));

            newW = Math.max(MIN_WIDTH, Math.min(newW, Math.min(MAX_WIDTH, screenWidth - getX())));
            newH = Math.max(MIN_HEIGHT, Math.min(newH, Math.min(MAX_HEIGHT, screenHeight - getY())));

            this.setWidth(newW);
            this.setHeight(newH);

            this.editBox.setWidth(newW - PADDING * 2);
            this.editBox.setHeight(newH - HEADER_HEIGHT - PADDING * 2);

            note.setWidth(newW);
            note.setHeight(newH);
            StickyNoteManager.getInstance().save();
            return true;
        }

        if (!note.isMinimized() && this.editBox.isFocused()) {
            return this.editBox.mouseDragged(event, dx, dy);
        }

        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean hadResize = this.isResizing;
        this.isDraggingHeader = false;
        this.isResizing = false;

        if (hadResize) {
            rebuildEditBox();
        }

        if (!note.isMinimized()) {
            this.editBox.mouseReleased(event);
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!note.isMinimized() && isMouseOver(mouseX, mouseY)) {
            return this.editBox.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private static StickyNoteWidget activeEditingWidget = null;

    public static boolean isAnyEditing() {
        return activeEditingWidget != null && activeEditingWidget.isEditing();
    }

    public static void clearActiveEditing() {
        activeEditingWidget = null;
    }

    public boolean isEditing() {
        return !note.isMinimized() && this.editBox != null && this.editBox.isFocused();
    }

    public void unfocusEditBox() {
        if (activeEditingWidget == this) {
            activeEditingWidget = null;
        }
        if (this.editBox != null) {
            this.editBox.setFocused(false);
        }
        this.setFocused(false);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (isEditing()) {
            if (event.key() == InputConstants.KEY_ESCAPE) {
                unfocusEditBox();
                return true;
            }
            this.editBox.keyPressed(event);
            // While editing, consume all key presses so other keybind actions and navigation are ignored
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (isEditing()) {
            this.editBox.charTyped(event);
            return true;
        }
        return super.charTyped(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (isEditing()) {
            this.editBox.keyReleased(event);
            return true;
        }
        return super.keyReleased(event);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            if (activeEditingWidget == this) {
                activeEditingWidget = null;
            }
            if (this.editBox != null) {
                this.editBox.setFocused(false);
            }
        }
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int curH = note.isMinimized() ? HEADER_HEIGHT : getHeight();
        StickyNoteColor col = note.getColor();

        // 1. Soft drop shadow
        graphics.fill(x + 2, y + 2, x + w + 2, y + curH + 2, 0x30000000);

        // 2. Note body
        graphics.fill(x, y, x + w, y + curH, col.getBackground());

        // 3. Subtle outer border
        graphics.fill(x, y, x + 1, y + curH, col.getBorder());
        graphics.fill(x + w - 1, y, x + w, y + curH, col.getBorder());
        graphics.fill(x, y + curH - 1, x + w, y + curH, col.getBorder());
        graphics.fill(x, y, x + w, y + 1, col.getHeader());

        // 4. Header bar
        graphics.fill(x, y, x + w, y + HEADER_HEIGHT, col.getHeader());
        graphics.fill(x, y + HEADER_HEIGHT - 1, x + w, y + HEADER_HEIGHT, col.getBorder());

        // 5. Header title (clean text, no pin icon)
        graphics.text(font, "Note", x + 6, y + 4, col.getTextColor(), false);

        // 6. Header buttons (Ordered left-to-right: Color [●], Minimize [-], Close [✕])
        // Each button is exactly 12x12 at y + 2
        int right = x + w;
        int yBtn = y + 2;

        int colorBtnX = right - 42;
        int minBtnX = right - 28;
        int closeBtnX = right - 14;

        // --- Color palette button (left of - and ✕) ---
        if (isOverColor(mouseX, mouseY)) {
            graphics.fill(colorBtnX, yBtn, colorBtnX + BTN_SIZE, yBtn + BTN_SIZE, 0x33000000);
        }
        int dotColor = col.getTextColor();
        // Centered 6x6 circular bullet ● with 3px margins on all 4 sides
        graphics.fill(colorBtnX + 4, yBtn + 3, colorBtnX + 8, yBtn + 4, dotColor);
        graphics.fill(colorBtnX + 3, yBtn + 4, colorBtnX + 9, yBtn + 8, dotColor);
        graphics.fill(colorBtnX + 4, yBtn + 8, colorBtnX + 8, yBtn + 9, dotColor);

        // --- Minimize / unfold button (middle) ---
        if (isOverMinimize(mouseX, mouseY)) {
            graphics.fill(minBtnX, yBtn, minBtnX + BTN_SIZE, yBtn + BTN_SIZE, 0x33000000);
        }
        if (!note.isMinimized()) {
            // Centered 6x2 minus bar with 3px horizontal and 5px vertical margins
            graphics.fill(minBtnX + 3, yBtn + 5, minBtnX + 9, yBtn + 7, col.getTextColor());
        } else {
            // Centered 6x6 plus sign with 3px margins on all 4 sides
            graphics.fill(minBtnX + 3, yBtn + 5, minBtnX + 9, yBtn + 7, col.getTextColor());
            graphics.fill(minBtnX + 5, yBtn + 3, minBtnX + 7, yBtn + 9, col.getTextColor());
        }

        // --- Close button (rightmost) ---
        boolean closeHover = isOverClose(mouseX, mouseY);
        if (closeHover) {
            graphics.fill(closeBtnX, yBtn, closeBtnX + BTN_SIZE, yBtn + BTN_SIZE, 0x55E53935);
        }
        int closeCol = closeHover ? 0xFFFFFFFF : col.getTextColor();
        // Centered bold 6x6 pixel cross (2px line weight) with 3px margins on all 4 sides
        int bx = closeBtnX + 3;
        int by = yBtn + 3;
        // Row 0: ##..##
        graphics.fill(bx, by, bx + 2, by + 1, closeCol);
        graphics.fill(bx + 4, by, bx + 6, by + 1, closeCol);
        // Row 1: .####.
        graphics.fill(bx + 1, by + 1, bx + 5, by + 2, closeCol);
        // Rows 2 & 3: ..##..
        graphics.fill(bx + 2, by + 2, bx + 4, by + 4, closeCol);
        // Row 4: .####.
        graphics.fill(bx + 1, by + 4, bx + 5, by + 5, closeCol);
        // Row 5: ##..##
        graphics.fill(bx, by + 5, bx + 2, by + 6, closeCol);
        graphics.fill(bx + 4, by + 5, bx + 6, by + 6, closeCol);

        // 7. Content area & resize handle (when not minimized)
        if (!note.isMinimized()) {
            this.editBox.extractRenderState(graphics, mouseX, mouseY, a);

            // Resize handle at bottom-right
            int rx = x + w;
            int ry = y + curH;
            int handleCol = isOverResize(mouseX, mouseY) ? 0xAA000000 : 0x44000000;
            graphics.fill(rx - 3, ry - 3, rx - 1, ry - 1, handleCol);
            graphics.fill(rx - 6, ry - 3, rx - 4, ry - 1, handleCol);
            graphics.fill(rx - 3, ry - 6, rx - 1, ry - 4, handleCol);
            graphics.fill(rx - 9, ry - 3, rx - 7, ry - 1, handleCol);
            graphics.fill(rx - 6, ry - 6, rx - 4, ry - 4, handleCol);
            graphics.fill(rx - 3, ry - 9, rx - 1, ry - 7, handleCol);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal("Sticky note: " + note.getText()));
    }

    public StickyNote getNote() {
        return note;
    }
}
