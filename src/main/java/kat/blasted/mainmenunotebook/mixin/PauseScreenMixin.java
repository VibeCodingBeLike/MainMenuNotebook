package kat.blasted.mainmenunotebook.mixin;

import kat.blasted.mainmenunotebook.client.gui.StickyNoteScreenHost;
import kat.blasted.mainmenunotebook.model.NoteScope;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    @Unique
    private StickyNoteScreenHost mainmenunotebook$host;

    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void mainmenunotebook$init(CallbackInfo ci) {
        mainmenunotebook$host = new StickyNoteScreenHost(this, NoteScope.PAUSE_MENU, this::addWidget, this::removeWidget);
        mainmenunotebook$host.init();

        ScreenMouseEvents.allowMouseClick(this).register((screen, event) -> {
            if (mainmenunotebook$host != null && mainmenunotebook$host.mouseClicked(event, false)) {
                return false;
            }
            return true;
        });

        ScreenKeyboardEvents.allowKeyPress(this).register((screen, event) -> {
            if (mainmenunotebook$host != null && mainmenunotebook$host.keyPressed(event)) {
                return false;
            }
            return true;
        });

        ScreenKeyboardEvents.allowCharType(this).register((screen, event) -> {
            if (mainmenunotebook$host != null && mainmenunotebook$host.charTyped(event)) {
                return false;
            }
            return true;
        });

        ScreenKeyboardEvents.allowKeyRelease(this).register((screen, event) -> {
            if (mainmenunotebook$host != null && mainmenunotebook$host.keyReleased(event)) {
                return false;
            }
            return true;
        });
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void mainmenunotebook$renderStickyNotesAtFront(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (mainmenunotebook$host != null) {
            mainmenunotebook$host.render(graphics, mouseX, mouseY, a);
        }
    }
}
