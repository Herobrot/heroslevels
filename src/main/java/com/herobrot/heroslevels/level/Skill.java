package com.herobrot.heroslevels.level;

import net.minecraft.network.chat.Component;

import java.util.List;

public record Skill(int id, String key, int maxLevel, List<SkillAttribute> attributes) {
    public Component getText() {
        return Component.translatable("skill.heroslevels." + key);
    }
}