package com.herobrot.heroslevels.network;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.level.PlayerSkill;
import com.herobrot.heroslevels.level.Skill;
import com.herobrot.heroslevels.level.SkillBonus;
import com.herobrot.heroslevels.network.packet.*;
import com.herobrot.heroslevels.registry.EnchantmentRegistry;
import com.herobrot.heroslevels.registry.HerosEnchantment;
import com.herobrot.heroslevels.screen.LevelScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;

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
}