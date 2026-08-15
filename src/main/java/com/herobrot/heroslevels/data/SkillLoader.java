package com.herobrot.heroslevels.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.init.ConfigInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.level.Skill;
import com.herobrot.heroslevels.level.SkillAttribute;
import com.herobrot.heroslevels.level.SkillBonus;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;

public class SkillLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();
    private static final Logger LOGGER = HerosLevels.LOGGER;

    public SkillLoader() { super(GSON, "skills"); }

    private static class SkillBuilder {
        String key;
        int maxLevel;
        final List<SkillAttribute> attributes = new ArrayList<>();
        final Set<ResourceLocation> baseOverrideAttributeTypes = new HashSet<>();
        int nextAttributeId = 0;

        boolean isEmpty() {
            return key == null;
        }
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        LevelManager.SKILLS.clear();
        LevelManager.BONUSES.clear();
        Map<Integer, SkillBuilder> builders = new HashMap<>();
        Set<Integer> replaceLocked = new HashSet<>();
        object.forEach((id, element) -> {
            try {
                if (!ConfigInit.CONFIG.defaultSkills
                        && id.getNamespace().equals(HerosLevels.MOD_ID)
                        && id.getPath().equals("default")) { return; }
                JsonObject data = element.getAsJsonObject();
                for (String mapKey : data.keySet())
                    processSkillEntry(data.getAsJsonObject(mapKey), mapKey, builders, replaceLocked);
            } catch (Exception e) {
                LOGGER.error("[Hero's Levels-ERROR]: Error cargando recurso {}: {}", id, e);
            }
        });
        for (Map.Entry<Integer, SkillBuilder> entry : builders.entrySet()) {
            int identification = entry.getKey();
            SkillBuilder builder = entry.getValue();
            LevelManager.SKILLS.put(identification, new Skill(identification, builder.key, builder.maxLevel, builder.attributes));
        }
        int totalSkills = LevelManager.SKILLS.size();
        for (int i = 0; i < totalSkills; i++)
            if (!LevelManager.SKILLS.containsKey(i))
                LOGGER.error("[Hero's Levels-ERROR]: Falta la skill con id {}", i);
    }

    private static void processSkillEntry(JsonObject skillJsonObject, String mapKey, Map<Integer, SkillBuilder> builders, Set<Integer> replaceLocked) {
        int identification = skillJsonObject.get("id").getAsInt();
        if (replaceLocked.contains(identification)) {
            LOGGER.warn("[Hero's Levels-WARN]: La skill {} (id {}) ya fue reemplazada con 'replace: true' en otro archivo; " +
                    "se ignora esta definición adicional.", mapKey, identification);
            return;
        }
        boolean replace = skillJsonObject.has("replace") && skillJsonObject.get("replace").getAsBoolean();
        if (replace) {
            builders.put(identification, new SkillBuilder());
            replaceLocked.add(identification);
            purgeBonusContributions(identification);
        }
        SkillBuilder builder = builders.computeIfAbsent(identification, i -> new SkillBuilder());

        if (builder.isEmpty()) {
            builder.key = skillJsonObject.get("key").getAsString();
            builder.maxLevel = skillJsonObject.get("level").getAsInt();
        } else if (!replace && (skillJsonObject.has("key") || skillJsonObject.has("level")))
            LOGGER.warn("[Hero's Levels-WARN]: La skill {} (id {}) intenta redefinir 'key'/'level' sin 'replace: true'; " +
                    "esos campos se ignoran, solo se fusionan attributes/bonus.", mapKey, identification);

        if (skillJsonObject.has("attributes"))
            loadAttributes(skillJsonObject.getAsJsonArray("attributes"), builder, identification, mapKey);
        if (skillJsonObject.has("bonus"))
            loadBonuses(skillJsonObject.getAsJsonArray("bonus"), identification, mapKey);
    }

    private static void loadAttributes(com.google.gson.JsonArray attributesArray, SkillBuilder builder, int identification, String mapKey) {
        for (JsonElement attributeElement : attributesArray) {
            JsonObject attributeJsonObject = attributeElement.getAsJsonObject();
            ResourceLocation attrTypeId = ResourceLocation.parse(attributeJsonObject.get("type").getAsString());
            var optionalAttribute = BuiltInRegistries.ATTRIBUTE.getOptional(attrTypeId);
            if (optionalAttribute.isEmpty()) {
                LOGGER.warn("Atributo {} invalido en la skill {}.", attrTypeId, identification);
                continue;
            }

            var attributeHolder = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(optionalAttribute.get());
            float levelValue = attributeJsonObject.get("value").getAsFloat();
            AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(attributeJsonObject.get("operation").getAsString().toUpperCase());
            float baseValue = SkillAttribute.NO_BASE_VALUE_OVERRIDE;
            if (attributeJsonObject.has("base")) {
                if (builder.baseOverrideAttributeTypes.contains(attrTypeId))
                    LOGGER.warn("[Hero's Levels-WARN]: La skill {} (id {}) tiene múltiples definiciones de 'base' " +
                            "para el atributo {}; se ignora esta segunda declaración.",
                            mapKey, identification, attrTypeId);
                else {
                    baseValue = attributeJsonObject.get("base").getAsFloat();
                    builder.baseOverrideAttributeTypes.add(attrTypeId);
                }
            }
            int displayGroupId = attributeJsonObject.has("id") ? attributeJsonObject.get("id").getAsInt() : SkillAttribute.NO_DISPLAY_GROUP;
            int attributeId = builder.nextAttributeId++;
            builder.attributes.add(new SkillAttribute(attributeId, displayGroupId, attributeHolder, baseValue, levelValue, operation));
        }
    }

    private static void loadBonuses(com.google.gson.JsonArray bonusArray, int identification, String mapKey) {
        for (JsonElement bonusElement : bonusArray) {
            JsonObject bonusJsonObject = bonusElement.getAsJsonObject();
            String bonusKey = bonusJsonObject.get("key").getAsString();
            int bonusLevel = bonusJsonObject.get("level").getAsInt();
            if (!SkillBonus.BONUS_KEYS.contains(bonusKey)) {
                LOGGER.warn("[Hero's Levels-WARN]: Bonus {} invalido en la skill {} (id {}).",
                        bonusKey, mapKey, identification);
                continue;
            }
            List<SkillBonus> contributions = LevelManager.BONUSES.computeIfAbsent(bonusKey, k -> new ArrayList<>());
            contributions.removeIf(existing -> existing.id() == identification);
            contributions.add(new SkillBonus(bonusKey, identification, bonusLevel));
        }
    }

    private static void purgeBonusContributions(int identification) {
        for (List<SkillBonus> contributions : LevelManager.BONUSES.values())
            contributions.removeIf(bonus -> bonus.id() == identification);
    }
}