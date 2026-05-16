package cc.lvjia.wings.server.config;

import cc.lvjia.wings.server.item.ImmutableWingSettings;
import cc.lvjia.wings.server.item.WingSettings;
import net.minecraft.resources.Identifier;

public final class ConfigWingSettings implements WingSettings {
    private static final int MIN_SATIATION = 0;
    private static final int MAX_SATIATION = 20;
    private static final double MIN_EXERTION = 0.0D;
    private static final double MAX_EXERTION = 10.0D;

    private final Identifier key;
    private final Data defaults;
    private Data current;

    ConfigWingSettings(Identifier key) {
        this(key, new Data());
    }

    ConfigWingSettings(Identifier key, Data defaults) {
        this.key = key;
        this.defaults = defaults.copy().normalize();
        this.current = this.defaults.copy();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        if (!Double.isFinite(value)) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
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

    public WingSettings toImmutable() {
        return ImmutableWingSettings.of(this.getRequiredFlightSatiation(), this.getFlyingExertion(), this.getRequiredLandSatiation(), this.getLandingExertion());
    }

    public Data toData() {
        return this.current.copy();
    }

    public Data defaultData() {
        return this.defaults.copy();
    }

    public void apply(Data data) {
        this.current = (data != null ? data : this.defaults).copy().normalize();
    }

    public void validate() {
        this.current.normalize();
    }

    public static final class Data {
        public int requiredFlightSatiation = 5;
        public double flyingExertion = 0.0001D;
        public int requiredLandSatiation = 2;
        public double landingExertion = 0.005D;

        Data copy() {
            Data copy = new Data();
            copy.requiredFlightSatiation = this.requiredFlightSatiation;
            copy.flyingExertion = this.flyingExertion;
            copy.requiredLandSatiation = this.requiredLandSatiation;
            copy.landingExertion = this.landingExertion;
            return copy;
        }

        Data normalize() {
            this.requiredFlightSatiation = clamp(this.requiredFlightSatiation, MIN_SATIATION, MAX_SATIATION);
            this.flyingExertion = clamp(this.flyingExertion, MIN_EXERTION, MAX_EXERTION);
            this.requiredLandSatiation = clamp(this.requiredLandSatiation, MIN_SATIATION, MAX_SATIATION);
            this.landingExertion = clamp(this.landingExertion, MIN_EXERTION, MAX_EXERTION);
            return this;
        }
    }
}
