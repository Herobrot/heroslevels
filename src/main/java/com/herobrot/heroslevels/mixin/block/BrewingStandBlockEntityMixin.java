package com.herobrot.heroslevels.mixin.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.herobrot.heroslevels.util.RestrictionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {

    @WrapOperation(
            method = "serverTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;isBrewable(Lnet/minecraft/world/item/alchemy/PotionBrewing;Lnet/minecraft/core/NonNullList;)Z")
    )
    private static boolean heroslevels$wrapIsBrewable(PotionBrewing potionBrewing, NonNullList<ItemStack> items, Operation<Boolean> original, Level level, BlockPos pos, BlockState state, BrewingStandBlockEntity blockEntity) {
        boolean isBrewable = original.call(potionBrewing, items);
        if (!isBrewable) return false;
        return !RestrictionHelper.isBrewingRestrictedForViewers(level, pos, blockEntity);
    }
}