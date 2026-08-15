package com.herobrot.heroslevels.mixin.client;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.init.ConfigInit;
import com.herobrot.heroslevels.level.LevelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Gui.class)
public class GuiMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyConstant(method = "renderExperienceLevel", constant = @Constant(intValue = 8453920))
    private int modifyExperienceNumberColor(int originalColor) {
        if (this.minecraft.player != null) {
            LevelManager levelManager = this.minecraft.player.getData(AttachmentInit.LEVEL_MANAGER);
            if (levelManager.hasAvailableLevel())
                return ConfigInit.CONFIG.availablePointsColor;
        }
        return originalColor;
    }
}