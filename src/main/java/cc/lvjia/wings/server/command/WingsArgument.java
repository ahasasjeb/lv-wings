package cc.lvjia.wings.server.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.server.apparatus.FlightApparatus;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class WingsArgument implements ArgumentType<FlightApparatus> {
    private static final Collection<String> EXAMPLES = Arrays.asList("magical", "wings");
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_WING = new DynamicCommandExceptionType(e -> Component.translatable("wings.wingsNotFound", e));

    public WingsArgument() {
    }

    public static WingsArgument wings() {
        return new WingsArgument();
    }

    public static FlightApparatus getWings(CommandContext<CommandSourceStack> ctx, String value) {
        return ctx.getArgument(value, FlightApparatus.class);
    }

    @Override
    public FlightApparatus parse(StringReader reader) throws CommandSyntaxException {
        ResourceLocation key = ResourceLocation.read(reader);
        return WingsMod.WINGS.getOptional(key).orElseThrow(() -> ERROR_UNKNOWN_WING.create(key));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(WingsMod.WINGS.keySet(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
