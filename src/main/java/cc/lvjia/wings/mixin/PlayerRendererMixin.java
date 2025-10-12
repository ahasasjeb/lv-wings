package cc.lvjia.wings.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import cc.lvjia.wings.server.asm.WingsHooksClient;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修改玩家渲染器，支持翅膀飞行时的渲染状态
 */
@Mixin(AvatarRenderer.class)
public abstract class PlayerRendererMixin {
    /**
     * 跟踪渲染状态，用于翅膀飞行时的渲染调整
     */
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = @At("HEAD"))
    private void wings$trackRenderState(Avatar avatar, AvatarRenderState state, float partialTick, CallbackInfo ci) {
        if (avatar instanceof AbstractClientPlayer player) {
            WingsHooksClient.onExtractPlayerRenderState(player, state);
        }
    }

    /**
     * 应用旋转，用于翅膀飞行时的身体旋转
     */
    @Inject(method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",
            at = @At("RETURN"))
    private void wings$applyRotations(AvatarRenderState state, PoseStack poseStack, float ageInTicks, float rotationYaw, CallbackInfo ci) {
        WingsHooksClient.onApplyPlayerRotations(state, poseStack);
    }
}
