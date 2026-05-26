package cc.lvjia.wings.client.flight;

import cc.lvjia.wings.util.MathH;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import java.util.Random;

public final class AnimatorAvian implements Animator {
    private static final int LAND_TRANSITION_DURATION = 12;

    private static final int GLIDE_TRANSITION_DURATION = 60;

    private static final int IDLE_TRANSITION_DURATION = 18;

    private static final int LIFT_TRANSITION_DURATION = 20;

    private static final int FALL_TRANSITION_DURATION = 8;

    private final Movement restPosition = new RestPosition();

    private Movement movement = new IdleMovement();

    private float prevFlapCycle;

    private float flapCycle;

    private static SimplexNoise createNoise() {
        return new SimplexNoise(new LegacyRandomSource(new Random().nextLong()));
    }

    private void beginMovement(Movement movement, int transitionDuration) {
        this.setMovement(new Transition(this.movement, movement, transitionDuration));
    }

    private void setMovement(Movement movement) {
        this.movement.onEnd();
        this.movement = movement;
    }

    private void flap(float amount) {
        this.flapCycle += amount;
    }

    private float getFlapTime(float delta) {
        return MathH.lerp(this.prevFlapCycle, this.flapCycle, delta);
    }

    @Override
    public void beginLand() {
        this.beginMovement(new LandMovement(), LAND_TRANSITION_DURATION);
    }

    @Override
    public void beginGlide() {
        this.beginMovement(new GlideMovement(), GLIDE_TRANSITION_DURATION);
    }

    @Override
    public void beginIdle() {
        this.beginMovement(new IdleMovement(), IDLE_TRANSITION_DURATION);
    }

    @Override
    public void beginLift() {
        this.beginMovement(new LiftMovement(), LIFT_TRANSITION_DURATION);
    }

    @Override
    public void beginFall() {
        this.beginMovement(new FallMovement(), FALL_TRANSITION_DURATION);
    }

    public void getWingRotation(int index, float delta, RotationAngles rotation) {
        this.movement.getWingRotation(index, delta, rotation);
    }

    public void getFeatherRotation(int index, float delta, RotationAngles rotation) {
        this.movement.getFeatherRotation(index, delta, rotation);
    }

    @Override
    public void update() {
        this.prevFlapCycle = this.flapCycle;
        this.flap(this.movement.update());
    }

    private float getWeight(int index) {
        return Math.min(Math.abs(index - 1), 2) / 2.0F;
    }

    private interface Movement {
        void getWingRotation(int index, float delta, RotationAngles rotation);

        void getFeatherRotation(int index, float delta, RotationAngles rotation);

        float update();

        default void onEnd() {
        }
    }

    @FunctionalInterface
    private interface RotationGetter {
        void get(Movement movement, int index, float delta, RotationAngles rotation);
    }

    private record WingPose(float[] x, float[] y, float[] z) {
            private static final int POSE_SIZE = 4;

        private static Builder builder() {
                return new Builder();
            }

            public void get(int index, RotationAngles rotation) {
                if (index < 0 || index >= POSE_SIZE) {
                    rotation.set(0.0F, 0.0F, 0.0F);
                    return;
                }
                rotation.set(this.x[index], this.y[index], this.z[index]);
            }

            private static final class Builder {
                private final float[] x = new float[POSE_SIZE];
                private final float[] y = new float[POSE_SIZE];
                private final float[] z = new float[POSE_SIZE];

                private Builder() {
                }

                private Builder with(int index, double x, double y, double z) {
                    this.x[index] = (float) x;
                    this.y[index] = (float) y;
                    this.z[index] = (float) z;
                    return this;
                }

                private WingPose build() {
                    return new WingPose(this.x, this.y, this.z);
                }
            }
        }

    private final class RestPosition implements Movement {
        private final WingPose wing = WingPose.builder()
                .with(0, 0.0D, -23.5D, -16.0D)
                .with(1, 0.0D, 13.0D, 29.0D)
                .with(2, 0.0D, 12.0D, -28.0D)
                .with(3, 0.0D, 4.0D, 18.3D)
                .build();

