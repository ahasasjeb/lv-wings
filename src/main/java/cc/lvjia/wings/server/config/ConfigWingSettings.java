package cc.lvjia.wings.server.config;

import cc.lvjia.wings.server.item.ImmutableWingSettings;
import cc.lvjia.wings.server.item.WingSettings;
import net.minecraft.resources.Identifier;

public final class ConfigWingSettings implements WingSettings {
    private final Identifier key;
    private final int requiredFlightSatiation;
    private final float flyingExertion;
    private final int requiredLandSatiation;
    private final float landingExertion;

    ConfigWingSettings(Identifier key) {
        this(key, 5, 0.0001F, 2, 0.005F);
    }

    ConfigWingSettings(Identifier key, int requiredFlightSatiation, float flyingExertion, int requiredLandSatiation, float landingExertion) {
        this.key = key;
        this.requiredFlightSatiation = requiredFlightSatiation;
        this.flyingExertion = flyingExertion;
        this.requiredLandSatiation = requiredLandSatiation;
        this.landingExertion = landingExertion;
    }

    public Identifier getKey() {
        return this.key;
    }

    @Override
    public int getRequiredFlightSatiation() {
        return this.requiredFlightSatiation;
    }

    @Override
    public float getFlyingExertion() {
        return this.flyingExertion;
    }

    @Override
    public int getRequiredLandSatiation() {
        return this.requiredLandSatiation;
    }

    @Override
    public float getLandingExertion() {
        return this.landingExertion;
    }

    public WingSettings toImmutable() {
        return ImmutableWingSettings.of(this.requiredFlightSatiation, this.flyingExertion, this.requiredLandSatiation, this.landingExertion);
    }

    public void validate() {
    }
}
