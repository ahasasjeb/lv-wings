package cc.lvjia.wings.mixin;

import cc.lvjia.wings.client.hooks.EmptyOffhandRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FirstPersonHandsAndItemsRenderState.class)
public abstract class FirstPersonHandsAndItemsRenderStateMixin implements EmptyOffhandRenderState {
    @Unique
    private boolean wings$renderEmptyOffhand;

    @Override
    public boolean wings$renderEmptyOffhand() {
        return this.wings$renderEmptyOffhand;
    }

    @Override
    public void wings$setRenderEmptyOffhand(boolean render) {
        this.wings$renderEmptyOffhand = render;
    }
}
