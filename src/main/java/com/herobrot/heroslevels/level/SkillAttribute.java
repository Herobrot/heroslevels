package com.herobrot.heroslevels.level;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record SkillAttribute(int id, int displayGroupId, Holder<Attribute> attribute, float baseValue, float levelValue, AttributeModifier.Operation operation) {
    public static final float NO_BASE_VALUE_OVERRIDE = -10000.0f;
    public static final int NO_DISPLAY_GROUP = -1;
}