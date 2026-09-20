package com.herobrot.heroslevels.util;

import com.herobrot.heroslevels.entity.LevelExperienceOrbEntity;
import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.init.ConfigInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.level.SkillBonus;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.IntToDoubleFunction;

public class BonusHelper {

    private static double sumBonus(Player player, String key, IntToDoubleFunction perLevelValue) {
        LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
        double total = 0.0;
        for (SkillBonus bonus : LevelManager.BONUSES.getOrDefault(key, List.of())) {
            int level = levelManager.getSkillLevel(bonus.id());
            if (level >= bonus.level())
                total += perLevelValue.applyAsDouble(level);

        }
        return total;
    }

    private static boolean anyChance(Player player, String key, IntToDoubleFunction chanceForLevel) {
        LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
        for (SkillBonus bonus : LevelManager.BONUSES.getOrDefault(key, List.of())) {
            int level = levelManager.getSkillLevel(bonus.id());
            if (level >= bonus.level() && player.getRandom().nextFloat() <= chanceForLevel.applyAsDouble(level))
                return true;
        }
        return false;
    }

    private static boolean anyThreshold(Player player, String key) {
        LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
        for (SkillBonus bonus : LevelManager.BONUSES.getOrDefault(key, List.of())) {
            int level = levelManager.getSkillLevel(bonus.id());
            if (level >= bonus.level()) return true;
        }
        return false;
    }

    // Bonus de combate a distancia
    public static void bowBonus(LivingEntity shooter, Projectile projectile) {
        if (!(shooter instanceof Player player) || !(projectile instanceof AbstractArrow arrow)) return;
        double bonusDamage = sumBonus(player, "bowDamage",
                level -> ConfigInit.CONFIG.bowDamageBonus * level);
        if (bonusDamage > 0) arrow.setBaseDamage(arrow.getBaseDamage() + bonusDamage);
        if (anyChance(player, "bowDoubleDamageChance", level -> ConfigInit.CONFIG.bowDoubleDamageChanceBonus))
            arrow.setBaseDamage(arrow.getBaseDamage() * 2D);
    }

    public static void crossbowBonus(LivingEntity shooter, Projectile projectile) {
        if (!(shooter instanceof Player player) || !(projectile instanceof AbstractArrow arrow)) return;
        double bonusDamage = sumBonus(player, "crossbowDamage", level -> ConfigInit.CONFIG.crossbowDamageBonus * level);
        if (bonusDamage > 0) arrow.setBaseDamage(arrow.getBaseDamage() + bonusDamage);
        if (anyChance(player, "crossbowDoubleDamageChance", level -> ConfigInit.CONFIG.crossbowDoubleDamageChanceBonus))
            arrow.setBaseDamage(arrow.getBaseDamage() * 2D);
    }

    // Ítems / durabilidad / pociones
    public static boolean itemDamageChanceBonus(@Nullable Player player) {
        if (player == null) return false;
        return anyChance(player, "itemDamageChance", level -> ConfigInit.CONFIG.itemDamageChanceBonus * level);
    }

    public static int getPotionAmplifierBoost(Player player) {
        double amplifierChance = sumBonus(player, "potionAmplifierChanceBonus", level -> ConfigInit.CONFIG.potionAmplifierChanceBonus * level);
        return player.getRandom().nextFloat() <= amplifierChance ? 1 : 0;
    }

    public static MobEffectInstance applyPotionBonuses(@Nullable Player player, MobEffectInstance effectInstance) {
        if (player == null) return effectInstance;
        int amplifierBoost = getPotionAmplifierBoost(player);
        double durationChance = sumBonus(player, "potionDurationBonus",
                level -> ConfigInit.CONFIG.potionDurationChanceBonus * level);
        double durationMultiplier = 0.0;
        if (player.getRandom().nextFloat() <= durationChance)
            durationMultiplier = durationChance;
        if (amplifierBoost == 0 && durationMultiplier == 0.0) return effectInstance;
        int newDuration = (int) (effectInstance.getDuration() * (1.0 + durationMultiplier));
        int newAmplifier = effectInstance.getAmplifier() + amplifierBoost;
        return new MobEffectInstance(
                effectInstance.getEffect(),
                newDuration,
                newAmplifier,
                effectInstance.isAmbient(),
                effectInstance.isVisible(),
                effectInstance.showIcon()
        );
    }

