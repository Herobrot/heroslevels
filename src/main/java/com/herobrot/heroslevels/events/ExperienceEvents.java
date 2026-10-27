package com.herobrot.heroslevels.events;

import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.entity.LevelExperienceOrbEntity;
import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.init.ConfigInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.util.DeathHelper;
import com.herobrot.heroslevels.util.LevelHelper;
import com.herobrot.heroslevels.util.PacketHelper;
import com.herobrot.heroslevels.init.TagInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.ChunkPos;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = HerosLevels.MOD_ID)
public class ExperienceEvents {

    public static final String SPAWNER_TAG = "heroslevels:from_spawner";

    @SubscribeEvent
    public static void onEntitySpawn(FinalizeSpawnEvent event) {
        if (event.getSpawnType() == MobSpawnType.SPAWNER) event.getEntity().addTag(SPAWNER_TAG);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (ConfigInit.CONFIG.disableMobFarms && event.getSource().getEntity() instanceof ServerPlayer killer && event.getEntity() instanceof Mob)
            killer.getData(AttachmentInit.LEVEL_MANAGER).recordMobKill(new ChunkPos(event.getEntity().blockPosition()));
        if (event.getEntity() instanceof ServerPlayer victim) DeathHelper.applyDeathXpLoss(victim);
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player)
            if (ConfigInit.CONFIG.disableMobFarms) {
                LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
                if (ConfigInit.CONFIG.disableMobFarmsLoot && levelManager.isMobFarmLimitReached()) event.setCanceled(true);
            }
    }

    @SubscribeEvent
    public static void onMobExperienceDrop(LivingExperienceDropEvent event) {
        if (event.getAttackingPlayer() instanceof ServerPlayer player) {
            if (ConfigInit.CONFIG.disableMobFarms && player.getData(AttachmentInit.LEVEL_MANAGER).isMobFarmLimitReached()) return;
            if (event.getEntity().getType().is(TagInit.RESTRICTED_ENTITY_EXPERIENCE_ENTITIES)) return;
            if (!ConfigInit.CONFIG.spawnerMobXP && event.getEntity().getTags().contains(SPAWNER_TAG)) return;
            float multiplier = ConfigInit.CONFIG.mobXPMultiplier;
            if (event.getEntity() instanceof EnderDragon) multiplier = ConfigInit.CONFIG.dragonXPMultiplier;
            int droppedXp = (int) (event.getDroppedExperience() * multiplier);
            if (droppedXp > 0) LevelExperienceOrbEntity.spawnCustomOrb((ServerLevel) player.level(), event.getEntity().position(), droppedXp);
        }
    }

    @SubscribeEvent
    public static void onAnimalBreed(BabyEntitySpawnEvent event) {
        if (event.getCausedByPlayer() instanceof ServerPlayer player && ConfigInit.CONFIG.breedingXPMultiplier > 0.0F) {
            float multiplier = LevelHelper.getLevelBasedMultiplier(player);
            int baseVanillaXp = player.getRandom().nextInt(7) + 1;
            int customXp = (int) (baseVanillaXp * ConfigInit.CONFIG.breedingXPMultiplier * multiplier);
            if (customXp > 0) LevelExperienceOrbEntity.spawnCustomOrb((ServerLevel) player.level(), event.getParentA().position(), customXp);
        }
    }

    @SubscribeEvent
    public static void onPlayerFish(ItemFishedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && ConfigInit.CONFIG.fishingXPMultiplier > 0.0F) {
            float multiplier = LevelHelper.getLevelBasedMultiplier(player);
            int customXp = (int) ((player.getRandom().nextInt(6) + 1) * ConfigInit.CONFIG.fishingXPMultiplier * multiplier);
            if (customXp > 0) LevelExperienceOrbEntity.spawnCustomOrb((ServerLevel) player.level(), player.position().add(0.0, 0.5D, 0.0), customXp);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        DeathHelper.copyLevelManagerData(event.getOriginal().getData(AttachmentInit.LEVEL_MANAGER), event.getEntity().getData(AttachmentInit.LEVEL_MANAGER));
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PacketHelper.syncPlayerSkills(serverPlayer);
            PacketHelper.updateLevels(serverPlayer);
            LevelHelper.applyAllSkills(serverPlayer);
        }
    }
}