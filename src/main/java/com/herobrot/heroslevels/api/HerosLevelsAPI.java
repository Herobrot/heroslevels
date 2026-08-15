package com.herobrot.heroslevels.api;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.level.LevelManager;
import net.minecraft.server.level.ServerPlayer;

public class HerosLevelsAPI {

    public static int getOverallLevel(ServerPlayer player) {
        return getLevelManager(player).getOverallLevel();
    }

    public static void addExperience(ServerPlayer player, int experience) {
        getLevelManager(player).addExperience(experience);
    }

    public static void addExperienceLevels(ServerPlayer player, int levels) {
        getLevelManager(player).addExperienceLevels(levels);
    }

    public static int getSkillLevel(ServerPlayer player, int skillId) {
        return getLevelManager(player).getSkillLevel(skillId);
    }

    public static void setSkillLevel(ServerPlayer player, int skillId, int level) {
        getLevelManager(player).setSkillLevel(skillId, level);
    }

    public static int getSkillPoints(ServerPlayer player) {
        return getLevelManager(player).getSkillPoints();
    }

    private static LevelManager getLevelManager(ServerPlayer player) {
        return player.getData(AttachmentInit.LEVEL_MANAGER);
    }
}