package com.herobrot.heroslevels.mixin.player;

import com.herobrot.heroslevels.util.ClientRestrictionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Minecraft.class, priority = 999)
public class MinecraftMixin {

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void continueAttackMixin(boolean leftClick, CallbackInfo info) {
        if (ClientRestrictionHelper.isAttackRestricted(this.player, false))
            info.cancel();
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void startAttackMixin(CallbackInfoReturnable<Boolean> info) {
        if (ClientRestrictionHelper.isAttackRestricted(this.player, true))
            info.setReturnValue(false);
    }
}