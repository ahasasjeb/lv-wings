package cc.lvjia.wings.server.command;

import cc.lvjia.wings.server.apparatus.FlightApparatus;
import cc.lvjia.wings.server.item.BatBloodBottleItem;
import cc.lvjia.wings.server.item.WingsBottleItem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class WingsCommand {
    private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.wings.give.failed"));

    private static final SimpleCommandExceptionType ERROR_TAKE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.wings.take.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("wings").requires(cs -> cs.hasPermission(2))
                .then(literal("give")
                        .then(argument("wings", WingsArgument.wings())
                                .executes(WingsCommand::giveWingSelf))
                        .then(argument("targets", EntityArgument.players())
                                .then(argument("wings", WingsArgument.wings())
                                        .executes(WingsCommand::giveWing))))
                .then(literal("take")
                        .executes(WingsCommand::takeWingsSelf)
                        .then(argument("wings", WingsArgument.wings())
                                .executes(WingsCommand::takeSpecificWingsSelf))
                        .then(argument("targets", EntityArgument.players())
                                .executes(WingsCommand::takeWings)
                                .then(argument("wings", WingsArgument.wings()).executes(WingsCommand::takeSpecificWings)))));
    }

    private static Collection<ServerPlayer> getSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return List.of(ctx.getSource().getPlayerOrException());
    }

    private static int giveWingSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return executeGiveWing(ctx, getSelf(ctx), WingsArgument.getWings(ctx, "wings"));
    }

    private static int giveWing(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return executeGiveWing(ctx, EntityArgument.getPlayers(ctx, "targets"), WingsArgument.getWings(ctx, "wings"));
    }

    private static int executeGiveWing(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets, FlightApparatus wings) throws CommandSyntaxException {
        int count = 0;
        for (ServerPlayer player : targets) {
            if (WingsBottleItem.giveWing(player, wings)) {
                count++;
            }
        }
        if (count == 0) {
            throw ERROR_GIVE_FAILED.create();
        }
        if (targets.size() == 1) {
            ctx.getSource().sendSuccess(() -> Component.translatable("commands.wings.give.success.single", targets.iterator().next().getDisplayName()), true);
        } else {
            ctx.getSource().sendSuccess(() -> Component.translatable("commands.wings.give.success.multiple", targets.size()), true);
        }
        return count;
    }

    private static int takeWingsSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return executeTakeWings(ctx, getSelf(ctx));
    }

    private static int takeWings(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return executeTakeWings(ctx, EntityArgument.getPlayers(ctx, "targets"));
    }

    private static int executeTakeWings(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets) throws CommandSyntaxException {
        int count = 0;
        for (ServerPlayer player : targets) {
            if (BatBloodBottleItem.removeWings(player)) {
                count++;
            }
        }
        if (count == 0) {
            throw ERROR_TAKE_FAILED.create();
        }
        if (targets.size() == 1) {
            ctx.getSource().sendSuccess(() -> Component.translatable("commands.wings.take.success.single", targets.iterator().next().getDisplayName()), true);
        } else {
            ctx.getSource().sendSuccess(() -> Component.translatable("commands.wings.take.success.multiple", targets.size()), true);
        }
        return count;
    }

    private static int takeSpecificWingsSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return executeTakeSpecificWings(ctx, getSelf(ctx), WingsArgument.getWings(ctx, "wings"));
    }

    private static int takeSpecificWings(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return executeTakeSpecificWings(ctx, EntityArgument.getPlayers(ctx, "targets"), WingsArgument.getWings(ctx, "wings"));
    }

    private static int executeTakeSpecificWings(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets, FlightApparatus wings) throws CommandSyntaxException {
        int count = 0;
        for (ServerPlayer player : targets) {
            if (BatBloodBottleItem.removeWings(player, wings)) {
                count++;
            }
        }
        if (count == 0) {
            throw ERROR_TAKE_FAILED.create();
        }
        if (targets.size() == 1) {
            ctx.getSource().sendSuccess(() -> Component.translatable("commands.wings.take.success.single", targets.iterator().next().getDisplayName()), true);
        } else {
            ctx.getSource().sendSuccess(() -> Component.translatable("commands.wings.take.success.multiple", targets.size()), true);
        }
        return count;
    }
}
