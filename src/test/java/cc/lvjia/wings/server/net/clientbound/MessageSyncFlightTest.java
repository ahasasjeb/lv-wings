package cc.lvjia.wings.server.net.clientbound;

import cc.lvjia.wings.server.flight.FlightAnimationState;
import cc.lvjia.wings.server.flight.FlightDefault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageSyncFlightTest {
    @Test
    void queuedMessageRetainsStateWhenSourceChanges() {
        FlightDefault source = new FlightDefault();
        source.setIsFlying(true);
        source.setTimeFlying(12);
        MessageSyncFlight message = new MessageSyncFlight(42, source);

        source.setIsFlying(false);
        source.setTimeFlying(0);

        assertEquals(42, message.playerId());
        assertNotSame(source, message.flight());
        assertTrue(message.flight().isFlying());
        assertEquals(12, message.flight().getTimeFlying());
        assertSame(source.getWing(), message.flight().getWing());
        assertEquals(FlightAnimationState.IDLE, message.flight().getAnimationState());
    }
}
