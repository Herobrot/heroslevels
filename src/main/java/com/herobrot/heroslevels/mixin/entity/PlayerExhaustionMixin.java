package com.herobrot.heroslevels.mixin.entity;

import com.herobrot.heroslevels.util.BonusHelper;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public class PlayerExhaustionMixin {

    @ModifyVariable(method = "causeFoodExhaustion", at = @At("HEAD"), argsOnly = true)
    private float modifyExhaustionMixin(float originalAmount) {
        return originalAmount * BonusHelper.exhaustionReductionBonus((Player) (Object) this);
    }
}