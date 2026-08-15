package com.herobrot.heroslevels.util;

import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.init.ConfigInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.level.restriction.PlayerRestriction;
import com.herobrot.heroslevels.registry.EnchantmentRegistry;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TooltipUtil {

    public static final ResourceLocation JADE_BLOCK_UID = ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "block_restrictions");
    public static final ResourceLocation JADE_ENTITY_UID = ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "entity_restrictions");

    private static final Component USABLE_HEADER = Component.translatable("restriction.heroslevels.usable.tooltip");
    private static final Component MINEABLE_HEADER = Component.translatable("restriction.heroslevels.mineable.tooltip");
    private static final Component CRAFTABLE_HEADER = Component.translatable("restriction.heroslevels.craftable.tooltip");
    private static final Component BREWABLE_HEADER = Component.translatable("restriction.heroslevels.brewable.tooltip");

    private static final int ICON_SIZE = 16;
    private static final int ICON_TEXT_GAP = 4;
    private static final int LINE_SPACING = 2;
    private static final int TITLE_SPACING = 6;
    private static final int FONT_HEIGHT = 9;

    private static final int PADDING_TOP = 6;
    private static final int PADDING_BOTTOM = 6;
    private static final int PADDING_LEFT = 6;
    private static final int PADDING_RIGHT = 6;

    // Colores indicativos de restricción (ARGB)
    private static final int BACKGROUND_COLOR = 0xBF191919; // Gris oscuro translúcido
    private static final int BORDER_COLOR = 0xBF7F0200;     // Rojo oscuro translúcido

    private static void appendMissingRequirementLines(List<Component> lines, LevelManager levelManager, boolean isCreative, Map<Integer, PlayerRestriction> restrictionMap, int targetId) {
        PlayerRestriction playerRestriction = restrictionMap.get(targetId);
        if (playerRestriction == null) return;
        boolean hasUnmetRestrictions = isCreative;
        if (!hasUnmetRestrictions)
            for (Map.Entry<Integer, Integer> entry : playerRestriction.skillLevelRestrictions().entrySet())
                if (levelManager.getSkillLevel(entry.getKey()) < entry.getValue()) {
                    hasUnmetRestrictions = true;
                    break;
                }
        if (!hasUnmetRestrictions) return;
        for (Map.Entry<Integer, Integer> entry : playerRestriction.skillLevelRestrictions().entrySet()) {
            if (isCreative || levelManager.getSkillLevel(entry.getKey()) < entry.getValue())
                lines.add(Component.translatable("restriction.heroslevels."
                        + LevelManager.SKILLS.get(entry.getKey()).key() + ".tooltip",
                        entry.getValue()).withStyle(ChatFormatting.RED));
        }
    }

    private static void appendRestrictionInfo(LevelManager levelManager, boolean isCreative, Map<Integer, PlayerRestriction> restrictionMap, int targetId, Component header, List<Component> lines, Set<String> addedHeaders) {
        List<Component> missingLines = new ArrayList<>();
        appendMissingRequirementLines(missingLines, levelManager, isCreative, restrictionMap, targetId);
        if (missingLines.isEmpty()) return;
        String headerKey = header.getString();
        if (addedHeaders.add(headerKey)) lines.add(header);
        lines.addAll(missingLines);
    }

    public static void renderItemTooltip(Minecraft client, ItemStack stack, List<Component> lines) {
        if (client.player == null) return;
        LevelManager levelManager = client.player.getData(AttachmentInit.LEVEL_MANAGER);
        boolean isCreative = client.player.isCreative();
        Set<String> addedHeaders = new HashSet<>();
        if (stack.getItem() instanceof BlockItem blockItem) {
            int blockId = BuiltInRegistries.BLOCK.getId(blockItem.getBlock());
            appendRestrictionInfo(levelManager, isCreative, LevelManager.BLOCK_RESTRICTIONS, blockId, USABLE_HEADER, lines, addedHeaders);
            appendRestrictionInfo(levelManager, isCreative, LevelManager.MINING_RESTRICTIONS, blockId, MINEABLE_HEADER, lines, addedHeaders);
        }

        int itemId = BuiltInRegistries.ITEM.getId(stack.getItem());
        appendRestrictionInfo(levelManager, isCreative, LevelManager.ITEM_RESTRICTIONS, itemId, USABLE_HEADER, lines, addedHeaders);
        appendRestrictionInfo(levelManager, isCreative, LevelManager.CRAFTING_RESTRICTIONS, itemId, CRAFTABLE_HEADER, lines, addedHeaders);

        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        iteratingOnEnchantments(lines, levelManager, isCreative, enchantments);
        ItemEnchantments storedEnchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        iteratingOnEnchantments(lines, levelManager, isCreative, storedEnchantments);
        if (stack.getItem() instanceof SpawnEggItem spawnEggItem) {
            int entityId = BuiltInRegistries.ENTITY_TYPE.getId(spawnEggItem.getType(stack));
            appendRestrictionInfo(levelManager, isCreative, LevelManager.ENTITY_RESTRICTIONS, entityId, USABLE_HEADER, lines, addedHeaders);
        }
        PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
        if (potionContents != null)
            potionContents.potion().ifPresent(potionHolder -> {
                int potionId = BuiltInRegistries.POTION.getId(potionHolder.value());
                appendRestrictionInfo(levelManager, isCreative, LevelManager.BREWING_RESTRICTIONS, potionId, BREWABLE_HEADER, lines, addedHeaders);
            });
    }

    private static void iteratingOnEnchantments(List<Component> lines, LevelManager levelManager, boolean isCreative, ItemEnchantments enchantments) {
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            int enchId = EnchantmentRegistry.getId(entry.getKey(), entry.getIntValue());
            if (isCreative || !levelManager.hasRequiredEnchantmentLevel(entry.getKey(), entry.getIntValue())) {
                PlayerRestriction restriction = LevelManager.ENCHANTMENT_RESTRICTIONS.get(enchId);
                if (restriction != null) {
                    String exactVanillaEnchName = Enchantment.getFullname(entry.getKey(), entry.getIntValue()).getString();
                    int insertIndex = -1;
                    for (int i = 0; i < lines.size(); i++)
                        if (lines.get(i).getString().equals(exactVanillaEnchName)) {
                            insertIndex = i + 1;
                            break;
                        }

                    List<Component> restrictionLines = new ArrayList<>();
                    for (Map.Entry<Integer, Integer> req : restriction.skillLevelRestrictions().entrySet())
                        if (isCreative || levelManager.getSkillLevel(req.getKey()) < req.getValue())
                            restrictionLines.add(Component.literal("  ")
                                    .append(Component.translatable("restriction.heroslevels."
                                            + LevelManager.SKILLS.get(req.getKey()).key() + ".tooltip",
                                            req.getValue())).withStyle(ChatFormatting.RED));
                    if (insertIndex != -1) lines.addAll(insertIndex, restrictionLines);
                    else lines.addAll(restrictionLines);
                }
            }
        }
    }

    public static void renderBlockOverlay(Minecraft client, GuiGraphics guiGraphics, Block block) {
        if (!ConfigInit.CONFIG.showLockedBlockInfo || client.player == null) return;
        LevelManager levelManager = client.player.getData(AttachmentInit.LEVEL_MANAGER);
        boolean miningRestricted = !levelManager.hasRequiredMiningLevel(block);
        boolean blockRestricted = !levelManager.hasRequiredBlockLevel(block);
        if (!miningRestricted && !blockRestricted) return;
        List<Component> textList = new ArrayList<>();
        if (miningRestricted) {
            textList.add(Component.literal(block.getName().getString()));
            appendRestrictionLines(levelManager, levelManager.getRequiredMiningLevel(block), textList);
        }
        if (blockRestricted) {
            if (textList.isEmpty())
                textList.add(Component.literal(block.getName().getString()));
            textList.add(Component.translatable("restriction.heroslevels.block_usage"));
            appendRestrictionLines(levelManager, levelManager.getRequiredBlockLevel(block), textList);
        }
        ResourceLocation blockLoc = BuiltInRegistries.BLOCK.getKey(block);
        ResourceLocation itemLoc = CompatUtil.getVanillaItemForBlock(blockLoc);
        int x = guiGraphics.guiWidth()/2 + ConfigInit.CONFIG.lockedBlockInfoPosX;
        int y = ConfigInit.CONFIG.lockedBlockInfoPosY;
        renderTooltipBox(client, guiGraphics, textList, itemLoc, x, y);
    }

    public static void renderEntityOverlay(Minecraft client, GuiGraphics guiGraphics, EntityType<?> entityType) {
        if (!ConfigInit.CONFIG.showLockedBlockInfo || client.player == null) return;
        LevelManager levelManager = client.player.getData(AttachmentInit.LEVEL_MANAGER);
        if (levelManager.hasRequiredEntityLevel(entityType)) return;
        List<Component> textList = new ArrayList<>();
        textList.add(Component.literal(entityType.getDescription().getString()));
        appendRestrictionLines(levelManager, levelManager.getRequiredEntityLevel(entityType), textList);
        int x = guiGraphics.guiWidth() / 2 + ConfigInit.CONFIG.lockedBlockInfoPosX;
        int y = ConfigInit.CONFIG.lockedBlockInfoPosY;
        renderTooltipBox(client, guiGraphics, textList, null, x, y);
    }

    private static void appendRestrictionLines(LevelManager levelManager, Map<Integer, Integer> restrictions, List<Component> textList) {
        for (Map.Entry<Integer, Integer> entry : restrictions.entrySet()) {
            ChatFormatting formatting = levelManager.getSkillLevel(entry.getKey()) < entry.getValue() ? ChatFormatting.RED : ChatFormatting.GREEN;
            String translationKey = "restriction.heroslevels." + LevelManager.SKILLS.get(entry.getKey()).key() + ".tooltip";
            textList.add(Component.translatable(translationKey, entry.getValue()).withStyle(formatting));
        }
    }

    private static void renderTooltipBox(Minecraft client, GuiGraphics guiGraphics, List<Component> textList, @Nullable ResourceLocation identifier, int x, int y) {
        int maxTextWidth = 0;
        for (int i = 0; i < textList.size(); i++) {
            int width = client.font.width(textList.get(i));
            if (i == 0 && identifier != null) width += ICON_SIZE + ICON_TEXT_GAP;
            if (width > maxTextWidth) maxTextWidth = width;
        }
        int boxWidth = maxTextWidth + PADDING_LEFT + PADDING_RIGHT;
        int boxHeight = PADDING_TOP + PADDING_BOTTOM;
        for (int i = 0; i < textList.size(); i++) {
            boxHeight += FONT_HEIGHT;
            if (i < textList.size() - 1) boxHeight += (i == 0) ? TITLE_SPACING : LINE_SPACING;
        }
        int startX = x - (boxWidth / 2);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0, 0.0, 400.0);
        guiGraphics.fill(startX, y, startX + boxWidth, y + boxHeight, BACKGROUND_COLOR);
        guiGraphics.renderOutline(startX, y, boxWidth, boxHeight, BORDER_COLOR);
        int currentY = y + PADDING_TOP;
        for (int i = 0; i < textList.size(); i++) {
            Component text = textList.get(i);
            int xOffset = (i == 0 && identifier != null) ? ICON_SIZE + ICON_TEXT_GAP : 0;
            guiGraphics.drawString(client.font, text, startX + PADDING_LEFT + xOffset, currentY, 0xFFFFFF, false);
            currentY += FONT_HEIGHT;
            if (i == 0) currentY += TITLE_SPACING;
            else currentY += LINE_SPACING;
        }
        if (identifier != null)
            guiGraphics.renderItem(BuiltInRegistries.ITEM.get(identifier).getDefaultInstance(),
                    startX + PADDING_LEFT, y + PADDING_TOP - 3);
        guiGraphics.pose().popPose();
    }

    public static void appendBlockTooltipForJade(List<Component> lines, LevelManager levelManager, boolean isCreative, Block block) {
        int blockId = BuiltInRegistries.BLOCK.getId(block);
        appendMissingRequirementLines(lines, levelManager, isCreative, LevelManager.BLOCK_RESTRICTIONS, blockId);
        appendMissingRequirementLines(lines, levelManager, isCreative, LevelManager.MINING_RESTRICTIONS, blockId);
    }

    public static void appendEntityTooltipForJade(List<Component> lines, LevelManager levelManager, boolean isCreative, Entity entity) {
        int entityId = BuiltInRegistries.ENTITY_TYPE.getId(entity.getType());
        appendMissingRequirementLines(lines, levelManager, isCreative, LevelManager.ENTITY_RESTRICTIONS, entityId);
        if (entity instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            if (!stack.isEmpty()) {
                int itemId = BuiltInRegistries.ITEM.getId(stack.getItem());
                appendMissingRequirementLines(lines, levelManager, isCreative, LevelManager.ITEM_RESTRICTIONS, itemId);
                appendMissingRequirementLines(lines, levelManager, isCreative, LevelManager.CRAFTING_RESTRICTIONS, itemId);
            }
        }
    }
}