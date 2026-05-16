package cc.lvjia.wings.server.asm;

import net.minecraft.world.entity.player.Player;

public class PlayerFlightCheckEvent {
    private final Player player;
    private boolean flying;

    public PlayerFlightCheckEvent(Player player) {
        this.player = player;
    }

    public Player getEntity() {
        return this.player;
    }

    public boolean isFlying() {
        return this.flying;
    }

    public void setFlying() {
        this.flying = true;
    }
}
