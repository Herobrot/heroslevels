package com.herobrot.heroslevels.network;

import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.init.ConfigInit;
import com.herobrot.heroslevels.init.CriteriaInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.level.PlayerSkill;
import com.herobrot.heroslevels.level.Skill;
import com.herobrot.heroslevels.network.packet.StatPacket;
import com.herobrot.heroslevels.util.LevelHelper;
import com.herobrot.heroslevels.util.PacketHelper;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public class ServerPayloadHandler {

    public static void handleStat(StatPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            int id = payload.id();
            int level = payload.level();
            if (level <= 0 || level > 10_000) return;
            LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
            if (levelManager.getSkillPoints() - level >= 0) {
                Skill skill = LevelManager.SKILLS.get(id);
                PlayerSkill playerSkill = levelManager.getPlayerSkills().get(id);
                if (skill == null || playerSkill == null) {
                    HerosLevels.LOGGER.warn("[Hero's Levels-WARN]: StatPacket con id de skill inválido ({}) recibido de {}", id, player.getScoreboardName());
                    return;
                }
                if (ConfigInit.CONFIG.overallMaxLevel > 0 && ConfigInit.CONFIG.overallMaxLevel <= levelManager.getOverallLevel())
                    return;
                boolean canExceedMax = false;
                if (ConfigInit.CONFIG.allowHigherSkillLevel) {
                    canExceedMax = true;
                    for (Skill skillCheck : LevelManager.SKILLS.values())
                        if (skillCheck.maxLevel() > levelManager.getSkillLevel(skillCheck.id())) {
                            canExceedMax = false;
                            break;
                        }
                }
                if (!canExceedMax && playerSkill.getLevel() >= skill.maxLevel()) return;
                int previousLevel = playerSkill.getLevel();
                int maxAllowed = canExceedMax ? Integer.MAX_VALUE : skill.maxLevel();
                int newLevel = Math.min(previousLevel + level, maxAllowed);
                int actualLevelsGained = newLevel - previousLevel;
                if (actualLevelsGained <= 0) return;
                levelManager.setSkillLevel(id, newLevel);
                levelManager.setSkillPoints(levelManager.getSkillPoints() - actualLevelsGained);
                LevelHelper.clearSkillModifiers(player, skill.key());
                LevelHelper.updateSkill(player, skill);
                PacketHelper.updateLevels(player);
                for (int i = previousLevel + 1; i <= newLevel; i++)
                    CriteriaInit.SKILL_UP.get().trigger(player, skill.key(), i);
                PacketDistributor.sendToPlayer(player, new StatPacket(id, levelManager.getSkillLevel(id)));
            }
        });
    }

    public static void handleAttributeSync(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            var attackDamageInstance = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackDamageInstance != null)
                player.connection.send(new ClientboundUpdateAttributesPacket(player.getId(), List.of(attackDamageInstance)));
        });
    }
}