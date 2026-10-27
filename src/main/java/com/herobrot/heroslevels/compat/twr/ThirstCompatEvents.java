package com.herobrot.heroslevels.compat.twr;

import com.herobrot.heroslevels.HerosLevels;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

@EventBusSubscriber(modid = HerosLevels.MOD_ID)
public class ThirstCompatEvents {

        @SubscribeEvent
        public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
            if (!HerosLevels.isThirstLoaded) return;
            if (!(event.getEntity() instanceof ServerPlayer player)) return;
            ItemStack consumed = event.getItem();
            if (!consumed.isEmpty()) ThirstCompat.applyDrinkBonus(player, consumed);
        }

}
