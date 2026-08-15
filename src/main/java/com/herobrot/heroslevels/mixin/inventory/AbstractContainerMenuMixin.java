package com.herobrot.heroslevels.mixin.inventory;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.util.RestrictionHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {

    @Inject(method = "removed", at = @At("TAIL"))
    private void heroslevels$onBrewingMenuClosed(Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (((Object)this) instanceof BrewingStandMenu menu) {
                Container container = menu.getSlot(0).container;
                if (container instanceof BrewingStandBlockEntity blockEntity)
                    RestrictionHelper.handleMenuClose(serverPlayer, blockEntity);
            }
            else if (((Object)this) instanceof AbstractFurnaceMenu menu) {
                Container container = menu.getSlot(0).container;
                if (container instanceof AbstractFurnaceBlockEntity blockEntity)
                    RestrictionHelper.handleFurnaceMenuClose(serverPlayer, blockEntity);
            }
        }
    }
}