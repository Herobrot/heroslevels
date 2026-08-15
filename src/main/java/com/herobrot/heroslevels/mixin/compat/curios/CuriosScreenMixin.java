package com.herobrot.heroslevels.mixin.compat.curios;

import com.herobrot.heroslevels.util.ClientRestrictionHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.client.gui.CuriosScreen;

@Mixin(value = CuriosScreen.class, remap = false)
public class CuriosScreenMixin {

    @Inject(method = "renderSlot", at = @At("TAIL"))
    private void heroslevels$highlightRestrictedCuriosSlots(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        if (!slot.hasItem()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isCreative() || player.isSpectator()) return;
        ClientRestrictionHelper.renderRestrictedSlotOverlay(guiGraphics, slot, player);
    }
}