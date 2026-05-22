package com.toni.wings.server.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.toni.wings.WingsMod;
import com.toni.wings.server.apparatus.FlightApparatus;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class WingsArgument implements ArgumentType<FlightApparatus> {
    private static final Collection<String> EXAMPLES = Arrays.asList("magical", "wings");
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_WING = new DynamicCommandExceptionType(e -> Component.translatable("wings.wingsNotFound", e));

    public WingsArgument() {
    }

    @Nonnull
    public static WingsArgument wings() {
        return new WingsArgument();
    }

    @Nonnull
    public static FlightApparatus getWings(@Nonnull CommandContext<CommandSourceStack> ctx, @Nonnull String value) throws CommandSyntaxException {
        return Objects.requireNonNull(ctx.getArgument(value, FlightApparatus.class));
    }

    @Override
    @Nonnull
    public FlightApparatus parse(StringReader reader) throws CommandSyntaxException {
        ResourceLocation key = ResourceLocation.read(Objects.requireNonNull(reader));
        return Objects.requireNonNull(WingsMod.WINGS.getOptional(key).orElseThrow(() -> ERROR_UNKNOWN_WING.create(key)));
    }

    @Override
    @Nonnull
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
        return Objects.requireNonNull(SharedSuggestionProvider.suggestResource(Objects.requireNonNull(WingsMod.WINGS.keySet()), Objects.requireNonNull(builder)));
    }

    @Override
    @Nonnull
    public Collection<String> getExamples() {
        return Objects.requireNonNull(EXAMPLES);
    }
}
