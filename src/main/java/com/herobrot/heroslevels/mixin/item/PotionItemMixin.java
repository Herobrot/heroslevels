package com.herobrot.heroslevels.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.herobrot.heroslevels.util.BonusHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(PotionItem.class)
public class PotionItemMixin {

    @WrapOperation(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionContents;forEachEffect(Ljava/util/function/Consumer;)V"))
    private void wrapForEachEffect(PotionContents instance, Consumer<MobEffectInstance> originalConsumer, Operation<Void> original, ItemStack stack, Level level, LivingEntity entityLiving) {
        Consumer<MobEffectInstance> wrappedConsumer = (effect) -> {
            if (entityLiving instanceof Player player)
                effect = BonusHelper.applyPotionBonuses(player, effect);
            originalConsumer.accept(effect);
        };
        original.call(instance, wrappedConsumer);
    }
}