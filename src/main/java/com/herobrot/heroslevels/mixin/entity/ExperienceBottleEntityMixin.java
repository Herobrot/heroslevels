package com.herobrot.heroslevels.mixin.entity;

import com.herobrot.heroslevels.entity.LevelExperienceOrbEntity;
import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.init.ConfigInit;
import com.herobrot.heroslevels.level.LevelManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownExperienceBottle.class)
public abstract class ExperienceBottleEntityMixin extends ThrowableItemProjectile {

    public ExperienceBottleEntityMixin(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    @SuppressWarnings("resource")
    @Inject(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"), cancellable = true)
    protected void onHitMixin(HitResult result, CallbackInfo info) {
        if (ConfigInit.CONFIG.bottleXPMultiplier > 0.0F && this.level() instanceof ServerLevel serverLevel) {
            int baseExp = 3 + this.level().random.nextInt(5) + this.level().random.nextInt(5);
            float multiplier = ConfigInit.CONFIG.bottleXPMultiplier;
            if (ConfigInit.CONFIG.dropXPBasedOnLvl && this.getOwner() instanceof ServerPlayer serverPlayer) {
                LevelManager levelManager = serverPlayer.getData(AttachmentInit.LEVEL_MANAGER);
                multiplier += ConfigInit.CONFIG.basedOnMultiplier * levelManager.getOverallLevel();
            }
            LevelExperienceOrbEntity.spawnCustomOrb(serverLevel, this.position(), (int) (baseExp * multiplier));
            this.discard();
            info.cancel();
        }
    }
}