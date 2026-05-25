package cc.lvjia.wings.client.flight;

import cc.lvjia.wings.client.apparatus.WingForm;
import cc.lvjia.wings.client.flight.state.State;
import cc.lvjia.wings.client.flight.state.StateIdle;
import cc.lvjia.wings.server.flight.FlightAnimationState;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.util.function.FloatConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings("null")
public final class FlightViewDefault implements FlightView {
    private static final WingState ABSENT_ANIMATOR = new WingState() {
        @Override
        public WingState nextAbsent() {
            return this;
        }

        @Override
        public WingState next(@NonNull WingForm<? extends @NonNull Animator> form) {
            return PresentWingState.newState(form);
        }

        @Override
        public void update(Flight flight, Player player) {
        }

        @Override
        public void ifFormPresent(Consumer<FormRenderer> consumer) {
        }
    };

    private final @NonNull Flight flight;

    private final @NonNull Player player;

    private @NonNull WingState animator = ABSENT_ANIMATOR;

    private int lastUpdateTick = Integer.MIN_VALUE;

    public FlightViewDefault(@NonNull Player player, @NonNull Flight flight) {
        this.player = Objects.requireNonNull(player, "player");
        this.flight = Objects.requireNonNull(flight, "flight");
    }

    @Override
    public void ifFormPresent(Consumer<FormRenderer> consumer) {
        this.animator.ifFormPresent(consumer);
    }

    @Override
    public void tick() {
        int currentTick = this.player.tickCount;
        if (this.lastUpdateTick == currentTick) {
            return;
        }
        this.lastUpdateTick = currentTick;
        this.animator = WingForm.get(this.flight.getWing())
                .map(view -> this.animator.next(view))
                .orElseGet(this.animator::nextAbsent);
        this.animator.update(this.flight, this.player);
    }

    @Override
    public void tickEyeHeight(float value, FloatConsumer valueOut) {
        if (this.flight.isFlying() || (this.flight.getFlyingAmount(1.0F) > 0.0F && this.player.getPose() == Pose.FALL_FLYING)) {
            valueOut.accept(1.0F);
        }
    }

    private interface Strategy {
        void update(@NonNull Flight flight, @NonNull Player player);

        void ifFormPresent(@NonNull Consumer<FormRenderer> consumer);
    }

    interface WingState {
        @NonNull WingState nextAbsent();

        WingState next(@NonNull WingForm<? extends @NonNull Animator> form);

        void update(@NonNull Flight flight, @NonNull Player player);

        void ifFormPresent(@NonNull Consumer<FormRenderer> consumer);
    }

    private record PresentWingState(@NonNull WingForm<? extends @NonNull Animator> wing, Strategy behavior) implements WingState {

        public static <T extends @NonNull Animator> WingState newState(@NonNull WingForm<T> shape) {
            return new PresentWingState(shape, new WingStrategy<>(shape));
        }

        @Override
        public WingState nextAbsent() {
            return ABSENT_ANIMATOR;
        }

        @Override
        public WingState next(@NonNull WingForm<? extends @NonNull Animator> form) {
            if (this.wing.equals(form)) {
                return this;
            }
            return PresentWingState.newState(form);
        }

        @Override
        public void update(Flight flight, Player player) {
            this.behavior.update(flight, player);
        }

        @Override
        public void ifFormPresent(Consumer<FormRenderer> consumer) {
            this.behavior.ifFormPresent(consumer);
        }

        private static class WingStrategy<T extends @NonNull Animator> implements Strategy {
            private final @NonNull WingForm<T> shape;

            private final @NonNull T animator;

            private final @NonNull FormRenderer renderer;

            private @NonNull State state;

            private @Nullable FlightAnimationState remoteAnimationState;

            public WingStrategy(@NonNull WingForm<T> shape) {
                this.shape = Objects.requireNonNull(shape, "shape");
                this.animator = Objects.requireNonNull(shape.createAnimator(), "animator");
                this.state = new StateIdle();
                this.renderer = new FormRenderer() {
                    @Override
                    public @NonNull Identifier getTexture() {
                        return WingStrategy.this.shape.getTexture();
                    }

                    @Override
                    public @NonNull RenderType getRenderType() {
                        return WingStrategy.this.shape.getRenderType();
                    }

                    @Override
                    public void render(@NonNull PoseStack matrixStack, @NonNull VertexConsumer buffer, int packedLight,
                            int packedOverlay, float red, float green, float blue, float alpha, float delta) {
                        WingStrategy.this.shape.getModel().render(WingStrategy.this.animator, delta, matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
                    }
                };
            }

            @Override
            public void update(@NonNull Flight flight, @NonNull Player player) {
                this.animator.update();
                if (!player.isLocalPlayer()) {
                    this.applyRemoteAnimationState(flight.getAnimationState());
                    return;
                }
                this.remoteAnimationState = null;
                State state = Objects.requireNonNull(this.state.update(
                        flight,
                        player.getX() - player.xo,
                        player.getY() - player.yo,
                        player.getZ() - player.zo,
                        player
                ), "state");
                if (!this.state.equals(state)) {
                    state.beginAnimation(this.animator);
                }
                this.state = state;
            }

            private void applyRemoteAnimationState(@NonNull FlightAnimationState animationState) {
                FlightAnimationState safeAnimationState = Objects.requireNonNull(animationState, "animation state");
                if (this.remoteAnimationState == safeAnimationState) {
                    return;
                }
                this.remoteAnimationState = safeAnimationState;
                State state = State.create(safeAnimationState);
                if (!this.state.getClass().equals(state.getClass())) {
                    state.beginAnimation(this.animator);
                }
                this.state = state;
            }

            @Override
            public void ifFormPresent(Consumer<FormRenderer> consumer) {
                consumer.accept(this.renderer);
            }
        }
    }
}