    public static float getLingeringCloudRadiusBonus(Player player) {
        double chance = sumBonus(player, "lingeringCloudRadiusChanceBonus", level -> ConfigInit.CONFIG.lingeringCloudRadiusChanceBonus * level);
        if (player.getRandom().nextFloat() <= chance) {
            double radiusGrowth = sumBonus(player, "lingeringCloudRadiusBonus", level -> ConfigInit.CONFIG.lingeringCloudRadiusBonus * level);
            return (float) radiusGrowth;
        }
        return 0.0f;
    }

    public static int getLingeringCloudDurationBonus(Player player) {
        double totalSeconds = sumBonus(player, "lingeringCloudDurationBonus", level -> ConfigInit.CONFIG.lingeringCloudDurationBonus * level);
        return (int) (totalSeconds * 20.0);
    }

    // Crianza
    public static void breedTwinChanceBonus(ServerLevel level, Player player, Animal animalEntity, Animal otherAnimalEntity) {
        LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
        for (SkillBonus bonus : LevelManager.BONUSES.getOrDefault("breedTwinChance", List.of())) {
            int skillLevel = levelManager.getSkillLevel(bonus.id());
            if (skillLevel >= bonus.level() && player.getRandom().nextFloat() <= ConfigInit.CONFIG.twinBreedChanceBonus) {
                AgeableMob extraOffspring = animalEntity.getBreedOffspring(level, otherAnimalEntity);
                if (extraOffspring != null) {
                    extraOffspring.setBaby(true);
                    extraOffspring.moveTo(animalEntity.getX(), animalEntity.getY(), animalEntity.getZ(), player.getRandom().nextFloat() * 360F, 0.0F);
                    level.addFreshEntity(extraOffspring);
                }
            }
        }
    }

    // Supervivencia
    public static float fallDamageReductionBonus(Player player) {
        return (float) sumBonus(player, "fallDamageReduction", level -> level * ConfigInit.CONFIG.fallDamageReductionBonus);
    }

    public static boolean deathGraceChanceBonus(Player player) {
        boolean triggered = anyChance(player, "deathGraceChance", level -> ConfigInit.CONFIG.deathGraceChanceBonus);
        if (triggered) {
            player.setHealth(1.0F);
            player.removeAllEffects();
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
        }
        return triggered;
    }

    public static float tntStrengthBonus(Player player) {
        return (float) sumBonus(player, "tntStrength", level -> ConfigInit.CONFIG.tntStrengthBonus);
    }

