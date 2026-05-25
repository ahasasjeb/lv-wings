package cc.lvjia.wings.server.command;

import cc.lvjia.wings.server.apparatus.FlightApparatus;
import cc.lvjia.wings.server.item.BatBloodBottleItem;
import cc.lvjia.wings.server.item.WingsBottleItem;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;

import java.util.Collection;
import java.util.List;

public final class WingsCommandActions {
    public static final PermissionCheck PERMISSION_CHECK = new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER);
    private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType(
            Component.translatable("commands.wings.give.failed"));
    private static final SimpleCommandExceptionType ERROR_TAKE_FAILED = new SimpleCommandExceptionType(
            Component.translatable("commands.wings.take.failed"));

    private WingsCommandActions() {
    }

    public static int giveWingSelf(CommandContext<CommandSourceStack> ctx, WingsGetter getter)
            throws CommandSyntaxException {
        return executeGiveWing(ctx, getSelf(ctx), getter.get(ctx, "wings"));
    }

    public static int giveWing(CommandContext<CommandSourceStack> ctx, WingsGetter getter)
            throws CommandSyntaxException {
        return executeGiveWing(ctx, EntityArgument.getPlayers(ctx, "targets"), getter.get(ctx, "wings"));
    }

    public static int takeWingsSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return executeTakeWings(ctx, getSelf(ctx));
    }

    public static int takeWings(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return executeTakeWings(ctx, EntityArgument.getPlayers(ctx, "targets"));
    }

    public static int takeSpecificWingsSelf(CommandContext<CommandSourceStack> ctx, WingsGetter getter)
            throws CommandSyntaxException {
        return executeTakeSpecificWings(ctx, getSelf(ctx), getter.get(ctx, "wings"));
    }

    public static int takeSpecificWings(CommandContext<CommandSourceStack> ctx, WingsGetter getter)
            throws CommandSyntaxException {
        return executeTakeSpecificWings(ctx, EntityArgument.getPlayers(ctx, "targets"), getter.get(ctx, "wings"));
    }

    private static Collection<ServerPlayer> getSelf(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {
        return List.of(ctx.getSource().getPlayerOrException());
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

    @FunctionalInterface
    public interface WingsGetter {
        FlightApparatus get(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException;
    }
}
