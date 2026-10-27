package com.herobrot.heroslevels.util;

import com.herobrot.heroslevels.entity.LevelExperienceOrbEntity;
import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.init.ConfigInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.level.PlayerSkill;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

import java.util.HashMap;
import java.util.Map;

public final class DeathHelper {

    private DeathHelper() {}

    @SuppressWarnings("resource")
    public static void applyDeathXpLoss(ServerPlayer victim) {
        if (!victim.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT) || victim.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) return;
        if (ConfigInit.CONFIG.levelRetainPercentage >= 100 && !ConfigInit.CONFIG.resetCurrentXp) return;
        LevelManager levelManager = victim.getData(AttachmentInit.LEVEL_MANAGER);
        int xpToDrop = 0;
        int totalXpLost = 0;

        if (ConfigInit.CONFIG.resetCurrentXp) {
            int currentProgressXp = (int) (levelManager.getLevelProgress() * levelManager.getNextLevelExperience());
            levelManager.setLevelProgress(0.0F);
            totalXpLost += currentProgressXp;
            xpToDrop += (int) (currentProgressXp * (ConfigInit.CONFIG.deathXpRecoveryPercent / 100.0F));
        }
        if (ConfigInit.CONFIG.levelRetainPercentage < 100) {
            int levelExperienceLoss = applyLevelRetention(levelManager);
            totalXpLost += levelExperienceLoss;
            xpToDrop += (int) (levelExperienceLoss * (ConfigInit.CONFIG.deathXpRecoveryPercent / 100.0F));
        }
        if (totalXpLost > 0)
            levelManager.setTotalLevelExperience(Math.max(0, levelManager.getTotalLevelExperience() - totalXpLost));
        if (xpToDrop <= 0) return;
        int maxCap = (int) (150 * (ConfigInit.CONFIG.mobXPMultiplier > 0 ? ConfigInit.CONFIG.mobXPMultiplier : 1.0F));
        LevelExperienceOrbEntity.spawnCustomOrb((ServerLevel) victim.level(), victim.position(), Math.min(xpToDrop, maxCap));
    }

    private static int applyLevelRetention(LevelManager levelManager) {
        float retainPercent = ConfigInit.CONFIG.levelRetainPercentage / 100.0F;
        int oldOverallLevel = levelManager.getOverallLevel();
        int retainedLevel = (int) (oldOverallLevel * retainPercent);

        Map<Integer, Integer> oldSkillLevels = new HashMap<>();
        for (Map.Entry<Integer, PlayerSkill> entry : levelManager.getPlayerSkills().entrySet())
            oldSkillLevels.put(entry.getKey(), entry.getValue().getLevel());
        int oldSkillPoints = levelManager.getSkillPoints();

        int levelExperienceLoss = LevelManager.getExperienceBetweenLevels(retainedLevel, oldOverallLevel);
        int spentOnSkills = 0;
        for (Map.Entry<Integer, Integer> entry : oldSkillLevels.entrySet()) {
            int retainingLevel = (int) (entry.getValue() * retainPercent);
            levelManager.setSkillLevel(entry.getKey(), retainingLevel);
            spentOnSkills += retainingLevel;
        }
        int retainingSkillPoints = (int) (oldSkillPoints * retainPercent);
        int totalPointsForLevel = retainedLevel * ConfigInit.CONFIG.pointsPerLevel + ConfigInit.CONFIG.startPoints;
        int remainder = totalPointsForLevel - spentOnSkills - retainingSkillPoints;
        levelManager.setSkillPoints(Math.max(0, retainingSkillPoints + remainder));
        levelManager.setOverallLevel(retainedLevel);
        return levelExperienceLoss;
    }

    public static void copyLevelManagerData(LevelManager oldManager, LevelManager newManager) {
        newManager.setOverallLevel(oldManager.getOverallLevel());
        newManager.setSkillPoints(oldManager.getSkillPoints());
        newManager.setLevelProgress(oldManager.getLevelProgress());
        newManager.setTotalLevelExperience(oldManager.getTotalLevelExperience());
        for (Map.Entry<Integer, PlayerSkill> entry : oldManager.getPlayerSkills().entrySet())
            newManager.setSkillLevel(entry.getKey(), entry.getValue().getLevel());
    }
}