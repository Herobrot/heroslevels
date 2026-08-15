package com.herobrot.heroslevels.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslib.util.JsonFileWriter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RestrictCommand {

    private static final SuggestionProvider<CommandSourceStack> SKILL_SUGGESTION_PROVIDER = (context, builder) -> {
        List<String> suggestions = new ArrayList<>();
        for (var skill : LevelManager.SKILLS.values()) suggestions.add(skill.key());
        return SharedSuggestionProvider.suggest(suggestions, builder);
    };

    private static final SuggestionProvider<CommandSourceStack> TYPE_SUGGESTION_PROVIDER = (context, builder) ->
            SharedSuggestionProvider.suggest(
                    List.of("items", "blocks", "crafting", "entities", "mining", "brewing", "enchantments"),
                    builder
            );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("heroslevels")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("restrict")
                        .then(Commands.argument("skill", StringArgumentType.word()).suggests(SKILL_SUGGESTION_PROVIDER)
                                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                        .then(Commands.argument("type", StringArgumentType.word()).suggests(TYPE_SUGGESTION_PROVIDER)
                                                .executes(RestrictCommand::execute))))));
    }

    private static int execute(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        String skill = StringArgumentType.getString(context, "skill").toLowerCase();
        int level = IntegerArgumentType.getInteger(context, "level");
        String typeInput = StringArgumentType.getString(context, "type").toLowerCase();
        MinecraftServer server = source.getServer();

        if (typeInput.equals("enchantment") || typeInput.equals("enchantments")) {
            Map<String, Integer> foundEnchantments = getHeldEnchantments(player);
            if (foundEnchantments.isEmpty()) {
                source.sendFailure(Component.literal("Sostén un ítem o libro con al menos un encantamiento."));
                return 0;
            }

            injectEnchantmentsIntoDatapack(skill, level, foundEnchantments)
                    .thenRun(() -> server.execute(() ->
                            source.sendSuccess(() -> Component.literal("§aAñadidos §f" + foundEnchantments.size()
                                    + " encantamiento(s) §aa §e" + skill + " " + level), true)
                    ))
                    .exceptionally(ex -> {
                        server.execute(() -> source.sendFailure(Component.literal("§cError al guardar la plantilla de restricción.")));
                        return null;
                    });
            return 1;
        }
        String targetId;
        String jsonArrayKey;
        switch (typeInput) {
            case "item", "items" -> { jsonArrayKey = "items"; targetId = getHandItemId(player); }
            case "crafting" -> { jsonArrayKey = "crafting"; targetId = getHandItemId(player); }
            case "block", "blocks" -> { jsonArrayKey = "blocks"; targetId = getTargetBlockId(player); }
            case "mining" -> { jsonArrayKey = "mining"; targetId = getTargetBlockId(player); }
            case "entity", "entities" -> { jsonArrayKey = "entities"; targetId = getTargetEntityId(player); }
            case "brewing" -> { jsonArrayKey = "brewing"; targetId = getHeldPotionId(player); }
            default -> {
                source.sendFailure(Component.literal("Tipo inválido. Usa: items, blocks, crafting, entities, mining, brewing, enchantments."));
                return 0;
            }
        }
        if (targetId == null) {
            source.sendFailure(Component.literal("Objetivo no válido para el tipo '" + typeInput + "'. Revisa qué sostienes o miras."));
            return 0;
        }
        final String finalTargetId = targetId;
        final String finalJsonArrayKey = jsonArrayKey;
        injectIntoDatapack(skill, level, jsonArrayKey, targetId)
                .thenRun(() -> server.execute(() ->
                        source.sendSuccess(() -> Component.literal("§aAñadido: §f" + finalTargetId + " §aa §e" + skill + " " + level + " (" + finalJsonArrayKey + ")"), true)
                ))
                .exceptionally(ex -> {
                    server.execute(() -> source.sendFailure(Component.literal("§cError al guardar la plantilla de restricción.")));
                    return null;
                });

        return 1;
    }

    private static String getHandItemId(ServerPlayer player) {
        ItemStack handItem = player.getMainHandItem();
        if (handItem.isEmpty()) return null;
        return BuiltInRegistries.ITEM.getKey(handItem.getItem()).toString();
    }

    @SuppressWarnings("resource")
    private static String getTargetBlockId(ServerPlayer player) {
        HitResult hitBlock = player.pick(20.0D, 0.0F, false);
        if (hitBlock.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitBlock;
            return BuiltInRegistries.BLOCK.getKey(player.level().getBlockState(blockHit.getBlockPos()).getBlock()).toString();
        }
        return null;
    }

    private static String getTargetEntityId(ServerPlayer player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 viewVec = player.getViewVector(1.0F);
        double distance = 20.0D;
        Vec3 endPos = eyePos.add(viewVec.scale(distance));
        AABB aabb = player.getBoundingBox().expandTowards(viewVec.scale(distance)).inflate(1.0D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player.level(), player, eyePos, endPos, aabb, e -> !e.isSpectator() && e.isPickable()
        );
        if (entityHit != null) return BuiltInRegistries.ENTITY_TYPE.getKey(entityHit.getEntity().getType()).toString();
        return null;
    }

    private static String getHeldPotionId(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!stack.has(DataComponents.POTION_CONTENTS)) return null;
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null || contents.potion().isEmpty()) return null;
        return Objects.requireNonNull(BuiltInRegistries.POTION.getKey(contents.potion().get().value())).toString();
    }

    private static Map<String, Integer> getHeldEnchantments(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        Map<String, Integer> result = new HashMap<>();
        collectEnchantments(stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY), result);
        collectEnchantments(stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY), result);
        return result;
    }

    private static void collectEnchantments(ItemEnchantments enchantments, Map<String, Integer> target) {
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet())
            entry.getKey().unwrapKey().ifPresent(key ->
                    target.put(key.location().toString(), entry.getIntValue())
            );
    }

    private static CompletableFuture<Void> injectIntoDatapack(String skill, int level, String arrayKey, String targetId) {
        return JsonFileWriter.updateJsonAsync("heroslevels/generated", "generated_restrictions.json", root -> {
            JsonObject categoryObj = getOrCreateCategory(root, skill, level);
            JsonArray typeArray;
            if (categoryObj.has(arrayKey))
                typeArray = categoryObj.getAsJsonArray(arrayKey);
            else {
                typeArray = new JsonArray();
                categoryObj.add(arrayKey, typeArray);
            }
            boolean alreadyExists = false;
            for (JsonElement el : typeArray) {
                if (el.getAsString().equals(targetId)) {
                    alreadyExists = true;
                    break;
                }
            }
            if (!alreadyExists) typeArray.add(targetId);
            return root;
        });
    }

    private static CompletableFuture<Void> injectEnchantmentsIntoDatapack(String skill, int level, Map<String, Integer> enchantmentLevels) {
        return JsonFileWriter.updateJsonAsync("heroslevels/generated", "generated_restrictions.json", root -> {
            JsonObject categoryObj = getOrCreateCategory(root, skill, level);
            JsonObject enchantmentsObj;
            if (categoryObj.has("enchantments"))
                enchantmentsObj = categoryObj.getAsJsonObject("enchantments");
            else {
                enchantmentsObj = new JsonObject();
                categoryObj.add("enchantments", enchantmentsObj);
            }
            for (Map.Entry<String, Integer> entry : enchantmentLevels.entrySet())
                enchantmentsObj.addProperty(entry.getKey(), entry.getValue());
            return root;
        });
    }

    private static JsonObject getOrCreateCategory(JsonObject root, String skill, int level) {
        String restrictionKey = "generated_" + skill + "_" + level;
        if (root.has(restrictionKey)) return root.getAsJsonObject(restrictionKey);
        JsonObject categoryObj = new JsonObject();
        categoryObj.addProperty("replace", false);
        JsonObject skillsObj = new JsonObject();
        skillsObj.addProperty(skill, level);
        categoryObj.add("skills", skillsObj);
        root.add(restrictionKey, categoryObj);
        return categoryObj;
    }
}