package com.toni.wings.server.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class VeinSettings {
    private static final Logger LOGGER = LogManager.getLogger("WingsConfig");
    private static final int MIN_SIZE = 8;
    private static final int MAX_SIZE = 32;
    private static final int MIN_COUNT = 0;
    private static final int MAX_COUNT = 128;
    private static final int MIN_WORLD_HEIGHT = -64;
    private static final int MAX_WORLD_HEIGHT = 320;

    private final String name;
    private final ModConfigSpec.IntValue size;
    private final ModConfigSpec.IntValue count;
    private final ModConfigSpec.IntValue minHeight;
    private final ModConfigSpec.IntValue maxHeight;
    private final int defaultSize;
    private final int defaultCount;
    private final int defaultMinHeight;
    private final int defaultMaxHeight;

    VeinSettings(String name, ModConfigSpec.Builder builder, int defaultSize, int defaultCount, int defaultMaxHeight) {
        this.name = name;
        this.defaultSize = defaultSize;
        this.defaultCount = defaultCount;
        this.defaultMinHeight = 0;
        this.defaultMaxHeight = defaultMaxHeight;

        builder.push(name);
        this.size = builder
            .comment("Vein size for ore '" + name + "'")
            .defineInRange("size", defaultSize, MIN_SIZE, MAX_SIZE);
        this.count = builder
            .comment("Number of ore veins per chunk for '" + name + "'")
            .defineInRange("count", defaultCount, MIN_COUNT, MAX_COUNT);
        this.minHeight = builder
            .comment("Minimum generation height for '" + name + "'")
            .defineInRange("minHeight", 0, MIN_WORLD_HEIGHT, MAX_WORLD_HEIGHT);
        this.maxHeight = builder
            .comment("Maximum generation height for '" + name + "'")
            .defineInRange("maxHeight", defaultMaxHeight, MIN_WORLD_HEIGHT, MAX_WORLD_HEIGHT);
        builder.pop();
    }

    public void getSize() {
        this.readInt(this.size, "size", MIN_SIZE, MAX_SIZE, this.defaultSize);
    }

    public void getCount() {
        this.readInt(this.count, "count", MIN_COUNT, MAX_COUNT, this.defaultCount);
    }

    public int getMinHeight() {
        return this.readInt(this.minHeight, "minHeight", MIN_WORLD_HEIGHT, MAX_WORLD_HEIGHT, this.defaultMinHeight);
    }

    public void getMaxHeight() {
        int min = this.getMinHeight();
        int max = this.readInt(this.maxHeight, "maxHeight", MIN_WORLD_HEIGHT, MAX_WORLD_HEIGHT, this.defaultMaxHeight);
        if (max < min) {
            LOGGER.warn("Max height {} is below min height {} for ore '{}'. Reverting to {}.", max, min, this.name, min);
            this.maxHeight.set(min);
        }
    }

    public void validate() {
        this.getSize();
        this.getCount();
        this.getMinHeight();
        this.getMaxHeight();
    }

    private int readInt(ModConfigSpec.IntValue value, String propertyName, int min, int max, int fallback) {
        int current = value.get();
        if (current < min || current > max) {
            LOGGER.warn("Ore '{}' property '{}' out of range: {} (expected {}-{}). Reverting to default {}.", this.name, propertyName, current, min, max, fallback);
            value.set(fallback);
            return fallback;
        }
        return current;
    }
}