        private final WingPose feather = WingPose.builder()
                .with(0, 0.0D, 0.0D, 23.48D)
                .build();

        @Override
        public void getWingRotation(int index, float delta, RotationAngles rotation) {
            this.wing.get(index, rotation);
        }

        @Override
        public void getFeatherRotation(int index, float delta, RotationAngles rotation) {
            this.feather.get(index, rotation);
        }

        @Override
        public float update() {
            return 0.0F;
        }
    }

    private final class LandMovement implements Movement {
        @Override
        public void getWingRotation(int index, float delta, RotationAngles rotation) {
            float pos = AnimatorAvian.this.getWeight(index + 1);
            float time = AnimatorAvian.this.getFlapTime(delta);
            float cycle = time - pos * 1.2F;
            double x = (Math.sin(cycle + MathH.PI / 2.0D) - 1.0D) / 2.0D * 20.0D + (1.0D - pos) * 50.0D;
            double y = (Math.sin(cycle) * 20.0D + (1.0D - pos) * 14.0D) *
                    (1.0D - pos * (Math.min(Math.sin(cycle + MathH.PI), 0.0D) / 2.0D + 1.0D) * Math.sin(time));
            AnimatorAvian.this.restPosition.getWingRotation(index, delta, rotation);
            rotation.add(x, y, 4.0D);
        }

        @Override
        public void getFeatherRotation(int index, float delta, RotationAngles rotation) {
            AnimatorAvian.this.restPosition.getFeatherRotation(index, delta, rotation);
        }

        @Override
        public float update() {
            return 0.67F;
        }
    }

    private final class GlideMovement implements Movement {
        private final SimplexNoise noise = createNoise();

        private int time;

        @Override
        public void getWingRotation(int index, float delta, RotationAngles rotation) {
            float pos = AnimatorAvian.this.getWeight(index);
            float time = AnimatorAvian.this.getFlapTime(delta);
            double y = (Math.sin(time) * 5.0D - 14.0D) * pos;
            AnimatorAvian.this.restPosition.getWingRotation(index, delta, rotation);
            rotation.add(0.0D, y, 0.0D);
        }

        @Override
        public void getFeatherRotation(int index, float delta, RotationAngles rotation) {
            double x = this.noise.getValue((this.time + delta) * 0.17D, index * 0.13D) * 1.25D;
            AnimatorAvian.this.restPosition.getFeatherRotation(index, delta, rotation);
            rotation.add(x, 0.0D, 0.0D);
        }

        @Override
        public float update() {
            this.time++;
            return 0.045F;
        }
    }

    private final class IdleMovement implements Movement {
        private final WingPose wing = WingPose.builder()
                .with(0, 40.0D, -60.0D, -50.0D)
                .with(1, 72.0D, 10.0D, 100.0D)
                .with(2, 0.0D, -10.0D, -120.0D)
                .with(3, 10.0D, 0.0D, 100.0D)
                .build();

        private final WingPose feather = WingPose.builder()
                .with(0, 10.0D, 20.0D, 23.48D)
                .with(1, 0.0D, 20.0D, -70.0D)
                .with(2, 0.0D, 10.0D, 40.0D)
                .with(3, -20.0D, 0.0D, 20.0D)
                .build();

        @Override
        public void getWingRotation(int index, float delta, RotationAngles rotation) {
            float pos = AnimatorAvian.this.getWeight(index);
            float time = AnimatorAvian.this.getFlapTime(delta);
            this.wing.get(index, rotation);
            rotation.add(0.0D, Math.sin(time) * 3.0D * pos, 0.0D);
        }

        @Override
        public void getFeatherRotation(int index, float delta, RotationAngles rotation) {
            float pos = AnimatorAvian.this.getWeight(index);
            float time = AnimatorAvian.this.getFlapTime(delta);
            this.feather.get(index, rotation);
            rotation.add(0.0D, -Math.sin(time) * 5.0D * pos, 0.0D);
        }

        @Override
        public float update() {
            return 0.035F;
        }
    }

    private final class LiftMovement implements Movement {
        private final int beginDuration = 20 * 5;

        private int beginTime;

