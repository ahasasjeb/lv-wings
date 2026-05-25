package cc.lvjia.wings.server.config;

import cc.lvjia.wings.server.item.WingSettings;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class ConfigWingSettings implements WingSettings {
    private final Identifier key;
    private final Data defaults;
    private Data current;

    ConfigWingSettings(Identifier key) {
        this(key, Data.defaults());
    }

    ConfigWingSettings(Identifier key, Data defaults) {
        this.key = key;
        this.defaults = defaults.copy().normalize();
        this.current = this.defaults.copy();
    }

    public Identifier getKey() {
        return this.key;
    }

    @Override
    public int getRequiredFlightSatiation() {
        return this.current.requiredFlightSatiation;
    }

    @Override
    public float getFlyingExertion() {
        return (float) this.current.flyingExertion;
    }

    @Override
    public int getRequiredLandSatiation() {
        return this.current.requiredLandSatiation;
    }

    @Override
    public float getLandingExertion() {
        return (float) this.current.landingExertion;
    }

    public Data defaultData() {
        return this.defaults.copy();
    }

    public void apply(@Nullable Data data) {
        this.current = (data != null ? data : this.defaults).copy().normalize();
    }

    public static final class Data {
        public int requiredFlightSatiation = WingsConfigDefaults.WING_SETTINGS.requiredFlightSatiation();
        public double flyingExertion = WingsConfigDefaults.WING_SETTINGS.flyingExertion();
        public int requiredLandSatiation = WingsConfigDefaults.WING_SETTINGS.requiredLandSatiation();
        public double landingExertion = WingsConfigDefaults.WING_SETTINGS.landingExertion();

        static Data defaults() {
            return from(WingsConfigDefaults.WING_SETTINGS);
        }

        static Data from(WingsConfigDefaults.WingSettingsData defaults) {
            Data data = new Data();
            data.requiredFlightSatiation = defaults.requiredFlightSatiation();
            data.flyingExertion = defaults.flyingExertion();
            data.requiredLandSatiation = defaults.requiredLandSatiation();
            data.landingExertion = defaults.landingExertion();
            return data;
        }

        Data copy() {
            Data copy = new Data();
            copy.requiredFlightSatiation = this.requiredFlightSatiation;
            copy.flyingExertion = this.flyingExertion;
            copy.requiredLandSatiation = this.requiredLandSatiation;
            copy.landingExertion = this.landingExertion;
            return copy;
        }

        Data normalize() {
            this.requiredFlightSatiation = WingsConfigDefaults.clamp(this.requiredFlightSatiation,
                    WingsConfigDefaults.WING_MIN_SATIATION, WingsConfigDefaults.WING_MAX_SATIATION);
            this.flyingExertion = WingsConfigDefaults.clamp(this.flyingExertion,
                    WingsConfigDefaults.WING_MIN_EXERTION, WingsConfigDefaults.WING_MAX_EXERTION);
            this.requiredLandSatiation = WingsConfigDefaults.clamp(this.requiredLandSatiation,
                    WingsConfigDefaults.WING_MIN_SATIATION, WingsConfigDefaults.WING_MAX_SATIATION);
            this.landingExertion = WingsConfigDefaults.clamp(this.landingExertion,
                    WingsConfigDefaults.WING_MIN_EXERTION, WingsConfigDefaults.WING_MAX_EXERTION);
            return this;
        }
    }
}
