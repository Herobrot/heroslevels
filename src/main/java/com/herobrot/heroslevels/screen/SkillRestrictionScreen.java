package com.herobrot.heroslevels.screen;

import com.herobrot.heroslevels.level.restriction.PlayerRestriction;
import com.herobrot.heroslevels.registry.EnchantmentRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SkillRestrictionScreen extends AbstractSkillPanelScreen {

    private Map<Integer, PlayerRestriction> restrictions;
    private final int code;

    private boolean sortAlphabetical = false;

    public SkillRestrictionScreen(Map<Integer, PlayerRestriction> restrictions, Component title, int code) {
        super(title);
        this.restrictions = restrictions;
        this.code = code;
    }

    @Override
    protected void init() {
        super.init();
        sortRestrictions();
    }

    @Override
    protected void renderExtraBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int sortU = LevelScreen.isPointWithinBounds(this.x + 179, this.y + 4, 14, 14, mouseX, mouseY) ? 14 : 0;
        int sortV = this.sortAlphabetical ? 180 : 166;
        guiGraphics.blit(LevelScreen.ICON_TEXTURE, this.x + 179, this.y + 4, sortU, sortV, 14, 14);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (LevelScreen.isPointWithinBounds(this.x + 179, this.y + 4, 14, 14, mouseX, mouseY)) {
            this.sortAlphabetical = !this.sortAlphabetical;
            sortRestrictions();
            if (this.minecraft != null) this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void sortRestrictions() {
        if (this.sortAlphabetical)
            this.restrictions = this.restrictions.entrySet().stream()
                    .sorted((entry1, entry2) -> {
                        String name1 = getRestrictionName(entry1.getKey(), this.code);
                        String name2 = getRestrictionName(entry2.getKey(), this.code);
                        return name1.compareToIgnoreCase(name2);
                    }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        else sortedRestrictionsSetStream();
        this.lines.clear();
        addPaginatedRestrictionLines(this.restrictions, this.code, new LinkedHashMap<>());
    }

    private String getRestrictionName(int id, int code) {
        return switch (code) {
            case 0 -> BuiltInRegistries.ITEM.byId(id).getDescription().getString();
            case 1 -> BuiltInRegistries.BLOCK.byId(id).getName().getString();
            case 2 -> BuiltInRegistries.ENTITY_TYPE.byId(id).getDescription().getString();
            case 3 -> {
                var ench = EnchantmentRegistry.getHerosEnchantment(id);
                if (ench != null) yield Enchantment.getFullname(ench.entry(), ench.level()).getString();
                yield String.valueOf(id);
            }
            default -> String.valueOf(id);
        };
    }

    private void sortedRestrictionsSetStream() {
        this.restrictions = this.restrictions.entrySet().stream()
                .sorted((entry1, entry2) -> {
                    int itemVar1 = entry1.getValue().skillLevelRestrictions().values().stream().findFirst().orElse(0);
                    int itemVar2 = entry2.getValue().skillLevelRestrictions().values().stream().findFirst().orElse(0);
                    return Integer.compare(itemVar1, itemVar2);
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}