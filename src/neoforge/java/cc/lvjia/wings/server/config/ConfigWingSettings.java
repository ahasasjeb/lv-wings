package cc.lvjia.wings.server.config;

import cc.lvjia.wings.server.item.WingSettings;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ConfigWingSettings implements WingSettings {
    private static final Logger LOGGER = LogManager.getLogger("WingsConfig");

    private final Identifier key;
    private final ModConfigSpec.IntValue requiredFlightSatiation;
    private final ModConfigSpec.DoubleValue flyingExertion;
    private final ModConfigSpec.IntValue requiredLandSatiation;
    private final ModConfigSpec.DoubleValue landingExertion;
    private final int defaultFlightSatiation;
    private final double defaultFlyingExertion;
    private final int defaultLandSatiation;
    private final double defaultLandingExertion;

    ConfigWingSettings(Identifier key, ModConfigSpec.Builder builder) {
        this(key, builder, WingsConfigDefaults.WING_SETTINGS);
    }

    ConfigWingSettings(Identifier key, ModConfigSpec.Builder builder, WingsConfigDefaults.WingSettingsData defaults) {
        this.key = key;
        this.defaultFlightSatiation = defaults.requiredFlightSatiation();
        this.defaultFlyingExertion = defaults.flyingExertion();
        this.defaultLandSatiation = defaults.requiredLandSatiation();
        this.defaultLandingExertion = defaults.landingExertion();

        builder.push(key.getPath());

        this.requiredFlightSatiation = builder
                .comment("使用 " + key + " 开始飞行所需的最低饱食度")
                .defineInRange("requiredFlightSatiation", this.defaultFlightSatiation,
                        WingsConfigDefaults.WING_MIN_SATIATION, WingsConfigDefaults.WING_MAX_SATIATION);
        this.flyingExertion = builder
                .comment("使用 " + key + " 飞行时每刻消耗的饱食度")
                .defineInRange("flyingExertion", this.defaultFlyingExertion,
                        WingsConfigDefaults.WING_MIN_EXERTION, WingsConfigDefaults.WING_MAX_EXERTION);
        this.requiredLandSatiation = builder
                .comment("使用 " + key + " 安全着陆所需的最低饱食度")
                .defineInRange("requiredLandSatiation", this.defaultLandSatiation,
                        WingsConfigDefaults.WING_MIN_SATIATION, WingsConfigDefaults.WING_MAX_SATIATION);
        this.landingExertion = builder
                .comment("使用 " + key + " 着陆时消耗的饱食度")
                .defineInRange("landingExertion", this.defaultLandingExertion,
                        WingsConfigDefaults.WING_MIN_EXERTION, WingsConfigDefaults.WING_MAX_EXERTION);

        builder.pop();
    }

    @Override
    public int getRequiredFlightSatiation() {
        return this.readInt(this.requiredFlightSatiation, "requiredFlightSatiation",
                WingsConfigDefaults.WING_MIN_SATIATION, WingsConfigDefaults.WING_MAX_SATIATION,
                this.defaultFlightSatiation);
    }

    @Override
    public float getFlyingExertion() {
        return this.readFloat(this.flyingExertion, "flyingExertion",
                WingsConfigDefaults.WING_MIN_EXERTION, WingsConfigDefaults.WING_MAX_EXERTION,
                this.defaultFlyingExertion);
    }

    @Override
    public int getRequiredLandSatiation() {
        return this.readInt(this.requiredLandSatiation, "requiredLandSatiation",
                WingsConfigDefaults.WING_MIN_SATIATION, WingsConfigDefaults.WING_MAX_SATIATION,
                this.defaultLandSatiation);
    }

    @Override
    public float getLandingExertion() {
        return this.readFloat(this.landingExertion, "landingExertion",
                WingsConfigDefaults.WING_MIN_EXERTION, WingsConfigDefaults.WING_MAX_EXERTION,
                this.defaultLandingExertion);
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