        @Override
        public void getWingRotation(int index, float delta, RotationAngles rotation) {
            float pos = AnimatorAvian.this.getWeight(index);
            float time = AnimatorAvian.this.getFlapTime(delta);
            float cycle = time - pos * 1.2F;
            double x = (Math.sin(cycle + MathH.PI / 2.0D) - 1.0D) / 2.0D * 16.0D + 8.0D;
            double y = (Math.sin(cycle) * 26.0D + 12.0D) *
                    (1.0D - pos * (Math.min(Math.sin(cycle + MathH.PI), 0.0D) / 2.0D + 1.0D) * Math.sin(time));
            AnimatorAvian.this.restPosition.getWingRotation(index, delta, rotation);
            rotation.add(x, y, 0.0D);
        }

        @Override
        public void getFeatherRotation(int index, float delta, RotationAngles rotation) {
            AnimatorAvian.this.restPosition.getFeatherRotation(index, delta, rotation);
        }

        @Override
        public float update() {
            float flap = MathH.lerp(0.375F, 0.225F, (float) this.beginTime / this.beginDuration);
            if (this.beginTime < this.beginDuration) {
                this.beginTime++;
            }
            return flap;
        }
    }

    private final class FallMovement implements Movement {
        private final SimplexNoise noise = createNoise();

        private final WingPose wing = WingPose.builder()
                .with(0, 30.0D, -23.0D, -50.0D)
                .with(1, -10.0D, 5.0D, -10.0D)
                .with(2, -30.0D, -20.0D, -20.0D)
                .with(3, -20.0D, 0.0D, 20.0D)
                .build();

        private int time;

        @Override
        public void getWingRotation(int index, float delta, RotationAngles rotation) {
            double n = this.noise.getValue((this.time + delta) * 0.18D, index * 0.13D) * 0.92D * (index + 1);
            this.wing.get(index, rotation);
            rotation.add(n, 0.0D, n);
        }

        @Override
        public void getFeatherRotation(int index, float delta, RotationAngles rotation) {
            double n = this.noise.getValue((this.time + delta) * 0.2D, index * 0.13D) * 1.75D;
            rotation.set(-n, n * 4.0D, 0.0D);
        }

        @Override
        public float update() {
            this.time++;
            return 0.0F;
        }
    }

    private final class Transition implements Movement {
        private final Movement start;

        private final Movement end;

        private final int duration;
        private final RotationAngles startRotation = new RotationAngles();
        private final RotationAngles endRotation = new RotationAngles();
        private int lastTime, time;
        private boolean isActive = true;

        private Transition(Movement start, Movement end, int duration) {
            this.start = start;
            this.end = end;
            this.duration = duration;
        }

        @Override
        public void getWingRotation(int index, float delta, RotationAngles rotation) {
            this.lerpRotation(index, delta, Movement::getWingRotation, rotation);
        }

        @Override
        public void getFeatherRotation(int index, float delta, RotationAngles rotation) {
            this.lerpRotation(index, delta, Movement::getFeatherRotation, rotation);
        }

        private void lerpRotation(int index, float delta, RotationGetter getter, RotationAngles rotation) {
            getter.get(this.start, index, delta, this.startRotation);
            getter.get(this.end, index, delta, this.endRotation);
            float t = MathH.easeInOut(MathH.lerp(this.lastTime, this.time, delta) / this.duration);
            rotation.set(
                    MathH.lerpDegrees(this.startRotation.x(), this.endRotation.x(), t),
                    MathH.lerpDegrees(this.startRotation.y(), this.endRotation.y(), t),
                    MathH.lerpDegrees(this.startRotation.z(), this.endRotation.z(), t)
            );
        }

        @Override
        public float update() {
            this.lastTime = this.time;
            float flapStart = this.start.update();
            float flapEnd = this.end.update();
            float flap = MathH.lerp(flapStart, flapEnd, (float) this.time / this.duration);
            if (this.time < this.duration) {
                this.time++;
            } else if (this.isActive) {
                AnimatorAvian.this.setMovement(this.end);
            }
            return flap;
        }

        @Override
        public void onEnd() {
            this.isActive = false;
        }
    }
}
