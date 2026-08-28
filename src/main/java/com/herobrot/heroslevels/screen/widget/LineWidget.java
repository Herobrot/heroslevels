package com.herobrot.heroslevels.screen.widget;

import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.init.ConfigInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.level.restriction.PlayerRestriction;
import com.herobrot.heroslevels.registry.EnchantmentRegistry;
import com.herobrot.heroslevels.registry.HerosEnchantment;
import com.herobrot.heroslevels.screen.LevelScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.minecraft.core.component.DataComponents.POTION_CONTENTS;

public class LineWidget {

    private final Minecraft client;
    @Nullable private final Component text;
    @Nullable private final List<FormattedCharSequence> wrappedText;
    @Nullable private final Map<Integer, PlayerRestriction> restrictions;
    private final int code;
    private final boolean isHeader;

    private final Map<Integer, ItemStack> customStacks = new HashMap<>();
    private final Map<Integer, ResourceLocation> customImages = new HashMap<>();

    public LineWidget(Minecraft client, @Nullable Component text, @Nullable Map<Integer, PlayerRestriction> restrictions, int code, boolean isHeader) {
        this.client = client;
        this.text = text;
        this.restrictions = restrictions;
        this.code = code;
        this.isHeader = isHeader;

        if (this.text != null) {
            Component finalText = isHeader ? this.text.copy().withStyle(ChatFormatting.BOLD) : this.text;
            this.wrappedText = client.font.split(finalText, 170);
        } else this.wrappedText = null;

        if (this.code == 2 && this.restrictions != null) {
            for (Integer id : this.restrictions.keySet()) {
                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.byId(id);
                ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
                ResourceLocation imageLoc = ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "textures/gui/sprites/entity/" + entityId.getPath() + ".png");
                boolean imageExists = client.getResourceManager().getResource(imageLoc).isPresent();
                if (imageExists) this.customImages.put(id, imageLoc);
                else if (SpawnEggItem.byId(entityType) != null) this.customStacks.put(id, new ItemStack(Objects.requireNonNull(SpawnEggItem.byId(entityType))));
                else {
                    Item item = BuiltInRegistries.ITEM.get(entityId);
                    if (item != Items.AIR) this.customStacks.put(id, new ItemStack(item));
                    else this.customImages.put(id, ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "textures/gui/sprites/entity/default.png"));
                }
            }
        } else if (this.code == 3 && this.restrictions != null) {
            for (Integer id : this.restrictions.keySet()) {
                HerosEnchantment enchantment = EnchantmentRegistry.getHerosEnchantment(id);
                if (enchantment != null) this.customStacks.put(id, EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment.entry(), enchantment.level())));
            }
        } else if (this.code == 4 && this.restrictions != null) {
            for (Integer id : this.restrictions.keySet()) {
                Potion potion = BuiltInRegistries.POTION.byId(id);
                if (potion != null) {
                    ItemStack potionStack = new ItemStack(Items.POTION);
                    Holder<Potion> potionHolder = BuiltInRegistries.POTION.wrapAsHolder(potion);
                    potionStack.set(POTION_CONTENTS, new PotionContents(potionHolder));
                    this.customStacks.put(id, potionStack);
                }
            }
        }
    }

    public int getHeight() {
        if (this.wrappedText != null) return Math.max(18, this.wrappedText.size() * 9 + 2);
        else if (this.restrictions != null) return 18;
        return 18;
    }

    public List<Component> render(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        if (this.text != null && this.wrappedText != null) {
            int color = this.isHeader ? ConfigInit.CONFIG.headerBoldTextColor : ConfigInit.CONFIG.normalTextColor;
            int offsetY = 4;
            for (FormattedCharSequence line : this.wrappedText) {
                guiGraphics.drawString(this.client.font, line, x, y + offsetY, color, true);
                offsetY += 9;
            }
            return null;
        } else if (this.restrictions != null) {
            int separator = 0;
            List<Component> pendingTooltip = null; // Aquí guardaremos el tooltip
            for (Map.Entry<Integer, PlayerRestriction> entry : this.restrictions.entrySet()) {
                Component tooltipTitle = null;
                guiGraphics.blit(LevelScreen.ICON_TEXTURE, x + separator - 1, y - 1, 0, 148, 18, 18);
                switch (this.code) {
                    case 0 -> {
                        Item item = BuiltInRegistries.ITEM.byId(entry.getKey());
                        tooltipTitle = item.getDescription();
                        guiGraphics.renderItem(item.getDefaultInstance(), x + separator, y);
                    }
                    case 1 -> {
                        Block block = BuiltInRegistries.BLOCK.byId(entry.getKey());
                        tooltipTitle = block.getName();
                        guiGraphics.renderItem(block.asItem().getDefaultInstance(), x + separator, y);
                    }
                    case 2 -> {
                        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.byId(entry.getKey());
                        tooltipTitle = entityType.getDescription();
                        if (this.customStacks.containsKey(entry.getKey())) guiGraphics.renderItem(this.customStacks.get(entry.getKey()), x + separator, y);
                        else if (this.customImages.containsKey(entry.getKey())) guiGraphics.blit(this.customImages.get(entry.getKey()), x + separator, y, 0, 0, 16, 16, 16, 16);
                    }
                    case 3 -> {
                        ItemStack stack = this.customStacks.get(entry.getKey());
                        if (stack != null) {
                            ItemEnchantments enchantments = stack.get(DataComponents.STORED_ENCHANTMENTS);
                            if (enchantments != null && !enchantments.isEmpty()) {
                                var firstEnchantment = enchantments.entrySet().iterator().next();
                                tooltipTitle = Enchantment.getFullname(firstEnchantment.getKey(), firstEnchantment.getIntValue());
                            }
                            guiGraphics.renderItem(stack, x + separator, y);
                        }
                    }
                    case 4 -> {
                        ItemStack stack = this.customStacks.get(entry.getKey());
                        if (stack != null) {
                            tooltipTitle = stack.getHoverName();
                            guiGraphics.renderItem(stack, x + separator, y);
                        }
                    }
                }
                if (pendingTooltip == null && tooltipTitle != null && LevelScreen.isPointWithinBounds(x + separator, y, 16, 16, mouseX, mouseY)) {
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(tooltipTitle);
                    for (Map.Entry<Integer, Integer> restriction : entry.getValue().skillLevelRestrictions().entrySet()) {
                        Component skillName = LevelManager.SKILLS.get(restriction.getKey()).getText();
                        Component levelStr = Component.translatable("text.heroslevels.gui.short_level", restriction.getValue());
                        tooltip.add(Component.empty().append(skillName).append(" ").append(levelStr));
                    }
                    pendingTooltip = tooltip;
                }
                separator += 18;
            }
            return pendingTooltip;
        }
        return null;
    }
}