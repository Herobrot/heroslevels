package com.herobrot.heroslevels.mixin.inventory;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.level.LevelManager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin {

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void heroslevels$restrictOffhandPlacement(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Slot slot = (Slot) (Object) this;
        if (slot.container instanceof Inventory inventory) {
            Player player = inventory.player;
            if (player != null && !player.isCreative() && !player.isSpectator())
                if (slot.getContainerSlot() == 40) {
                    LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
                    if (!levelManager.hasRequiredItemAndEnchantmentLevel(stack))
                        cir.setReturnValue(false);
                }
        }
    }
}