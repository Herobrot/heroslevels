package com.herobrot.heroslevels.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.init.ConfigInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.level.Skill;
import com.herobrot.heroslevels.level.restriction.PlayerRestriction;
import com.herobrot.heroslevels.registry.EnchantmentRegistry;
import com.herobrot.heroslevels.util.CompatUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;

public class RestrictionLoader extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = HerosLevels.LOGGER;
    private static final Gson GSON = new GsonBuilder().create();

    private static final Set<Integer> blockSeen = new HashSet<>();
    private static final Set<Integer> craftingSeen = new HashSet<>();
    private static final Set<Integer> entitySeen = new HashSet<>();
    private static final Set<Integer> itemSeen = new HashSet<>();
    private static final Set<Integer> miningSeen = new HashSet<>();
    private static final Set<Integer> enchantmentSeen = new HashSet<>();
    private static final Set<Integer> brewingSeen = new HashSet<>();

    private final HolderLookup.Provider provider;

    public RestrictionLoader(HolderLookup.Provider provider) {
        super(GSON, "restrictions");
        this.provider = provider;
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        LevelManager.BLOCK_RESTRICTIONS.clear();
        LevelManager.CRAFTING_RESTRICTIONS.clear();
        LevelManager.ENTITY_RESTRICTIONS.clear();
        LevelManager.ITEM_RESTRICTIONS.clear();
        LevelManager.MINING_RESTRICTIONS.clear();
        LevelManager.ENCHANTMENT_RESTRICTIONS.clear();
        LevelManager.BREWING_RESTRICTIONS.clear();
        blockSeen.clear();
        craftingSeen.clear();
        entitySeen.clear();
        itemSeen.clear();
        miningSeen.clear();
        enchantmentSeen.clear();
        brewingSeen.clear();

        if (!ConfigInit.CONFIG.restrictions) return;
        EnchantmentRegistry.updateEnchantments(this.provider);
        Map<String, Integer> skillKeyIdMap = new HashMap<>();
        for (Skill skill : LevelManager.SKILLS.values())
            skillKeyIdMap.put(skill.key(), skill.id());
        object.forEach((id, element) -> {
            try {
                if (!ConfigInit.CONFIG.defaultRestrictions
                        && id.getNamespace().equals(HerosLevels.MOD_ID)
                        && id.getPath().equals("default"))
                    return;
                JsonObject data = element.getAsJsonObject();
                for (String mapKey : data.keySet()) {
                    JsonObject restrictionJsonObject = data.getAsJsonObject(mapKey);
                    Map<Integer, Integer> skillLevelRestrictions = new HashMap<>();
                    boolean replace = restrictionJsonObject.has("replace") && restrictionJsonObject.get("replace").getAsBoolean();
                    JsonObject skillRestrictions = restrictionJsonObject.getAsJsonObject("skills");
                    for (String skillKey : skillRestrictions.keySet()) {
                        if (skillKeyIdMap.containsKey(skillKey))
                            skillLevelRestrictions.put(skillKeyIdMap.get(skillKey),
                                    skillRestrictions.get(skillKey).getAsInt());
                        else
                            LOGGER.warn("[Hero's Levels-WARN]: La restricción {} contiene una skill no reconocida llamada {}.",
                                    mapKey, skillKey);
                    }
                    if (skillLevelRestrictions.isEmpty()) {
                        LOGGER.warn("[Hero's Levels-WARN]: La restricción {} no contiene ninguna skill valida.", mapKey);
                        continue;
                    }
                    loadArrayRestriction(restrictionJsonObject, "blocks", BuiltInRegistries.BLOCK, LevelManager.BLOCK_RESTRICTIONS, blockSeen, replace, skillLevelRestrictions, mapKey);
                    loadArrayRestriction(restrictionJsonObject, "crafting", BuiltInRegistries.ITEM, LevelManager.CRAFTING_RESTRICTIONS, craftingSeen, replace, skillLevelRestrictions, mapKey);
                    loadArrayRestriction(restrictionJsonObject, "entities", BuiltInRegistries.ENTITY_TYPE, LevelManager.ENTITY_RESTRICTIONS, entitySeen, replace, skillLevelRestrictions, mapKey);
                    loadArrayRestriction(restrictionJsonObject, "items", BuiltInRegistries.ITEM, LevelManager.ITEM_RESTRICTIONS, itemSeen, replace, skillLevelRestrictions, mapKey);
                    loadArrayRestriction(restrictionJsonObject, "mining", BuiltInRegistries.BLOCK, LevelManager.MINING_RESTRICTIONS, miningSeen, replace, skillLevelRestrictions, mapKey);
                    loadArrayRestriction(restrictionJsonObject, "brewing", BuiltInRegistries.POTION, LevelManager.BREWING_RESTRICTIONS, brewingSeen, replace, skillLevelRestrictions, mapKey);
                    loadEnchantmentRestriction(restrictionJsonObject, replace, skillLevelRestrictions, mapKey);
                }
            } catch (Exception e) {
                LOGGER.error("[HerosLevel-ERROR]: Ocurrió mientras se cargaba el recurso {}. {}", id.toString(), e.toString());
            }
        });
    }

    private static <T> void loadArrayRestriction(
            JsonObject restrictionJsonObject,
            String arrayKey,
            Registry<T> registry,
            Map<Integer, PlayerRestriction> targetMap,
            Set<Integer> seenIds,
            boolean replace,
            Map<Integer, Integer> skillLevelRestrictions,
            String mapKey
    ) {
        if (!restrictionJsonObject.has(arrayKey)) return;
        for (JsonElement element : restrictionJsonObject.getAsJsonArray(arrayKey)) {
            ResourceLocation identifier = ResourceLocation.parse(element.getAsString());
            List<ResourceLocation> targetIdentifiers = (registry == BuiltInRegistries.BLOCK)
                    ? CompatUtil.getAssociatedBlockIds(identifier)
                    : List.of(identifier);
            for (ResourceLocation targetId : targetIdentifiers) {
                Optional<T> optionalValue = registry.getOptional(targetId);
                if (optionalValue.isEmpty()) {
                    if (targetId.equals(identifier))
                        LOGGER.warn("[Hero's Levels-WARN]: La restricción {} contiene un {} id no reconocido llamado {}.",
                                mapKey, arrayKey, targetId);
                    continue;
                }
                T value = optionalValue.get();
                int rawId = registry.getId(value);
                if (replace) seenIds.add(rawId);
                else if (seenIds.contains(rawId)) continue;
                targetMap.put(rawId, new PlayerRestriction(rawId, skillLevelRestrictions));
            }
        }
    }

    private static void loadEnchantmentRestriction(
            JsonObject restrictionJsonObject,
            boolean replace,
            Map<Integer, Integer> skillLevelRestrictions,
            String mapKey
    ) {
        if (!restrictionJsonObject.has("enchantments")) return;
        JsonObject enchantmentObject = restrictionJsonObject.getAsJsonObject("enchantments");
        for (String enchantment : enchantmentObject.keySet()) {
            ResourceLocation enchantmentIdentifier = ResourceLocation.parse(enchantment);
            int level = enchantmentObject.get(enchantment).getAsInt();
            int enchantmentRawId = EnchantmentRegistry.getId(enchantmentIdentifier, level);
            if (enchantmentRawId == -1) {
                LOGGER.warn("[Hero's Levels-WARN]: La restricción {} contiene un enchantment id no reconocido llamado {}.",
                        mapKey, enchantmentIdentifier);
                continue;
            }
            if (replace) enchantmentSeen.add(enchantmentRawId);
            else if (enchantmentSeen.contains(enchantmentRawId)) continue;
            LevelManager.ENCHANTMENT_RESTRICTIONS.put(enchantmentRawId, new PlayerRestriction(enchantmentRawId, skillLevelRestrictions));
        }
    }
}