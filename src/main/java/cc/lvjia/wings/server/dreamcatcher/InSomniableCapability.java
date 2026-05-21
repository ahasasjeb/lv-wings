package cc.lvjia.wings.server.dreamcatcher;

import cc.lvjia.wings.WingsAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

@SuppressWarnings("null")
public final class InSomniableCapability {
    private InSomniableCapability() {
    }

    public static InSomniable getInSomniable(Player player) {
        return WingsAttachments.getInSomniable(player);
    }

    public static void onPlayerClone(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
        getInSomniable(newPlayer).clone(getInSomniable(oldPlayer));
    }
}
