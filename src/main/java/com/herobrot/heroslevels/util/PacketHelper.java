package com.herobrot.heroslevels.util;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.level.Skill;
import com.herobrot.heroslevels.level.SkillBonus;
import com.herobrot.heroslevels.network.packet.*;
import com.herobrot.heroslevels.registry.EnchantmentRegistry;
import com.herobrot.heroslevels.registry.HerosEnchantment;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PacketHelper {

    public static void refreshTabListDisplay(ServerPlayer serverPlayer) {
        serverPlayer.server.getPlayerList().broadcastAll(
                new ClientboundPlayerInfoUpdatePacket(
                        ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE,
                        serverPlayer
                )
        );
    }

    public static void updateLevels(ServerPlayer serverPlayer) {
        LevelManager levelManager = serverPlayer.getData(AttachmentInit.LEVEL_MANAGER);
        PacketDistributor.sendToPlayer(serverPlayer, new LevelPacket(
                levelManager.getOverallLevel(),
                levelManager.getSkillPoints(),
                levelManager.getTotalLevelExperience(),
                levelManager.getLevelProgress()
        ));
    }

    public static void updateSkills(ServerPlayer serverPlayer) {
        List<Integer> skillIds = new ArrayList<>();
        List<String> skillKeys = new ArrayList<>();
        List<Integer> skillMaxLevels = new ArrayList<>();
        List<SkillSyncPacket.SkillAttributesRecord> skillAttributes = new ArrayList<>();
        List<SkillBonus> skillBonuses = new ArrayList<>();
        for (List<SkillBonus> contributions : LevelManager.BONUSES.values())
            skillBonuses.addAll(contributions);
        for (Skill skill : LevelManager.SKILLS.values()) {
            skillIds.add(skill.id());
            skillKeys.add(skill.key());
            skillMaxLevels.add(skill.maxLevel());
            skillAttributes.add(new SkillSyncPacket.SkillAttributesRecord(new ArrayList<>(skill.attributes())));
        }
        PacketDistributor.sendToPlayer(serverPlayer, new SkillSyncPacket(
                skillIds, skillKeys, skillMaxLevels, skillAttributes, new SkillSyncPacket.SkillBonusesRecord(skillBonuses)
        ));
    }

    public static void syncPlayerSkills(ServerPlayer serverPlayer) {
        LevelManager levelManager = serverPlayer.getData(AttachmentInit.LEVEL_MANAGER);
        List<PlayerSkillSyncPacket.PlayerSkillRecord> skills = levelManager.getPlayerSkills().values().stream()
                .map(playerSkill -> new PlayerSkillSyncPacket.PlayerSkillRecord(playerSkill.getId(), playerSkill.getLevel()))
                .toList();
        PacketDistributor.sendToPlayer(serverPlayer, new PlayerSkillSyncPacket(skills));
    }

    public static void syncEnchantments(ServerPlayer serverPlayer) {
        List<Integer> keys = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        List<Integer> levels = new ArrayList<>();
        for (Map.Entry<Integer, HerosEnchantment> entry : EnchantmentRegistry.ENCHANTMENTS.entrySet()) {
            entry.getValue().entry().unwrapKey().ifPresent(resourceKey -> {
                keys.add(entry.getKey());
                ids.add(resourceKey.location().toString());
                levels.add(entry.getValue().level());
            });
        }
        PacketDistributor.sendToPlayer(serverPlayer, new EnchantmentPacket(EnchantmentRegistry.INDEX_ENCHANTMENTS, keys, ids, levels));
    }
}