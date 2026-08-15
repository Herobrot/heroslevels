package com.herobrot.heroslevels.mixin.misc;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.util.BonusHelper;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {

    @Shadow @Final private DataSlot cost;

    @SuppressWarnings("DataFlowIssue")
    public AnvilMenuMixin() {
        super(null, 0, null, null);
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    protected void mayPickupMixin(Player player, boolean hasItem, CallbackInfoReturnable<Boolean> info) {
        if (BonusHelper.anvilXpCapBonus(player)) info.setReturnValue(true);
    }

    @Inject(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V", ordinal = 4))
    private void createResultMixin(CallbackInfo info) {
        if (this.cost.get() > 1)
            this.cost.set(BonusHelper.anvilXpDiscountBonus(this.player, this.cost.get()));
    }

    @Inject(method = "onTake", at = @At("HEAD"))
    private void onTakeMixin(Player player, ItemStack stack, CallbackInfo info) {
        if (BonusHelper.anvilXpChanceBonus(player)) this.cost.set(0);
    }

    @SuppressWarnings({"resource", "ConstantConditions"})
    @Inject(method = "createResult", at = @At("TAIL"))
    private void heroslevels$restrictAnvilResult(CallbackInfo ci) {
        ItemStack result = this.resultSlots.getItem(0);
        if (!result.isEmpty()) {
            Player player = this.player;
            if (player != null && !player.isCreative() && !player.isSpectator()) {
                LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
                boolean restricted = !levelManager.hasRequiredCraftingLevel(result.getItem())
                        || !levelManager.hasRequiredItemAndEnchantmentLevel(result);
                if (restricted) {
                    this.resultSlots.setItem(0, ItemStack.EMPTY);
                    this.setRemoteSlot(2, ItemStack.EMPTY);
                    if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer)
                        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), 2, ItemStack.EMPTY));
                }
            }
        }
    }
}