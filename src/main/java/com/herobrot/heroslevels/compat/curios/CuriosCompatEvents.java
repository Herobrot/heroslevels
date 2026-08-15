package com.herobrot.heroslevels.compat.curios;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.level.LevelManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import top.theillusivec4.curios.api.event.CurioCanEquipEvent;

public class CuriosCompatEvents {

    @SubscribeEvent
    public static void onCurioEquip(CurioCanEquipEvent event) {
        ItemStack stack = event.getStack();
        if (stack.isEmpty()) return;
        if (event.getEntity() instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) return;
            LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
            if (!levelManager.hasRequiredItemAndEnchantmentLevel(stack))
                event.setEquipResult(TriState.FALSE);
        }
    }
}