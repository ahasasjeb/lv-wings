package com.toni.wings.server.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class WingsOreConfig {
    public static final ModConfigSpec SPEC;
    public static final VeinSettings FAIRY_DUST;
    public static final VeinSettings AMETHYST;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Ore generation configuration values").push("worldgen");

        FAIRY_DUST = new VeinSettings("fairy_dust", builder, 9, 10, 0, 64);
        AMETHYST = new VeinSettings("amethyst", builder, 8, 1, 0, 16);

        builder.pop();
        SPEC = builder.build();
    }

    private WingsOreConfig() {
    }

    public static void validate() {
        FAIRY_DUST.validate();
        AMETHYST.validate();
    }
}
