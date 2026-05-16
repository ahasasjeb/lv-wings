package cc.lvjia.wings.server.asm;

import net.minecraft.world.entity.LivingEntity;

public final class GetLivingHeadLimitEvent {
    private final LivingEntity living;
    private float hardLimit;

    private float softLimit;

    private GetLivingHeadLimitEvent(LivingEntity living) {
        this.living = living;
    }

    public static GetLivingHeadLimitEvent create(LivingEntity living) {
        GetLivingHeadLimitEvent ev = new GetLivingHeadLimitEvent(living);
        ev.setHardLimit(75.0F);
        ev.setSoftLimit(50.0F);
        return ev;
    }

    public LivingEntity getEntity() {
        return this.living;
    }

    public float getHardLimit() {
        return this.hardLimit;
    }

    public void setHardLimit(float hardLimit) {
        this.hardLimit = hardLimit;
    }

    public float getSoftLimit() {
        return this.softLimit;
    }

    public void setSoftLimit(float softLimit) {
        this.softLimit = softLimit;
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
        return this.hardLimit == 75.0F && this.softLimit == 50.0F;
    }
}
