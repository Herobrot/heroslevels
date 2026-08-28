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
import net.minecraft.util.Mth;
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

    private static final int SCROLLBAR_X = 186;
    private static final int SCROLLBAR_Y = 20;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int TRACK_HEIGHT = 187;
    private static final int SLIDER_HEIGHT = 31;
    private static final int MAX_SLIDER_OFFSET = 156;

    private boolean scrolling = false;

    protected AbstractSkillPanelScreen(Component title) {
        super(title);
    }

    @Override
    public Class<? extends Screen> heroslib$getParentScreenClass() { return InventoryScreen.class; }

    @Override
    protected void init() {
        super.init();
        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, this.title, this.x + 7, this.y + 7, 0x3F3F3F, false);
        renderExtraHeader(guiGraphics, mouseX, mouseY, partialTick);

        List<Component> pendingTooltip = null;
        int currentY = this.y + 24;
        for (int i = this.lineIndex; i < this.lines.size(); i++) {
            LineWidget widget = this.lines.get(i);
            List<Component> tooltip = widget.render(guiGraphics, this.x + 12, currentY, mouseX, mouseY);
            if (tooltip != null) pendingTooltip = tooltip;
            currentY += widget.getHeight();
            if (currentY > this.y + 194) break;
        }

        if (pendingTooltip != null)
            guiGraphics.renderTooltip(this.font, pendingTooltip, Optional.empty(), mouseX, mouseY);

    }

    protected void renderExtraHeader(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(guiGraphics);
        guiGraphics.blit(BACKGROUND_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight, 256, 256);
        if (this.lines.size() > 10) {
            int maxRow = this.lines.size() - 10;
            float progress = (float) this.lineIndex / maxRow;
            int sliderY = (int) (progress * MAX_SLIDER_OFFSET);
            guiGraphics.blit(BACKGROUND_TEXTURE, this.x + SCROLLBAR_X, this.y + SCROLLBAR_Y + sliderY, 200, 0, SCROLLBAR_WIDTH, SLIDER_HEIGHT);
        } else
            guiGraphics.blit(BACKGROUND_TEXTURE, this.x + SCROLLBAR_X, this.y + SCROLLBAR_Y, 206, 0, SCROLLBAR_WIDTH, SLIDER_HEIGHT);
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

    private boolean isInsideScrollbar(double mouseX, double mouseY) {
        return LevelScreen.isPointWithinBounds(this.x + SCROLLBAR_X, this.y + SCROLLBAR_Y, SCROLLBAR_WIDTH, TRACK_HEIGHT, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.lines.size() > 10 && isInsideScrollbar(mouseX, mouseY)) {
            this.scrolling = true;
            updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.scrolling) this.scrolling = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scrolling) {
            updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void updateScrollFromMouse(double mouseY) {
        int trackTop = this.y + SCROLLBAR_Y;
        float progress = ((float) mouseY - trackTop - (SLIDER_HEIGHT / 2.0f)) / MAX_SLIDER_OFFSET;
        progress = Mth.clamp(progress, 0.0F, 1.0F);
        int maxRow = this.lines.size() - 10;
        this.lineIndex = Math.round(progress * maxRow);
    }

    @Override
    public int heroslib$getGuiLeft() { return this.x; }

    @Override
    public int heroslib$getGuiTop() { return this.y; }

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