package com.herobrot.heroslevels.command;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.level.Skill;
import com.herobrot.heroslevels.util.LevelHelper;
import com.herobrot.heroslevels.util.PacketHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LevelCommand {
    private enum Operation {
        ADD,
        REMOVE,
        SET,
        GET
    }

    private static final SuggestionProvider<CommandSourceStack> SKILLS_SUGGESTION_PROVIDER = (context, builder) -> {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("experience");
        suggestions.add("points");
        suggestions.add("level");
        suggestions.add("all");
        for (Skill skill : LevelManager.SKILLS.values())
            suggestions.add(skill.key());
        return SharedSuggestionProvider.suggest(suggestions, builder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("heroslevels")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("level")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.literal("add")
                                        .then(Commands.argument("skillKey", StringArgumentType.word()).suggests(SKILLS_SUGGESTION_PROVIDER)
                                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                        .executes(context -> executeSkillCommand(
                                                                context.getSource(),
                                                                EntityArgument.getPlayers(context, "targets"),
                                                                StringArgumentType.getString(context, "skillKey"),
                                                                IntegerArgumentType.getInteger(context, "amount"),
                                                                Operation.ADD
                                                        )))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("skillKey", StringArgumentType.word()).suggests(SKILLS_SUGGESTION_PROVIDER)
                                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                        .executes(context -> executeSkillCommand(
                                                                context.getSource(),
                                                                EntityArgument.getPlayers(context, "targets"),
                                                                StringArgumentType.getString(context, "skillKey"),
                                                                IntegerArgumentType.getInteger(context, "amount"),
                                                                Operation.REMOVE
                                                        )))))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("skillKey", StringArgumentType.word()).suggests(SKILLS_SUGGESTION_PROVIDER)
                                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                        .executes(context -> executeSkillCommand(
                                                                context.getSource(),
                                                                EntityArgument.getPlayers(context, "targets"),
                                                                StringArgumentType.getString(context, "skillKey"),
                                                                IntegerArgumentType.getInteger(context, "amount"),
                                                                Operation.SET
                                                        )))))
                                .then(Commands.literal("get")
                                        .then(Commands.argument("skillKey", StringArgumentType.word()).suggests(SKILLS_SUGGESTION_PROVIDER)
                                                .executes(context -> executeSkillCommand(
                                                        context.getSource(),
                                                        EntityArgument.getPlayers(context, "targets"),
                                                        StringArgumentType.getString(context, "skillKey"),
                                                        0,
                                                        Operation.GET
                                                )))))));
    }

    private static int executeSkillCommand(CommandSourceStack source, Collection<ServerPlayer> targets, String skillKey, int amount, Operation op) {
        skillKey = skillKey.toLowerCase();
        boolean isGlobalKey = skillKey.equals("experience") || skillKey.equals("points")
                || skillKey.equals("level") || skillKey.equals("all");
        Skill targetSkill = null;

        if (!isGlobalKey) {
            final String finalKey = skillKey;
            targetSkill = LevelManager.SKILLS.values().stream()
                    .filter(skill -> skill.key().equals(finalKey))
                    .findFirst()
                    .orElse(null);

            if (targetSkill == null) {
                source.sendFailure(Component.translatable("commands.heroslevels.skill_not_found", skillKey));
                return 0;
            }
        }

        for (ServerPlayer serverPlayer : targets) {
            LevelManager levelManager = serverPlayer.getData(AttachmentInit.LEVEL_MANAGER);
            String playerName = serverPlayer.getScoreboardName();

            if (!isGlobalKey) {
                if (op == Operation.GET) {
                    final int lvl = levelManager.getSkillLevel(targetSkill.id());
                    final String sKey = StringUtils.capitalize(targetSkill.key());
                    source.sendSuccess(() -> Component.translatable("commands.heroslevels.print_stat", playerName, sKey, lvl), false);
                    continue;
                }
                applySkillChange(serverPlayer, levelManager, targetSkill, amount, op);
            } else {
                switch (skillKey) {
                    case "experience" -> {
                        if (op == Operation.ADD) levelManager.addExperience(amount);
                        else if (op == Operation.REMOVE) forceSetExperience(levelManager, Math.max(0, levelManager.getTotalLevelExperience() - amount));
                        else if (op == Operation.SET) forceSetExperience(levelManager, Math.max(0, amount));
                        else if (op == Operation.GET) {
                            source.sendSuccess(() -> Component.translatable("commands.heroslevels.print_xp", playerName, levelManager.getTotalLevelExperience(), levelManager.getNextLevelExperience()), false);
                            continue;
                        }
                    }
                    case "points" -> {
                        if (op == Operation.GET) {
                            source.sendSuccess(() -> Component.translatable("commands.heroslevels.print_stat", playerName, "Points", levelManager.getSkillPoints()), false);
                            continue;
                        }
                        levelManager.setSkillPoints(calculateNewValue(levelManager.getSkillPoints(), amount, op));
                    }
                    case "level" -> {
                        if (op == Operation.GET) {
                            source.sendSuccess(() -> Component.translatable("commands.heroslevels.print_stat", playerName, "Global Level", levelManager.getOverallLevel()), false);
                            continue;
                        }
                        levelManager.setOverallLevel(calculateNewValue(levelManager.getOverallLevel(), amount, op));
                        PacketHelper.refreshTabListDisplay(serverPlayer);
                    }
                    case "all" -> {
                        if (op == Operation.GET) {
                            source.sendSuccess(() -> Component.translatable("commands.heroslevels.print_all_header", playerName), false);
                            source.sendSuccess(() -> Component.translatable("commands.heroslevels.print_stat", playerName, "Global Level", levelManager.getOverallLevel()), false);
                            source.sendSuccess(() -> Component.translatable("commands.heroslevels.print_stat", playerName, "Points", levelManager.getSkillPoints()), false);
                            for (Skill skill : LevelManager.SKILLS.values()) {
                                final String sKey = StringUtils.capitalize(skill.key());
                                source.sendSuccess(() -> Component.translatable("commands.heroslevels.print_stat", playerName, sKey, levelManager.getSkillLevel(skill.id())), false);
                            }
                            continue;
                        } else
                            for (Skill skill : LevelManager.SKILLS.values())
                                applySkillChange(serverPlayer, levelManager, skill, amount, op);
                    }
                }
            }
            PacketHelper.updateLevels(serverPlayer);
            PacketHelper.syncPlayerSkills(serverPlayer);
            source.sendSuccess(() -> Component.translatable("commands.heroslevels.success", playerName), true);
        }
        return targets.size();
    }

    private static int calculateNewValue(int current, int amount, Operation op) {
        return switch (op) {
            case ADD -> current + amount;
            case REMOVE -> Math.max(0, current - amount);
            case SET -> Math.max(0, amount);
            case GET -> current;
        };
    }

    private static void forceSetExperience(LevelManager levelManager, int absoluteXP) {
        levelManager.setTotalLevelExperience(absoluteXP);
        if (levelManager.isMaxLevel()) levelManager.setLevelProgress(1.0f);
        else levelManager.setLevelProgress((float) absoluteXP / (float) levelManager.getNextLevelExperience());
    }

    private static void applySkillChange(ServerPlayer serverPlayer, LevelManager levelManager, Skill skill, int amount, Operation op) {
        int newVal = calculateNewValue(levelManager.getSkillLevel(skill.id()), amount, op);
        newVal = Mth.clamp(newVal, 0, skill.maxLevel());
        levelManager.setSkillLevel(skill.id(), newVal);
        if (!skill.attributes().isEmpty()) {
            LevelHelper.clearSkillModifiers(serverPlayer, skill.key());
            LevelHelper.updateSkill(serverPlayer, skill);
        }
    }
}