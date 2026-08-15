package com.herobrot.heroslevels.events;

import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.entity.LevelExperienceOrbEntity;
import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.init.ConfigInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.level.PlayerSkill;
import com.herobrot.heroslevels.util.LevelHelper;
import com.herobrot.heroslevels.util.PacketHelper;
import com.herobrot.heroslevels.init.TagInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = HerosLevels.MOD_ID)
public class ExperienceEvents {

    public static final String SPAWNER_TAG = "heroslevels:from_spawner";

    @SubscribeEvent
    public static void onEntitySpawn(FinalizeSpawnEvent event) {
        if (event.getSpawnType() == MobSpawnType.SPAWNER) {
            event.getEntity().addTag(SPAWNER_TAG);
        }
    }

    @SubscribeEvent
    @SuppressWarnings("resource")
    public static void onLivingDeath(LivingDeathEvent event) {
        if (ConfigInit.CONFIG.disableMobFarms
                && event.getSource().getEntity() instanceof ServerPlayer killer
                && event.getEntity() instanceof net.minecraft.world.entity.Mob) {
            LevelManager killerLevelManager = killer.getData(AttachmentInit.LEVEL_MANAGER);
            killerLevelManager.recordMobKill(new ChunkPos(event.getEntity().blockPosition()));
        }
        if (!(event.getEntity() instanceof ServerPlayer victim)) return;
        if (!victim.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT) ||
                victim.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY))
            return;
        if (ConfigInit.CONFIG.levelRetainPercentage >= 100 && !ConfigInit.CONFIG.resetCurrentXp)
            return;
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
            float retainPercent = ConfigInit.CONFIG.levelRetainPercentage / 100.0F;
            int oldOverallLevel = levelManager.getOverallLevel();
            int retainedLevel = (int) (oldOverallLevel * retainPercent);

            Map<Integer, Integer> oldSkillLevels = new HashMap<>();
            for (Map.Entry<Integer, PlayerSkill> entry : levelManager.getPlayerSkills().entrySet())
                oldSkillLevels.put(entry.getKey(), entry.getValue().getLevel());
            int oldSkillPoints = levelManager.getSkillPoints();

            int levelExperienceLoss = LevelManager.getExperienceBetweenLevels(retainedLevel, oldOverallLevel);
            totalXpLost += levelExperienceLoss;
            xpToDrop += (int) (levelExperienceLoss * (ConfigInit.CONFIG.deathXpRecoveryPercent / 100.0F));
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
        }
        if (totalXpLost > 0)
            levelManager.setTotalLevelExperience(Math.max(0, levelManager.getTotalLevelExperience() - totalXpLost));
        if (xpToDrop <= 0) return;
        int maxCap = (int) (150 * (ConfigInit.CONFIG.mobXPMultiplier > 0 ? ConfigInit.CONFIG.mobXPMultiplier : 1.0F));
        xpToDrop = Math.min(xpToDrop, maxCap);
        LevelExperienceOrbEntity.spawnCustomOrb((ServerLevel) victim.level(), victim.position(), xpToDrop);
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player)
            if (ConfigInit.CONFIG.disableMobFarms) {
                LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
                if (ConfigInit.CONFIG.disableMobFarmsLoot && levelManager.isMobFarmLimitReached())
                    event.setCanceled(true);
            }
    }

    @SubscribeEvent
    public static void onMobExperienceDrop(LivingExperienceDropEvent event) {
        if (event.getAttackingPlayer() instanceof ServerPlayer player) {
            if (ConfigInit.CONFIG.disableMobFarms && player.getData(AttachmentInit.LEVEL_MANAGER).isMobFarmLimitReached())
                return;
            if (event.getEntity().getType().is(TagInit.RESTRICTED_ENTITY_EXPERIENCE_ENTITIES)) return;
            if (!ConfigInit.CONFIG.spawnerMobXP && event.getEntity().getTags().contains(SPAWNER_TAG)) return;
            float multiplier = ConfigInit.CONFIG.mobXPMultiplier;
            if (event.getEntity() instanceof EnderDragon) multiplier = ConfigInit.CONFIG.dragonXPMultiplier;
            int droppedXp = (int) (event.getDroppedExperience() * multiplier);
            if (droppedXp > 0)
                LevelExperienceOrbEntity.spawnCustomOrb((ServerLevel) player.level(), event.getEntity().position(), droppedXp);
        }
    }

    @SubscribeEvent
    public static void onAnimalBreed(BabyEntitySpawnEvent event) {
        if (event.getCausedByPlayer() instanceof ServerPlayer player && ConfigInit.CONFIG.breedingXPMultiplier > 0.0F) {
            float multiplier = LevelHelper.getLevelBasedMultiplier(player);
            int baseVanillaXp = player.getRandom().nextInt(7) + 1;
            int customXp = (int) (baseVanillaXp * ConfigInit.CONFIG.breedingXPMultiplier * multiplier);
            if (customXp > 0)
                LevelExperienceOrbEntity.spawnCustomOrb((ServerLevel) player.level(), event.getParentA().position(), customXp);
        }
    }

    @SubscribeEvent
    public static void onPlayerFish(ItemFishedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && ConfigInit.CONFIG.fishingXPMultiplier > 0.0F) {
            float multiplier = LevelHelper.getLevelBasedMultiplier(player);
            int customXp = (int) ((player.getRandom().nextInt(6) + 1) * ConfigInit.CONFIG.fishingXPMultiplier * multiplier);
            if (customXp > 0)
                LevelExperienceOrbEntity.spawnCustomOrb((ServerLevel) player.level(), player.position().add(0.0, 0.5D, 0.0), customXp);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();
        LevelManager oldManager = original.getData(AttachmentInit.LEVEL_MANAGER);
        LevelManager newManager = newPlayer.getData(AttachmentInit.LEVEL_MANAGER);
        copyManagerData(oldManager, newManager);
        if (newPlayer instanceof ServerPlayer serverPlayer) {
            PacketHelper.syncPlayerSkills(serverPlayer);
            PacketHelper.updateLevels(serverPlayer);
            LevelHelper.applyAllSkills(serverPlayer);
        }
    }

    private static void copyManagerData(LevelManager oldManager, LevelManager newManager) {
        newManager.setOverallLevel(oldManager.getOverallLevel());
        newManager.setSkillPoints(oldManager.getSkillPoints());
        newManager.setLevelProgress(oldManager.getLevelProgress());
        newManager.setTotalLevelExperience(oldManager.getTotalLevelExperience());
        for (Map.Entry<Integer, PlayerSkill> entry : oldManager.getPlayerSkills().entrySet())
            newManager.setSkillLevel(entry.getKey(), entry.getValue().getLevel());
    }
}