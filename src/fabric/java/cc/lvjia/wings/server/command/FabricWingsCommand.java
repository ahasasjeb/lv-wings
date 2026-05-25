package cc.lvjia.wings.server.command;

import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.server.apparatus.FlightApparatus;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@SuppressWarnings("null")
public class FabricWingsCommand {
    public static void register(@NonNull CommandDispatcher<CommandSourceStack> dispatcher,
            @NonNull CommandBuildContext buildContext) {
        dispatcher.register(literal("wings").requires(Commands.hasPermission(WingsCommandActions.PERMISSION_CHECK))
                .then(literal("give")
                        .then(argument("wings", ResourceArgument.resource(buildContext, WingsMod.WINGS_KEY))
                                .executes(ctx -> WingsCommandActions.giveWingSelf(ctx, FabricWingsCommand::getWings)))
                        .then(argument("targets", EntityArgument.players())
                                .then(argument("wings", ResourceArgument.resource(buildContext, WingsMod.WINGS_KEY))
                                        .executes(ctx -> WingsCommandActions.giveWing(ctx, FabricWingsCommand::getWings)))))
                .then(literal("take")
                        .executes(WingsCommandActions::takeWingsSelf)
                        .then(argument("wings", ResourceArgument.resource(buildContext, WingsMod.WINGS_KEY))
                                .executes(ctx -> WingsCommandActions.takeSpecificWingsSelf(ctx, FabricWingsCommand::getWings)))
                        .then(argument("targets", EntityArgument.players())
                                .executes(WingsCommandActions::takeWings)
                                .then(argument("wings", ResourceArgument.resource(buildContext, WingsMod.WINGS_KEY))
                                        .executes(ctx -> WingsCommandActions.takeSpecificWings(ctx, FabricWingsCommand::getWings))))));
    }

    private static @NonNull FlightApparatus getWings(@NonNull CommandContext<CommandSourceStack> ctx,
            @NonNull String name) throws CommandSyntaxException {
        return Objects.requireNonNull(ResourceArgument.getResource(ctx, name, WingsMod.WINGS_KEY).value(),
                "wings argument");
    }
}
