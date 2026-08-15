package com.herobrot.heroslevels.mixin.block;

import com.herobrot.heroslevels.entity.LevelExperienceOrbEntity;
import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.init.ConfigInit;
import com.herobrot.heroslevels.init.TagInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.util.RestrictionHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {

    @Unique
    @Nullable
    private ServerPlayer heroslevels$serverPlayerEntity = null;

    @Shadow
    @Final
    private Object2IntOpenHashMap<ResourceLocation> recipesUsed;

    @Inject(method = "awardUsedRecipesAndPopExperience(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("HEAD"))
    private void capturePlayerMixin(ServerPlayer player, CallbackInfo info) {
        this.heroslevels$serverPlayerEntity = player;
    }

    @Inject(method = "getRecipesToAwardAndPopExperience(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;)Ljava/util/List;", at = @At("HEAD"))
    private void dropCustomXpMixin(ServerLevel level, Vec3 pos, CallbackInfoReturnable<List<RecipeHolder<?>>> info) {
        if (ConfigInit.CONFIG.furnaceXPMultiplier > 0.0F)
            for (Object2IntMap.Entry<ResourceLocation> entry : this.recipesUsed.object2IntEntrySet())
                level.getRecipeManager().byKey(entry.getKey()).ifPresent(recipeHolder -> {
                    if (!recipeHolder.value().getResultItem(level.registryAccess()).is(TagInit.RESTRICTED_FURNACE_EXPERIENCE_ITEMS)) {
                        if (recipeHolder.value() instanceof AbstractCookingRecipe cookingRecipe) {
                            float xpPerItem = cookingRecipe.getExperience();
                            int count = entry.getIntValue();
                            float totalXp = count * xpPerItem;
                            int baseAmount = Mth.floor(totalXp);
                            float fractional = Mth.frac(totalXp);
                            if (fractional != 0.0F && Math.random() < (double) fractional) baseAmount++;
                            float multiplier = 1.0F;
                            if (ConfigInit.CONFIG.dropXPBasedOnLvl && this.heroslevels$serverPlayerEntity != null) {
                                LevelManager levelManager = this.heroslevels$serverPlayerEntity.getData(AttachmentInit.LEVEL_MANAGER);
                                multiplier += ConfigInit.CONFIG.basedOnMultiplier * levelManager.getOverallLevel();
                            }
                            int finalXp = (int) (baseAmount * ConfigInit.CONFIG.furnaceXPMultiplier * multiplier);
                            if (finalXp > 0) LevelExperienceOrbEntity.spawnCustomOrb(level, pos, finalXp);
                        }
                    }
                });
    }

    @WrapOperation(
            method = "serverTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;canBurn(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/crafting/RecipeHolder;Lnet/minecraft/core/NonNullList;ILnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;)Z")
    )
    private static boolean heroslevels$wrapCanBurn(RegistryAccess registryAccess, RecipeHolder<?> recipe, NonNullList<ItemStack> items, int maxStackSize, AbstractFurnaceBlockEntity blockEntity, Operation<Boolean> original, Level level, BlockPos pos, BlockState state) {
        boolean canBurn = original.call(registryAccess, recipe, items, maxStackSize, blockEntity);
        if (!canBurn) return false;
        return !RestrictionHelper.isSmeltingRestrictedForViewers(level, pos, recipe);
    }
}