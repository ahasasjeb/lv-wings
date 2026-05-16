package cc.lvjia.wings.server.asm;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class PlayerFlownEvent {
    private final Player player;
    private final Vec3 direction;

    public PlayerFlownEvent(Player player, Vec3 direction) {
        this.player = player;
        this.direction = direction;
    }

    public Player getEntity() {
        return this.player;
    }

    public Vec3 getDirection() {
        return this.direction;
    }
}
