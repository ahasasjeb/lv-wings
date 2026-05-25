package cc.lvjia.wings.server.apparatus;

import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.item.WingSettings;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class BuffedFlightApparatus implements FlightApparatus {
    private final @NonNull FlightApparatus delegate;
    private final @NonNull List<@NonNull EffectSettings> effects;
    private final @NonNull MobAvoidanceSettings mobAvoidance;

    public BuffedFlightApparatus(@NonNull WingSettings settings, EffectSettings... effects) {
        this(new SimpleFlightApparatus(settings), MobAvoidanceSettings.DEFAULT, effects);
    }

    public BuffedFlightApparatus(@NonNull WingSettings settings, @NonNull MobAvoidanceSettings mobAvoidance,
            EffectSettings... effects) {
        this(new SimpleFlightApparatus(settings), mobAvoidance, effects);
    }

    public BuffedFlightApparatus(@NonNull FlightApparatus delegate, EffectSettings... effects) {
        this(delegate, MobAvoidanceSettings.DEFAULT, effects);
    }

    public BuffedFlightApparatus(@NonNull FlightApparatus delegate, @NonNull MobAvoidanceSettings mobAvoidance,
            EffectSettings... effects) {
        this.delegate = Objects.requireNonNull(delegate, "委托");
        this.mobAvoidance = Objects.requireNonNull(mobAvoidance, "敌对生物回避设置");
        this.effects = copyNonNullEffects(effects);
    }

    private static @NonNull List<@NonNull EffectSettings> copyNonNullEffects(EffectSettings... effects) {
        List<@NonNull EffectSettings> configuredEffects = new ArrayList<>();
        Arrays.stream(Objects.requireNonNull(effects, "效果列表"))
                .filter(Objects::nonNull)
                .forEach(configuredEffects::add);
        return List.copyOf(configuredEffects);
    }

    private static void applyHostileMobAvoidance(@NonNull Player player, @NonNull MobAvoidanceSettings settings) {
        if (!player.isAlive()) {
            return;
        }
        double radius = settings.radius();
        if (radius <= 0.0D) {
            return;
        }
        double radiusSquared = radius * radius;
        AABB searchBox = player.getBoundingBox().inflate(radius);
        List<@NonNull Mob> hostiles = player.level().getEntitiesOfClass(Mob.class, searchBox,
                mob -> isRepellableHostile(mob, player, radiusSquared));
        if (hostiles.isEmpty()) {
            return;
        }
        for (Mob mob : hostiles) {
            neutralizeAggression(mob, player);
            pushAwayFromPlayer(mob, player, settings);
        }
    }

    private static boolean isRepellableHostile(@NonNull Mob mob, @NonNull Player player, double radiusSquared) {
        if (!(mob instanceof Enemy)) {
            return false;
        }
        if (!mob.isAlive() || mob.isRemoved() || mob.isSpectator()) {
            return false;
        }
        if (!mob.isEffectiveAi() || mob.isNoAi()) {
            return false;
        }
        if (mob.isAlliedTo(player)) {
            return false;
        }
        return !(mob.distanceToSqr(player) > radiusSquared);
    }

    private static void neutralizeAggression(@NonNull Mob mob, @NonNull Player player) {
        if (mob.getTarget() == player) {
            mob.setTarget(null);
        }
        if (mob.getLastHurtByMob() == player) {
            mob.setLastHurtByMob(null);
        }
        mob.setAggressive(false);
        mob.getNavigation().stop();
    }

    private static void pushAwayFromPlayer(@NonNull Mob mob, @NonNull Player player,
            @NonNull MobAvoidanceSettings settings) {
        double radius = settings.radius();
        if (radius <= 0.0D) {
            return;
        }
        Vec3 offset = mob.position().subtract(player.position());
        double distanceSquared = offset.lengthSqr();
        if (distanceSquared < 1.0E-6D) {
            offset = new Vec3(1.0D, 0.0D, 0.0D);
            distanceSquared = 1.0D;
        }
        double distance = Math.sqrt(distanceSquared);
        if (distance >= radius) {
            return;
        }
        double strengthFactor = 1.0D - (distance / radius);
        if (strengthFactor <= 0.0D) {
            return;
        }

        double pushX = 0.0D;
        double pushY = 0.0D;
        double pushZ = 0.0D;

        if (settings.horizontalPush() > 0.0D) {
            Vec3 horizontal = new Vec3(offset.x, 0.0D, offset.z);
            double horizontalLength = horizontal.length();
            if (horizontalLength > 1.0E-6D) {
                Vec3 horizontalNormal = horizontal.scale(1.0D / horizontalLength);
                double scale = settings.horizontalPush() * strengthFactor;
                pushX = horizontalNormal.x * scale;
                pushZ = horizontalNormal.z * scale;
            }
        }

        if (settings.verticalPush() > 0.0D) {
            pushY = settings.verticalPush() * strengthFactor;
        }

        if (pushX != 0.0D || pushY != 0.0D || pushZ != 0.0D) {
            mob.push(pushX, pushY, pushZ);
        }
    }

    @Override
    public void onFlight(@NonNull Player player, @NonNull Vec3 direction) {
        this.delegate.onFlight(player, direction);
    }

    @Override
    public void onLanding(@NonNull Player player, @NonNull Vec3 direction) {
        this.delegate.onLanding(player, direction);
    }

    @Override
    public boolean isUsable(@NonNull Player player) {
        return this.delegate.isUsable(player);
    }

    @Override
    public boolean isLandable(@NonNull Player player) {
        return this.delegate.isLandable(player);
    }

    @Override
    public @NonNull FlightState createState(@NonNull Flight flight) {
        FlightState base = this.delegate.createState(flight);
        boolean hasEffects = !this.effects.isEmpty();
        boolean avoidanceEnabled = this.mobAvoidance.isEnabled();
        if (!hasEffects && !avoidanceEnabled) {
            return base;
        }
        return new FlightState() {
            private int mobAvoidanceCooldown;

            @Override
            public void onUpdate(@NonNull Player player) {
                base.onUpdate(player);
                if (!player.level().isClientSide()) {
                    if (hasEffects) {
                        effects.forEach(effect -> effect.apply(player));
                    }
                    if (avoidanceEnabled) {
                        if (--this.mobAvoidanceCooldown <= 0) {
                            this.mobAvoidanceCooldown = Math.max(1, mobAvoidance.intervalTicks());
                            applyHostileMobAvoidance(player, mobAvoidance);
                        }
                    }
                }
            }
        };
    }

    public record EffectSettings(@NonNull Holder<MobEffect> effect, int amplifier, int durationTicks,
            int refreshThreshold) {
        public EffectSettings {
            Objects.requireNonNull(effect, "效果");
            if (durationTicks <= 0) {
                throw new IllegalArgumentException("持续时间必须为正数");
            }
            if (refreshThreshold < 0) {
                throw new IllegalArgumentException("刷新阈值必须为非负数");
            }
        }

        public static @NonNull EffectSettings of(@NonNull Holder<MobEffect> effect, int amplifier,
                int durationTicks, int refreshThreshold) {
            return new EffectSettings(effect, amplifier, durationTicks, refreshThreshold);
        }

        private void apply(@NonNull Player player) {
            MobEffectInstance existing = player.getEffect(this.effect);
            if (existing == null || existing.getAmplifier() < this.amplifier || existing.getDuration() <= this.refreshThreshold) {
                player.addEffect(new MobEffectInstance(this.effect, this.durationTicks, this.amplifier, true, false, false));
            }
        }
    }

    /**
     * 配置在装置激活时在服务器端应用的敌对生物回避气泡。
     * 默认配置清除仇恨并温和地推开大约14格半径内的敌对生物。
     */
    public record MobAvoidanceSettings(double radius, double horizontalPush, double verticalPush, int intervalTicks) {
        public static final MobAvoidanceSettings DEFAULT = new MobAvoidanceSettings(14.0D, 0.35D, 0.05D, 10);
        public static final MobAvoidanceSettings DISABLED = new MobAvoidanceSettings(0.0D, 0.0D, 0.0D, 0);

        public MobAvoidanceSettings {
            if (radius < 0.0D) {
                throw new IllegalArgumentException("半径必须为非负数");
            }
            if (horizontalPush < 0.0D) {
                throw new IllegalArgumentException("水平推力必须为非负数");
            }
            if (verticalPush < 0.0D) {
                throw new IllegalArgumentException("垂直推力必须为非负数");
            }
            if (intervalTicks < 0) {
                throw new IllegalArgumentException("间隔时间必须为非负数");
            }
        }

        public boolean isEnabled() {
            return this.radius > 0.0D && this.intervalTicks > 0 && (this.horizontalPush > 0.0D || this.verticalPush > 0.0D);
        }
    }
}
