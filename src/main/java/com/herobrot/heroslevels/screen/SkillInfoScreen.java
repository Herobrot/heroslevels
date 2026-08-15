package com.herobrot.heroslevels.screen;

import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.level.Skill;
import com.herobrot.heroslevels.level.SkillBonus;
import com.herobrot.heroslevels.level.restriction.PlayerRestriction;
import com.herobrot.heroslevels.screen.widget.LineWidget;
import com.herobrot.heroslevels.util.CompatUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SkillInfoScreen extends AbstractSkillPanelScreen {

    private final Skill skill;
    private final LevelManager levelManager;
    private static final int MAX_INFO_LINES = 50;

    public SkillInfoScreen(LevelManager levelManager, int skillId) {
        this(levelManager, requireSkill(skillId));
    }

    private SkillInfoScreen(LevelManager levelManager, Skill skill) {
        super(skill.getText());
        this.skill = skill;
        this.levelManager = levelManager;
    }

    private static Skill requireSkill(int skillId) {
        Skill skill = LevelManager.SKILLS.get(skillId);
        if (skill == null) throw new IllegalStateException("[Hero's Levels-ERROR]: Skill inexistente con id " + skillId);
        return skill;
    }

    @Override
    protected void init() {
        super.init();
        Language language = Language.getInstance();

        // --- Skill Info ---
        for (int i = 0; i < MAX_INFO_LINES; i++) {
            String skillExtra = "skill.heroslevels." + this.skill.key() + "." + i;
            if (!language.has(skillExtra)) break;
            this.lines.add(new LineWidget(this.minecraft, Component.translatable(skillExtra), null, 0, false));
        }
        if (!this.lines.isEmpty())
            this.lines.addFirst(new LineWidget(this.minecraft, Component.translatable("skill.heroslevels.info"), null, 0, true));
        int skillInfoLines = this.lines.size();

        // --- Bonus Info ---
        for (String bonusKey : SkillBonus.BONUS_KEYS) {
            List<SkillBonus> bonuses = LevelManager.BONUSES.getOrDefault(bonusKey, List.of());
            for (SkillBonus bonus : bonuses) {
                if (bonus.id() != this.skill.id()) continue;
                for (int i = 0; i < MAX_INFO_LINES; i++) {
                    String bonusInfo = "bonus.heroslevels." + bonus.key() + "." + i;
                    if (!language.has(bonusInfo)) break;
                    String rawText = language.getOrDefault(bonusInfo);
                    if (rawText.startsWith("---")) break;
                    Component bonusInfoText = Component.translatable(bonusInfo, Component.translatable("text.heroslevels.gui.short_lower_level", bonus.level()));
                    this.lines.add(skillInfoLines, new LineWidget(this.minecraft, bonusInfoText, null, 0, false));
                }
            }
        }
        if (this.lines.size() > skillInfoLines)
            this.lines.add(skillInfoLines, new LineWidget(this.minecraft, Component.translatable("bonus.heroslevels.info"), null, 0, true));

        // --- Restrictions ---
        addRestrictionLines(LevelManager.ITEM_RESTRICTIONS, Component.translatable("restriction.heroslevels.item_usage"), 0);
        addRestrictionLines(LevelManager.BLOCK_RESTRICTIONS, Component.translatable("restriction.heroslevels.block_usage"), 1);
        addRestrictionLines(LevelManager.ENTITY_RESTRICTIONS, Component.translatable("restriction.heroslevels.entity_usage"), 2);
        addRestrictionLines(LevelManager.ENCHANTMENT_RESTRICTIONS, Component.translatable("restriction.heroslevels.enchantments"), 3);
        addRestrictionLines(LevelManager.BREWING_RESTRICTIONS, Component.translatable("restriction.heroslevels.brewing"), 4);
    }

    private void addRestrictionLines(Map<Integer, PlayerRestriction> levelRestrictions, Component restrictionText, int code) {
        Map<Integer, Map<Integer, PlayerRestriction>> map = new TreeMap<>();
        for (Map.Entry<Integer, PlayerRestriction> itemRestriction : levelRestrictions.entrySet()) {
            if (code == 0 || code == 1) {
                Block block = BuiltInRegistries.BLOCK.byId(itemRestriction.getKey());
                if (CompatUtil.hasReplacement(block)) continue;
            }
            for (Map.Entry<Integer, Integer> specificRestriction : itemRestriction.getValue().skillLevelRestrictions().entrySet())
                if (specificRestriction.getKey() == this.skill.id()) {
                    map.computeIfAbsent(specificRestriction.getValue(), k -> new TreeMap<>()).put(itemRestriction.getKey(), itemRestriction.getValue());
                    break;
                }
        }
        if (!map.isEmpty())
            this.lines.add(new LineWidget(this.minecraft, restrictionText, null, 0, true));
        for (Map.Entry<Integer, Map<Integer, PlayerRestriction>> restrictions : map.entrySet()) {
            this.lines.add(new LineWidget(this.minecraft, Component.translatable("text.heroslevels.gui.short_level", restrictions.getKey()), null, 0, true));
            if (restrictions.getValue().size() > 9)
                addPaginatedRestrictionLines(restrictions.getValue(), code, new TreeMap<>());
            else
                this.lines.add(new LineWidget(this.minecraft, null, restrictions.getValue(), code, false));
        }
    }

    @Override
    protected void renderExtraHeader(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.drawString(this.font, Component.translatable("text.heroslevels.gui.short_level", this.levelManager.getSkillLevel(this.skill.id())), this.x + 11 + this.font.width(this.title), this.y + 7, 0x3F3F3F, false);
    }
}