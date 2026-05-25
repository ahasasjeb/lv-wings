package cc.lvjia.wings.client.asm;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.world.entity.player.Player;

public class AnimatePlayerModelEvent {
    private final Player player;
    private final PlayerModel model;

    private final float ticksExisted;

    private final float pitch;

    public AnimatePlayerModelEvent(Player player, PlayerModel model, float ticksExisted, float pitch) {
        this.player = player;
        this.model = model;
        this.ticksExisted = ticksExisted;
        this.pitch = pitch;
    }

    public Player getEntity() {
        return this.player;
    }

    public PlayerModel getModel() {
        return this.model;
    }

    public float getTicksExisted() {
        return this.ticksExisted;
    }

    public float getPitch() {
        return this.pitch;
    }
}
