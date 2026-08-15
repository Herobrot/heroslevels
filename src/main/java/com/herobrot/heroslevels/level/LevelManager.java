package com.herobrot.heroslevels.level;

import com.herobrot.heroslevels.api.events.HerosLevelUpEvent;
import com.herobrot.heroslevels.init.ConfigInit;
import com.herobrot.heroslevels.init.CriteriaInit;
import com.herobrot.heroslevels.level.restriction.PlayerRestriction;
import com.herobrot.heroslevels.registry.EnchantmentRegistry;
import com.herobrot.heroslevels.util.LevelHelper;
import com.herobrot.heroslevels.util.PacketHelper;
import com.herobrot.heroslib.attachment.AttachmentRegistryHelper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;

public class LevelManager implements AttachmentRegistryHelper.INBTSyncable {

    public static final Map<Integer, Skill> SKILLS = new TreeMap<>();
    public static final Map<Integer, PlayerRestriction> BLOCK_RESTRICTIONS = new HashMap<>();
    public static final Map<Integer, PlayerRestriction> CRAFTING_RESTRICTIONS = new HashMap<>();
    public static final Map<Integer, PlayerRestriction> ENTITY_RESTRICTIONS = new HashMap<>();
    public static final Map<Integer, PlayerRestriction> ITEM_RESTRICTIONS = new HashMap<>();
    public static final Map<Integer, PlayerRestriction> MINING_RESTRICTIONS = new HashMap<>();
    public static final Map<Integer, PlayerRestriction> ENCHANTMENT_RESTRICTIONS = new HashMap<>();
    public static final Map<Integer, PlayerRestriction> BREWING_RESTRICTIONS = new HashMap<>();
    public static final Map<String, List<SkillBonus>> BONUSES = new HashMap<>();

    private final Player playerEntity;
    private Map<Integer, PlayerSkill> playerSkills = new HashMap<>();
    private final Set<String> managedBaseAttributes = new HashSet<>();
    private float savedHealth = -1.0F;

    // Level
    private int overallLevel;
    private int totalLevelExperience;
    private float levelProgress;
    private int skillPoints;
    private ChunkPos lastKillChunk = null;
    private int killedMobsInChunk = 0;

    public LevelManager(Player playerEntity) {
        this.playerEntity = playerEntity;
        for (Skill skill : SKILLS.values())
            this.playerSkills.put(skill.id(), new PlayerSkill(skill.id(), 0));
    }

    // Sin uso en el proyecto
    public Player getPlayerEntity() {
        return playerEntity;
    }

    public void readNbt(CompoundTag tag) {
        this.overallLevel = tag.getInt("Level");
        this.levelProgress = tag.getFloat("LevelProgress");
        this.totalLevelExperience = tag.getInt("TotalLevelExperience");
        this.skillPoints = tag.getInt("SkillPoints");

        ListTag skills = tag.getList("Skills", Tag.TAG_COMPOUND);
        for (int i = 0; i < skills.size(); i++) {
            PlayerSkill skill = new PlayerSkill(skills.getCompound(i));
            if (!SKILLS.containsKey(skill.getId())) continue;
            playerSkills.put(skill.getId(), skill);
        }
        if (tag.contains("SavedHealth"))
            this.savedHealth = tag.getFloat("SavedHealth");

        this.managedBaseAttributes.clear();
        ListTag managedAttrs = tag.getList("ManagedBaseAttributes", Tag.TAG_STRING);
        for (int i = 0; i < managedAttrs.size(); i++)
            this.managedBaseAttributes.add(managedAttrs.getString(i));
    }

    public void writeNbt(CompoundTag tag) {
        tag.putInt("Level", this.overallLevel);
        tag.putFloat("LevelProgress", this.levelProgress);
        tag.putInt("TotalLevelExperience", this.totalLevelExperience);
        tag.putInt("SkillPoints", this.skillPoints);

        ListTag skills = new ListTag();
        for (PlayerSkill skill : this.playerSkills.values())
            skills.add(skill.writeDataToNbt());

        tag.put("Skills", skills);
        if (this.playerEntity != null)
            tag.putFloat("SavedHealth", this.playerEntity.getHealth());

        ListTag managedAttrs = new ListTag();
        for (String key : this.managedBaseAttributes)
            managedAttrs.add(StringTag.valueOf(key));

        tag.put("ManagedBaseAttributes", managedAttrs);
    }

    // Getters & Setters Base
    public Map<Integer, PlayerSkill> getPlayerSkills() { return playerSkills; }
    // Sin uso en el proyecto
    public void setPlayerSkills(Map<Integer, PlayerSkill> playerSkills) { this.playerSkills = playerSkills; }

