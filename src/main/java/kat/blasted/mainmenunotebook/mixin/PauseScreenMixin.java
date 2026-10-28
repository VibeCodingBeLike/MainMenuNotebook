package kat.blasted.mainmenunotebook.mixin;

import kat.blasted.mainmenunotebook.client.gui.StickyNoteScreenHost;
import kat.blasted.mainmenunotebook.model.NoteScope;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
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
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void mainmenunotebook$renderStickyNotesAtFront(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (mainmenunotebook$host != null) {
            mainmenunotebook$host.render(graphics, mouseX, mouseY, a);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mainmenunotebook$onMouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (mainmenunotebook$host != null && mainmenunotebook$host.mouseClicked(event, doubleClick)) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (mainmenunotebook$host != null && mainmenunotebook$host.keyPressed(event)) {
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (mainmenunotebook$host != null && mainmenunotebook$host.charTyped(event)) {
            return true;
        }
        return super.charTyped(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (mainmenunotebook$host != null && mainmenunotebook$host.keyReleased(event)) {
            return true;
        }
        return super.keyReleased(event);
    }
}
