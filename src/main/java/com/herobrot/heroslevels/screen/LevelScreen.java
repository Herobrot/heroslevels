package com.herobrot.heroslevels.screen;

import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.init.ConfigInit;

import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.level.Skill;
import com.herobrot.heroslevels.level.SkillAttribute;
import com.herobrot.heroslevels.network.packet.AttributeSyncPacket;
import com.herobrot.heroslevels.network.packet.StatPacket;
import com.herobrot.heroslib.client.event.ITabbedScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class LevelScreen extends Screen implements ITabbedScreen {

    public static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "textures/gui/skill_background.png");
    public static final ResourceLocation ATTRIBUTE_BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "textures/gui/attribute_background.png");
    public static final ResourceLocation ICON_TEXTURE = ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "textures/gui/icons.png");

    private final int backgroundWidth = 200;
    private final int backgroundHeight = 215;
    private int x;
    private int y;

    private static final int SKILL_SCROLLBAR_X = 186;
    private static final int SKILL_SCROLLBAR_Y = 87;
    private static final int SKILL_SLIDER_WIDTH = 6;
    private static final int SKILL_SLIDER_HEIGHT = 34;
    private static final int SKILL_MAX_OFFSET = 86;
    private static final int SKILL_TRACK_HEIGHT = SKILL_MAX_OFFSET + SKILL_SLIDER_HEIGHT; // 120

    private static final int ATT_SCROLLBAR_X = 270;
    private static final int ATT_SCROLLBAR_Y = 8;
    private static final int ATT_SLIDER_WIDTH = 6;
    private static final int ATT_SLIDER_HEIGHT = 41;
    private static final int ATT_MAX_OFFSET = 158;
    private static final int ATT_TRACK_HEIGHT = ATT_MAX_OFFSET + ATT_SLIDER_HEIGHT; // 199

    private final Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI).rotateLocalY(0f);
    private boolean turnClientPlayer = false;
    private boolean scrollingSkills = false;
    private boolean scrollingAttributes = false;

    private LevelManager levelManager;

    private final List<SkillAttribute> attributes = new ArrayList<>();
    private final List<ResourceLocation> attributeSprites = new ArrayList<>();
    private final Map<Integer, ResourceLocation> skillSprites = new HashMap<>();

    private boolean showAttributes = false;
    private int attributeRow = 0;

    private final WidgetButtonPage[] levelButtons = new WidgetButtonPage[12];
    private int skillRow = 0;

    private boolean buttonsDirty = true;

    public LevelScreen() { super(Component.translatable("screen.heroslevels.skill_screen")); }

    @Override
    public Class<? extends Screen> heroslib$getParentScreenClass() { return InventoryScreen.class; }

    @Override
    protected void init() {
        super.init();
        PacketDistributor.sendToServer(new AttributeSyncPacket());
        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;
        if (this.minecraft != null && this.minecraft.player != null)
            this.levelManager = this.minecraft.player.getData(AttachmentInit.LEVEL_MANAGER);
        Map<Integer, SkillAttribute> skillAttributes = new TreeMap<>();
        for (Skill skill : LevelManager.SKILLS.values())
            for (SkillAttribute skillAttribute : skill.attributes()) {
                if (skillAttribute.displayGroupId() < 0) continue;
                skillAttributes.put(skillAttribute.displayGroupId(), skillAttribute);
            }
        this.attributes.addAll(skillAttributes.values());
        for (Skill skill : LevelManager.SKILLS.values()) this.skillSprites.put(skill.id(), ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "textures/gui/sprites/" + skill.key() + ".png"));
        for (SkillAttribute attr : this.attributes) {
            ResourceLocation loc = attr.attribute().unwrapKey().map(ResourceKey::location).orElse(null);
            if (loc != null) this.attributeSprites.add(ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "textures/gui/sprites/" + loc.getPath() + ".png"));
            else this.attributeSprites.add(ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "textures/gui/sprites/unknown.png"));
        }
        for (int i = 0; i < 12; i++) {
            if (isSkillSlotOutOfBounds(i)) break;
            final int skillId = i;
            this.levelButtons[i] = this.addRenderableWidget(new WidgetButtonPage(
                    this.x + (i % 2 == 0 ? 80 : 169), this.y + 91 + i / 2 * 20, 13, 13, 33, 42, true, true, null,
                    button -> PacketDistributor.sendToServer(new StatPacket(this.skillRow * 2 + skillId, 1))
            ));
        }
        updateLevelButtons();
    }

    private boolean isSkillSlotOutOfBounds(int skillId) {
        return skillId >= LevelManager.SKILLS.size() || skillId >= this.levelManager.getPlayerSkills().size();
    }

    private int getMaxSkillScroll() {
        int size = this.levelManager.getPlayerSkills().size();
        if (size <= 12) return 0;
        int maxRow = (size - 12) / 2;
        if ((size - 12) % 2 != 0) maxRow++;
        return maxRow;
    }

    public void markButtonsDirty() { this.buttonsDirty = true; }

    @Override
    public void tick() {
        super.tick();
        if (this.minecraft != null && this.minecraft.player != null && this.turnClientPlayer) {
            double scaledWidth = this.minecraft.getWindow().getGuiScaledWidth();
            double scaledHeight = this.minecraft.getWindow().getGuiScaledHeight();
            double realWidth = this.minecraft.getWindow().getWidth();
            double realHeight = this.minecraft.getWindow().getHeight();
            double mouseX = this.minecraft.mouseHandler.xpos() * scaledWidth / realWidth;
            double mouseY = this.minecraft.mouseHandler.ypos() * scaledHeight / realHeight;
            if (isPointWithinBounds(this.x + 9, this.y + 67, 15, 10, mouseX, mouseY)) this.quaternionf.rotateLocalY(0.087f);
            else if (isPointWithinBounds(this.x + 41, this.y + 67, 15, 10, mouseX, mouseY)) this.quaternionf.rotateLocalY(-0.087f);
            else this.turnClientPlayer = false;
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.buttonsDirty) {
            this.updateLevelButtons();
            this.buttonsDirty = false;
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (this.minecraft != null && this.minecraft.player != null) {
            Component pendingTooltip = null;
            Component title = Component.translatable("text.heroslevels.gui.title", this.minecraft.player.getName());
            guiGraphics.drawString(this.font, title, this.x + 118 - this.font.width(title) / 2, this.y + 7, 0x3F3F3F, false);

            if (!this.attributes.isEmpty()) {
                if (this.showAttributes) {
                    guiGraphics.blit(ICON_TEXTURE, this.x + 178, this.y + 5, 30, 114, 15, 13);
                    guiGraphics.blit(ATTRIBUTE_BACKGROUND_TEXTURE, this.x + 202, this.y, 0, 0, 82, 215);
                    int maxAttributes = Math.min(this.attributes.size(), 15);
                    if (this.attributes.size() > 15) {
                        float progress = (float) this.attributeRow / (this.attributes.size() - 15);
                        int sliderY = (int) (progress * ATT_MAX_OFFSET);
                        guiGraphics.blit(ATTRIBUTE_BACKGROUND_TEXTURE, this.x + ATT_SCROLLBAR_X, this.y + ATT_SCROLLBAR_Y + sliderY, 82, 0, ATT_SLIDER_WIDTH, ATT_SLIDER_HEIGHT);
                    } else guiGraphics.blit(ATTRIBUTE_BACKGROUND_TEXTURE, this.x + ATT_SCROLLBAR_X, this.y + ATT_SCROLLBAR_Y, 88, 0, ATT_SLIDER_WIDTH, ATT_SLIDER_HEIGHT);
                    guiGraphics.drawString(this.font, Component.translatable("text.heroslevels.gui.attributes"), this.x + 214, this.y + 12, 0xE0E0E0, false);
                    int k = 27;
                    for (int i = this.attributeRow; i < this.attributeRow + maxAttributes; i++) {
                        ResourceLocation spriteLoc = this.attributeSprites.get(i);
                        guiGraphics.blit(spriteLoc, this.x + 214, this.y + k, 0, 0, 9, 9, 9, 9);
                        float attributeValue = (float) Math.round(this.minecraft.player.getAttributeValue(this.attributes.get(i).attribute()) * 100.0D) / 100.0F;
                        guiGraphics.drawString(this.font, Component.literal(String.valueOf(attributeValue)), this.x + 214 + 15, this.y + k, 0xE0E0E0, false);
                        k += 12;
                    }
                } else guiGraphics.blit(ICON_TEXTURE, this.x + 178, this.y + 5, 15, 114, 15, 13);
                if (isPointWithinBounds(this.x + 178, this.y + 5, 15, 13, mouseX, mouseY)) pendingTooltip = Component.translatable("text.heroslevels.gui.attributes");
            } else guiGraphics.blit(ICON_TEXTURE, this.x + 178, this.y + 5, 0, 114, 15, 13);
            Component skillLevelText = Component.translatable("text.heroslevels.gui.level", this.levelManager.getOverallLevel());
            guiGraphics.drawString(this.font, skillLevelText, this.x + 62, this.y + 42, 0x3F3F3F, false);
            Component skillPointText = Component.translatable("text.heroslevels.gui.points", this.levelManager.getSkillPoints());
            guiGraphics.drawString(this.font, skillPointText, this.x + 62, this.y + 54, 0x3F3F3F, false);
            guiGraphics.blit(ICON_TEXTURE, this.x + 62, this.y + 21, 0, 100, 131, 5);
            int nextLevelExperience = this.levelManager.getNextLevelExperience();
            float levelProgress = this.levelManager.getLevelProgress();
            long experience = (int) (nextLevelExperience * levelProgress);
            guiGraphics.blit(ICON_TEXTURE, this.x + 62, this.y + 21, 0, 105, (int) (130.0f * levelProgress), 5);
            Component currentXpText = Component.translatable("text.heroslevels.gui.current_xp", experience, nextLevelExperience);
            guiGraphics.drawString(this.font, currentXpText, this.x - this.font.width(currentXpText) / 2 + 127, this.y + 30, 0x3F3F3F, false);
            Component craftingTooltip = renderRestrictionIconAndGetTooltip(guiGraphics, LevelManager.CRAFTING_RESTRICTIONS, "crafting", 29, 30, 15, 0, mouseX, mouseY);
            if (craftingTooltip != null) pendingTooltip = craftingTooltip;
            Component miningTooltip = renderRestrictionIconAndGetTooltip(guiGraphics, LevelManager.MINING_RESTRICTIONS, "mining", 45, 75, 60, 45, mouseX, mouseY);
            if (miningTooltip != null) pendingTooltip = miningTooltip;

            renderPlayerModel(guiGraphics);

            if (isPointWithinBounds(this.x + 9, this.y + 67, 15, 10, mouseX, mouseY)) guiGraphics.blit(ICON_TEXTURE, this.x + 9, this.y + 67, 0, 138, 15, 10);
            else guiGraphics.blit(ICON_TEXTURE, this.x + 9, this.y + 67, 0, 128, 15, 10);
            if (isPointWithinBounds(this.x + 41, this.y + 67, 15, 10, mouseX, mouseY)) guiGraphics.blit(ICON_TEXTURE, this.x + 41, this.y + 67, 15, 138, 15, 10);
            else guiGraphics.blit(ICON_TEXTURE, this.x + 41, this.y + 67, 15, 128, 15, 10);
            if (pendingTooltip != null) guiGraphics.renderTooltip(this.font, pendingTooltip, mouseX, mouseY);
        }
    }

    private Component renderRestrictionIconAndGetTooltip(GuiGraphics guiGraphics, Map<?, ?> restrictions, String type, int yOffset, int uHover, int uNormal, int uEmpty, int mouseX, int mouseY) {
        if (!restrictions.isEmpty()) {
            if (isPointWithinBounds(this.x + 178, this.y + yOffset, 14, 13, mouseX, mouseY)) {
                guiGraphics.blit(ICON_TEXTURE, this.x + 178, this.y + yOffset, uHover, 80, 15, 13);
                return Component.translatable("restriction.heroslevels." + type);
            } else guiGraphics.blit(ICON_TEXTURE, this.x + 178, this.y + yOffset, uNormal, 80, 15, 13);
        } else guiGraphics.blit(ICON_TEXTURE, this.x + 178, this.y + yOffset, uEmpty, 80, 15, 13);
        return null;
    }

    private void renderPlayerModel(GuiGraphics guiGraphics) {
        if (this.minecraft == null || this.minecraft.player == null) return;
        float realYBodyRot = this.minecraft.player.yBodyRot;
        float realYBodyRotO = this.minecraft.player.yBodyRotO;
        float realYHeadRot = this.minecraft.player.getYHeadRot();
        float realYHeadRotO = this.minecraft.player.yHeadRotO;
        this.minecraft.player.yBodyRot = 180.0F;
        this.minecraft.player.yBodyRotO = 180.0F;
        this.minecraft.player.yHeadRot = 180.0F;
        this.minecraft.player.yHeadRotO = 180.0F;
        InventoryScreen.renderEntityInInventory(
                guiGraphics,
                this.x + 33, this.y + 43, 30,
                new Vector3f(0.0F, this.minecraft.player.getBbHeight() / 2.0F, 0.0F),
                this.quaternionf, null, this.minecraft.player
        );
        this.minecraft.player.yBodyRot = realYBodyRot;
        this.minecraft.player.yBodyRotO = realYBodyRotO;
        this.minecraft.player.yHeadRot = realYHeadRot;
        this.minecraft.player.yHeadRotO = realYHeadRotO;
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(guiGraphics);
        guiGraphics.blit(BACKGROUND_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight, 256, 256);
        for (int i = 0; i < 12; i++) {
            int skillId = i + this.skillRow * 2;
            if (isSkillSlotOutOfBounds(skillId)) break;
            Skill skill = LevelManager.SKILLS.get(skillId);
            int iconX = this.x + (i % 2 == 0 ? 11 : 99);
            int iconY = this.y + 89 + i / 2 * 20;
            guiGraphics.blit(BACKGROUND_TEXTURE, this.x + (i % 2 == 0 ? 8 : 96), this.y + 87 + i / 2 * 20, 0, 215, 88, 20);
            ResourceLocation spriteLoc = this.skillSprites.get(skillId);
            guiGraphics.blit(spriteLoc, iconX, iconY, 0, 0, 16, 16, 16, 16);
            Component skillLevel = Component.translatable("text.heroslevels.gui.current_level", this.levelManager.getSkillLevel(skillId), skill.maxLevel());
            guiGraphics.drawString(this.font, skillLevel, this.x + (i % 2 == 0 ? 53 : 141) - this.font.width(skillLevel) / 2, this.y + 94 + i / 2 * 20, 0x3F3F3F, false);
            if (isPointWithinBounds(iconX, iconY, 16, 16, mouseX, mouseY)) guiGraphics.renderTooltip(this.font, skill.getText(), mouseX, mouseY);
        }
        int maxSkillScroll = getMaxSkillScroll();
        if (maxSkillScroll > 0) {
            float progress = (float) this.skillRow / maxSkillScroll;
            int sliderY = (int) (progress * SKILL_MAX_OFFSET);
            guiGraphics.blit(BACKGROUND_TEXTURE, this.x + SKILL_SCROLLBAR_X, this.y + SKILL_SCROLLBAR_Y + sliderY, 200, 0, SKILL_SLIDER_WIDTH, SKILL_SLIDER_HEIGHT);
        } else guiGraphics.blit(BACKGROUND_TEXTURE, this.x + SKILL_SCROLLBAR_X, this.y + SKILL_SCROLLBAR_Y, 206, 0, SKILL_SLIDER_WIDTH, SKILL_SLIDER_HEIGHT);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    public void updateLevelButtons() {
        boolean maxedAllSkills = false;
        if (ConfigInit.CONFIG.allowHigherSkillLevel && this.levelManager.getSkillPoints() > 0) {
            maxedAllSkills = true;
            for (Skill skillCheck : LevelManager.SKILLS.values())
                if (skillCheck.maxLevel() > this.levelManager.getSkillLevel(skillCheck.id())) {
                    maxedAllSkills = false;
                    break;
                }
        }
        for (int i = 0; i < this.levelButtons.length; i++) {
            if (this.levelButtons[i] == null) break;
            int skillId = i + this.skillRow * 2;
            if (isSkillSlotOutOfBounds(skillId)) {
                this.levelButtons[i].visible = false;
                continue;
            }
            this.levelButtons[i].visible = true;
            Skill skill = LevelManager.SKILLS.get(skillId);
            if (ConfigInit.CONFIG.overallMaxLevel > 0 && this.levelManager.getOverallLevel() >= ConfigInit.CONFIG.overallMaxLevel) this.levelButtons[i].active = false;
            else if (skill.maxLevel() <= this.levelManager.getPlayerSkills().get(skillId).getLevel()) this.levelButtons[i].active = false;
            else this.levelButtons[i].active = this.levelManager.getSkillPoints() > 0;
            if (maxedAllSkills) this.levelButtons[i].active = true;
        }
    }

    public static boolean isPointWithinBounds(int x, int y, int width, int height, double pointX, double pointY) {
        return pointX >= (double) (x - 1) && pointX < (double) (x + width + 1)
                && pointY >= (double) (y - 1) && pointY < (double) (y + height + 1);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (this.turnClientPlayer) this.turnClientPlayer = false;
            if (this.scrollingSkills) this.scrollingSkills = false;
            if (this.scrollingAttributes) this.scrollingAttributes = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (this.showAttributes && this.attributes.size() > 15 && isPointWithinBounds(this.x + ATT_SCROLLBAR_X, this.y + ATT_SCROLLBAR_Y, ATT_SLIDER_WIDTH, ATT_TRACK_HEIGHT, mouseX, mouseY)) {
                this.scrollingAttributes = true;
                updateAttributeScrollFromMouse(mouseY);
                return true;
            }
            int maxSkillScroll = getMaxSkillScroll();
            if (maxSkillScroll > 0 && isPointWithinBounds(this.x + SKILL_SCROLLBAR_X, this.y + SKILL_SCROLLBAR_Y, SKILL_SLIDER_WIDTH, SKILL_TRACK_HEIGHT, mouseX, mouseY)) {
                this.scrollingSkills = true;
                updateSkillScrollFromMouse(mouseY);
                return true;
            }
        }

        if (!this.attributes.isEmpty() && isPointWithinBounds(this.x + 178, this.y + 5, 15, 13, mouseX, mouseY)) {
            this.showAttributes = !this.showAttributes;
            if (this.minecraft != null) this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }

        if (!LevelManager.CRAFTING_RESTRICTIONS.isEmpty() && isPointWithinBounds(this.x + 178, this.y + 29, 14, 13, mouseX, mouseY)) {
            if (this.minecraft != null) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this.minecraft.setScreen(new SkillRestrictionScreen(LevelManager.CRAFTING_RESTRICTIONS, Component.translatable("restriction.heroslevels.crafting"), 0));
            }
            return true;
        }

        if (!LevelManager.MINING_RESTRICTIONS.isEmpty() && isPointWithinBounds(this.x + 178, this.y + 45, 14, 13, mouseX, mouseY)) {
            if (this.minecraft != null) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this.minecraft.setScreen(new SkillRestrictionScreen(LevelManager.MINING_RESTRICTIONS, Component.translatable("restriction.heroslevels.mining"), 1));
            }
            return true;
        }

        if (isPointWithinBounds(this.x + 9, this.y + 67, 15, 10, mouseX, mouseY) || isPointWithinBounds(this.x + 41, this.y + 67, 15, 10, mouseX, mouseY)) {
            this.turnClientPlayer = true;
            if (this.minecraft != null) this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }

        for (int i = 0; i < 12; i++) {
            int skillId = i + this.skillRow * 2;
            if (isSkillSlotOutOfBounds(skillId)) break;
            int iconX = this.x + (i % 2 == 0 ? 11 : 99);
            int iconY = this.y + 89 + i / 2 * 20;
            if (isPointWithinBounds(iconX, iconY, 16, 16, mouseX, mouseY)) {
                if (this.minecraft != null) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    this.minecraft.setScreen(new SkillInfoScreen(this.levelManager, skillId));
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0) {
            if (this.scrollingAttributes) {
                updateAttributeScrollFromMouse(mouseY);
                return true;
            } else if (this.scrollingSkills) {
                updateSkillScrollFromMouse(mouseY);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void updateAttributeScrollFromMouse(double mouseY) {
        float progress = ((float) mouseY - (this.y + ATT_SCROLLBAR_Y) - (ATT_SLIDER_HEIGHT / 2.0f)) / ATT_MAX_OFFSET;
        progress = Mth.clamp(progress, 0.0F, 1.0F);
        int maxAttributeRow = this.attributes.size() - 15;
        this.attributeRow = Math.round(progress * maxAttributeRow);
    }

    private void updateSkillScrollFromMouse(double mouseY) {
        float progress = ((float) mouseY - (this.y + SKILL_SCROLLBAR_Y) - (SKILL_SLIDER_HEIGHT / 2.0f)) / SKILL_MAX_OFFSET;
        progress = Mth.clamp(progress, 0.0F, 1.0F);
        int maxSkillScroll = getMaxSkillScroll();
        if (maxSkillScroll > 0) {
            int oldSkillRow = this.skillRow;
            this.skillRow = Math.round(progress * maxSkillScroll);
            if (oldSkillRow != this.skillRow) this.buttonsDirty = true;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.showAttributes && this.attributes.size() > 15 && isPointWithinBounds(this.x + 209, this.y + 7, 68, 201, mouseX, mouseY)) {
            int maxAttributeRow = this.attributes.size() - 15;
            this.attributeRow = Math.clamp(this.attributeRow - (int) scrollY, 0, maxAttributeRow);
        }

        int maxSkillScroll = getMaxSkillScroll();
        if (maxSkillScroll > 0 && isPointWithinBounds(this.x + 7, this.y + 86, 186, 122, mouseX, mouseY)) {
            int oldSkillRow = this.skillRow;
            this.skillRow = Math.clamp(this.skillRow - (int) scrollY, 0, maxSkillScroll);
            if (oldSkillRow != this.skillRow) this.buttonsDirty = true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private static class WidgetButtonPage extends Button {
        private final boolean hoverOutline;
        private final int textureX;
        private final int textureY;
        private final List<Component> tooltip = new ArrayList<>();

        public WidgetButtonPage(int x, int y, int width, int height, int textureX, int textureY, boolean hoverOutline, boolean clickable, @Nullable Component tooltip, Button.OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
            this.hoverOutline = hoverOutline;
            this.textureX = textureX;
            this.textureY = textureY;
            this.active = clickable;
            if (tooltip != null) this.tooltip.add(tooltip);
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            int i = this.hoverOutline ? this.getStateFrameIndex() : 0;
            guiGraphics.blit(ICON_TEXTURE, this.getX(), this.getY(), this.textureX + i * this.width, this.textureY, this.width, this.height);
            if (this.isHovered() && !this.tooltip.isEmpty()) guiGraphics.renderTooltip(Minecraft.getInstance().font, this.tooltip, Optional.empty(), mouseX, mouseY);
        }

        private int getStateFrameIndex() {
            if (!this.active) return 0;
            if (this.isHovered()) return 2;
            return 1;
        }

        @Override
        public void updateWidgetNarration(@NotNull NarrationElementOutput output) {}
    }

    @Override
    public int heroslib$getGuiLeft() { return this.x; }

    @Override
    public int heroslib$getGuiTop() { return this.y; }
}