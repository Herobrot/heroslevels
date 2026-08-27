package com.herobrot.heroslevels.init;

import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.config.HerosLevelConfig;
import com.herobrot.heroslevels.entity.render.LevelExperienceOrbEntityRenderer;
import com.herobrot.heroslevels.screen.LevelScreen;
import com.herobrot.heroslib.client.tab.TabDefinition;
import com.herobrot.heroslib.client.tab.TabRegistry;
import com.herobrot.heroslib.config.ConfigSyncHelper;
import com.herobrot.heroslib.config.HerosConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class RenderInit {

    public static void registerTabs() {
        Minecraft mc = Minecraft.getInstance();
        TabRegistry.registerInventoryTab(TabDefinition.builder(
                                ResourceLocation.withDefaultNamespace("inventory"),
                                Component.translatable("screen.heroslevels.inventory_screen"),
                                InventoryScreen.class,
                                0
                        )
                        .icon(ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "textures/gui/sprites/bag_tab_icon.png"))
                        .onOpen(() -> mc.player != null ? new InventoryScreen(mc.player) : null)
                        .build()
        );
        TabRegistry.registerInventoryTab(TabDefinition.builder(
                                ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "skills"),
                                Component.translatable("screen.heroslevels.skill_screen"),
                                LevelScreen.class,
                                1
                        )
                        .icon(ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "textures/gui/sprites/skill_tab_icon.png"))
                        .onOpen(LevelScreen::new)
                        .keyMapping(KeyInit.SCREEN_KEY)
                        .allowKeySwitch(() -> ConfigInit.CONFIG.switchScreen)
                        .build()
        );
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityInit.LEVEL_EXPERIENCE_ORB.get(), LevelExperienceOrbEntityRenderer::new);
    }

    public static void registerConfigSync() {
        HerosConfigManager.registerClientSync(HerosLevels.MOD_ID, (jsonReceived) -> {
            HerosLevelConfig serverConfig = ConfigSyncHelper.SYNC_GSON.fromJson(jsonReceived, HerosLevelConfig.class);
            if (serverConfig != null) {
                ConfigSyncHelper.mergeServerConfig(ConfigInit.CONFIG, serverConfig);
                HerosLevels.LOGGER.info("[Hero's Levels]: Configuración del servidor aplicada correctamente.");
            }
        });
    }
}