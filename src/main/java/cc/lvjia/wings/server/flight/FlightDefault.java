package cc.lvjia.wings.server.flight;

import cc.lvjia.wings.WingsCore;
import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.server.apparatus.FlightApparatus;
import cc.lvjia.wings.server.config.WingsConfig;
import cc.lvjia.wings.server.effect.WingsEffects;
import cc.lvjia.wings.util.CubicBezier;
import cc.lvjia.wings.util.MathH;
import cc.lvjia.wings.util.NBTSerializer;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 飞行状态默认实现，同时承载服务端权威逻辑与客户端预测渲染。
 * <p>
 * 核心职责：
 * <ol>
 *   <li><b>状态管理：</b>持有飞行中/翅膀类型/动画状态，通过监听器模式解耦同步和 UI 更新。</li>
 *   <li><b>飞行物理：</b>{@link #onWornUpdate(Player)} 每 tick 计算俯仰/偏航/升力，驱动玩家运动。</li>
 *   <li><b>动画节流：</b>委托 {@link FlightAnimationTracker} 控制状态同步频率，避免网络包泛滥。</li>
 *   <li><b>网络序列化：</b>{@link #serialize(FriendlyByteBuf)} / {@link #deserialize(FriendlyByteBuf)}
 *       用于服务端→客户端的增量同步。</li>
 *   <li><b>持久化：</b>通过 {@link #CODEC} 和 {@link Serializer} 支持 NBT 和
 *       {@link net.minecraft.world.level.storage.ValueInput}/{@link net.minecraft.world.level.storage.ValueOutput}
 *       两种存储格式。</li>
 * </ol>
 * </p>
 */
// @SuppressWarnings("null"): 部分 Minecraft 原生 API 返回类型标记为 @Nullable，
// 但 Flight 接口和本实现保证所有公开 getter 不会返回 null，此处压制以避免在每个方法上加抑制注解。
@SuppressWarnings("null")
public final class FlightDefault implements Flight {
    /** 飞行动画插值曲线：缓出（先快后慢），使翅膀展开/收起过渡自然。 */
    private static final CubicBezier FLY_AMOUNT_CURVE = new CubicBezier(0.37F, 0.13F, 0.3F, 1.12F);

    /** 飞行时长初始值（未飞行状态）。 */
    private static final int INITIAL_TIME_FLYING = 0;

    /** 飞行时长上限 tick，达到后若在地面则在本地玩家侧自动关闭飞行。 */
    private static final int MAX_TIME_FLYING = 20;

    /** 前向最小速度（玩家未按 W 时的滑行速度）。 */
    private static final float MIN_SPEED = 0.03F;

    /** 前向最大速度（玩家按住 W 时的全速）。 */
    private static final float MAX_SPEED = 0.0715F;

    /** 起飞时垂直方向的基础抬升力。 */
    private static final float Y_BOOST = 0.05F;

    /** 可着陆状态下，下落速度的衰减系数（0.9 = 每次减少 10% 垂直速度）。 */
    private static final float FALL_REDUCTION = 0.9F;

    /**
     * 俯仰角偏移量：玩家实际俯仰减去此值后用于计算飞行方向。
     * 30° 偏移使玩家视角略向下时仍保持水平飞行，更符合直觉。
     */
    private static final float PITCH_OFFSET = 30.0F;

    /** 反序列化兜底翅膀 ID——当读取到无效 ID 时回退到 NONE。 */
    private static final Identifier DEFAULT_WING_ID = WingsCore.Names.NONE;

    /*
    飞行物理调参说明：
    - MIN/MAX_SPEED：控制前向速度范围，通过 player.zza（W/S 输入）线性插值。
    - Y_BOOST：起飞时垂直方向的轻微抬升补偿，使起飞手感更柔和。
    - FALL_REDUCTION：可着陆状态下对下落速度的乘性衰减，防止高速坠落。
    - PITCH_OFFSET：视角俯仰偏移量，使水平飞行时的实际视角更自然。
    - FLY_AMOUNT_CURVE：Bezier 曲线，控制 getFlyingAmount() 的翅膀展开/收起动画速率。
    */
    public static final Codec<FlightDefault> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.BOOL.optionalFieldOf(Serializer.IS_FLYING, false).forGetter(FlightDefault::isFlying),
                    Codec.INT.optionalFieldOf(Serializer.TIME_FLYING, INITIAL_TIME_FLYING)
                            .forGetter(FlightDefault::getTimeFlying),
                    Codec.STRING.optionalFieldOf(Serializer.WING, DEFAULT_WING_ID.toString())
                            .forGetter(FlightDefault::getWingId))
            .apply(instance, FlightDefault::fromPersistentData));

    private final List<FlyingListener> flyingListeners = Lists.newArrayList();

    private final List<SyncListener> syncListeners = Lists.newArrayList();

    private final WingState voidState = new WingState(FlightApparatus.NONE, FlightApparatus.FlightState.NONE);

    private final FlightAnimationTracker animationTracker = new FlightAnimationTracker();

    private int prevTimeFlying = INITIAL_TIME_FLYING;

    private int timeFlying = INITIAL_TIME_FLYING;

    private boolean isFlying;

    private FlightApparatus flightApparatus = FlightApparatus.NONE;

    private FlightAnimationState animationState = FlightAnimationState.IDLE;

    private WingState state = this.voidState;

    private static Identifier wingIdFor(@Nullable FlightApparatus wing) {
        Identifier key = wing != null ? WingsMod.WINGS.getKey(wing) : null;
        return key != null ? key : DEFAULT_WING_ID;
    }

    private static FlightApparatus wingFrom(@Nullable Identifier id) {
        return id != null ? WingsMod.WINGS.getOptional(id).orElse(FlightApparatus.NONE) : FlightApparatus.NONE;
    }

    private static FlightApparatus wingFrom(@Nullable String rawId) {
        return wingFrom(Identifier.tryParse(rawId));
    }

    private static FlightDefault fromPersistentData(boolean isFlying, int timeFlying, String wingId) {
        FlightDefault flight = new FlightDefault();
        flight.setIsFlying(isFlying);
        flight.setTimeFlying(timeFlying);
        flight.setWing(wingFrom(wingId));
        return flight;
    }

    private String getWingId() {
        return wingIdFor(this.getWing()).toString();
    }

    @Override
    public void setIsFlying(boolean isFlying, PlayerSet players) {
        // 状态有变化时才触发监听器和同步，避免无意义循环
        if (this.isFlying != isFlying) {
            this.isFlying = isFlying;
            this.flyingListeners.forEach(FlyingListener.onChangeUsing(isFlying));
            this.sync(players);
        }
    }

    @Override
    public boolean isFlying() {
        return this.isFlying;
    }

    @Override
    public int getTimeFlying() {
        return this.timeFlying;
    }

    @Override
    public void setTimeFlying(int timeFlying) {
        this.timeFlying = timeFlying;
    }

    @Override
    public void setWing(FlightApparatus wing, PlayerSet players) {
        Objects.requireNonNull(wing);
        // 翅膀类型变化时才切换内部状态并同步
        if (this.flightApparatus != wing) {
            this.flightApparatus = wing;
            this.state = this.state.next(wing);
            this.sync(players);
        }
    }

    @Override
    public FlightApparatus getWing() {
        return this.flightApparatus;
    }

    @Override
    public FlightAnimationState getAnimationState() {
        return this.animationState;
    }

    @Override
    public void setAnimationState(FlightAnimationState animationState) {
        this.loadAnimationState(animationState);
    }

    // 通过 Bezier 曲线计算翅膀展开/收起进度，delta 用于帧间插值
    @Override
    public float getFlyingAmount(float delta) {
        float amount = FLY_AMOUNT_CURVE
                .eval(MathH.lerp(this.getPrevTimeFlying(), this.getTimeFlying(), delta) / MAX_TIME_FLYING);
        return Mth.clamp(amount, 0.0F, 1.0F);
    }

    private int getPrevTimeFlying() {
        return this.prevTimeFlying;
    }

    private void setPrevTimeFlying(int prevTimeFlying) {
        this.prevTimeFlying = prevTimeFlying;
    }

    @Override
    public void registerFlyingListener(FlyingListener listener) {
        this.flyingListeners.add(listener);
    }

    @Override
    public void registerSyncListener(SyncListener listener) {
        this.syncListeners.add(listener);
    }

    // 能否起飞：不在水中（或允许水下飞行）、有翅膀药水效果、饥饿值 > 0、翅膀装置可用
    @Override
    public boolean canFly(Player player) {
        return !this.isUnderwaterFlightBlocked(player) && this.hasEffect(player)
                && player.getFoodData().getFoodLevel() > 0
                && this.flightApparatus.isUsable(player);
    }

    @Override
    public boolean hasEffect(Player player) {
        return WingsEffects.WINGS.isBound() && player.hasEffect(WingsEffects.WINGS);
    }

    // 能否着陆：委托给当前翅膀装置判断
    @Override
    public boolean canLand(Player player) {
        return this.flightApparatus.isLandable(player);
    }

    /**
     * 每 tick 的飞行物理更新与状态同步（核心逻辑）。
     * <p>
     * <b>飞行物理：</b>
     * <ol>
     *   <li>前向速度由 {@code player.zza}（W/S 输入）在 MIN_SPEED~MAX_SPEED 间插值。</li>
     *   <li>俯仰角决定垂直/水平分量分配：低头（俯仰>0）获得更多升力，抬头（俯仰<0）获得更多前向速度。</li>
     *   <li>仰头超过 30° 时升力系数逐渐下降（模拟空气阻力）。</li>
     *   <li>可着陆时应用 {@link #FALL_REDUCTION} 衰减下落速度并重置摔落距离。</li>
     * </ol>
     * <b>状态同步：</b>服务端检查水中禁飞、药水效果和装备可用性，决定是否继续飞行或强制落地。
     * </p>
     *
     * @param player 当前玩家实体
     */
    private void onWornUpdate(Player player) {
        boolean underwaterFlightBlocked = this.isUnderwaterFlightBlocked(player);
        if (underwaterFlightBlocked && this.isFlying()) {
            // 水中飞行被禁：客户端仅停本地（避免预测冲突），服务端广播停飞。
            if (player.level().isClientSide()) {
                this.setIsFlying(false, PlayerSet.empty());
            } else {
                this.setIsFlying(false, PlayerSet.ofAll());
            }
        }
        if (player.isEffectiveAi() && !underwaterFlightBlocked) {
            if (this.isFlying()) {
                // 根据 W/S 输入量线性插值前向速度
                float speed = Mth.clampedLerp(MIN_SPEED, MAX_SPEED, player.zza);
                // 俯仰越陡，升力补偿越小 —— 玩家低头俯冲时减少额外上抬
                float elevationBoost = MathH.transform(
                        Math.abs(player.getXRot()),
                        45.0F, 90.0F,
                        1.0F, 0.0F);
                // 将玩家视角旋转映射到世界坐标系的速度增量
                float pitch = -MathH.toRadians(player.getXRot() - PITCH_OFFSET * elevationBoost);
                float yaw = -MathH.toRadians(player.getYRot()) - MathH.PI;
                float vxz = -Mth.cos(pitch);
                float vy = Mth.sin(pitch);
                // 仰头（俯仰<0）时垂直速度适当缩放，避免抬头飞速度过快
                if (player.getXRot() < 0.0F) {
                    float verticalSpeedScale = MathH.transform(
                            -player.getXRot(),
                            0.0F, 90.0F,
                            1.0F, 0.8F);
                    vy = vy * verticalSpeedScale;
                }
                // 向上因子：低头俯冲时升力递减，抬头超过 30° 时也递减（模拟失速）
                float upwardFactor = 1.0F;
                if (player.getXRot() > 0.0F) {
                    upwardFactor = elevationBoost;
                } else if (player.getXRot() < -30.0F) {
                    float pitchAbs = -player.getXRot();
                    upwardFactor = MathH.transform(
                            pitchAbs,
                            30.0F, 90.0F,
                            1.0F, 0.72F);
                }
                float vz = Mth.cos(yaw);
                float vx = Mth.sin(yaw);
                player.setDeltaMovement(player.getDeltaMovement().add(
                        vx * vxz * speed,
                        vy * speed + Y_BOOST * upwardFactor,
                        vz * vxz * speed));
            }
            // 可着陆时：衰减下落速度 + 清除摔落距离，防止摔伤
            if (this.canLand(player)) {
                Vec3 mot = player.getDeltaMovement();
                if (mot.y() < 0.0D) {
                    player.setDeltaMovement(mot.multiply(1.0D, FALL_REDUCTION, 1.0D));
                }
                player.fallDistance = 0.0F;
            }
        }
        // 服务端独占：检查翅膀装置状态，决定是否切换或强制停飞
        if (!player.level().isClientSide()) {
            if (underwaterFlightBlocked) {
                this.state = this.state.notFlying();
                return;
            }
            if (this.canFly(player)) {
                (this.state = this.state.next(this.flightApparatus)).onUpdate(player);
            } else if (this.isFlying()) {
                this.setIsFlying(false, PlayerSet.ofAll());
                this.state = this.state.notFlying();
            }
        }
    }

    @Override
    public void tick(Player player) {
        boolean hasEffect = this.hasEffect(player);
        if (hasEffect || !player.isEffectiveAi()) {
            // 有药水效果或非 AI 实体（如玩家），执行飞行物理更新
            if (!hasEffect && !player.level().isClientSide()) {
                this.setWing(FlightApparatus.NONE, PlayerSet.ofAll());
            }
            this.onWornUpdate(player);
        } else if (!player.level().isClientSide()) {
            // 无药水效果且是服务端 → 清除翅膀并强制停飞
            this.setWing(FlightApparatus.NONE, PlayerSet.ofAll());
            if (this.isFlying()) {
                this.setIsFlying(false, PlayerSet.ofAll());
            }
        }
        // 记录上一 tick 飞行时长用于帧间插值
        this.setPrevTimeFlying(this.getTimeFlying());
        // 飞行中累加 timeFlying（上限 MAX），地面时递减至 0
        if (this.isFlying()) {
            if (this.getTimeFlying() < MAX_TIME_FLYING) {
                this.setTimeFlying(this.getTimeFlying() + 1);
            } else if (player.isLocalPlayer() && player.onGround()) {
                // 客户端本地预测：飞行满且着地时自行停飞
                this.setIsFlying(false, PlayerSet.ofOthers());
            }
        } else {
            if (this.getTimeFlying() > INITIAL_TIME_FLYING) {
                this.setTimeFlying(this.getTimeFlying() - 1);
            }
        }
        // 服务端推进动画状态机，需要同步时向追踪者发包
        if (!player.level().isClientSide()) {
            boolean shouldSyncAnimation = this.animationTracker.tick(this, player);
            this.animationState = this.animationTracker.getState();
            if (shouldSyncAnimation) {
                this.sync(PlayerSet.ofOthers());
            }
        }
    }

    // 玩家挥翅时委托给当前翅膀装置（触发粒子/音效等）
    @Override
    public void onFlown(Player player, Vec3 direction) {
        if (this.isFlying()) {
            this.flightApparatus.onFlight(player, direction);
        } else if (player.getDeltaMovement().y() < -0.5D) {
            this.flightApparatus.onLanding(player, direction);
        }
    }

    // 从旧 Flight 复制全部状态（玩家克隆/维度切换时）
    @Override
    public void clone(Flight other) {
        this.setIsFlying(other.isFlying());
        this.setTimeFlying(other.getTimeFlying());
        this.setWing(other.getWing());
        this.loadAnimationState(other.getAnimationState());
    }

    @Override
    public void sync(PlayerSet players) {
        // 通过监听器解耦网络层：只通知"状态变了"，不关心具体怎么发包
        this.syncListeners.forEach(SyncListener.onSyncUsing(players));
    }

    // 将飞行状态写入网络缓冲区（服务端→客户端）
    @Override
    public void serialize(FriendlyByteBuf buf) {
        buf.writeBoolean(this.isFlying());
        buf.writeVarInt(this.getTimeFlying());
        buf.writeIdentifier(wingIdFor(this.getWing()));
        buf.writeByte(this.getAnimationState().id());
    }

    // 从网络缓冲区读取飞行状态（客户端接收）
    @Override
    public void deserialize(FriendlyByteBuf buf) {
        this.setIsFlying(buf.readBoolean());
        this.setTimeFlying(buf.readVarInt());
        Identifier wingId;
        try {
            wingId = buf.readIdentifier();
        } catch (IllegalArgumentException ex) {
            wingId = DEFAULT_WING_ID;
        }
        this.setWing(wingFrom(wingId));
        this.loadAnimationState(FlightAnimationState.byId(buf.readUnsignedByte()));
    }

    private void loadAnimationState(FlightAnimationState animationState) {
        this.animationState = Objects.requireNonNull(animationState);
        this.animationTracker.load(animationState);
    }

    // 检测水下飞行是否被配置禁止
    private boolean isUnderwaterFlightBlocked(Player player) {
        return player.isUnderWater() && !WingsConfig.isUnderwaterFlightAllowed();
    }

    // NBT 序列化器：将 FlightDefault ↔ CompoundTag 互转，用于玩家数据持久化
    public static final class Serializer implements NBTSerializer<FlightDefault, CompoundTag> {
        private static final String IS_FLYING = "isFlying";

        private static final String TIME_FLYING = "timeFlying";

        private static final String WING = "wing";

        private final Supplier<FlightDefault> factory;

        public Serializer(Supplier<FlightDefault> factory) {
            this.factory = factory;
        }

        @Override
        public CompoundTag serialize(FlightDefault instance) {
            CompoundTag compound = new CompoundTag();
            compound.putBoolean(IS_FLYING, instance.isFlying());
            compound.putInt(TIME_FLYING, instance.getTimeFlying());
            compound.putString(WING, wingIdFor(instance.getWing()).toString());
            return compound;
        }

        @Override
        public FlightDefault deserialize(CompoundTag compound) {
            FlightDefault f = Objects.requireNonNull(this.factory.get(), "flight factory");
            f.setIsFlying(compound.getBoolean(IS_FLYING).orElse(false), PlayerSet.ofAll());
            f.setTimeFlying(compound.getInt(TIME_FLYING).orElse(INITIAL_TIME_FLYING));
            String wingIdRaw = compound.contains(WING)
                    ? compound.getString(WING).orElse(DEFAULT_WING_ID.toString())
                    : DEFAULT_WING_ID.toString();
            f.setWing(wingFrom(wingIdRaw));
            return f;
        }

        // 写入 ValueOutput（新版数据存储 API）
        public void serialize(FlightDefault instance, ValueOutput output) {
            output.putBoolean(IS_FLYING, instance.isFlying());
            output.putInt(TIME_FLYING, instance.getTimeFlying());
            output.putString(WING, wingIdFor(instance.getWing()).toString());
        }

        // 从 ValueInput 读取（新版数据存储 API）
        public FlightDefault deserialize(ValueInput input) {
            FlightDefault flight = Objects.requireNonNull(this.factory.get(), "flight factory");
            flight.setIsFlying(input.getBooleanOr(IS_FLYING, false), PlayerSet.ofAll());
            flight.setTimeFlying(input.getIntOr(TIME_FLYING, INITIAL_TIME_FLYING));
            String wingIdRaw = input.getStringOr(WING, DEFAULT_WING_ID.toString());
            flight.setWing(wingFrom(wingIdRaw));
            return flight;
        }
    }

    // 当前翅膀装置的状态包装：缓存 FlightState 避免每 tick 重新创建
    private final class WingState {
        private final FlightApparatus apparatus;

        private final FlightApparatus.FlightState activity;

        private WingState(FlightApparatus apparatus, FlightApparatus.FlightState activity) {
            this.apparatus = apparatus;
            this.activity = activity;
        }

        // 切换到非飞行状态（voidState）
        private WingState notFlying() {
            return FlightDefault.this.voidState;
        }

        // 切换到新翅膀装置的状态（缓存避免重复创建）
        private WingState next(FlightApparatus wf) {
            if (this.apparatus.equals(wf)) {
                return this;
            }
            return new WingState(wf, wf.createState(FlightDefault.this));
        }

        // 委托当前 FlightState 执行每 tick 更新
        private void onUpdate(Player player) {
            this.activity.onUpdate(player);
        }
    }
}
