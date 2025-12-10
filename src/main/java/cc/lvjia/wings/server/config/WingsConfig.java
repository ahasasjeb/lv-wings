package cc.lvjia.wings.server.config;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WingsConfig {
    public static final ModConfigSpec SPEC;
    private static final Logger LOGGER = LogManager.getLogger("WingsConfig");
    private static final List<String> DEFAULT_WEAR_OBSTRUCTIONS = List.of("minecraft:elytra");
    private static final ModConfigSpec.ConfigValue<List<? extends String>> WEAR_OBSTRUCTIONS;
    private static final boolean DEFAULT_ALLOW_UNDERWATER_FLIGHT = false;
    private static final ModConfigSpec.BooleanValue ALLOW_UNDERWATER_FLIGHT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("General configuration for lv wings").push("general");

        WEAR_OBSTRUCTIONS = builder
                .comment("List of item IDs that prevent players from equipping wings.")
                .defineListAllowEmpty("wearObstructions", DEFAULT_WEAR_OBSTRUCTIONS, () -> "", value -> value instanceof String && Identifier.tryParse((String) value) != null);

        ALLOW_UNDERWATER_FLIGHT = builder
            .comment("Whether players can fly while underwater. Disabled by default.")
            .define("allowUnderwaterFlight", DEFAULT_ALLOW_UNDERWATER_FLIGHT);

        builder.pop();
        SPEC = builder.build();
    }

    private WingsConfig() {
    }

    public static List<String> getWearObstructions() {
        List<? extends String> entries = WEAR_OBSTRUCTIONS.get();
        if (entries == null) {
            LOGGER.warn("Wear obstructions list is null. Reverting to defaults {}.", DEFAULT_WEAR_OBSTRUCTIONS);
            WEAR_OBSTRUCTIONS.set(DEFAULT_WEAR_OBSTRUCTIONS);
            return Collections.unmodifiableList(new ArrayList<>(DEFAULT_WEAR_OBSTRUCTIONS));
        }
        List<String> sanitized = new ArrayList<>();
        for (Object entryObj : entries) {
            if (entryObj == null) {
                continue;
            }
            String entry = entryObj.toString().trim();
            if (entry.isEmpty()) {
                continue;
            }
            if (Identifier.tryParse(entry) == null) {
                LOGGER.warn("Ignoring invalid wear obstruction id '{}'. Expected a namespaced id such as 'minecraft:elytra'.", entry);
                continue;
            }
            sanitized.add(entry);
        }

        if (sanitized.isEmpty()) {
            LOGGER.warn("No valid wear obstruction entries found. Reverting to defaults {}.", DEFAULT_WEAR_OBSTRUCTIONS);
            WEAR_OBSTRUCTIONS.set(DEFAULT_WEAR_OBSTRUCTIONS);
            sanitized.addAll(DEFAULT_WEAR_OBSTRUCTIONS);
        }

        return Collections.unmodifiableList(sanitized);
    }

    public static String[] getWearObstructionsArray() {
        return getWearObstructions().toArray(String[]::new);
    }

    public static boolean isUnderwaterFlightAllowed() {
        Boolean allow = ALLOW_UNDERWATER_FLIGHT.get();
        if (allow == null) {
            LOGGER.warn("Underwater flight flag is null. Reverting to default {}.", DEFAULT_ALLOW_UNDERWATER_FLIGHT);
            ALLOW_UNDERWATER_FLIGHT.set(DEFAULT_ALLOW_UNDERWATER_FLIGHT);
            return DEFAULT_ALLOW_UNDERWATER_FLIGHT;
        }
        return allow;
    }

    public static void validate() {
        getWearObstructions();
        isUnderwaterFlightAllowed();
    }
}
