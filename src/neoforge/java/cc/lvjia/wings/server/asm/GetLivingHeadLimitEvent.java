package cc.lvjia.wings.server.asm;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public final class GetLivingHeadLimitEvent extends LivingEvent {
    private float hardLimit;

    private float softLimit;

    private boolean modified;

    private GetLivingHeadLimitEvent(LivingEntity living) {
        super(living);
    }

    public static GetLivingHeadLimitEvent create(LivingEntity living) {
        return new GetLivingHeadLimitEvent(living);
    }

    public float getHardLimit() {
        return this.hardLimit;
    }

    public void setHardLimit(float hardLimit) {
        this.hardLimit = hardLimit;
        this.modified = true;
    }

    public float getSoftLimit() {
        return this.softLimit;
    }

    public void setSoftLimit(float softLimit) {
        this.softLimit = softLimit;
        this.modified = true;
    }

    public void disableHardLimit() {
        this.setHardLimit(Float.POSITIVE_INFINITY);
    }

    public void disableSoftLimit() {
        this.setSoftLimit(Float.POSITIVE_INFINITY);
    }

    public boolean hasHardLimit() {
        return this.getHardLimit() < Float.POSITIVE_INFINITY;
    }

    public boolean hasSoftLimit() {
        return this.getSoftLimit() < Float.POSITIVE_INFINITY;
    }

    public boolean isVanilla() {
        return !this.modified;
    }
}
