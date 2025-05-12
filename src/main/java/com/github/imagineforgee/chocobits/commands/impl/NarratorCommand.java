package com.github.imagineforgee.chocobits.commands.impl;

import com.github.imagineforgee.chocobits.commands.ModCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class NarratorCommand implements ModCommand {
    private static final String PERSISTED_TAG = Player.PERSISTED_NBT_TAG;
    private static final String NARRATOR_KEY = "ELUnlockNarrator";

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("narrator")
                .then(Commands.argument("state", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(List.of("on", "off"), builder))
                        .requires(source -> source.getEntity() instanceof ServerPlayer)
                        .executes(ctx -> {
                            String state = StringArgumentType.getString(ctx, "state");
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            return toggleNarrator(ctx, state, player);
                        }));
    }

    private int toggleNarrator(CommandContext<CommandSourceStack> ctx, String state, ServerPlayer player) {
        boolean enable = state.equalsIgnoreCase("on");
        CompoundTag persistentData = player.getPersistentData();
        CompoundTag forgeData = persistentData.getCompound(PERSISTED_TAG);
        forgeData.putByte(NARRATOR_KEY, enable ? (byte) 1 : (byte) 0);
        persistentData.put(PERSISTED_TAG, forgeData);

        ctx.getSource().sendSuccess(Component.literal("Narrator " + (enable ? "enabled" : "disabled") + "."), true);
        return 1;
    }
}



