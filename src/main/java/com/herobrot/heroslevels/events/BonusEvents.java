package com.herobrot.heroslevels.events;

import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.util.BonusHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = HerosLevels.MOD_ID)
public class BonusEvents {

    @SubscribeEvent
    @SuppressWarnings("resource")
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide() && player.tickCount % 20 == 0)
            BonusHelper.healthRegenBonus(player);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        DamageSource source = event.getSource();
        if (event.getEntity() instanceof Player player) {
            BonusHelper.damageReflectionBonus(player, source, event.getOriginalDamage());
            if (BonusHelper.evadingDamageBonus(player)) {
                event.setNewDamage(0.0F);
                return;
            }
        }

        if (source.getEntity() instanceof Player player) {
            float newDamage = event.getNewDamage();
            if (BonusHelper.meleeDoubleDamageBonus(player))
                newDamage *= 2.0F;
            if (BonusHelper.meleeCriticalAttackChanceBonus(player))
                newDamage += BonusHelper.meleeCriticalDamageBonus(player);
            event.setNewDamage(newDamage);
        }
    }

    @SubscribeEvent
    public static void onLivingKnockback(LivingKnockBackEvent event) {
        if (event.getEntity().getLastAttacker() instanceof Player player
                && BonusHelper.meleeKnockbackAttackChanceBonus(player))
            event.setStrength(event.getStrength() * 1.5F);
    }

    @SubscribeEvent
    public static void onArrowSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof AbstractArrow arrow && arrow.getOwner() instanceof Player player)
            if (player.getMainHandItem().getItem() instanceof BowItem
                    || player.getOffhandItem().getItem() instanceof BowItem)
                BonusHelper.bowBonus(player, arrow);
            else if (player.getMainHandItem().getItem() instanceof CrossbowItem
                    || player.getOffhandItem().getItem() instanceof CrossbowItem)
                BonusHelper.crossbowBonus(player, arrow);
    }

    @SubscribeEvent
    public static void onBlockBreakBonus(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (!player.isCreative() && !player.isSpectator()) {
            BonusHelper.miningDropChanceBonus(player, event.getState(), event.getPos());
            BonusHelper.plantDropChanceBonus(player, event.getState(), event.getPos());
        }
    }

    @SubscribeEvent
    @SuppressWarnings("resource")
    public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getEntity() instanceof Player player && event.getItem().has(DataComponents.FOOD))
            BonusHelper.foodIncreasionBonus(player, event.getItem());
    }

    @SubscribeEvent
    @SuppressWarnings("resource")
    public static void onAnimalBreed(BabyEntitySpawnEvent event) {
        if (event.getCausedByPlayer() != null && event.getParentA() instanceof Animal parentA
                && event.getParentB() instanceof Animal parentB
                && event.getCausedByPlayer().level() instanceof ServerLevel serverLevel)
            BonusHelper.breedTwinChanceBonus(serverLevel, event.getCausedByPlayer(), parentA, parentB);
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            float reduction = BonusHelper.fallDamageReductionBonus(player);
            if (reduction > 0) event.setDistance(Math.max(0, event.getDistance() - reduction));
        }
    }


    @SubscribeEvent(priority = EventPriority.LOW)
    @SuppressWarnings("resource")
    public static void onDeathGrace(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof Player player
                && !player.level().isClientSide()
                && player.getHealth() - event.getNewDamage() <= 0
                && BonusHelper.deathGraceChanceBonus(player))
            event.setNewDamage(0.0F);
    }

    @SubscribeEvent
    @SuppressWarnings("resource")
    public static void onVillagerTrade(TradeWithVillagerEvent event) {
        if (event.getEntity().level() instanceof ServerLevel serverLevel)
            BonusHelper.tradeXpBonus(serverLevel, event.getEntity(),
                    event.getAbstractVillager(),event.getMerchantOffer().getXp());
    }

    @SubscribeEvent
    public static void onVillagerTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof AbstractVillager
                && event.getTargetType() instanceof Player player
                && BonusHelper.merchantImmuneBonus(player))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerSpawn(PlayerEvent.PlayerRespawnEvent event) {
        BonusHelper.healthAbsorptionBonus(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        BonusHelper.healthAbsorptionBonus(event.getEntity());
    }
}