    public Set<String> getManagedBaseAttributes() { return managedBaseAttributes; }
    public void setOverallLevel(int overallLevel) { this.overallLevel = overallLevel; }
    public int getOverallLevel() { return overallLevel; }
    public void setTotalLevelExperience(int totalLevelExperience) { this.totalLevelExperience = totalLevelExperience; }
    public int getTotalLevelExperience() { return totalLevelExperience; }
    public void setSkillPoints(int skillPoints) { this.skillPoints = skillPoints; }
    public int getSkillPoints() { return skillPoints; }
    public void setLevelProgress(float levelProgress) { this.levelProgress = levelProgress; }
    public float getLevelProgress() { return levelProgress; }
    public float getSavedHealth() { return savedHealth; }
    public void clearSavedHealth() { this.savedHealth = -1.0F; }

    // Helpers genérico
    private boolean hasRequiredLevel(Map<Integer, PlayerRestriction> restrictions, int objId) {
        PlayerRestriction playerRestriction = restrictions.get(objId);
        if (playerRestriction == null) return true;
        for (Map.Entry<Integer, Integer> entry : playerRestriction.skillLevelRestrictions().entrySet())
            if (this.getSkillLevel(entry.getKey()) < entry.getValue()) return false;
        return true;
    }

    private Map<Integer, Integer> getRequiredLevel(Map<Integer, PlayerRestriction> restrictions, int objId) {
        PlayerRestriction playerRestriction = restrictions.get(objId);
        return playerRestriction != null ? playerRestriction.skillLevelRestrictions() : Map.of();
    }

    // Skill Management
    public void setSkillLevel(int skillId, int level) {
        this.playerSkills.computeIfAbsent(skillId, id -> new PlayerSkill(id, 0)).setLevel(level);
    }

    public int getSkillLevel(int skillId) {
        PlayerSkill skill = this.playerSkills.get(skillId);
        return skill != null ? skill.getLevel() : 0;
    }

    @SuppressWarnings("resource")
    public void addExperience(int experience) {
        if (!isMaxLevel() && this.playerEntity instanceof ServerPlayer serverPlayer) {
            this.levelProgress += Math.max((float) experience / getNextLevelExperience(), 0);
            this.totalLevelExperience = Mth.clamp(this.totalLevelExperience + experience, 0, Integer.MAX_VALUE);
            while (this.levelProgress >= 1.0F && !isMaxLevel()) {
                float excessXpRatio = this.levelProgress - 1.0F;
                float excessXpFlat = excessXpRatio * (float) getNextLevelExperience();
                int oldLevel = this.overallLevel;
                addExperienceLevels(1);
                HerosLevelUpEvent event = new HerosLevelUpEvent(this.playerEntity, oldLevel, this.overallLevel);
                if (!NeoForge.EVENT_BUS.post(event).isCanceled()) {
                    CriteriaInit.LEVEL_UP.get().trigger(serverPlayer);
                    PacketHelper.refreshTabListDisplay(serverPlayer);
                    if (this.overallLevel > 0)
                        serverPlayer.level().playSound(
                                null,
                                serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                                net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP,
                                net.minecraft.sounds.SoundSource.PLAYERS,
                                1.0F, 1.0F
                        );
                }
                if (isMaxLevel()) this.levelProgress = 0.0F;
                else this.levelProgress = excessXpFlat / getNextLevelExperience();

            }
        }
    }

    public void addExperienceLevels(int levels) {
        this.overallLevel += levels;
        this.skillPoints += ConfigInit.CONFIG.pointsPerLevel;
        if (this.overallLevel < 0) {
            this.overallLevel = 0;
            this.levelProgress = 0.0F;
            this.totalLevelExperience = 0;
        }
    }

    public boolean isMaxLevel() {
        if (ConfigInit.CONFIG.overallMaxLevel > 0)
            return this.overallLevel >= ConfigInit.CONFIG.overallMaxLevel;
        else {
            int maxLevel = 0;
            for (Skill skill : SKILLS.values()) maxLevel += skill.maxLevel();
            return this.overallLevel >= maxLevel;
        }
    }

    public boolean hasAvailableLevel() { return this.skillPoints > 0; }

    public int getNextLevelExperience() {
        if (isMaxLevel()) return 0;
        return getExperienceCostForLevel(this.overallLevel);
    }

    public static int getExperienceCostForLevel(int level) {
        int experienceCost = (int) (ConfigInit.CONFIG.xpBaseCost + ConfigInit.CONFIG.xpCostMultiplicator
                * Math.pow(level, ConfigInit.CONFIG.xpExponent));
        if (ConfigInit.CONFIG.xpMaxCost != 0) return Math.min(experienceCost, ConfigInit.CONFIG.xpMaxCost);
        return experienceCost;
    }

    public static int getExperienceBetweenLevels(int fromLevel, int toLevel) {
        if (toLevel <= fromLevel) return 0;
        int total = 0;
        for (int lvl = fromLevel; lvl < toLevel; lvl++) total += getExperienceCostForLevel(lvl);
        return total;
    }

