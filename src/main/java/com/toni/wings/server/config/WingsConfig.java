package com.toni.wings.server.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WingsConfig {
    private static final Logger LOGGER = LogManager.getLogger("WingsConfig");
    private static final List<String> DEFAULT_WEAR_OBSTRUCTIONS = List.of("minecraft:elytra");
    private static final double DEFAULT_WING_HEIGHT_OFFSET = 0.0D;
    private static final double MIN_WING_HEIGHT_OFFSET = -1.0D;
    private static final double MAX_WING_HEIGHT_OFFSET = 1.0D;

    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> WEAR_OBSTRUCTIONS;
    private static final ForgeConfigSpec.DoubleValue WING_HEIGHT_OFFSET;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("General configuration for lv wings").push("general");

        WEAR_OBSTRUCTIONS = builder
            .comment("List of item IDs that prevent players from equipping wings.")
            .defineList("wearObstructions", DEFAULT_WEAR_OBSTRUCTIONS, value -> value instanceof String && ResourceLocation.isValidResourceLocation((String) value));

        WING_HEIGHT_OFFSET = builder
            .comment("Vertical wing render offset in blocks. Positive values move wings upward; only Y is changed.")
            .defineInRange("wingHeightOffset", DEFAULT_WING_HEIGHT_OFFSET,
                MIN_WING_HEIGHT_OFFSET, MAX_WING_HEIGHT_OFFSET);

        builder.pop();
        SPEC = builder.build();
    }

    private WingsConfig() {
    }

    public static List<String> getWearObstructions() {
        List<? extends String> entries = WEAR_OBSTRUCTIONS.get();
        List<String> sanitized = new ArrayList<>();
        for (Object entryObj : entries) {
            if (entryObj == null) {
                continue;
            }
            String entry = entryObj.toString().trim();
            if (entry.isEmpty()) {
                continue;
            }
            if (!ResourceLocation.isValidResourceLocation(entry)) {
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

    public static double getWingHeightOffset() {
        Double value = WING_HEIGHT_OFFSET.get();
        if (value == null || !Double.isFinite(value)) {
            LOGGER.warn("Wing height offset is invalid. Reverting to default {}.", DEFAULT_WING_HEIGHT_OFFSET);
            WING_HEIGHT_OFFSET.set(DEFAULT_WING_HEIGHT_OFFSET);
            return DEFAULT_WING_HEIGHT_OFFSET;
        }
        return Math.max(MIN_WING_HEIGHT_OFFSET, Math.min(MAX_WING_HEIGHT_OFFSET, value));
    }

    public static void validate() {
        getWearObstructions();
        getWingHeightOffset();
    }
}
