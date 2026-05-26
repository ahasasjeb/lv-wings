package cc.lvjia.wings.client.hooks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public final class ClientRenderHookSupport {
    private static final ThreadLocal<AbstractClientPlayer> RENDERING_PLAYER = new ThreadLocal<>();

    private ClientRenderHookSupport() {
    }

    public static void onExtractPlayerRenderState(AbstractClientPlayer player) {
        RENDERING_PLAYER.set(player);
    }

    public static void withResolvedPlayer(@Nullable AvatarRenderState state, Consumer<AbstractClientPlayer> action) {
        try {
            AbstractClientPlayer player = resolvePlayer(state);
            if (player != null) {
                action.accept(player);
            }
        } finally {
            RENDERING_PLAYER.remove();
        }
    }

    public static @Nullable AbstractClientPlayer resolvePlayer(@Nullable AvatarRenderState state) {
        if (state != null) {
            Minecraft minecraft = Minecraft.getInstance();
            ClientLevel level = minecraft.level;
            if (level != null) {
                Entity entity = level.getEntity(state.id);
                if (entity instanceof AbstractClientPlayer found) {
                    return found;
                }
            }
        }
        return RENDERING_PLAYER.get();
    }
}
