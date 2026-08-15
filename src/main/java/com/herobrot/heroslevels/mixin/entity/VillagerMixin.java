package com.herobrot.heroslevels.mixin.entity;

import com.herobrot.heroslevels.util.BonusHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager {

    @SuppressWarnings("DataFlowIssue")
    public VillagerMixin() {
        super(null, null);
    }

    @Inject(method = "updateSpecialPrices", at = @At("TAIL"))
    private void updateSpecialPricesMixin(Player player, CallbackInfo info) {
        if (!player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE))
            for (MerchantOffer tradeOffer : this.getOffers()) {
                int originalPrice = tradeOffer.getBaseCostA().getCount();
                int discount = (int) (originalPrice - originalPrice * BonusHelper.priceDiscountBonus(player));
                tradeOffer.addToSpecialPriceDiff(-discount);
            }
    }
}