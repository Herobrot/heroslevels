package com.herobrot.heroslevels.mixin.entity;

import com.herobrot.heroslevels.HerosLevels;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.herobrot.heroslevels.util.BonusHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ThrownPotion.class)
public class ThrownPotionMixin {

    @WrapOperation(method = "applySplash", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean wrapSplashEffect(LivingEntity livingEntity, MobEffectInstance effectInstance, Entity source, Operation<Boolean> original) {
        if (source instanceof Player player)
            effectInstance = BonusHelper.applyPotionBonuses(player, effectInstance);
        return original.call(livingEntity, effectInstance, source);
    }

    @WrapOperation(
            method = "applySplash",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffect;applyInstantenousEffect(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/LivingEntity;ID)V")
    )
    private void wrapInstantSplashEffect(MobEffect effect, Entity source, Entity indirectSource, LivingEntity livingEntity, int amplifier, double health, Operation<Void> original) {
        if (indirectSource instanceof Player player)
            amplifier += BonusHelper.getPotionAmplifierBoost(player);
        original.call(effect, source, indirectSource, livingEntity, amplifier, health);
    }

    @ModifyArg(
            method = "makeAreaOfEffectCloud",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),
            index = 0
    )
    private Entity heroslevels$modifyCloudBeforeSpawn(Entity entity) {
        if (entity instanceof AreaEffectCloud cloud) {
            Entity owner = ((Projectile)(Object)this).getOwner();
            if (owner instanceof Player player) {
                float radiusBonus = BonusHelper.getLingeringCloudRadiusBonus(player);
                if (radiusBonus > 0) cloud.setRadius(cloud.getRadius() + radiusBonus);
                int durationBonus = BonusHelper.getLingeringCloudDurationBonus(player);
                if (durationBonus > 0) cloud.setDuration(cloud.getDuration() + durationBonus);
                cloud.setRadiusPerTick(-cloud.getRadius() / (float) cloud.getDuration());
            }
        }
        return entity;
    }
}