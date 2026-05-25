package cc.lvjia.wings.server.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class NeoForgeWingsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("wings").requires(Commands.hasPermission(WingsCommandActions.PERMISSION_CHECK))
                .then(literal("give")
                        .then(argument("wings", WingsArgument.wings())
                                .executes(ctx -> WingsCommandActions.giveWingSelf(ctx, WingsArgument::getWings)))
                        .then(argument("targets", EntityArgument.players())
                                .then(argument("wings", WingsArgument.wings())
                                        .executes(ctx -> WingsCommandActions.giveWing(ctx, WingsArgument::getWings)))))
                .then(literal("take")
                        .executes(WingsCommandActions::takeWingsSelf)
                        .then(argument("wings", WingsArgument.wings())
                                .executes(ctx -> WingsCommandActions.takeSpecificWingsSelf(ctx, WingsArgument::getWings)))
                        .then(argument("targets", EntityArgument.players())
                                .executes(WingsCommandActions::takeWings)
                                .then(argument("wings", WingsArgument.wings())
                                        .executes(ctx -> WingsCommandActions.takeSpecificWings(ctx, WingsArgument::getWings))))));
    }
}
