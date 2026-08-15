package com.herobrot.heroslevels.mixin.inventory;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.level.LevelManager;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {

    @Shadow @Final public int[] costs;
    @Shadow @Final public int[] enchantClue;

    @Unique
    private Player heroslevels$player;

    @Unique
    private static boolean heroslevels$hasAnyAllowedLevel(Holder<Enchantment> holder, LevelManager levelManager) {
        int max = holder.value().getMaxLevel();
        for (int lvl = 1; lvl <= max; lvl++) if (levelManager.hasRequiredEnchantmentLevel(holder, lvl)) return true;
        return false;
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    private void onInit(int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess pAccess, CallbackInfo ci) {
        this.heroslevels$player = pPlayerInventory.player;
    }

    @Inject(method = "getEnchantmentList", at = @At("RETURN"), cancellable = true)
    private void onGetEnchantmentList(RegistryAccess pRegistryAccess, ItemStack pStack, int pEnchantSlot, int pLevel, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        if (this.heroslevels$player == null || this.heroslevels$player.isCreative()) return;
        LevelManager levelManager = this.heroslevels$player.getData(AttachmentInit.LEVEL_MANAGER);
        List<EnchantmentInstance> original = cir.getReturnValue();
        if (original == null || original.isEmpty()) return;
        List<EnchantmentInstance> filtered = new ArrayList<>();
        for (EnchantmentInstance inst : original)
            if (levelManager.hasRequiredEnchantmentLevel(inst.enchantment, inst.level))
                filtered.add(inst);

        if (filtered.isEmpty()) {
            Optional<HolderSet.Named<Enchantment>> optional = pRegistryAccess.registryOrThrow(Registries.ENCHANTMENT).getTag(EnchantmentTags.IN_ENCHANTING_TABLE);
            if (optional.isPresent()) {
                java.util.stream.Stream<Holder<Enchantment>> allowedStream = optional.get().stream()
                        .filter(holder -> heroslevels$hasAnyAllowedLevel(holder, levelManager));
                List<EnchantmentInstance> safeRngList = EnchantmentHelper.selectEnchantment(this.heroslevels$player.getRandom(), pStack, pLevel, allowedStream);
                for (EnchantmentInstance rngInst : safeRngList)
                    if (levelManager.hasRequiredEnchantmentLevel(rngInst.enchantment, rngInst.level))
                        filtered.add(rngInst);
            }
        }
        cir.setReturnValue(filtered);
    }

    @Inject(method = "slotsChanged", at = @At("RETURN"))
    private void onSlotsChanged(Container pInventory, CallbackInfo ci) {
        for (int i = 0; i < 3; i++) if (this.enchantClue[i] == -1) this.costs[i] = 0;
    }
}