package cc.lvjia.wings.server.dreamcatcher;

import cc.lvjia.wings.WingsAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

@SuppressWarnings("null")
public final class InSomniableCapability {
    private InSomniableCapability() {
    }

    public static Optional<InSomniable> getInSomniable(Player player) {
        return Optional.of(WingsAttachments.getInSomniable(player));
    }

    public static void onPlayerClone(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
        getInSomniable(oldPlayer)
                .ifPresent(oldInstance -> getInSomniable(newPlayer)
                        .ifPresent(newInstance -> newInstance.clone(oldInstance)));
    }
}
