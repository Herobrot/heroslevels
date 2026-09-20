package com.herobrot.heroslevels.util;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.level.LevelManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RestrictionHelper {

    private static final Component LOCKED_MESSAGE = Component.
            translatable("restriction.heroslevels.locked.tooltip").withStyle(ChatFormatting.RED);

    @SuppressWarnings("resource")
    public static void sendLockedMessage(Player player) {
        if (!player.level().isClientSide())
            player.displayClientMessage(LOCKED_MESSAGE, true);
    }

    public static void sendLockedMessageClient(Player player) {
        player.displayClientMessage(LOCKED_MESSAGE, true);
    }

    public static boolean isBrewingRestrictedForViewers(Level level, BlockPos pos, Container items) {
        ItemStack ingredient = items.getItem(3);
        if (ingredient.isEmpty()) return false;
        PotionBrewing brewing = level.potionBrewing();
        ItemStack[] results = getSimulatedResults(brewing, items);
        if (isRecipeNotRestricted(results)) return false;
        List<ServerPlayer> viewers = getViewers(level, pos, BrewingStandMenu.class);
        if (viewers.isEmpty()) return false;
        for (ServerPlayer player : viewers)
            if (canPlayerBrew(player, results) || player.isCreative()) return false;
        return true;
    }

    public static boolean isSmeltingRestrictedForViewers(Level level, BlockPos pos, RecipeHolder<?> recipe) {
        if (recipe == null) return false;
        ItemStack result = recipe.value().getResultItem(level.registryAccess());
        if (result.isEmpty()) return false;
        if (!LevelManager.hasCraftingRestriction(result.getItem())) return false;
        List<ServerPlayer> viewers = getViewers(level, pos, AbstractFurnaceMenu.class);
        if (viewers.isEmpty()) return false;
        for (ServerPlayer player : viewers)
            if (player.isCreative() || player.getData(AttachmentInit.LEVEL_MANAGER).hasRequiredCraftingLevel(result.getItem()))
                return false;
        return true;
    }

    public static void handleMenuClose(ServerPlayer player, BrewingStandBlockEntity blockEntity) {
        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide) return;
        if (player.isCreative()) return;
        PotionBrewing brewing = level.potionBrewing();
        ItemStack[] results = getSimulatedResults(brewing, blockEntity);
        if (isRecipeNotRestricted(results)) return;
        if (canPlayerBrew(player, results)) return;
        List<ServerPlayer> viewers = getViewers(level, blockEntity.getBlockPos(), BrewingStandMenu.class);
        for (ServerPlayer otherPlayer : viewers) {
            if (otherPlayer.getUUID().equals(player.getUUID())) continue;
            if (canPlayerBrew(otherPlayer, results)) return;
        }
        ejectItem(blockEntity, level, 3, null);
    }

    public static void handleFurnaceMenuClose(ServerPlayer player, AbstractFurnaceBlockEntity blockEntity) {
        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide) return;
        if (player.isCreative()) return;
        ItemStack input = blockEntity.getItem(0);
        if (input.isEmpty()) return;
        RecipeType<? extends AbstractCookingRecipe> type = RecipeType.SMELTING;
        if (blockEntity.getType() == BlockEntityType.BLAST_FURNACE) type = RecipeType.BLASTING;
        else if (blockEntity.getType() == BlockEntityType.SMOKER) type = RecipeType.SMOKING;
        SingleRecipeInput recipeInput = new SingleRecipeInput(input);
        Optional<? extends RecipeHolder<? extends AbstractCookingRecipe>>
                optional = level.getRecipeManager().getRecipeFor(type, recipeInput, level);
        if (optional.isEmpty()) return;
        ItemStack result = optional.get().value().getResultItem(level.registryAccess());
        if (result.isEmpty() || !LevelManager.hasCraftingRestriction(result.getItem())) return;
        LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
        if (levelManager.hasRequiredCraftingLevel(result.getItem())) return;
        List<ServerPlayer> viewers = getViewers(level, blockEntity.getBlockPos(), AbstractFurnaceMenu.class);
        for (ServerPlayer otherPlayer : viewers) {
            if (otherPlayer.getUUID().equals(player.getUUID())) continue;
            if (otherPlayer.isCreative() || otherPlayer.getData(AttachmentInit.LEVEL_MANAGER).hasRequiredCraftingLevel(result.getItem())) return;
        }
        Direction facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        ejectItem(blockEntity, level, 0, facing);
    }


    private static void ejectItem(BlockEntity blockEntity, Level level, int slot, @Nullable Direction direction) {
        ItemStack itemToEject = ((Container) blockEntity).getItem(slot);
        if (!itemToEject.isEmpty()) {
            BlockPos pos = blockEntity.getBlockPos();
            double spawnX, spawnY, spawnZ;
            double motionX = 0, motionY, motionZ = 0;
            if (direction != null) {
                spawnX = pos.getX() + 0.5 + direction.getStepX() * 0.5;
                spawnY = pos.getY() + 0.3 + direction.getStepY() * 0.1;
                spawnZ = pos.getZ() + 0.5 + direction.getStepZ() * 0.5;
                motionX = direction.getStepX() * 0.2F;
                motionY = 0.1F;
                motionZ = direction.getStepZ() * 0.2F;
            } else {
                spawnX = pos.getX() + 0.5;
                spawnY = pos.getY() + 1.0;
                spawnZ = pos.getZ() + 0.5;
                motionY = 0.2F;
            }
            ItemEntity itemEntity = new ItemEntity(level, spawnX, spawnY, spawnZ, itemToEject);
            itemEntity.setDeltaMovement(
                    motionX + level.random.triangle(0.0, 0.11),
                    motionY + level.random.triangle(0.0, 0.11),
                    motionZ + level.random.triangle(0.0, 0.11));
            level.addFreshEntity(itemEntity);
            ((Container) blockEntity).setItem(slot, ItemStack.EMPTY);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (level instanceof ServerLevel serverLevel)
                serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        4, 0.25, 0.25, 0.25, 0.0);
        }
    }

    private static List<ServerPlayer> getViewers(Level level, BlockPos pos, Class<? extends AbstractContainerMenu> menuClass) {
        AABB searchBox = new AABB(pos).inflate(16.0);
        List<ServerPlayer> nearbyPlayers = level.getEntitiesOfClass(ServerPlayer.class, searchBox);
        List<ServerPlayer> viewers = new ArrayList<>();
        for (ServerPlayer player : nearbyPlayers)
            if (menuClass.isInstance(player.containerMenu)) {
                AbstractContainerMenu menu = player.containerMenu;
                Container menuContainer = menu.getSlot(0).container;
                if (menuContainer instanceof BlockEntity be && be.getBlockPos().equals(pos))
                    viewers.add(player);
            }
        return viewers;
    }

    private static ItemStack[] getSimulatedResults(PotionBrewing brewing, Container items) {
        ItemStack ingredient = items.getItem(3);
        ItemStack[] results = new ItemStack[3];
        if (ingredient.isEmpty()) return results;
        for (int i = 0; i < 3; i++) {
            ItemStack input = items.getItem(i);
            if (!input.isEmpty() && brewing.hasMix(input, ingredient))
                results[i] = brewing.mix(ingredient, input);
        }
        return results;
    }

    private static boolean isRecipeNotRestricted(ItemStack[] results) {
        for (ItemStack result : results)
            if (result != null && result.has(DataComponents.POTION_CONTENTS)) {
                PotionContents contents = result.get(DataComponents.POTION_CONTENTS);
                if (contents != null && contents.potion().isPresent()) {
                    int potionId = BuiltInRegistries.POTION.getId(contents.potion().get().value());
                    if (LevelManager.BREWING_RESTRICTIONS.containsKey(potionId))
                        return false;
                }
            }
        return true;
    }

    private static boolean canPlayerBrew(ServerPlayer player, ItemStack[] results) {
        LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
        for (ItemStack result : results)
            if (result != null && !levelManager.hasRequiredBrewingLevel(result)) return false;
        return true;
    }
}