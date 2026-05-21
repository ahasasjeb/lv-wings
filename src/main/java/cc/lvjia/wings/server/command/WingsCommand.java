package cc.lvjia.wings.server.command;

import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.server.apparatus.FlightApparatus;
import cc.lvjia.wings.server.item.BatBloodBottleItem;
import cc.lvjia.wings.server.item.WingsBottleItem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;

import java.util.Collection;
import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class WingsCommand {
    private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType(
            Component.translatable("commands.wings.give.failed"));

    private static final SimpleCommandExceptionType ERROR_TAKE_FAILED = new SimpleCommandExceptionType(
            Component.translatable("commands.wings.take.failed"));

    private static final PermissionCheck PERMISSION_CHECK = new PermissionCheck.Require(
            Permissions.COMMANDS_GAMEMASTER);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(literal("wings").requires(Commands.hasPermission(PERMISSION_CHECK))
                .then(literal("give")
                        .then(argument("wings", ResourceArgument.resource(buildContext, WingsMod.WINGS_KEY))
                                .executes(WingsCommand::giveWingSelf))
                        .then(argument("targets", EntityArgument.players())
                                .then(argument("wings", ResourceArgument.resource(buildContext, WingsMod.WINGS_KEY))
                                        .executes(WingsCommand::giveWing))))
                .then(literal("take")
                        .executes(WingsCommand::takeWingsSelf)
                        .then(argument("wings", ResourceArgument.resource(buildContext, WingsMod.WINGS_KEY))
                                .executes(WingsCommand::takeSpecificWingsSelf))
                        .then(argument("targets", EntityArgument.players())
                                .executes(WingsCommand::takeWings)
                                .then(argument("wings", ResourceArgument.resource(buildContext, WingsMod.WINGS_KEY))
                                        .executes(WingsCommand::takeSpecificWings)))));
    }

    private static FlightApparatus getWings(CommandContext<CommandSourceStack> ctx, String name)
            throws CommandSyntaxException {
        return ResourceArgument.getResource(ctx, name, WingsMod.WINGS_KEY).value();
    }

    private static Collection<ServerPlayer> getSelf(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {
        return List.of(ctx.getSource().getPlayerOrException());
    }

    private static int giveWingSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return executeGiveWing(ctx, getSelf(ctx), getWings(ctx, "wings"));
    }

    private static int giveWing(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return executeGiveWing(ctx, EntityArgument.getPlayers(ctx, "targets"), getWings(ctx, "wings"));
    }

    private static int executeGiveWing(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets,
            FlightApparatus wings) throws CommandSyntaxException {
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
            ctx.getSource().sendSuccess(() -> Component.translatable("commands.wings.give.success.single",
                    targets.iterator().next().getDisplayName()), true);
        } else {
            int successCount = count;
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("commands.wings.give.success.multiple", successCount), true);
        }
        return count;
    }

    private static int takeWingsSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return executeTakeWings(ctx, getSelf(ctx));
    }

    private static int takeWings(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return executeTakeWings(ctx, EntityArgument.getPlayers(ctx, "targets"));
    }

    private static int executeTakeWings(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets)
            throws CommandSyntaxException {
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
            ctx.getSource().sendSuccess(() -> Component.translatable("commands.wings.take.success.single",
                    targets.iterator().next().getDisplayName()), true);
        } else {
            int successCount = count;
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("commands.wings.take.success.multiple", successCount), true);
        }
        return count;
    }

    private static int takeSpecificWingsSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return executeTakeSpecificWings(ctx, getSelf(ctx), getWings(ctx, "wings"));
    }

    private static int takeSpecificWings(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return executeTakeSpecificWings(ctx, EntityArgument.getPlayers(ctx, "targets"), getWings(ctx, "wings"));
    }

    private static int executeTakeSpecificWings(CommandContext<CommandSourceStack> ctx,
            Collection<ServerPlayer> targets, FlightApparatus wings) throws CommandSyntaxException {
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
            ctx.getSource().sendSuccess(() -> Component.translatable("commands.wings.take.success.single",
                    targets.iterator().next().getDisplayName()), true);
        } else {
            int successCount = count;
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("commands.wings.take.success.multiple", successCount), true);
        }
        return count;
    }
}
