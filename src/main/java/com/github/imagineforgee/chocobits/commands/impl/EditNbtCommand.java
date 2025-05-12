package com.github.imagineforgee.chocobits.commands.impl;

import com.github.imagineforgee.chocobits.commands.ModCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EditNbtCommand implements ModCommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("editnbt")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) ->
                                        SharedSuggestionProvider.suggest(List.of("persisted", "vanilla"), builder))
                                .then(Commands.argument("key", StringArgumentType.word())
                                        .suggests(this::suggestNBTKeys)
                                        .then(Commands.argument("value", StringArgumentType.string())
                                                .suggests((ctx, builder) ->
                                                        SharedSuggestionProvider.suggest(List.of(
                                                                "true", "false", "1", "0", "\"YourString\""
                                                        ), builder))
                                                .executes(this::editNBT)))));
    }

    private int editNBT(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        String type = StringArgumentType.getString(ctx, "type");
        String key = StringArgumentType.getString(ctx, "key");
        String valueStr = StringArgumentType.getString(ctx, "value");

        try {
            if (type.equalsIgnoreCase("persisted")) {
                CompoundTag data = target.getPersistentData();
                CompoundTag persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
                putTypedValue(persisted, key, valueStr);
                data.put(Player.PERSISTED_NBT_TAG, persisted);

            } else if (type.equalsIgnoreCase("vanilla")) {
                CompoundTag tag = new CompoundTag();
                target.saveWithoutId(tag);
                putTypedValue(tag, key, valueStr);
                target.load(tag);
            } else {
                ctx.getSource().sendFailure(Component.literal("Unknown type. Use 'persisted' or 'vanilla'."));
                return 0;
            }

            ctx.getSource().sendSuccess(Component.literal("Set " + key + " for " + target.getName().getString()), true);
            return 1;

        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed to set key: " + e.getMessage()));
            return 0;
        }
    }

    private void putTypedValue(CompoundTag tag, String key, String valueStr) {
        if (valueStr.equalsIgnoreCase("true") || valueStr.equalsIgnoreCase("false")) {
            tag.putBoolean(key, Boolean.parseBoolean(valueStr));
        } else if (valueStr.matches("-?\\d+")) {
            tag.putInt(key, Integer.parseInt(valueStr));
        } else if (valueStr.matches("-?\\d+\\.\\d+")) {
            tag.putDouble(key, Double.parseDouble(valueStr));
        } else {
            tag.putString(key, valueStr.replace("\"", ""));
        }
    }

    private CompletableFuture<Suggestions> suggestNBTKeys(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder sb) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        String type = StringArgumentType.getString(ctx, "type");

        CompoundTag tag = type.equalsIgnoreCase("persisted")
                ? target.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG)
                : type.equalsIgnoreCase("vanilla")
                ? target.saveWithoutId(new CompoundTag())
                : null;

        if (tag == null) {
            return sb.buildFuture();
        }

        return SharedSuggestionProvider.suggest(tag.getAllKeys(), sb);
    }

    private CompoundTag getNBTDataForPlayer(ServerPlayer target, String type) {
        CompoundTag tag = new CompoundTag();
        if (type.equalsIgnoreCase("persisted")) {
            tag = target.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        } else if (type.equalsIgnoreCase("vanilla")) {
            target.saveWithoutId(tag);
        }
        return tag;
    }
}

