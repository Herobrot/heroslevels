package com.herobrot.heroslevels.compat.accessories;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.level.LevelManager;
import io.wispforest.accessories.api.events.CanEquipCallback;
import io.wispforest.accessories.api.slot.SlotReference;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class AccessoriesCompatEvents implements CanEquipCallback {

    @Override
    public TriState canEquip(ItemStack stack, SlotReference reference) {
        if (stack.isEmpty()) return TriState.DEFAULT;
        if (reference.entity() instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) return TriState.DEFAULT;
            LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
            if (!levelManager.hasRequiredItemAndEnchantmentLevel(stack))
                return TriState.FALSE;
        }
        return TriState.DEFAULT;
    }
}