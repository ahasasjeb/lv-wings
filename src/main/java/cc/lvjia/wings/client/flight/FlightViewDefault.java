package cc.lvjia.wings.client.flight;

import cc.lvjia.wings.client.apparatus.WingForm;
import cc.lvjia.wings.client.flight.state.State;
import cc.lvjia.wings.client.flight.state.StateIdle;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.util.function.FloatConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public final class FlightViewDefault implements FlightView {
    private static final WingState ABSENT_ANIMATOR = new WingState() {
        @Override
        public WingState nextAbsent() {
            return this;
        }

        @Override
        public WingState next(WingForm<?> form) {
            return PresentWingState.newState(form);
        }

        @Override
        public void update(Flight flight, Player player) {
        }

        @Override
        public void ifFormPresent(Consumer<FormRenderer> consumer) {
        }
    };

    private final Flight flight;

    private final Player player;

    private WingState animator = ABSENT_ANIMATOR;

    private int lastUpdateTick = Integer.MIN_VALUE;

    public FlightViewDefault(Player player, Flight flight) {
        this.player = player;
        this.flight = flight;
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
        void update(Flight flight, Player player);

        void ifFormPresent(Consumer<FormRenderer> consumer);
    }

    interface WingState {
        WingState nextAbsent();

        WingState next(WingForm<?> form);

        void update(Flight flight, Player player);

        void ifFormPresent(Consumer<FormRenderer> consumer);
    }

    private record PresentWingState(WingForm<?> wing, Strategy behavior) implements WingState {

        public static <T extends Animator> WingState newState(WingForm<T> shape) {
                return new PresentWingState(shape, new WingStrategy<>(shape));
            }

            @Override
            public WingState nextAbsent() {
                return ABSENT_ANIMATOR;
            }

            @Override
            public WingState next(WingForm<?> form) {
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

            private static class WingStrategy<T extends Animator> implements Strategy {
                private final WingForm<T> shape;

                private final T animator;

                private State state;

                public WingStrategy(WingForm<T> shape) {
                    this.shape = shape;
                    this.animator = shape.createAnimator();
                    this.state = new StateIdle();
                }

                @Override
                public void update(Flight flight, Player player) {
                    this.animator.update();
                    State state = this.state.update(
                            flight,
                            player.getX() - player.xo,
                            player.getY() - player.yo,
                            player.getZ() - player.zo,
                            player
                    );
                    if (!this.state.equals(state)) {
                        state.beginAnimation(this.animator);
                    }
                    this.state = state;
                }

                @Override
                public void ifFormPresent(Consumer<FormRenderer> consumer) {
                    consumer.accept(new FormRenderer() {
                        @Override
                        public ResourceLocation getTexture() {
                            return WingStrategy.this.shape.getTexture();
                        }

                        @Override
                        public RenderType getRenderType() {
                            return WingStrategy.this.shape.getRenderType();
                        }

                        @Override
                        public void render(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha, float delta) {
                            WingStrategy.this.shape.getModel().render(WingStrategy.this.animator, delta, matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
                        }
                    });
                }
            }
        }
}
