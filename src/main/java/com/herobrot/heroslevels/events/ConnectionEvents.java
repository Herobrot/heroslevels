package com.herobrot.heroslevels.events;

import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.init.ConfigInit;
import com.herobrot.heroslevels.util.LevelHelper;
import com.herobrot.heroslevels.util.PacketHelper;
import com.herobrot.heroslib.config.ConfigSyncHelper;
import com.herobrot.heroslib.network.GenericConfigSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = HerosLevels.MOD_ID)
public class ConnectionEvents {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) syncAllData(serverPlayer, true);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) syncAllData(serverPlayer, false);
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) syncAllData(serverPlayer, false);
    }

    private static void syncAllData(ServerPlayer serverPlayer, boolean isFirstLogin) {
        try {
            if (isFirstLogin) {
                String cleanJson = ConfigSyncHelper.SYNC_GSON.toJson(ConfigInit.CONFIG);
                PacketDistributor.sendToPlayer(serverPlayer, new GenericConfigSyncPayload(HerosLevels.MOD_ID, cleanJson));
                PacketHelper.updateSkills(serverPlayer);
                PacketHelper.syncEnchantments(serverPlayer);
                PacketHelper.syncRestrictions(serverPlayer);
            }
            PacketHelper.syncPlayerSkills(serverPlayer);
            PacketHelper.updateLevels(serverPlayer);
            LevelHelper.applyAllSkills(serverPlayer);
        } catch (Exception e) {
            HerosLevels.LOGGER.error("[Hero's Levels-ERROR]: Error sincronizando datos con el cliente para {}",
                    serverPlayer.getScoreboardName(), e);
        }
    }
}