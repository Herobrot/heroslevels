package com.herobrot.heroslevels.mixin.compat.enchantinginfuser;

import com.herobrot.heroslevels.compat.enchantinginfuser.EnchantingInfuserCompat;
import fuzs.enchantinginfuser.network.client.ServerboundEnchantmentLevelMessage;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = InfuserMenu.class, remap = false)
public class InfuserMenuMixin {

    @Inject(method = "clickClientEnchantmentLevelButton", at = @At("HEAD"), cancellable = true)
    private void onClientEnchantmentClick(Holder<Enchantment> enchantment, int enchantmentLevel, ServerboundEnchantmentLevelMessage.Operation operation, CallbackInfoReturnable<Boolean> cir) {
        if (operation == ServerboundEnchantmentLevelMessage.Operation.ADD_ALL) {
            int maxAllowed = enchantmentLevel;
            while (!EnchantingInfuserCompat.isEnchantmentRestricted(enchantment, maxAllowed + 1)) {
                maxAllowed++;
                if (maxAllowed > 100) break;
            }
            if (maxAllowed > enchantmentLevel) {
                InfuserMenu menu = (InfuserMenu)(Object)this;
                for (int i = enchantmentLevel; i < maxAllowed; i++)
                    menu.clickClientEnchantmentLevelButton(enchantment, i, ServerboundEnchantmentLevelMessage.Operation.ADD);
            }
            cir.setReturnValue(true);
        }
    }
}