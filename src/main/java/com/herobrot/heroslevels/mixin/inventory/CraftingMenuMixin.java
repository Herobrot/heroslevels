package com.herobrot.heroslevels.mixin.inventory;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.level.LevelManager;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {

    @Inject(method = "slotChangedCraftingGrid", at = @At("TAIL"))
    private static void onSlotChangedCraftingGridMixin(AbstractContainerMenu menu, Level level, Player player, CraftingContainer craftMatrix, ResultContainer resultMatrix, @Nullable RecipeHolder<CraftingRecipe> recipe, CallbackInfo ci) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer && !player.isCreative()) {
            ItemStack result = resultMatrix.getItem(0);
            if (!result.isEmpty()) {
                LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
                if (!levelManager.hasRequiredCraftingLevel(result.getItem())) {
                    resultMatrix.setItem(0, ItemStack.EMPTY);
                    menu.setRemoteSlot(0, ItemStack.EMPTY);
                    serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), 0, ItemStack.EMPTY));
                }
            }
        }
    }
}