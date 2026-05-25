package cc.lvjia.wings.server.dreamcatcher;

import cc.lvjia.wings.WingsAttachments;
import cc.lvjia.wings.WingsMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = WingsMod.ID)
public final class InSomniableCapability {
    public static final EntityCapability<InSomniable, Void> INSOMNIABLE_CAPABILITY =
            EntityCapability.createVoid(WingsMod.locate("insomniable"), InSomniable.class);

    private InSomniableCapability() {
    }

    public static InSomniable getInSomniable(Player player) {
        return player.getData(WingsAttachments.INSOMNIABLE.get());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        getInSomniable(event.getEntity()).clone(getInSomniable(event.getOriginal()));
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(INSOMNIABLE_CAPABILITY, EntityType.PLAYER, (player, ctx) ->
                player.getData(WingsAttachments.INSOMNIABLE.get())
        );
    }
}
