package cc.lvjia.wings.client.hooks;

import cc.lvjia.wings.client.asm.AnimatePlayerModelEvent;
import cc.lvjia.wings.client.asm.ApplyPlayerRotationsEvent;
import cc.lvjia.wings.client.asm.GetCameraEyeHeightEvent;
import cc.lvjia.wings.client.event.EmptyOffHandPresentEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

public final class WingsHooksClient {
    private WingsHooksClient() {
    }

    public static float onGetCameraEyeHeight(Entity entity, float eyeHeight) {
        GetCameraEyeHeightEvent ev = GetCameraEyeHeightEvent.create(entity, eyeHeight);
        NeoForge.EVENT_BUS.post(ev);
        return ev.getValue();
    }

    public static void onSetPlayerRotationAngles(AvatarRenderState state, PlayerModel model) {
        ClientRenderHookSupport.withResolvedPlayer(
                state,
                player -> NeoForge.EVENT_BUS.post(new AnimatePlayerModelEvent(player, model, state.ageInTicks,
                        state.xRot)));
    }

    public static void onExtractPlayerRenderState(AbstractClientPlayer player, AvatarRenderState state) {
        ClientRenderHookSupport.onExtractPlayerRenderState(player);
    }

    public static void onApplyPlayerRotations(AvatarRenderState state, PoseStack matrixStack) {
        ClientRenderHookSupport.withResolvedPlayer(state, player -> {
            float delta = state.ageInTicks - player.tickCount;
            NeoForge.EVENT_BUS.post(new ApplyPlayerRotationsEvent(player, matrixStack, delta));
        });
    }

    public static boolean onCheckRenderEmptyHand(boolean isMainHand, ItemStack itemStackMainHand) {
        return isMainHand || ItemInHandHookSupport.canRenderEmptyOffhand(itemStackMainHand);
    }

    public static boolean onCheckDoReequipAnimation(ItemStack from, ItemStack to, int slot) {
        return ItemInHandHookSupport.onCheckDoReequipAnimation(
                from,
                to,
                slot,
                player -> {
                    EmptyOffHandPresentEvent ev = new EmptyOffHandPresentEvent(player);
                    NeoForge.EVENT_BUS.post(ev);
                    return ev.isAllowed();
                },
                (normalFrom, normalTo, hasSlotChange) ->
                        normalFrom.getItem().shouldCauseReequipAnimation(normalFrom, normalTo, hasSlotChange));
    }
}
