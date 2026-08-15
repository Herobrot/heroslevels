package com.herobrot.heroslevels.compat.enchantinginfuser;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.level.restriction.PlayerRestriction;
import com.herobrot.heroslevels.registry.EnchantmentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class EnchantingInfuserCompat {

    public static final WeakHashMap<Object, Holder<Enchantment>> CACHE = new WeakHashMap<>();

    public static boolean isEnchantmentRestricted(Holder<Enchantment> enchantment, int level) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.player.isCreative()) return false;
        LevelManager levelManager = client.player.getData(AttachmentInit.LEVEL_MANAGER);
        return !levelManager.hasRequiredEnchantmentLevel(enchantment, level);
    }

    public static List<Component> getRestrictionTooltipLines(Holder<Enchantment> enchantment, int level) {
        List<Component> lines = new ArrayList<>();
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return lines;
        LevelManager levelManager = client.player.getData(AttachmentInit.LEVEL_MANAGER);
        int enchId = EnchantmentRegistry.getId(enchantment, level);
        PlayerRestriction restriction = LevelManager.ENCHANTMENT_RESTRICTIONS.get(enchId);
        if (restriction != null) {
            for (Map.Entry<Integer, Integer> req : restriction.skillLevelRestrictions().entrySet()) {
                if (!client.player.isCreative() && levelManager.getSkillLevel(req.getKey()) < req.getValue()) {
                    Component skillName = LevelManager.SKILLS.get(req.getKey()).getText();
                    int currentLevel = levelManager.getSkillLevel(req.getKey());
                    int requiredLevel = req.getValue();
                    lines.add(Component.translatable("restriction.heroslevels.enchantment.power", skillName, currentLevel, requiredLevel).withStyle(ChatFormatting.RED));
                }
            }
        }
        return lines;
    }
}