package com.herobrot.heroslevels.mixin.compat.twr;

import cn.mlus.thirst.content.thirst.PlayerThirst;
import com.herobrot.heroslevels.util.BonusHelper;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerThirst.class)
public class PlayerThirstMixin {

    @ModifyVariable(method = "addExhaustion(Lnet/minecraft/world/entity/player/Player;F)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float modifyThirstExhaustionMixin(float amount, Player player) {
        return amount * BonusHelper.exhaustionThirstReductionBonus(player);
    }
}