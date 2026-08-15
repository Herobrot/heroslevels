package com.herobrot.heroslevels.mixin.client;

import com.herobrot.heroslevels.util.ClientRestrictionHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Inject(method = "renderSlot", at = @At("TAIL"))
    private void heroslevels$highlightRestrictedSlots(GuiGraphics graphics, Slot slot, CallbackInfo ci) {
        if (!slot.hasItem()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isCreative() || player.isSpectator()) return;
        ClientRestrictionHelper.renderRestrictedSlotOverlay(graphics, slot, player);
    }
}