package com.github.imagineforgee.chocobits.commands;

import com.github.imagineforgee.chocobits.commands.impl.EditNbtCommand;
import com.github.imagineforgee.chocobits.commands.impl.NarratorCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber
public class CommandManager {
    private static final List<ModCommand> COMMANDS = List.of(
            new NarratorCommand(),
            new EditNbtCommand()
    );

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        registerCommandRoot(dispatcher, "chocobits");
        registerCommandRoot(dispatcher, "cb");
    }

    private static void registerCommandRoot(CommandDispatcher<CommandSourceStack> dispatcher, String rootName) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(rootName);
        for (ModCommand command : COMMANDS) {
            root.then(command.build());
        }
        dispatcher.register(root);
    }
}
