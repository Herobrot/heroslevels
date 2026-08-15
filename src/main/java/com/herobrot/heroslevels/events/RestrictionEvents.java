package com.herobrot.heroslevels.events;

import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.util.RestrictionHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = HerosLevels.MOD_ID)
public class RestrictionEvents {

    private static boolean isMiningRestricted(Player player, LevelManager levelManager, Block block) {
        return !levelManager.hasRequiredMiningLevel(block)
                || !levelManager.hasRequiredItemAndEnchantmentLevel(player.getMainHandItem());
    }

    @SubscribeEvent
    public static void onBlockHit(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (!player.isCreative() && !player.isSpectator()) {
            LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
            Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
            if (isMiningRestricted(player, levelManager, block)) {
                RestrictionHelper.sendLockedMessage(player);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (!player.isCreative() && !player.isSpectator()) {
            LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
            if (isMiningRestricted(player, levelManager, event.getState().getBlock())) {
                RestrictionHelper.sendLockedMessage(player);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onItemUse(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (!player.isCreative() && !player.isSpectator()) {
            LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
            if (!levelManager.hasRequiredItemAndEnchantmentLevel(event.getItemStack())) {
                RestrictionHelper.sendLockedMessage(player);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.isCreative() || player.isSpectator()) return;
        if (!event.getLevel().mayInteract(player, event.getPos())) return;
        LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
        boolean blockLocked = !levelManager.hasRequiredBlockLevel(event.getLevel().getBlockState(event.getPos()).getBlock());
        boolean handLocked = !levelManager.hasRequiredItemAndEnchantmentLevel(event.getItemStack());
        if (blockLocked) {
            RestrictionHelper.sendLockedMessage(player);
            event.setUseBlock(TriState.FALSE);
        }
        if (handLocked) event.setUseItem(TriState.FALSE);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (!player.isCreative() && !player.isSpectator()) {
            LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
            if (!levelManager.hasRequiredEntityLevel(event.getTarget().getType())) {
                RestrictionHelper.sendLockedMessage(player);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!player.isCreative() && !player.isSpectator()) {
            LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
            if (!levelManager.hasRequiredItemAndEnchantmentLevel(player.getMainHandItem())) {
                RestrictionHelper.sendLockedMessageClient(player);
                event.setCanceled(true);
            }
        }
    }
}