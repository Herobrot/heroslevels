package com.herobrot.heroslevels.mixin.compat.enchantinginfuser;

import com.herobrot.heroslevels.compat.enchantinginfuser.EnchantingInfuserCompat;
import fuzs.enchantinginfuser.client.gui.components.EnchantingOperationButton;
import fuzs.enchantinginfuser.client.gui.screens.inventory.EnchantmentComponent;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EnchantingOperationButton.Add.class, remap = false)
public class EnchantingOperationButtonAddMixin {

    @Inject(method = "getActiveValue", at = @At("HEAD"), cancellable = true)
    private void onGetActiveValue(EnchantmentComponent enchantmentComponent, CallbackInfoReturnable<Boolean> cir) {
        Holder<Enchantment> ench = EnchantingInfuserCompat.CACHE.get(enchantmentComponent);
        if (ench != null) {
            int nextLevel = enchantmentComponent.enchantmentLevel() + 1;
            if (EnchantingInfuserCompat.isEnchantmentRestricted(ench, nextLevel)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "getTooltipComponent", at = @At("HEAD"), cancellable = true)
    private void onGetTooltipComponent(EnchantmentComponent enchantmentComponent, CallbackInfoReturnable<Component> cir) {
        Holder<Enchantment> ench = EnchantingInfuserCompat.CACHE.get(enchantmentComponent);
        if (ench != null) {
            int nextLevel = enchantmentComponent.enchantmentLevel() + 1;
            if (EnchantingInfuserCompat.isEnchantmentRestricted(ench, nextLevel)) {
                cir.setReturnValue(Component.translatable("restriction.heroslevels.enchantment.locked_desc").withStyle(net.minecraft.ChatFormatting.GRAY));
            }
        }
    }
}