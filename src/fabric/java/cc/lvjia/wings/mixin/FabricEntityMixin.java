package cc.lvjia.wings.mixin;

import cc.lvjia.wings.server.flight.FlightEventSupport;
import cc.lvjia.wings.server.flight.Flights;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class FabricEntityMixin {
    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z", at = @At("HEAD"), cancellable = true)
    private void wings$preventMountingWhileFlying(Entity vehicle, boolean force, boolean emitGameEvent,
                                                 CallbackInfoReturnable<Boolean> cir) {
        if (FlightEventSupport.isFlyingPlayer((Entity) (Object) this, Flights::get)) {
            cir.setReturnValue(false);
        }
    }
}
