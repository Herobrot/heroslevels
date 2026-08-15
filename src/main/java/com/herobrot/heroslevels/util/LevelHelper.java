package com.herobrot.heroslevels.util;

import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.init.ConfigInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.level.Skill;
import com.herobrot.heroslevels.level.SkillAttribute;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class LevelHelper {

    /**
     * Limpia modificadores de atributo aplicados previamente por el mod, buscándolos por
     * prefijo en vez de por índice posicional — necesario porque {@link SkillAttribute#id()}
     * es un índice autoincremental dependiente del orden/cantidad de atributos declarados en
     * el datapack, y por lo tanto puede cambiar entre versiones del datapack (reordenar,
     * insertar o eliminar un atributo desplaza los índices de los demás), dejando
     * modificadores "huérfanos" que antes nunca se limpiaban.
     *
     * @param specificSkillKey si es {@code null}, limpia TODOS los modificadores del mod en
     *                         el jugador (limpieza global, usar antes de reaplicar todas las
     *                         skills). Si se provee, limpia solo los de esa skill puntual.
     */
    public static void clearSkillModifiers(ServerPlayer serverPlayer, @Nullable String specificSkillKey) {
        String prefix = specificSkillKey != null
                ? HerosLevels.MOD_ID + ":" + specificSkillKey + "_"
                : HerosLevels.MOD_ID + ":";

        for (Attribute attributeType : BuiltInRegistries.ATTRIBUTE) {
            AttributeInstance instance = serverPlayer.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attributeType));
            if (instance == null) continue;
            instance.getModifiers().stream()
                    .map(AttributeModifier::id)
                    .filter(id -> id.toString().startsWith(prefix))
                    .toList()
                    .forEach(instance::removeModifier);
        }
    }

    /**
     * Aplica los modificadores vigentes de una skill. Asume que cualquier residuo de
     * versiones anteriores del datapack ya fue limpiado previamente — ver
     * {@link #clearSkillModifiers(ServerPlayer, String)}. Los callers que invocan este
     * método de forma aislada (fuera de {@link #applyAllSkills}) deben llamar primero a
     * {@code clearSkillModifiers(player, skill.key())} para esa skill puntual.
     */
    public static void updateSkill(ServerPlayer serverPlayer, Skill skill) {
        LevelManager levelManager = serverPlayer.getData(AttachmentInit.LEVEL_MANAGER);
        for (SkillAttribute skillAttribute : skill.attributes()) {
            AttributeInstance instance = serverPlayer.getAttribute(skillAttribute.attribute());
            if (instance != null) {
                if (skillAttribute.baseValue() > SkillAttribute.NO_BASE_VALUE_OVERRIDE) {
                    instance.setBaseValue(skillAttribute.baseValue());
                    skillAttribute.attribute().unwrapKey().ifPresent(key ->
                            levelManager.getManagedBaseAttributes().add(key.location().toString()));
                }

                if (levelManager.getSkillLevel(skill.id()) > 0) {
                    ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, skill.key() + "_" + skillAttribute.id());
                    AttributeModifier modifier = new AttributeModifier(
                            modifierId,
                            skillAttribute.levelValue() * levelManager.getSkillLevel(skill.id()),
                            skillAttribute.operation()
                    );
                    instance.addTransientModifier(modifier);
                }
            }
        }
    }

    public static void applyAllSkills(ServerPlayer serverPlayer) {
        LevelManager levelManager = serverPlayer.getData(AttachmentInit.LEVEL_MANAGER);
        clearSkillModifiers(serverPlayer, null);
        AttributeSupplier playerDefaults = DefaultAttributes.getSupplier(EntityType.PLAYER);

        for (String attrKey : new ArrayList<>(levelManager.getManagedBaseAttributes())) {
            BuiltInRegistries.ATTRIBUTE.getOptional(ResourceLocation.parse(attrKey)).ifPresent(attribute -> {
                var holder = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute);
                AttributeInstance instance = serverPlayer.getAttribute(holder);
                if (instance != null) {
                    if (playerDefaults.hasAttribute(holder))
                        instance.setBaseValue(playerDefaults.getBaseValue(holder));
                    else
                        instance.setBaseValue(attribute.getDefaultValue());
                }
            });
        }
        levelManager.getManagedBaseAttributes().clear();
        for (Skill skill : LevelManager.SKILLS.values()) updateSkill(serverPlayer, skill);
        serverPlayer.connection.send(new ClientboundUpdateAttributesPacket(serverPlayer.getId(),
                serverPlayer.getAttributes().getSyncableAttributes()));
        float savedHealth = levelManager.getSavedHealth();
        if (savedHealth > 0) {
            serverPlayer.setHealth(savedHealth);
            levelManager.clearSavedHealth();
        }
    }

    public static float getLevelBasedMultiplier(ServerPlayer player) {
        if (!ConfigInit.CONFIG.dropXPBasedOnLvl) return 1.0F;
        LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
        return 1.0F + ConfigInit.CONFIG.basedOnMultiplier * levelManager.getOverallLevel();
    }
}