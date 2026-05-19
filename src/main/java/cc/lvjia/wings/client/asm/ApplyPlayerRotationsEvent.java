package cc.lvjia.wings.client.asm;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.player.Player;

public class ApplyPlayerRotationsEvent {
    private final Player player;
    private final PoseStack matrixStack;

    private final float delta;

    public ApplyPlayerRotationsEvent(Player player, PoseStack matrixStack, float delta) {
        this.player = player;
        this.matrixStack = matrixStack;
        this.delta = delta;
    }

    public Player getEntity() {
        return this.player;
    }

    public PoseStack getMatrixStack() {
        return this.matrixStack;
    }

    public float getDelta() {
        return this.delta;
    }
}
