package cc.lvjia.wings.client.hooks;

import cc.lvjia.wings.client.FabricClientEventHandler;
import cc.lvjia.wings.client.asm.AnimatePlayerModelEvent;
import cc.lvjia.wings.client.asm.ApplyPlayerRotationsEvent;
import cc.lvjia.wings.client.asm.GetCameraEyeHeightEvent;
import cc.lvjia.wings.client.event.EmptyOffHandPresentEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public final class WingsHooksClient {
    private WingsHooksClient() {
    }

    public static float onGetCameraEyeHeight(Entity entity, float eyeHeight) {
        GetCameraEyeHeightEvent event = GetCameraEyeHeightEvent.create(entity, eyeHeight);
        FabricClientEventHandler.onGetCameraEyeHeight(event);
        return event.getValue();
    }

    public static float onComputeCameraRoll(float delta) {
        return FabricClientEventHandler.computeCameraRoll(delta);
    }

    public static void onSetPlayerRotationAngles(AvatarRenderState state, PlayerModel model) {
        ClientRenderHookSupport.withResolvedPlayer(state, player -> FabricClientEventHandler
                .onAnimatePlayerModel(new AnimatePlayerModelEvent(player, model, state.ageInTicks, state.xRot)));
    }

    public static void onExtractPlayerRenderState(AbstractClientPlayer player, AvatarRenderState state) {
        ClientRenderHookSupport.onExtractPlayerRenderState(player);
    }

    public static void onApplyPlayerRotations(AvatarRenderState state, PoseStack matrixStack) {
        ClientRenderHookSupport.withResolvedPlayer(state, player -> {
            float delta = state.ageInTicks - player.tickCount;
            FabricClientEventHandler.onApplyRotations(new ApplyPlayerRotationsEvent(player, matrixStack, delta));
        });
    }

    public static boolean onCheckRenderEmptyHand(boolean isMainHand, AbstractClientPlayer player, InteractionHand hand,
                                                 ItemStack itemStack, ItemStack itemStackMainHand) {
        return ItemInHandHookSupport.canRenderEmptyOffhand(isMainHand, player, hand, itemStack, itemStackMainHand);
    }

    public static boolean canRenderEmptyOffhand(ItemStack offHandItem, ItemStack mainHandItem) {
        return ItemInHandHookSupport.canRenderEmptyOffhand(offHandItem, mainHandItem);
    }

    public static boolean onCheckDoReequipAnimation(ItemStack from, ItemStack to, int slot) {
        return ItemInHandHookSupport.onCheckDoReequipAnimation(
                from,
                to,
                slot,
                player -> {
                    EmptyOffHandPresentEvent ev = new EmptyOffHandPresentEvent(player);
                    FabricClientEventHandler.onEmptyOffHandPresentEvent(ev);
                    return ev.isAllowed();
                },
                (normalFrom, normalTo, hasSlotChange) -> hasSlotChange || !ItemStack.matches(normalFrom, normalTo));
    }
}
