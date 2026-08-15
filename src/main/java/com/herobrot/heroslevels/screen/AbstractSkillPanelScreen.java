package com.herobrot.heroslevels.screen;

import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.level.restriction.PlayerRestriction;
import com.herobrot.heroslevels.screen.widget.LineWidget;
import com.herobrot.heroslib.client.event.ITabbedScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class AbstractSkillPanelScreen extends Screen implements ITabbedScreen {

    protected static final ResourceLocation BACKGROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "textures/gui/skill_info_background.png");

    protected final int backgroundWidth = 200;
    protected final int backgroundHeight = 215;
    protected int x;
    protected int y;

    protected final List<LineWidget> lines = new ArrayList<>();
    protected int lineIndex = 0;

    protected AbstractSkillPanelScreen(Component title) {
        super(title);
    }

    @Override
    public Class<? extends Screen> getParentScreenClass() {
        return InventoryScreen.class;
    }

    @Override
    protected void init() {
        super.init();
        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, this.title, this.x + 7, this.y + 7, 0x3F3F3F, false);
        renderExtraHeader(guiGraphics, mouseX, mouseY, partialTick);
        int currentY = this.y + 24;
        for (int i = this.lineIndex; i < this.lines.size(); i++) {
            LineWidget widget = this.lines.get(i);
            widget.render(guiGraphics, this.x + 12, currentY, mouseX, mouseY);
            currentY += widget.getHeight();
            if (currentY > this.y + 194) break;
        }
    }

    protected void renderExtraHeader(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(guiGraphics);
        guiGraphics.blit(BACKGROUND_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight, 256, 256);
        if (this.lines.size() > 10) {
            int scrollLevels = this.lines.size() - 10;
            int sliderY = this.lineIndex * 156 / scrollLevels;
            guiGraphics.blit(BACKGROUND_TEXTURE, this.x + 186, this.y + 20 + sliderY, 200, 0, 6, 31);
        } else
            guiGraphics.blit(BACKGROUND_TEXTURE, this.x + 186, this.y + 20, 206, 0, 6, 31);
        renderExtraBackground(guiGraphics, mouseX, mouseY, partialTick);
    }

    protected void renderExtraBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.lines.size() > 10 && LevelScreen.isPointWithinBounds(this.x + 7, this.y + 19, 186, 189, mouseX, mouseY)) {
            int maxRow = this.lines.size() - 10;
            this.lineIndex = Math.clamp(this.lineIndex - (int) scrollY, 0, maxRow);
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public int getGuiLeft() {
        return this.x;
    }

    @Override
    public int getGuiTop() {
        return this.y;
    }

    protected void addPaginatedRestrictionLines(Map<Integer, PlayerRestriction> restrictions, int code, Map<Integer, PlayerRestriction> reusableAccumulator) {
        int count = 0;
        for (Map.Entry<Integer, PlayerRestriction> entry : restrictions.entrySet()) {
            reusableAccumulator.put(entry.getKey(), entry.getValue());
            count++;
            if (count % 9 == 0) {
                Map<Integer, PlayerRestriction> clone = reusableAccumulator instanceof TreeMap ? new TreeMap<>(reusableAccumulator) : new LinkedHashMap<>(reusableAccumulator);
                this.lines.add(new LineWidget(this.minecraft, null, clone, code, false));
                reusableAccumulator.clear();
            }
        }
        if (!reusableAccumulator.isEmpty())
            this.lines.add(new LineWidget(this.minecraft, null, reusableAccumulator, code, false));
    }
}