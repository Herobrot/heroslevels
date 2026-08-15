package com.herobrot.heroslevels.mixin.entity;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.level.LevelManager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "getEquipmentSlotForItem", at = @At("HEAD"), cancellable = true)
    private void heroslevels$restrictArmorSlot(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> cir) {
        if ((Object) this instanceof Player player && !player.isCreative() && !player.isSpectator()) {
            LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
            if (!levelManager.hasRequiredItemAndEnchantmentLevel(stack))
                cir.setReturnValue(EquipmentSlot.MAINHAND);
        }
    }
}