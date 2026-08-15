package com.herobrot.heroslevels.mixin.client;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.level.LevelManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingScreen.class)
public abstract class SmithingScreenMixin extends ItemCombinerScreen<SmithingMenu> {

    public SmithingScreenMixin(SmithingMenu menu, Inventory playerInventory, Component title, ResourceLocation menuResource) {
        super(menu, playerInventory, title, menuResource);
    }

    @Shadow protected abstract boolean hasRecipeError();

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/SmithingScreen;renderOnboardingTooltips(Lnet/minecraft/client/gui/GuiGraphics;II)V"), cancellable = true)
    private void beforeTooltipsMixin(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (Minecraft.getInstance().player == null) return;
        LevelManager levelManager = Minecraft.getInstance().player.getData(AttachmentInit.LEVEL_MANAGER);
        ItemStack result = this.menu.getSlot(3).getItem();
        if (this.hasRecipeError() || (!result.isEmpty() && (!levelManager.hasRequiredCraftingLevel(result.getItem()) || !levelManager.hasRequiredItemAndEnchantmentLevel(result))))
            if (this.isHovering(65, 46, 28, 21, mouseX, mouseY)) {
                guiGraphics.renderTooltip(
                        this.font,
                        Component.translatable("restriction.heroslevels.locked.tooltip").withStyle(ChatFormatting.RED),
                        mouseX, mouseY
                );
                ci.cancel();
            }
    }
}