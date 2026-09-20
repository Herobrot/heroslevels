package com.herobrot.heroslevels.init;

import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.network.ClientPayloadHandler;
import com.herobrot.heroslevels.network.ServerPayloadHandler;
import com.herobrot.heroslevels.network.packet.*;
import com.herobrot.heroslib.network.PayloadRegistryManager;
import com.herobrot.heroslib.util.ModUtils;

public class NetworkInit {

    public static void init() {
        String mod_version = ModUtils.getModVersion(HerosLevels.MOD_ID);

        PayloadRegistryManager.registerBidirectional(HerosLevels.MOD_ID, mod_version,
                StatPacket.TYPE, StatPacket.STREAM_CODEC, ClientPayloadHandler::handleStat, ServerPayloadHandler::handleStat);
        PayloadRegistryManager.registerServerbound(HerosLevels.MOD_ID, mod_version,
                AttributeSyncPacket.TYPE, AttributeSyncPacket.STREAM_CODEC,
                (payload, context) -> ServerPayloadHandler.handleAttributeSync(context));

        PayloadRegistryManager.registerClientbound(HerosLevels.MOD_ID, mod_version,
                SkillSyncPacket.TYPE, SkillSyncPacket.STREAM_CODEC, ClientPayloadHandler::handleSkillSync);
        PayloadRegistryManager.registerClientbound(HerosLevels.MOD_ID, mod_version,
                PlayerSkillSyncPacket.TYPE, PlayerSkillSyncPacket.STREAM_CODEC, ClientPayloadHandler::handlePlayerSkillSync);
        PayloadRegistryManager.registerClientbound(HerosLevels.MOD_ID, mod_version,
                LevelPacket.TYPE, LevelPacket.STREAM_CODEC, ClientPayloadHandler::handleLevel);
        PayloadRegistryManager.registerClientbound(HerosLevels.MOD_ID, mod_version,
                EnchantmentPacket.TYPE, EnchantmentPacket.STREAM_CODEC, ClientPayloadHandler::handleEnchantment);
        PayloadRegistryManager.registerClientbound(HerosLevels.MOD_ID, mod_version,
                RestrictionsSyncPacket.TYPE, RestrictionsSyncPacket.STREAM_CODEC, ClientPayloadHandler::handleRestrictionsSync);
    }
}