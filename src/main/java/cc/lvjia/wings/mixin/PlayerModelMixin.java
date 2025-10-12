package cc.lvjia.wings.mixin;

import cc.lvjia.wings.server.asm.WingsHooksClient;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修改玩家模型动画，添加翅膀飞行动画
 */
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
    /**
     * 设置玩家模型旋转角度，用于翅膀动画
     */
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("RETURN"))
    private void wings$animatePlayerModel(AvatarRenderState state, CallbackInfo ci) {
        WingsHooksClient.onSetPlayerRotationAngles(state, (PlayerModel) (Object) this);
    }
}
