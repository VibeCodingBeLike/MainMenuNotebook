package kat.blasted.mainmenunotebook.mixin;

import kat.blasted.mainmenunotebook.client.gui.ScrollbarHidable;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractScrollArea.class)
public abstract class AbstractScrollAreaMixin implements ScrollbarHidable {
    @Unique
    private boolean mainmenunotebook$hideScrollbar = false;

    @Override
    public void mainmenunotebook$setHideScrollbar(boolean hide) {
        this.mainmenunotebook$hideScrollbar = hide;
    }

    @Override
    public boolean mainmenunotebook$isScrollbarHidden() {
        return this.mainmenunotebook$hideScrollbar;
    }

    @Inject(method = "extractScrollbar", at = @At("HEAD"), cancellable = true)
    private void mainmenunotebook$cancelScrollbar(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.mainmenunotebook$hideScrollbar) {
            ci.cancel();
        }
    }

    @Inject(method = "isOverScrollbar", at = @At("HEAD"), cancellable = true)
    private void mainmenunotebook$notOverScrollbar(double x, double y, CallbackInfoReturnable<Boolean> cir) {
        if (this.mainmenunotebook$hideScrollbar) {
            cir.setReturnValue(false);
        }
    }
}
