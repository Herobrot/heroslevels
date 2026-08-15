package com.herobrot.heroslevels.events;

import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.init.ConfigInit;
import com.herobrot.heroslevels.init.KeyInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.screen.LevelScreen;
import com.herobrot.heroslevels.util.ClientRestrictionHelper;
import com.herobrot.heroslevels.util.RestrictionHelper;
import com.herobrot.heroslevels.util.TooltipUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = HerosLevels.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    private static int cachedTabListLevel = -1;
    private static Component cachedTabListName = null;

    @SubscribeEvent
    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
            Minecraft client = Minecraft.getInstance();
            ClientRestrictionHelper.renderContainerRestrictions(event.getGuiGraphics(), client, screen, event.getMouseX(), event.getMouseY());
            ClientRestrictionHelper.renderInventoryLevel(event.getGuiGraphics(), client, screen);
        }
    }

    @SubscribeEvent
    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
        ClientRestrictionHelper.renderBlockHighlight(event);
    }

    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!ConfigInit.CONFIG.lockedHandUsage) return;
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && !client.player.isCreative() && !client.player.isSpectator()) {
            if (event.isAttack()) {
                LevelManager levelManager = client.player.getData(AttachmentInit.LEVEL_MANAGER);
                if (!levelManager.hasRequiredItemAndEnchantmentLevel(client.player.getMainHandItem())) {
                    RestrictionHelper.sendLockedMessageClient(client.player);
                    event.setCanceled(true);
                    event.setSwingHand(false);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (ConfigInit.CONFIG.showLevel && event.getEntity() instanceof Player player) {
            int level = player.getData(AttachmentInit.LEVEL_MANAGER).getOverallLevel();
            if (level != cachedTabListLevel || cachedTabListName == null) {
                cachedTabListLevel = level;
                cachedTabListName = Component.translatable("text.heroslevels.scoreboard", level, event.getContent());
            }
            event.setContent(cachedTabListName);
        }
    }

    @SubscribeEvent
    public static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        if (ConfigInit.CONFIG.showLevelList) {
            int level = event.getEntity().getData(AttachmentInit.LEVEL_MANAGER).getOverallLevel();
            if (level != cachedTabListLevel || cachedTabListName == null) {
                cachedTabListLevel = level;
                Component baseName = event.getDisplayName() != null ? event.getDisplayName() : event.getEntity().getName();
                cachedTabListName = Component.translatable("text.heroslevels.scoreboard", level, baseName);
            }
            event.setDisplayName(cachedTabListName);
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (event.getEntity() == null || event.getItemStack().isEmpty()) return;
        TooltipUtil.renderItemTooltip(Minecraft.getInstance(), event.getItemStack(), event.getToolTip());
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            if (!HerosLevels.isJadeLoaded && client.hitResult != null) {
                if (client.hitResult.getType() == HitResult.Type.BLOCK && client.level != null) {
                    BlockHitResult blockHit = (BlockHitResult) client.hitResult;
                    TooltipUtil.renderBlockOverlay(client, event.getGuiGraphics(), client.level.getBlockState(blockHit.getBlockPos()).getBlock());
                } else if (client.hitResult.getType() == HitResult.Type.ENTITY) {
                    EntityHitResult entityHit = (EntityHitResult) client.hitResult;
                    TooltipUtil.renderEntityOverlay(client, event.getGuiGraphics(), entityHit.getEntity().getType());
                }
            }
            ClientRestrictionHelper.renderHotbarRestrictions(event.getGuiGraphics(), client.player);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        while (KeyInit.SCREEN_KEY.consumeClick())
            if (client.screen == null) client.setScreen(new LevelScreen());
    }
}