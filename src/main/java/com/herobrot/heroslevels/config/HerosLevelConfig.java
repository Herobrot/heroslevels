package com.herobrot.heroslevels.config;

import com.herobrot.heroslib.api.ConfigSync.ClientOnly;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "heroslevels")
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class HerosLevelConfig implements ConfigData {

    // Level settings
    @ConfigEntry.Category("level_settings")
    @ConfigEntry.Gui.RequiresRestart
    @ConfigEntry.Gui.Tooltip
    public int overallMaxLevel = 0;
    @ConfigEntry.Category("level_settings")
    @ConfigEntry.Gui.Tooltip
    public boolean allowHigherSkillLevel = false;
    @ConfigEntry.Category("level_settings")
    @ConfigEntry.Gui.RequiresRestart
    public int startPoints = 5;
    @ConfigEntry.Category("level_settings")
    public int pointsPerLevel = 3;

    @ConfigEntry.Category("level_settings")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int deathXpRecoveryPercent = 15;
    @ConfigEntry.Category("level_settings")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    @ConfigEntry.Gui.Tooltip
    public int levelRetainPercentage = 100;
    @ConfigEntry.Category("level_settings")
    @ConfigEntry.Gui.Tooltip
    public boolean disableMobFarms = true;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("level_settings")
    public boolean disableMobFarmsLoot = true;
    @ConfigEntry.Category("level_settings")
    public int mobKillCount = 6;
    //@ConfigEntry.Category("level_settings")
    //Falta de implementar (tal vez ni lo haga)
    //public boolean opStrangePotion = false;
    @ClientOnly
    @ConfigEntry.Gui.Tooltip //Client only
    @ConfigEntry.Category("level_settings")
    public boolean lockedHandUsage = false;
    //@ConfigEntry.Category("level_settings")
    //@Deprecated El mod original lo agregaba para registrar ids del menú creativo
    //public boolean devMode = false;

    // Skill bonuses
    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float bowDamageBonus = 0.5F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float bowDoubleDamageChanceBonus = 0.1F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float crossbowDamageBonus = 0.5F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float crossbowDoubleDamageChanceBonus = 0.1F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float itemDamageChanceBonus = 0.01F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float potionAmplifierChanceBonus = 0.02F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float potionDurationChanceBonus = 0.025F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float lingeringCloudRadiusChanceBonus = 0.02F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float lingeringCloudRadiusBonus = 0.2F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public int lingeringCloudDurationBonus = 2;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float twinBreedChanceBonus = 0.2F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float fallDamageReductionBonus = 0.2F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float deathGraceChanceBonus = 0.2F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float tntStrengthBonus = 1F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float priceDiscountBonus = 0.01F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float tradeXpBonus = 0.02F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float miningDropChanceBonus = 0.01F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float plantDropChanceBonus = 0.01F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public int anvilXpCap = 30;
    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float anvilXpDiscountBonus = 0.01F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float anvilXpChanceBonus = 0.01F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float healthRegenBonus = 0.025F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float healthAbsorptionBonus = 4F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float exhaustionReductionBonus = 0.02F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float meleeKnockbackAttackChanceBonus = 0.01F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float meleeCriticalAttackChanceBonus = 0.01F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float meleeCriticalAttackDamageBonus = 0.3F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float meleeDoubleAttackDamageChanceBonus = 0.2F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float foodIncreasionBonus = 0.02F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float damageReflectionBonus = 0.02F;
    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float damageReflectionChanceBonus = 0.005F;

    @ConfigEntry.Category("bonus_settings")
    @ConfigEntry.Gui.Tooltip
    public float evadingDamageChanceBonus = 0.1F;

    // Experience settings
    @ConfigEntry.Category("experience_settings")
    @ConfigEntry.Gui.Tooltip
    public float xpCostMultiplicator = 0.1F;
    @ConfigEntry.Category("experience_settings")
    public int xpExponent = 2;
    @ConfigEntry.Category("experience_settings")
    public int xpBaseCost = 50;
    @ConfigEntry.Category("experience_settings")
    @ConfigEntry.Gui.Tooltip
    public int xpMaxCost = 0;
    @ConfigEntry.Category("experience_settings")
    public boolean resetCurrentXp = true;
    @ConfigEntry.Category("experience_settings")
    public boolean dropXPBasedOnLvl = false;
    @ConfigEntry.Category("experience_settings")
    @ConfigEntry.Gui.Tooltip
    public float basedOnMultiplier = 0.01F;
    @ConfigEntry.Category("experience_settings")
    public float breedingXPMultiplier = 1.0F;
    @ConfigEntry.Category("experience_settings")
    public float bottleXPMultiplier = 1.0F;
    @ConfigEntry.Category("experience_settings")
    public float dragonXPMultiplier = 0.5F;
    @ConfigEntry.Category("experience_settings")
    public float fishingXPMultiplier = 0.8F;
    @ConfigEntry.Category("experience_settings")
    public float furnaceXPMultiplier = 0.1F;
    @ConfigEntry.Category("experience_settings")
    public float oreXPMultiplier = 1.0F;
    @ConfigEntry.Category("experience_settings")
    public float tradingXPMultiplier = 0.3F;
    @ConfigEntry.Category("experience_settings")
    public float mobXPMultiplier = 1.0F;
    @ConfigEntry.Category("experience_settings")
    public boolean spawnerMobXP = false;

    @ClientOnly
    @ConfigEntry.Gui.Tooltip //Client only
    @ConfigEntry.Category("gui_settings")
    public boolean showLevelTab = true;
    @ClientOnly
    @ConfigEntry.Gui.Tooltip //Client only
    @ConfigEntry.Category("gui_settings")
    public boolean highlightLocked = true;
    @ClientOnly
    @ConfigEntry.Gui.Tooltip //Client only
    @ConfigEntry.Category("gui_settings")
    public boolean inventorySkillLevel = true;
    @ClientOnly
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("gui_settings")
    @ConfigEntry.ColorPicker
    public int availablePointsColor = 0x16FEE7;
    @ClientOnly
    @ConfigEntry.Category("gui_settings")
    @ConfigEntry.ColorPicker
    public int headerBoldTextColor = 0xFFFFFF;
    @ClientOnly
    @ConfigEntry.Category("gui_settings")
    @ConfigEntry.ColorPicker
    public int normalTextColor = 0xe2e2e2;
    @ClientOnly
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("gui_settings")
    @ConfigEntry.BoundedDiscrete(min = 50, max = 150)
    public int inventorySkillLevelScale = 65;
    @ClientOnly
    @ConfigEntry.Gui.Tooltip //Client only
    @ConfigEntry.Category("gui_settings")
    public int inventorySkillLevelPosX = 0;
    @ClientOnly
    @ConfigEntry.Gui.Tooltip //Client only
    @ConfigEntry.Category("gui_settings")
    public int inventorySkillLevelPosY = 0;
    @ClientOnly
    @ConfigEntry.Gui.Tooltip //Client only
    @ConfigEntry.Category("gui_settings")
    @ConfigEntry.Gui.RequiresRestart
    public boolean showLevelList = true;
    @ClientOnly
    @ConfigEntry.Gui.Tooltip //Client only
    @ConfigEntry.Category("gui_settings")
    public boolean showLevel = true;
    @ClientOnly
    @ConfigEntry.Gui.Tooltip //Client only
    @ConfigEntry.Category("gui_settings")
    public boolean switchScreen = false;
    @ClientOnly
    @ConfigEntry.Gui.Tooltip //Client only
    @ConfigEntry.Category("gui_settings")
    public boolean showLockedBlockInfo = false;
    @ClientOnly
    @ConfigEntry.Gui.Tooltip //Client only
    @ConfigEntry.Category("gui_settings")
    public int lockedBlockInfoPosX = 0;
    @ClientOnly
    @ConfigEntry.Gui.Tooltip //Client only
    @ConfigEntry.Category("gui_settings")
    public int lockedBlockInfoPosY = 0;

    @ConfigEntry.Category("progression_settings")
    public boolean restrictions = true;
    @ConfigEntry.Category("progression_settings")
    public boolean defaultRestrictions = true;
    @ConfigEntry.Category("progression_settings")
    @ConfigEntry.Gui.Tooltip
    public boolean defaultSkills = true;

}