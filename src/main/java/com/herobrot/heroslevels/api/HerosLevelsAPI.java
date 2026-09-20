package com.herobrot.heroslevels.api;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.init.CriteriaInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.level.Skill;
import com.herobrot.heroslevels.util.LevelHelper;
import com.herobrot.heroslevels.util.PacketHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Entry point for third-party mods to interact with the Hero's Levels progression system.
 * Read methods accept any Player and are valid on both server and client (using the synced
 * snapshot). Mutation methods require a ServerPlayer and are self-contained: they apply
 * attribute changes and sync to the client automatically, no extra calls needed.
 */
public class HerosLevelsAPI {

    // --- Reads (server and client) ---

    /** Returns the player's overall level. */
    public static int getOverallLevel(Player player) {
        return getLevelManager(player).getOverallLevel();
    }

    /** Returns the player's unspent skill points. */
    public static int getSkillPoints(Player player) {
        return getLevelManager(player).getSkillPoints();
    }

    /** Returns the player's total accumulated level experience. */
    public static int getTotalLevelExperience(Player player) {
        return getLevelManager(player).getTotalLevelExperience();
    }

    /** Returns the experience required for the player's next overall level, or 0 if already at max. */
    public static int getNextLevelExperience(Player player) {
        return getLevelManager(player).getNextLevelExperience();
    }

    /** Returns the player's current level of the skill identified by its datapack key, or 0 if the skill does not exist. */
    public static int getSkillLevel(Player player, String skillKey) {
        Skill skill = getSkill(skillKey);
        return skill != null ? getLevelManager(player).getSkillLevel(skill.id()) : 0;
    }

    // --- Mutations (server only, self-contained) ---

    /** Adds experience towards the player's next overall level, firing level-up logic as needed. */
    public static void addExperience(ServerPlayer player, int experience) {
        getLevelManager(player).addExperience(experience);
        PacketHelper.updateLevels(player);
    }

    /** Directly adds or removes overall levels; each level gained also grants skill points. */
    public static void addExperienceLevels(ServerPlayer player, int levels) {
        getLevelManager(player).addExperienceLevels(levels);
        PacketHelper.updateLevels(player);
    }

    /** Grants skill levels for free (no skill point cost), clamped to the skill's max level. Returns the levels actually gained. */
    public static int addSkillLevels(ServerPlayer player, String skillKey, int levels) {
        Skill skill = getSkill(skillKey);
        if (skill == null || levels <= 0) return 0;
        LevelManager levelManager = getLevelManager(player);
        int current = levelManager.getSkillLevel(skill.id());
        int newLevel = Math.min(current + levels, skill.maxLevel());
        if (newLevel == current) return 0;
        applySkillLevel(player, skill, newLevel);
        return newLevel - current;
    }

    /** Raises the skill by spending available skill points, clamped to the skill's max level. Returns the levels actually gained. */
    public static int addSkillLevelsWithPoints(ServerPlayer player, String skillKey, int levels) {
        Skill skill = getSkill(skillKey);
        if (skill == null || levels <= 0) return 0;
        LevelManager levelManager = getLevelManager(player);
        int current = levelManager.getSkillLevel(skill.id());
        int affordable = Math.min(levels, levelManager.getSkillPoints());
        int newLevel = Math.min(current + affordable, skill.maxLevel());
        if (newLevel == current) return 0;
        levelManager.setSkillPoints(levelManager.getSkillPoints() - (newLevel - current));
        applySkillLevel(player, skill, newLevel);
        return newLevel - current;
    }

    /** Removes skill levels as a punishment: no skill point refund and the overall level drops by the same amount, clamped at 0. Returns the levels actually removed. */
    public static int removeSkillLevels(ServerPlayer player, String skillKey, int levels) {
        Skill skill = getSkill(skillKey);
        if (skill == null || levels <= 0) return 0;
        LevelManager levelManager = getLevelManager(player);
        int current = levelManager.getSkillLevel(skill.id());
        int newLevel = Math.max(current - levels, 0);
        int removed = current - newLevel;
        if (removed == 0) return 0;
        levelManager.setOverallLevel(Math.max(levelManager.getOverallLevel() - removed, 0));
        applySkillLevel(player, skill, newLevel);
        return removed;
    }

    /** Resets the skill to level 0 and refunds its levels as skill points. Returns false if the skill does not exist or has no levels. */
    public static boolean resetSkill(ServerPlayer player, String skillKey) {
        Skill skill = getSkill(skillKey);
        return skill != null && getLevelManager(player).resetSkill(skill.id());
    }

    private static void applySkillLevel(ServerPlayer player, Skill skill, int newLevel) {
        LevelManager levelManager = getLevelManager(player);
        int previousLevel = levelManager.getSkillLevel(skill.id());
        levelManager.setSkillLevel(skill.id(), newLevel);
        LevelHelper.clearSkillModifiers(player, skill.key());
        LevelHelper.updateSkill(player, skill);
        for (int i = previousLevel + 1; i <= newLevel; i++)
            CriteriaInit.SKILL_UP.get().trigger(player, skill.key(), i);
        PacketHelper.syncPlayerSkills(player);
        PacketHelper.updateLevels(player);
    }

    @Nullable
    private static Skill getSkill(String skillKey) {
        for (Skill skill : LevelManager.SKILLS.values()) if (skill.key().equals(skillKey)) return skill;
        return null;
    }

    private static LevelManager getLevelManager(Player player) {
        return player.getData(AttachmentInit.LEVEL_MANAGER);
    }
}