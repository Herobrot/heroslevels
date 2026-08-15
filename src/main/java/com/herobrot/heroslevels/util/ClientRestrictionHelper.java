package com.herobrot.heroslevels.util;

import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.init.ConfigInit;
import com.herobrot.heroslevels.level.LevelManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class ClientRestrictionHelper {

    private static final ResourceLocation VANILLA_SMITHING_SPRITE = ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "container/error");
    private static final ResourceLocation CUSTOM_INVENTORY_SPRITE = ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "container/inventory_error");
    private static final ResourceLocation CUSTOM_BREWING_SPRITE = ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "container/brewing_stand/error");

    private static final Component LOCKED_TOOLTIP = Component.translatable("restriction.heroslevels.locked.tooltip").withStyle(ChatFormatting.RED);

    private static final int RESTRICTED_OVERLAY_COLOR = 0x40FF0000;
    private static final float SLOT_OVERLAY_Z = 200.0F;
    private static final float HOTBAR_SLOT_OVERLAY_Z = 400.0F;

    private static int cachedOverallLevel = -1;
    private static Component cachedInventoryLevelText = null;

    public static void renderContainerRestrictions(GuiGraphics graphics, Minecraft client, AbstractContainerScreen<?> screen, int mouseX, int mouseY) {
        if (client.player == null) return;
        var menu = screen.getMenu();
        int leftPos = screen.getGuiLeft();
        int topPos = screen.getGuiTop();
        if (menu instanceof CraftingMenu || menu instanceof InventoryMenu) {
            if (ClientCraftingPredictor.isCraftingRestricted(client.player, menu, client.level)) {
                boolean isCraftingTable = menu instanceof CraftingMenu;
                renderCraftingRestriction(graphics, mouseX, mouseY, leftPos, topPos, client.font, isCraftingTable);
            }
        } else if (menu instanceof AbstractFurnaceMenu furnaceMenu) {
            if (ClientCraftingPredictor.isFurnaceResultRestricted(client.player, furnaceMenu, client.level))
                renderFurnaceRestriction(graphics, mouseX, mouseY, leftPos, topPos, client.font);
        } else if (menu instanceof BrewingStandMenu brewingMenu) {
            if (ClientCraftingPredictor.isBrewingResultRestricted(client.player, brewingMenu, client.level))
                renderBrewingRestriction(graphics, mouseX, mouseY, leftPos, topPos, client.font);
        } else if (menu instanceof AnvilMenu anvilMenu) {
            if (ClientCraftingPredictor.isAnvilResultRestricted(client.player, anvilMenu))
                renderAnvilRestriction(graphics, mouseX, mouseY, leftPos, topPos, client.font);
        }
    }

    public static void renderInventoryLevel(GuiGraphics graphics, Minecraft client, AbstractContainerScreen<?> screen) {
        if (!ConfigInit.CONFIG.inventorySkillLevel
                || !(screen instanceof EffectRenderingInventoryScreen)
                || screen instanceof CreativeModeInventoryScreen
                || client.player == null) return;

        LevelManager levelManager = client.player.getData(AttachmentInit.LEVEL_MANAGER);
        int currentLevel = levelManager.getOverallLevel();
        if (currentLevel != cachedOverallLevel) {
            cachedOverallLevel = currentLevel;
            cachedInventoryLevelText = Component.translatable("text.heroslevels.gui.short_level", currentLevel);
        }
        int color = levelManager.getSkillPoints() > 0 ? ConfigInit.CONFIG.availablePointsColor : 0xFFFFFF;
        float scale = ConfigInit.CONFIG.inventorySkillLevelScale / 100.0F;
        int textWidth = client.font.width(cachedInventoryLevelText);
        int textHeight = client.font.lineHeight;

        int leftPos = screen.getGuiLeft();
        int topPos = screen.getGuiTop();
        int centerX = leftPos + 50 + ConfigInit.CONFIG.inventorySkillLevelPosX;
        int centerY = topPos + 8 + ConfigInit.CONFIG.inventorySkillLevelPosY + (textHeight / 2);

        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 100);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(client.font, cachedInventoryLevelText, -(textWidth / 2), -(textHeight / 2),
                color, true);
        graphics.pose().popPose();
    }

    public static void renderBlockHighlight(RenderHighlightEvent.Block event) {
        if (!ConfigInit.CONFIG.highlightLocked) return;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        if (client.player.isCreative() || client.player.isSpectator()) return;
        BlockPos pos = event.getTarget().getBlockPos();
        BlockState state = client.level.getBlockState(pos);
        LevelManager levelManager = client.player.getData(AttachmentInit.LEVEL_MANAGER);
        if (!levelManager.hasRequiredMiningLevel(state.getBlock())) {
            VertexConsumer vertexConsumer = event.getMultiBufferSource().getBuffer(RenderType.lines());
            Vec3 camPos = event.getCamera().getPosition();
            LevelRenderer.renderVoxelShape(
                    event.getPoseStack(),
                    vertexConsumer,
                    state.getShape(client.level, pos),
                    pos.getX() - camPos.x,
                    pos.getY() - camPos.y,
                    pos.getZ() - camPos.z,
                    1.0F, 0.0F, 0.0F, 0.5F,
                    true
            );
            event.setCanceled(true);
        }
    }

    public static void renderCraftingRestriction(GuiGraphics graphics, int mouseX, int mouseY, int leftPos, int topPos, Font font, boolean isCraftingTable) {
        ResourceLocation selectedSprite = isCraftingTable ? VANILLA_SMITHING_SPRITE : CUSTOM_INVENTORY_SPRITE;
        int arrowX = isCraftingTable ? 87 : 135;
        int arrowY = isCraftingTable ? 32 : 29;
        int width = isCraftingTable ? 28 : 16;
        int height = isCraftingTable ? 21 : 13;
        positionOfArrow(graphics, mouseX, mouseY, leftPos, topPos, font, selectedSprite, arrowX, arrowY, width, height);
    }

    public static void renderBrewingRestriction(GuiGraphics graphics, int mouseX, int mouseY, int leftPos, int topPos, Font font) {
        positionOfArrow(graphics, mouseX, mouseY, leftPos, topPos, font, CUSTOM_BREWING_SPRITE, 97, 16, 9, 28);
    }

    public static void renderFurnaceRestriction(GuiGraphics graphics, int mouseX, int mouseY, int leftPos, int topPos, Font font) {
        positionOfArrow(graphics, mouseX, mouseY, leftPos, topPos, font, VANILLA_SMITHING_SPRITE, 77, 32, 28, 21);
    }

    public static void renderAnvilRestriction(GuiGraphics graphics, int mouseX, int mouseY, int leftPos, int topPos, Font font) {
        positionOfArrow(graphics, mouseX, mouseY, leftPos, topPos, font, VANILLA_SMITHING_SPRITE, 99, 45, 28, 21);
    }

    private static void positionOfArrow(GuiGraphics graphics, int mouseX, int mouseY, int leftPos, int topPos, Font font, ResourceLocation selectedSprite, int arrowX, int arrowY, int width, int height) {
        int absoluteX = leftPos + arrowX;
        int absoluteY = topPos + arrowY;
        graphics.blitSprite(selectedSprite, absoluteX, absoluteY, width, height);
        if (mouseX >= absoluteX && mouseX < absoluteX + width && mouseY >= absoluteY && mouseY < absoluteY + height)
            graphics.renderTooltip(font, LOCKED_TOOLTIP, mouseX, mouseY);
    }

    public static boolean isAttackRestricted(LocalPlayer player, boolean showMessage) {
        if (!ConfigInit.CONFIG.lockedHandUsage) return false;
        if (player == null || player.isCreative() || player.isSpectator()) return false;
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) return false;
        LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
        if (!levelManager.hasRequiredItemAndEnchantmentLevel(mainHand)) {
            if (showMessage)
                RestrictionHelper.sendLockedMessageClient(player);
            return true;
        }
        return false;
    }

    public static void renderRestrictedSlotOverlay(GuiGraphics graphics, Slot slot, LocalPlayer player) {
        if (slot.container instanceof ResultContainer) return;
        LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
        if (!levelManager.hasRequiredItemAndEnchantmentLevel(slot.getItem())) {
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F, SLOT_OVERLAY_Z);
            graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, RESTRICTED_OVERLAY_COLOR);
            graphics.pose().popPose();
        }
    }

    public static void renderHotbarRestrictions(GuiGraphics graphics, LocalPlayer player) {
        if (player.isCreative() || player.isSpectator()) return;
        LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
        Inventory inventory = player.getInventory();
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        int hotbarLeftX = screenWidth / 2 - 88;
        int hotbarY = screenHeight - 19;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) continue;
            if (!levelManager.hasRequiredItemAndEnchantmentLevel(stack)) {
                int slotX = hotbarLeftX + i * 20;
                graphics.pose().pushPose();
                graphics.pose().translate(0.0F, 0.0F, HOTBAR_SLOT_OVERLAY_Z);
                graphics.fill(slotX, hotbarY, slotX + 16, hotbarY + 16, RESTRICTED_OVERLAY_COLOR);
                graphics.pose().popPose();
            }
        }
    }
}