package com.herobrot.heroslevels.registry;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

public record HerosEnchantment(Holder<Enchantment> entry, int level) {}