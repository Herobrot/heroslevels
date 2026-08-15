package com.herobrot.heroslevels.mixin.compat.enchantinginfuser;

import com.herobrot.heroslevels.compat.enchantinginfuser.EnchantingInfuserCompat;
import fuzs.enchantinginfuser.client.gui.screens.inventory.EnchantmentComponent;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen$EnchantmentSelectionList$Entry", remap = false)
public class InfuserScreenEntryMixin {

    @Shadow @Final private EnchantmentComponent enchantmentComponent;

    @Inject(method = "getFontColor", at = @At("HEAD"), cancellable = true)
    private void onGetFontColor(CallbackInfoReturnable<Integer> cir) {
        Holder<Enchantment> ench = EnchantingInfuserCompat.CACHE.get(this.enchantmentComponent);
        if (ench != null && EnchantingInfuserCompat.isEnchantmentRestricted(ench, 1))
            cir.setReturnValue(0x685E4A);
    }

    @Inject(method = "getYImage", at = @At("HEAD"), cancellable = true)
    private void onGetYImage(CallbackInfoReturnable<Integer> cir) {
        Holder<Enchantment> ench = EnchantingInfuserCompat.CACHE.get(this.enchantmentComponent);
        if (ench != null && EnchantingInfuserCompat.isEnchantmentRestricted(ench, 1))
            cir.setReturnValue(0);
    }
}