package com.herobrot.heroslevels.mixin.block;

import com.herobrot.heroslevels.entity.LevelExperienceOrbEntity;
import com.herobrot.heroslevels.init.ConfigInit;
import com.herobrot.heroslevels.init.TagInit;
import com.herobrot.heroslevels.util.LevelHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {

    @Unique
    private static final ThreadLocal<Player> CURRENT_MINER = new ThreadLocal<>();

    @Unique
    private static final ThreadLocal<BlockState> CURRENT_BLOCK = new ThreadLocal<>();

    @Inject(method = "playerDestroy", at = @At("HEAD"))
    private void onPlayerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool, CallbackInfo ci) {
        if (!level.isClientSide()) {
            CURRENT_MINER.set(player);
            CURRENT_BLOCK.set(state);
        }
    }

    @Inject(method = "playerDestroy", at = @At("RETURN"))
    private void afterPlayerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool, CallbackInfo ci) {
        if (!level.isClientSide()) {
            CURRENT_MINER.remove();
            CURRENT_BLOCK.remove();
        }
    }

    @Inject(method = "popExperience", at = @At("HEAD"))
    private void onPopExperience(ServerLevel level, BlockPos pos, int amount, CallbackInfo ci) {
        Player player = CURRENT_MINER.get();
        BlockState minedState = CURRENT_BLOCK.get();
        if (minedState != null && minedState.is(TagInit.RESTRICTED_ORE_EXPERIENCE_BLOCKS)) return;
        if (player instanceof ServerPlayer serverPlayer && ConfigInit.CONFIG.oreXPMultiplier > 0.0F) {
            float multiplier = LevelHelper.getLevelBasedMultiplier(serverPlayer);
            int customXp = (int) (amount * ConfigInit.CONFIG.oreXPMultiplier * multiplier);
            if (customXp > 0) LevelExperienceOrbEntity.spawnCustomOrb(level, pos.getCenter(), customXp);
        }
    }
}