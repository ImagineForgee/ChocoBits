package com.github.imagineforgee.chocobits.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

public interface ModCommand {
    LiteralArgumentBuilder<CommandSourceStack> build();
}