package com.herobrot.heroslevels.events;

import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.util.LevelHelper;
import com.herobrot.heroslevels.util.PacketHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

@EventBusSubscriber(modid = HerosLevels.MOD_ID)
public class DatapackSyncEvents {

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) LevelHelper.applyAllSkills(event.getPlayer());
        else
            for (ServerPlayer player : event.getPlayerList().getPlayers()) {
                LevelHelper.applyAllSkills(player);
                PacketHelper.updateLevels(player);
            }
    }
}