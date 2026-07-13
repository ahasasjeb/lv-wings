package com.toni.wings.client.flight;

import com.toni.wings.server.flight.FlightAnimationState;

public final class FlightAnimationVisuals {
    private FlightAnimationVisuals() {
    }

    public static void begin(FlightAnimationState state, Animator animator) {
        switch (state) {
            case LIFT -> animator.beginLift();
            case GLIDE -> animator.beginGlide();
            case LAND -> animator.beginLand();
            case FALL -> animator.beginFall();
            case IDLE -> animator.beginIdle();
        }
    }
}