    // Comercio
    public static float priceDiscountBonus(Player player) {
        if (player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE))
            return 1.0f;
        double totalDiscount = sumBonus(player, "priceDiscount", level -> level * ConfigInit.CONFIG.priceDiscountBonus);
        return (float) Math.max(0.0, 1.0 - totalDiscount);
    }

    public static void tradeXpBonus(ServerLevel serverLevel, @Nullable Player player, AbstractVillager merchant, int amount) {
        amount = (int) (amount * ConfigInit.CONFIG.tradingXPMultiplier);
        if (amount > 0) {
            if (player instanceof ServerPlayer serverPlayer) {
                double bonusPercent = sumBonus(player, "tradeXp", level -> level * ConfigInit.CONFIG.tradeXpBonus);
                float levelMultiplier = LevelHelper.getLevelBasedMultiplier(serverPlayer);
                amount = (int) (amount * (1.0 + bonusPercent) * levelMultiplier);
            }
            LevelExperienceOrbEntity.spawnCustomOrb(serverLevel, merchant.position().add(0.0D, 0.5D, 0.0D), amount);
        }
    }

    public static boolean merchantImmuneBonus(Player player) {
        return anyThreshold(player, "merchantImmune");
    }

    // Minería / cosecha
    @SuppressWarnings("resource")
    public static void miningDropChanceBonus(Player player, BlockState state, BlockPos pos) {
        Holder<Enchantment> silkTouch = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.SILK_TOUCH);
        if (!state.is(Tags.Blocks.ORES) || EnchantmentHelper.getEnchantmentLevel(silkTouch, player) > 0) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        boolean triggered = anyChance(player, "miningDropChance", level -> level * ConfigInit.CONFIG.miningDropChanceBonus);
        if (triggered) {
            List<ItemStack> list = Block.getDrops(state, serverLevel, pos, null);
            if (!list.isEmpty()) {
                Block.popResource(player.level(), pos, list.getFirst().split(1));
            }
        }
    }

    @SuppressWarnings("resource")
    public static void plantDropChanceBonus(Player player, BlockState state, BlockPos pos) {
        Holder<Enchantment> silkTouch = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.SILK_TOUCH);
        if (EnchantmentHelper.getEnchantmentLevel(silkTouch, player) > 0) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        boolean triggered = anyChance(player, "plantDropChance", level -> level * ConfigInit.CONFIG.plantDropChanceBonus);
        if (triggered) {
            List<ItemStack> list = Block.getDrops(state, serverLevel, pos, null);
            for (ItemStack itemStack : list) {
                if (itemStack.is(Tags.Items.CROPS)) {
                    Block.popResource(player.level(), pos, itemStack);
                    break;
                }
            }
        }
    }

    // Yunque
    public static boolean anvilXpCapBonus(Player player) {
        return anyThreshold(player, "anvilXpCap");
    }

    public static int anvilXpDiscountBonus(Player player, int levelCost) {
        if (levelCost > ConfigInit.CONFIG.anvilXpCap && anvilXpCapBonus(player))
            return ConfigInit.CONFIG.anvilXpCap;
        double totalDiscount = sumBonus(player, "anvilXpDiscount", level -> level * ConfigInit.CONFIG.anvilXpDiscountBonus);
        if (totalDiscount <= 0) return levelCost;
        return (int) (levelCost * Math.max(0.0, 1.0 - totalDiscount));
    }

    public static boolean anvilXpChanceBonus(Player player) {
        return anyChance(player, "anvilXpChance", level -> level * ConfigInit.CONFIG.anvilXpChanceBonus);
    }

    // Vida
    public static void healthRegenBonus(Player player) {
        double totalHeal = sumBonus(player, "healthRegen", level -> level * ConfigInit.CONFIG.healthRegenBonus);
        if (totalHeal > 0) player.heal((float) totalHeal);
    }

    public static void healthAbsorptionBonus(Player player) {
        if (anyThreshold(player, "healthAbsorption")) {
            float currentAbsorption = player.getAbsorptionAmount();
            float targetAbsorption = ConfigInit.CONFIG.healthAbsorptionBonus;
            player.setAbsorptionAmount(Math.max(currentAbsorption, targetAbsorption));
        }
    }

    public static float exhaustionReductionBonus(Player player) {
        double totalReduction = sumBonus(player, "exhaustionReduction", level -> level * ConfigInit.CONFIG.exhaustionReductionBonus);
        if (totalReduction <= 0) return 1.0f;
        return (float) Math.max(0.0, 1.0 - totalReduction);
    }

    // Combate cuerpo a cuerpo
    public static boolean meleeKnockbackAttackChanceBonus(Player player) {
        return anyChance(player, "meleeKockbackAttackChance", level -> level * ConfigInit.CONFIG.meleeKnockbackAttackChanceBonus);
    }

    public static boolean meleeCriticalAttackChanceBonus(Player player) {
        return anyChance(player, "meleeCriticalAttackChance", level -> level * ConfigInit.CONFIG.meleeCriticalAttackChanceBonus);
    }

    public static float meleeCriticalDamageBonus(Player player) {
        return (float) sumBonus(player, "meleeCriticalAttackDamage", level -> level * ConfigInit.CONFIG.meleeCriticalAttackDamageBonus);
    }

    public static boolean meleeDoubleDamageBonus(Player player) {
        return anyChance(player, "meleeDoubleAttackDamageChance", level -> ConfigInit.CONFIG.meleeDoubleAttackDamageChanceBonus);
    }

    // Comida
    public static void foodIncreasionBonus(Player player, ItemStack itemStack) {
        FoodProperties foodProperties = itemStack.get(DataComponents.FOOD);
        if (foodProperties == null) return;
        double totalMultiplier = sumBonus(player, "foodIncreasion", level -> level * ConfigInit.CONFIG.foodIncreasionBonus);
        if (totalMultiplier <= 0) return;
        player.getFoodData().eat((int) (foodProperties.nutrition() * totalMultiplier), (float) (foodProperties.saturation() * totalMultiplier));
    }

    // Reflejo de daño
    @SuppressWarnings("resource")
    public static void damageReflectionBonus(Player player, DamageSource source, float amount) {
        if (source.getEntity() == null || player.level().isClientSide()) return;
        if (!anyChance(player, "damageReflectionChance", level -> level * ConfigInit.CONFIG.damageReflectionChanceBonus)) return;
        double totalReflected = sumBonus(player, "damageReflection", level -> level * ConfigInit.CONFIG.damageReflectionBonus);
        if (totalReflected > 0) source.getEntity().hurt(source, amount * (float) totalReflected);
    }

    public static boolean evadingDamageBonus(Player player) {
        return anyChance(player, "evadingDamageChance", level -> ConfigInit.CONFIG.evadingDamageChanceBonus);
    }
}