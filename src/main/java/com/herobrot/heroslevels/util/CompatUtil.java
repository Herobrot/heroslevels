package com.herobrot.heroslevels.util;

import com.herobrot.heroslevels.HerosLevels;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;

public class CompatUtil {

    private static final ResourceLocation ENCHANTING_TABLE = ResourceLocation.parse("minecraft:enchanting_table");
    private static final ResourceLocation ANVIL = ResourceLocation.parse("minecraft:anvil");
    private static final ResourceLocation CHIPPED_ANVIL = ResourceLocation.parse("minecraft:chipped_anvil");
    private static final ResourceLocation DAMAGED_ANVIL = ResourceLocation.parse("minecraft:damaged_anvil");

    /**
     * Devuelve una lista de IDs asociados a un bloque original.
     * Si el bloque es de Vanilla y hay un mod de compat cargado, añade el ID del mod.
     */
    public static List<ResourceLocation> getAssociatedBlockIds(ResourceLocation originalId) {
        if (HerosLevels.isEasyMagicLoaded && originalId.equals(ENCHANTING_TABLE))
            return List.of(originalId, ResourceLocation.fromNamespaceAndPath("easymagic", "minecraft/enchanting_table"));
        else if (HerosLevels.isEasyAnvilsLoaded && isAnvilVariant(originalId))
            return List.of(originalId, ResourceLocation.fromNamespaceAndPath("easyanvils", originalId.getNamespace() + "/" + originalId.getPath()));
        return List.of(originalId);
    }

    /**
     * Inversa de getAssociatedBlockIds.
     * Si el bloque pertenece a EasyAnvils/EasyMagic, reconstruye y devuelve el ID de Vanilla
     * original para que podamos buscar su BlockItem para renderizado.
     */
    public static ResourceLocation getVanillaItemForBlock(ResourceLocation blockId) {
        if (blockId.getNamespace().equals("easymagic") || blockId.getNamespace().equals("easyanvils")) {
            String path = blockId.getPath();
            String[] parts = path.split("/", 2);
            if (parts.length == 2)
                return ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
        }
        return blockId;
    }

    /**
     * Comprueba si un bloque Vanilla tiene un reemplazo activo por mods de compat.
     * Usado por SkillInfoScreen para ocultar el bloque Vanilla si el de mod ya se está mostrando.
     */
    public static boolean hasReplacement(Block vanillaBlock) {
        ResourceLocation vanillaId = BuiltInRegistries.BLOCK.getKey(vanillaBlock);
        if (HerosLevels.isEasyMagicLoaded && vanillaId.equals(ENCHANTING_TABLE))
            return BuiltInRegistries.BLOCK.containsKey(ResourceLocation.fromNamespaceAndPath
                    ("easymagic", "minecraft/enchanting_table"));
        if (HerosLevels.isEasyAnvilsLoaded && isAnvilVariant(vanillaId))
            return BuiltInRegistries.BLOCK.containsKey(ResourceLocation.fromNamespaceAndPath
                    ("easyanvils", vanillaId.getNamespace() + "/" + vanillaId.getPath()));
        return false;
    }

    private static boolean isAnvilVariant(ResourceLocation id) {
        return id.equals(ANVIL) || id.equals(CHIPPED_ANVIL) || id.equals(DAMAGED_ANVIL);
    }
}