package cc.lvjia.wings.client.flight;

import cc.lvjia.wings.server.flight.FlightAnimationState;

public final class FlightAnimationVisuals {
    private FlightAnimationVisuals() {
    }

    public static void begin(FlightAnimationState animationState, Animator animator) {
        switch (animationState) {
            case LIFT -> animator.beginLift();
            case GLIDE -> animator.beginGlide();
            case LAND -> animator.beginLand();
            case FALL -> animator.beginFall();
            case IDLE -> animator.beginIdle();
        }
    }
}