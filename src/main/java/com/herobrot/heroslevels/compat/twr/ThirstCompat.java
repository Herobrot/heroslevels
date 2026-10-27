package com.herobrot.heroslevels.compat.twr;

import cn.mlus.thirst.api.ThirstHelper;
import cn.mlus.thirst.foundation.common.capability.ModAttachment;
import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.util.BonusHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ThirstCompat {

    private ThirstCompat() {}

    public static void applyDrinkBonus(Player player, ItemStack itemStack) {
        double multiplier = BonusHelper.getDrinkIncreasionMultiplier(player);
        if (multiplier <= 0 || !ThirstHelper.itemRestoresThirst(itemStack)
                || !ThirstHelper.playerRestoresThirst(itemStack, player)) return;
        int extraThirst = (int) (ThirstHelper.getThirst(itemStack) * multiplier);
        int extraQuenched = (int) (ThirstHelper.getQuenched(itemStack) * multiplier);
        player.getData(ModAttachment.PLAYER_THIRST).drink(extraThirst, extraQuenched);
    }
}
