package com.herobrot.heroslevels.network;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.level.PlayerSkill;
import com.herobrot.heroslevels.level.Skill;
import com.herobrot.heroslevels.level.SkillBonus;
import com.herobrot.heroslevels.level.restriction.PlayerRestriction;
import com.herobrot.heroslevels.network.packet.*;
import com.herobrot.heroslevels.registry.EnchantmentRegistry;
import com.herobrot.heroslevels.registry.HerosEnchantment;
import com.herobrot.heroslevels.screen.LevelScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientPayloadHandler {

    private static void notifyLevelScreenDirty() {
        if (Minecraft.getInstance().screen instanceof LevelScreen levelScreen)
            levelScreen.markButtonsDirty();
    }

    public static void handleSkillSync(SkillSyncPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
            LevelManager.SKILLS.clear();
            for (int i = 0; i < payload.skillIds().size(); i++) {
                int skillId = payload.skillIds().get(i);
                Skill skill = new Skill(
                        skillId,
                        payload.skillKeys().get(i),
                        payload.skillMaxLevels().get(i),
                        payload.skillAttributes().get(i).skillAttributes()
                );
                LevelManager.SKILLS.put(skillId, skill);
                levelManager.getPlayerSkills().computeIfAbsent(skillId, id -> new PlayerSkill(id, 0));
            }
            LevelManager.BONUSES.clear();
            for (SkillBonus bonus : payload.skillBonuses().skillBonuses())
                LevelManager.BONUSES.computeIfAbsent(bonus.key(), k -> new ArrayList<>()).add(bonus);
            notifyLevelScreenDirty();
        });
    }

    public static void handlePlayerSkillSync(PlayerSkillSyncPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
            for (PlayerSkillSyncPacket.PlayerSkillRecord record : payload.skills())
                levelManager.setSkillLevel(record.id(), record.level());
            notifyLevelScreenDirty();
        });
    }

    public static void handleLevel(LevelPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
            levelManager.setOverallLevel(payload.overallLevel());
            levelManager.setSkillPoints(payload.skillPoints());
            levelManager.setTotalLevelExperience(payload.totalLevelExperience());
            levelManager.setLevelProgress(payload.levelProgress());
            notifyLevelScreenDirty();
        });
    }

    public static void handleStat(StatPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
            levelManager.setSkillLevel(payload.id(), payload.level());
            notifyLevelScreenDirty();
        });
    }

    @SuppressWarnings("resource")
    public static void handleEnchantment(EnchantmentPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            EnchantmentRegistry.ENCHANTMENTS.clear();
            EnchantmentRegistry.INDEX_ENCHANTMENTS.clear();
            RegistryAccess registryAccess = context.player().level().registryAccess();
            var registry = registryAccess.registryOrThrow(Registries.ENCHANTMENT);
            for (int i = 0; i < payload.keys().size(); i++) {
                int key = payload.keys().get(i);
                String enchantmentId = payload.ids().get(i);
                Holder<Enchantment> entry = registry.getHolder(ResourceLocation.parse(enchantmentId))
                        .orElseThrow(() -> new IllegalStateException("Missing enchantment ID: " + enchantmentId));
                int level = payload.levels().get(i);
                EnchantmentRegistry.ENCHANTMENTS.put(key, new HerosEnchantment(entry, level));
            }
            EnchantmentRegistry.INDEX_ENCHANTMENTS.putAll(payload.indexed());
        });
    }

    public static void handleRestrictionsSync(RestrictionsSyncPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            LevelManager.BLOCK_RESTRICTIONS.clear();
            LevelManager.CRAFTING_RESTRICTIONS.clear();
            LevelManager.ENTITY_RESTRICTIONS.clear();
            LevelManager.ITEM_RESTRICTIONS.clear();
            LevelManager.MINING_RESTRICTIONS.clear();
            LevelManager.ENCHANTMENT_RESTRICTIONS.clear();
            LevelManager.BREWING_RESTRICTIONS.clear();
            loadRestrictions(payload.block(), BuiltInRegistries.BLOCK, LevelManager.BLOCK_RESTRICTIONS);
            loadRestrictions(payload.crafting(), BuiltInRegistries.ITEM, LevelManager.CRAFTING_RESTRICTIONS);
            loadRestrictions(payload.entity(), BuiltInRegistries.ENTITY_TYPE, LevelManager.ENTITY_RESTRICTIONS);
            loadRestrictions(payload.item(), BuiltInRegistries.ITEM, LevelManager.ITEM_RESTRICTIONS);
            loadRestrictions(payload.mining(), BuiltInRegistries.BLOCK, LevelManager.MINING_RESTRICTIONS);
            loadRestrictions(payload.brewing(), BuiltInRegistries.POTION, LevelManager.BREWING_RESTRICTIONS);
            for (RestrictionsSyncPacket.EnchantmentRestrictionRecord record : payload.enchantments()) {
                int enchantmentId = EnchantmentRegistry.getId(ResourceLocation.parse(record.enchantmentId()), record.level());
                if (enchantmentId == -1) continue;
                LevelManager.ENCHANTMENT_RESTRICTIONS.put(enchantmentId, new PlayerRestriction(enchantmentId, toSkillMap(record.skills())));
            }
            notifyLevelScreenDirty();
        });
    }

    private static <T> void loadRestrictions(List<RestrictionsSyncPacket.RestrictionRecord> records, Registry<T> registry, Map<Integer, PlayerRestriction> target) {
        for (RestrictionsSyncPacket.RestrictionRecord record : records) {
            T value = registry.get(ResourceLocation.parse(record.targetId()));
            if (value == null) continue;
            int rawId = registry.getId(value);
            target.put(rawId, new PlayerRestriction(rawId, toSkillMap(record.skills())));
        }
    }

    private static Map<Integer, Integer> toSkillMap(List<RestrictionsSyncPacket.SkillLevelRecord> skills) {
        Map<Integer, Integer> skillLevels = new HashMap<>();
        for (RestrictionsSyncPacket.SkillLevelRecord skill : skills)
            skillLevels.put(skill.skillId(), skill.level());
        return skillLevels;
    }
}