package kat.blasted.mainmenunotebook.mixin;

import kat.blasted.mainmenunotebook.client.gui.StickyNoteWidget;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void mainmenunotebook$onKeyPress(long window, int action, KeyEvent event, CallbackInfo ci) {
        if (StickyNoteWidget.isAnyEditing()) {
            if (action == 1 || action == 2) {
                if (this.minecraft.options.keyFullscreen.matches(event)) {
                    return;
                }
                if (this.minecraft.options.keyScreenshot.matches(event)) {
                    return;
                }
                Screen screen = this.minecraft.gui.screen();
                if (screen != null) {
                    screen.afterKeyboardAction();
                    screen.keyPressed(event);
                }
                ci.cancel();
            } else if (action == 0) {
                Screen screen = this.minecraft.gui.screen();
                if (screen != null) {
                    screen.keyReleased(event);
                }
                ci.cancel();
            }
        }
    }
}
