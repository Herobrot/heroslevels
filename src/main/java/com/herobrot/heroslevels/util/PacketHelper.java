package com.herobrot.heroslevels.util;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.level.Skill;
import com.herobrot.heroslevels.level.SkillBonus;
import com.herobrot.heroslevels.level.restriction.PlayerRestriction;
import com.herobrot.heroslevels.network.packet.*;
import com.herobrot.heroslevels.registry.EnchantmentRegistry;
import com.herobrot.heroslevels.registry.HerosEnchantment;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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

    public static void syncRestrictions(ServerPlayer serverPlayer) {
        PacketDistributor.sendToPlayer(serverPlayer, new RestrictionsSyncPacket(
                toRecords(LevelManager.BLOCK_RESTRICTIONS, BuiltInRegistries.BLOCK),
                toRecords(LevelManager.CRAFTING_RESTRICTIONS, BuiltInRegistries.ITEM),
                toRecords(LevelManager.ENTITY_RESTRICTIONS, BuiltInRegistries.ENTITY_TYPE),
                toRecords(LevelManager.ITEM_RESTRICTIONS, BuiltInRegistries.ITEM),
                toRecords(LevelManager.MINING_RESTRICTIONS, BuiltInRegistries.BLOCK),
                toEnchantmentRecords(),
                toRecords(LevelManager.BREWING_RESTRICTIONS, BuiltInRegistries.POTION)
        ));
    }

    private static <T> List<RestrictionsSyncPacket.RestrictionRecord> toRecords(Map<Integer, PlayerRestriction> restrictions, Registry<T> registry) {
        List<RestrictionsSyncPacket.RestrictionRecord> records = new ArrayList<>();
        for (PlayerRestriction restriction : restrictions.values()) {
            T value = registry.byId(restriction.id());
            if (value == null) continue;
            ResourceLocation targetId = registry.getKey(value);
            if (targetId == null) continue;
            records.add(new RestrictionsSyncPacket.RestrictionRecord(targetId.toString(), toSkillRecords(restriction)));
        }
        return records;
    }

    private static List<RestrictionsSyncPacket.EnchantmentRestrictionRecord> toEnchantmentRecords() {
        List<RestrictionsSyncPacket.EnchantmentRestrictionRecord> records = new ArrayList<>();
        for (PlayerRestriction restriction : LevelManager.ENCHANTMENT_RESTRICTIONS.values()) {
            HerosEnchantment enchantment = EnchantmentRegistry.getHerosEnchantment(restriction.id());
            if (enchantment == null) continue;
            ResourceLocation enchantmentId = enchantment.entry().unwrapKey().map(ResourceKey::location).orElse(null);
            if (enchantmentId == null) continue;
            records.add(new RestrictionsSyncPacket.EnchantmentRestrictionRecord(
                    enchantmentId.toString(), enchantment.level(), toSkillRecords(restriction)));
        }
        return records;
    }

    private static List<RestrictionsSyncPacket.SkillLevelRecord> toSkillRecords(PlayerRestriction restriction) {
        List<RestrictionsSyncPacket.SkillLevelRecord> skills = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : restriction.skillLevelRestrictions().entrySet())
            skills.add(new RestrictionsSyncPacket.SkillLevelRecord(entry.getKey(), entry.getValue()));
        return skills;
    }
}