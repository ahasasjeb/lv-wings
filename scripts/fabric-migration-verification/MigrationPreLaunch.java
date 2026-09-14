import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public final class MigrationPreLaunch implements PreLaunchEntrypoint {
    @Override public void onPreLaunch() {
        try {
            String[] targets = {
                "net.minecraft.world.entity.Entity", "net.minecraft.world.entity.LivingEntity",
                "net.minecraft.world.entity.player.Player", "net.minecraft.server.level.ServerPlayer",
                "net.minecraft.server.network.ServerGamePacketListenerImpl", "net.minecraft.client.Camera",
                "net.minecraft.client.model.player.PlayerModel", "net.minecraft.client.renderer.entity.player.AvatarRenderer",
                "net.minecraft.client.player.FirstPersonHandsAndItems",
                "net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer",
                "net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState"
            };
            for (String target : targets) {
                Class.forName(target, false, getClass().getClassLoader());
                System.out.println("MIXIN_OK " + target);
            }
            MigrationProbe.verify();
            System.out.println("MIGRATION_SMOKE_PASSED");
            System.exit(0);
        } catch (Throwable failure) {
            failure.printStackTrace();
            System.exit(1);
        }
    }
}
