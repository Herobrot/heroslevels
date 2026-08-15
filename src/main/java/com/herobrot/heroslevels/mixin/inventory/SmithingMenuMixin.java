package com.herobrot.heroslevels.mixin.inventory;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.level.LevelManager;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingMenu.class)
public abstract class SmithingMenuMixin extends ItemCombinerMenu {

    public SmithingMenuMixin() {
        super(null, 0, null, null);
    }

    @Inject(method = "createResult", at = @At("TAIL"))
    private void onCreateResultMixin(CallbackInfo ci) {
        ItemStack result = this.resultSlots.getItem(0);

        if (!result.isEmpty() && this.player != null && !this.player.isCreative()) {
            LevelManager levelManager = this.player.getData(AttachmentInit.LEVEL_MANAGER);
            if (!levelManager.hasRequiredCraftingLevel(result.getItem())
                    || !levelManager.hasRequiredItemAndEnchantmentLevel(result))
                this.resultSlots.setItem(0, ItemStack.EMPTY);
        }
    }
}