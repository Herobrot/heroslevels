package com.herobrot.heroslevels.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.herobrot.heroslevels.util.BonusHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AreaEffectCloud.class)
public class AreaEffectCloudMixin {

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean wrapCloudEffect(LivingEntity livingEntity, MobEffectInstance effectInstance, Entity source, Operation<Boolean> original) {
        if (source instanceof AreaEffectCloud cloud && cloud.getOwner() instanceof Player player)
            effectInstance = BonusHelper.applyPotionBonuses(player, effectInstance);
        return original.call(livingEntity, effectInstance, source);
    }

    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffect;applyInstantenousEffect(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/LivingEntity;ID)V")
    )
    private void wrapInstantCloudEffect(MobEffect effect, Entity source, Entity indirectSource, LivingEntity livingEntity, int amplifier, double health, Operation<Void> original) {
        if (indirectSource instanceof Player player) amplifier += BonusHelper.getPotionAmplifierBoost(player);
        original.call(effect, source, indirectSource, livingEntity, amplifier, health);
    }
}