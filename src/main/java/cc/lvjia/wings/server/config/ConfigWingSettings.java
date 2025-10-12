package cc.lvjia.wings.server.config;

import cc.lvjia.wings.server.item.ImmutableWingSettings;
import cc.lvjia.wings.server.item.WingSettings;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ConfigWingSettings implements WingSettings {
    private static final Logger LOGGER = LogManager.getLogger("WingsConfig");
    private static final int MIN_SATIATION = 0;
    private static final int MAX_SATIATION = 20;
    private static final double MIN_EXERTION = 0.0D;
    private static final double MAX_EXERTION = 10.0D;

    private final ResourceLocation key;
    private final ModConfigSpec.IntValue requiredFlightSatiation;
    private final ModConfigSpec.DoubleValue flyingExertion;
    private final ModConfigSpec.IntValue requiredLandSatiation;
    private final ModConfigSpec.DoubleValue landingExertion;
    private final int defaultFlightSatiation;
    private final double defaultFlyingExertion;
    private final int defaultLandSatiation;
    private final double defaultLandingExertion;

    ConfigWingSettings(ResourceLocation key, ModConfigSpec.Builder builder) {
        this(key, builder, 5, 0.0001D, 2, 0.005D);
    }

    ConfigWingSettings(ResourceLocation key, ModConfigSpec.Builder builder, int defaultFlightSatiation, double defaultFlyingExertion, int defaultLandSatiation, double defaultLandingExertion) {
        this.key = key;
        this.defaultFlightSatiation = defaultFlightSatiation;
        this.defaultFlyingExertion = defaultFlyingExertion;
        this.defaultLandSatiation = defaultLandSatiation;
        this.defaultLandingExertion = defaultLandingExertion;

        builder.push(key.getPath());

        this.requiredFlightSatiation = builder
                .comment("使用 " + key + " 开始飞行所需的最低饱食度")
                .defineInRange("requiredFlightSatiation", defaultFlightSatiation, MIN_SATIATION, MAX_SATIATION);
        this.flyingExertion = builder
                .comment("使用 " + key + " 飞行时每刻消耗的饱食度")
                .defineInRange("flyingExertion", defaultFlyingExertion, MIN_EXERTION, MAX_EXERTION);
        this.requiredLandSatiation = builder
                .comment("使用 " + key + " 安全着陆所需的最低饱食度")
                .defineInRange("requiredLandSatiation", defaultLandSatiation, MIN_SATIATION, MAX_SATIATION);
        this.landingExertion = builder
                .comment("使用 " + key + " 着陆时消耗的饱食度")
                .defineInRange("landingExertion", defaultLandingExertion, MIN_EXERTION, MAX_EXERTION);

        builder.pop();
    }

    public ResourceLocation getKey() {
        return this.key;
    }

    @Override
    public int getRequiredFlightSatiation() {
        return this.readInt(this.requiredFlightSatiation, "requiredFlightSatiation", MIN_SATIATION, MAX_SATIATION, this.defaultFlightSatiation);
    }

    @Override
    public float getFlyingExertion() {
        return this.readFloat(this.flyingExertion, "flyingExertion", MIN_EXERTION, MAX_EXERTION, this.defaultFlyingExertion);
    }

    @Override
    public int getRequiredLandSatiation() {
        return this.readInt(this.requiredLandSatiation, "requiredLandSatiation", MIN_SATIATION, MAX_SATIATION, this.defaultLandSatiation);
    }

    @Override
    public float getLandingExertion() {
        return this.readFloat(this.landingExertion, "landingExertion", MIN_EXERTION, MAX_EXERTION, this.defaultLandingExertion);
    }

    public WingSettings toImmutable() {
        return ImmutableWingSettings.of(this.getRequiredFlightSatiation(), this.getFlyingExertion(), this.getRequiredLandSatiation(), this.getLandingExertion());
    }

    public void validate() {
        this.getRequiredFlightSatiation();
        this.getFlyingExertion();
        this.getRequiredLandSatiation();
        this.getLandingExertion();
    }

    private int readInt(ModConfigSpec.IntValue value, String propertyName, int min, int max, int fallback) {
        if (value == null) {
            LOGGER.warn("翅膀 '{}' 属性 '{}' 为空。恢复为默认值 {}。", this.key, propertyName, fallback);
            return fallback;
        }
        int current = value.get();
        if (current < min || current > max) {
            LOGGER.warn("翅膀 '{}' 属性 '{}' 超出范围: {} (预期 {}-{})。恢复为默认值 {}。", this.key, propertyName, current, min, max, fallback);
            value.set(fallback);
            return fallback;
        }
        return current;
    }

    private float readFloat(ModConfigSpec.DoubleValue value, String propertyName, double min, double max, double fallback) {
        if (value == null) {
            LOGGER.warn("翅膀 '{}' 属性 '{}' 为空。恢复为默认值 {}。", this.key, propertyName, fallback);
            return (float) fallback;
        }
        double current = value.get();
        if (Double.isNaN(current) || Double.isInfinite(current) || current < min || current > max) {
            LOGGER.warn("翅膀 '{}' 属性 '{}' 无效: {} (预期在 {} 和 {} 之间)。恢复为默认值 {}。", this.key, propertyName, current, min, max, fallback);
            value.set(fallback);
            return (float) fallback;
        }
        return (float) current;
    }
}
