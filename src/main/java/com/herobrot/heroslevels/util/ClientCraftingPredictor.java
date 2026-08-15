package com.herobrot.heroslevels.util;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.level.LevelManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class ClientCraftingPredictor {

    private static CraftingInput cachedCraftingInput = null;
    private static boolean cachedCraftingRestricted = false;

    private static ItemStack cachedFurnaceInput = ItemStack.EMPTY;
    private static ItemStack cachedFurnaceOutput = ItemStack.EMPTY;
    private static boolean cachedFurnaceRestricted = false;

    private static String cachedBrewingSignature = "";
    private static boolean cachedBrewingRestricted = false;

    public static boolean isCraftingRestricted(Player player, AbstractContainerMenu menu, Level level) {
        if (player.isCreative()) return false;
        CraftingInput input = null;
        if (menu instanceof CraftingMenu craftingMenu)
            input = ((CraftingContainer) craftingMenu.getSlot(1).container).asCraftInput();
        else if (menu instanceof InventoryMenu inventoryMenu)
            input = inventoryMenu.getCraftSlots().asCraftInput();
        if (input == null) return false;
        if (input.equals(cachedCraftingInput)) return cachedCraftingRestricted;
        cachedCraftingInput = input;
        cachedCraftingRestricted = getCraftingMapped(player, level, input);
        return cachedCraftingRestricted;
    }

    private static boolean getCraftingMapped(Player player, Level level, CraftingInput input) {
        Optional<RecipeHolder<CraftingRecipe>> optional = level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, level);
        if (optional.isEmpty()) return false;
        ItemStack result = optional.get().value().assemble(input, level.registryAccess());
        if (result.isEmpty()) return false;
        LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
        return !levelManager.hasRequiredCraftingLevel(result.getItem());
    }

    public static boolean isFurnaceResultRestricted(Player player, AbstractFurnaceMenu menu, Level level) {
        if (player.isCreative()) return false;
        LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
        ItemStack output = menu.getSlot(2).getItem();
        if (!output.isEmpty())
            return !levelManager.hasRequiredCraftingLevel(output.getItem());
        ItemStack input = menu.getSlot(0).getItem();
        if (ItemStack.matches(input, cachedFurnaceInput) && ItemStack.matches(output, cachedFurnaceOutput))
            return cachedFurnaceRestricted;
        cachedFurnaceInput = input.copy();
        cachedFurnaceOutput = output.copy();
        if (input.isEmpty()) {
            cachedFurnaceRestricted = false;
            return false;
        }
        RecipeType<? extends AbstractCookingRecipe> type = RecipeType.SMELTING;
        if (menu instanceof BlastFurnaceMenu) type = RecipeType.BLASTING;
        else if (menu instanceof SmokerMenu) type = RecipeType.SMOKING;
        SingleRecipeInput recipeInput = new SingleRecipeInput(input);
        Optional<? extends RecipeHolder<? extends AbstractCookingRecipe>> optional =
                level.getRecipeManager().getRecipeFor(type, recipeInput, level);
        if (optional.isPresent()) {
            ItemStack result = optional.get().value().getResultItem(level.registryAccess());
            cachedFurnaceRestricted = !levelManager.hasRequiredCraftingLevel(result.getItem());
            return cachedFurnaceRestricted;
        }
        cachedFurnaceRestricted = false;
        return false;
    }

    public static boolean isBrewingResultRestricted(Player player, BrewingStandMenu menu, Level level) {
        if (player.isCreative()) return false;
        LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
        ItemStack ingredient = menu.getSlot(3).getItem();
        if (ingredient.isEmpty()) return false;
        ItemStack in0 = menu.getSlot(0).getItem();
        ItemStack in1 = menu.getSlot(1).getItem();
        ItemStack in2 = menu.getSlot(2).getItem();
        String currentSignature = getBrewingSignature(in0, in1, in2, ingredient);
        if (currentSignature.equals(cachedBrewingSignature))
            return cachedBrewingRestricted;

        cachedBrewingSignature = currentSignature;
        PotionBrewing brewing = level.potionBrewing();
        cachedBrewingRestricted = false;
        for (int i = 0; i < 3; i++) {
            ItemStack input = menu.getSlot(i).getItem();
            if (!input.isEmpty() && brewing.hasMix(input, ingredient)) {
                ItemStack result = brewing.mix(ingredient, input);
                if (!result.isEmpty() && (!levelManager.hasRequiredCraftingLevel(result.getItem()) || !levelManager.hasRequiredBrewingLevel(result))) {
                    cachedBrewingRestricted = true;
                    break;
                }
            }
        }
        return cachedBrewingRestricted;
    }

    private static String getBrewingSignature(ItemStack in0, ItemStack in1, ItemStack in2, ItemStack ingredient) {
        return in0.toString() + "|" + in1.toString() + "|" + in2.toString() + "|" + ingredient.toString();
    }

    public static boolean isAnvilResultRestricted(Player player, AnvilMenu menu) {
        if (player.isCreative()) return false;
        LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
        ItemStack output = menu.getSlot(2).getItem();
        if (!output.isEmpty())
            return !levelManager.hasRequiredCraftingLevel(output.getItem())
                    || !levelManager.hasRequiredItemAndEnchantmentLevel(output);
        ItemStack input1 = menu.getSlot(0).getItem();
        ItemStack input2 = menu.getSlot(1).getItem();
        if (!input1.isEmpty() && !levelManager.hasRequiredItemAndEnchantmentLevel(input1))
            return true;
        return !input2.isEmpty() && !levelManager.hasRequiredItemAndEnchantmentLevel(input2);
    }
}