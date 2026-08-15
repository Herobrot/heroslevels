package com.herobrot.heroslevels.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EnchantmentRegistry {

    public record EnchantmentKey(ResourceLocation id, int level) {}

    public static final Map<Integer, HerosEnchantment> ENCHANTMENTS = new HashMap<>();
    public static final Map<EnchantmentKey, Integer> INDEX_ENCHANTMENTS = new HashMap<>();

    private static HolderLookup.Provider provider;

    public static HerosEnchantment getHerosEnchantment(int key) {
        return ENCHANTMENTS.get(key);
    }

    public static int getId(Holder<Enchantment> enchantment, int level) {
        return enchantment.unwrapKey().map(key -> getId(key.location(), level)).orElse(-1);
    }

    public static int getId(ResourceLocation identifier, int level) {
        return getOrRegisterId(identifier, level, provider);
    }

    public static int getOrRegisterId(ResourceLocation identifier, int level, HolderLookup.Provider currentProvider) {
        EnchantmentKey key = new EnchantmentKey(identifier, level);
        if (INDEX_ENCHANTMENTS.containsKey(key)) return INDEX_ENCHANTMENTS.get(key);
        if (currentProvider != null) {
            ResourceKey<Enchantment> enchantmentKey = ResourceKey.create(Registries.ENCHANTMENT, identifier);
            Optional<Holder.Reference<Enchantment>> holderOpt = currentProvider.lookup(Registries.ENCHANTMENT)
                    .flatMap(lookup -> lookup.get(enchantmentKey));
            if (holderOpt.isPresent()) {
                int newId = ENCHANTMENTS.size();
                ENCHANTMENTS.put(newId, new HerosEnchantment(holderOpt.get(), level));
                INDEX_ENCHANTMENTS.put(key, newId);
                return newId;
            }
        }
        return -1;
    }

    public static void updateEnchantments(HolderLookup.Provider provider) {
        ENCHANTMENTS.clear();
        INDEX_ENCHANTMENTS.clear();
        EnchantmentRegistry.provider = provider;
        var wrapper = provider.lookup(Registries.ENCHANTMENT);
        if (wrapper.isPresent())
            for (Holder.Reference<Enchantment> enchantment : wrapper.get().listElements().toList()) {
                ResourceLocation id = enchantment.key().location();
                int maxLevel = enchantment.value().getMaxLevel();
                for (int i = 1; i <= maxLevel; i++) {
                    int newId = ENCHANTMENTS.size();
                    ENCHANTMENTS.put(newId, new HerosEnchantment(enchantment, i));
                    INDEX_ENCHANTMENTS.put(new EnchantmentKey(id, i), newId);
                }
            }
    }
}