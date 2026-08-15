package com.herobrot.heroslevels.mixin.compat.enchantinginfuser;

import com.herobrot.heroslevels.compat.enchantinginfuser.EnchantingInfuserCompat;
import fuzs.enchantinginfuser.client.gui.screens.inventory.EnchantmentComponent;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

@Mixin(value = EnchantmentComponent.class, remap = false)
public class EnchantmentComponentMixin {

    @Inject(method = "create", at = @At("RETURN"))
    private static void onCreate(Holder<Enchantment> enchantment, InfuserMenu.EnchantmentValues values, ItemEnchantments itemEnchants, CallbackInfoReturnable<EnchantmentComponent> cir) {
        EnchantingInfuserCompat.CACHE.put(cir.getReturnValue(), enchantment);
    }

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void onGetDisplayName(Holder<Enchantment> enchantment, int maxWidth, Font font, int enchantmentSeed, CallbackInfoReturnable<Component> cir) {
        if (EnchantingInfuserCompat.isEnchantmentRestricted(enchantment, 1)) {
            int enchantmentId = Objects.requireNonNull(Minecraft.getInstance().getConnection()).registryAccess().registryOrThrow(Registries.ENCHANTMENT).getIdOrThrow(enchantment.value());
            EnchantmentNames.getInstance().initSeed(enchantmentSeed + enchantmentId);
            int width = (int) (maxWidth * 0.72F);
            FormattedText randomName = EnchantmentNames.getInstance().getRandomName(font, width);
            List<FormattedCharSequence> lines = font.split(randomName, width);
            if (!lines.isEmpty()) cir.setReturnValue(fuzs.puzzleslib.api.util.v1.ComponentHelper.getAsComponent(lines.getFirst()));
            else cir.setReturnValue(Component.literal("???????"));
        }
    }

    @Inject(method = "getTooltip", at = @At("HEAD"), cancellable = true)
    private void onGetTooltip(Holder<Enchantment> enchantment, CallbackInfoReturnable<List<Component>> cir) {
        if (EnchantingInfuserCompat.isEnchantmentRestricted(enchantment, 1))
            cir.setReturnValue(EnchantingInfuserCompat.getRestrictionTooltipLines(enchantment, 1));
    }

    @Inject(method = "getWeakPowerTooltip", at = @At("HEAD"), cancellable = true)
    private void onGetWeakPowerTooltip(Component component, CallbackInfoReturnable<List<Component>> cir) {
        EnchantmentComponent instance = (EnchantmentComponent)(Object)this;
        Holder<Enchantment> ench = EnchantingInfuserCompat.CACHE.get(instance);
        if (ench != null) {
            int nextLevel = instance.enchantmentLevel() + 1;
            if (EnchantingInfuserCompat.isEnchantmentRestricted(ench, nextLevel))
                cir.setReturnValue(EnchantingInfuserCompat.getRestrictionTooltipLines(ench, nextLevel));
        }
    }
}