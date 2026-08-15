package com.herobrot.heroslevels.init;

import com.herobrot.heroslevels.HerosLevels;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class TagInit {

    public static final TagKey<Item> RESTRICTED_FURNACE_EXPERIENCE_ITEMS =
            createTag(Registries.ITEM, "restricted_furnace_experience_items");

    public static final TagKey<Block> RESTRICTED_ORE_EXPERIENCE_BLOCKS =
            createTag(Registries.BLOCK, "restricted_ore_experience_blocks");

    public static final TagKey<EntityType<?>> RESTRICTED_ENTITY_EXPERIENCE_ENTITIES =
            createTag(Registries.ENTITY_TYPE, "restricted_entity_experience_entities");

    private static <T> TagKey<T> createTag(ResourceKey<? extends Registry<T>> registry, String path) {
        return TagKey.create(registry, ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, path));
    }
}