    // --- Block Restrictions ---
    public boolean hasRequiredBlockLevel(Block block) {
        return hasRequiredLevel(BLOCK_RESTRICTIONS, BuiltInRegistries.BLOCK.getId(block));
    }

    public Map<Integer, Integer> getRequiredBlockLevel(Block block) {
        return getRequiredLevel(BLOCK_RESTRICTIONS, BuiltInRegistries.BLOCK.getId(block));
    }

    // --- Crafting Restrictions ---
    public boolean hasRequiredCraftingLevel(Item item) {
        return hasRequiredLevel(CRAFTING_RESTRICTIONS, BuiltInRegistries.ITEM.getId(item));
    }

    // --- Entity Restrictions ---
    public boolean hasRequiredEntityLevel(EntityType<?> entityType) {
        return hasRequiredLevel(ENTITY_RESTRICTIONS, BuiltInRegistries.ENTITY_TYPE.getId(entityType));
    }

    public Map<Integer, Integer> getRequiredEntityLevel(EntityType<?> entityType) {
        return getRequiredLevel(ENTITY_RESTRICTIONS, BuiltInRegistries.ENTITY_TYPE.getId(entityType));
    }

    // --- Item Restrictions ---
    public boolean hasRequiredItemLevel(Item item) {
        return hasRequiredLevel(ITEM_RESTRICTIONS, BuiltInRegistries.ITEM.getId(item));
    }

    public boolean hasRequiredItemAndEnchantmentLevel(ItemStack stack) {
        if (!hasRequiredItemLevel(stack.getItem())) return false;
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet())
            if (!hasRequiredEnchantmentLevel(entry.getKey(), entry.getIntValue()))
                return false;
        ItemEnchantments storedEnchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : storedEnchantments.entrySet())
            if (!hasRequiredEnchantmentLevel(entry.getKey(), entry.getIntValue()))
                return false;
        return true;
    }

    // --- Mining Restrictions ---
    public boolean hasRequiredMiningLevel(Block block) {
        return hasRequiredLevel(MINING_RESTRICTIONS, BuiltInRegistries.BLOCK.getId(block));
    }

    public Map<Integer, Integer> getRequiredMiningLevel(Block block) {
        return getRequiredLevel(MINING_RESTRICTIONS, BuiltInRegistries.BLOCK.getId(block));
    }

    // --- Enchantment Restrictions ---
    public boolean hasRequiredEnchantmentLevel(Holder<Enchantment> enchantment, int level) {
        ResourceLocation id = enchantment.unwrapKey().map(ResourceKey::location).orElse(null);
        if (id == null) return true;
        int enchantmentId = EnchantmentRegistry.getOrRegisterId(id, level, this.playerEntity.level().registryAccess());
        if (enchantmentId == -1) return true;
        return hasRequiredLevel(ENCHANTMENT_RESTRICTIONS, enchantmentId);
    }

    // Sin uso en el proyecto
    public Map<Integer, Integer> getRequiredEnchantmentLevel(Holder<Enchantment> enchantment, int level) {
        return getRequiredLevel(ENCHANTMENT_RESTRICTIONS, EnchantmentRegistry.getId(enchantment, level));
    }

    // Sin uso en el proyecto
    public boolean resetSkill(int skillId) {
        int level = this.getSkillLevel(skillId);
        if (level > 0) {
            this.setSkillPoints(this.getSkillPoints() + level);
            this.setSkillLevel(skillId, 0);
            if (this.playerEntity instanceof ServerPlayer serverPlayer) {
                PacketHelper.syncPlayerSkills(serverPlayer);
                Skill skill = SKILLS.get(skillId);
                if (skill != null) {
                    LevelHelper.clearSkillModifiers(serverPlayer, skill.key());
                    LevelHelper.updateSkill(serverPlayer, skill);
                }
                PacketHelper.updateLevels(serverPlayer);
            }
            return true;
        }
        return false;
    }

    public boolean hasRequiredBrewingLevel(ItemStack stack) {
        if (stack.has(DataComponents.POTION_CONTENTS)) {
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            if (contents != null && contents.potion().isPresent()) {
                int potionId = BuiltInRegistries.POTION.getId(contents.potion().get().value());
                return hasRequiredLevel(BREWING_RESTRICTIONS, potionId);
            }
        }
        return true;
    }

    public void recordMobKill(ChunkPos chunkPos) {
        if (this.lastKillChunk != null && this.lastKillChunk.equals(chunkPos))
            this.killedMobsInChunk++;
        else {
            this.lastKillChunk = chunkPos;
            this.killedMobsInChunk = 1;
        }
    }

    public boolean isMobFarmLimitReached() {
        return this.killedMobsInChunk > ConfigInit.CONFIG.mobKillCount;
    }
}