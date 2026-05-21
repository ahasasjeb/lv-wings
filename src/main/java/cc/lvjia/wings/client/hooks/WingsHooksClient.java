package cc.lvjia.wings.client.hooks;

import cc.lvjia.wings.client.ClientEventHandler;
import cc.lvjia.wings.client.asm.AnimatePlayerModelEvent;
import cc.lvjia.wings.client.asm.ApplyPlayerRotationsEvent;
import cc.lvjia.wings.client.asm.GetCameraEyeHeightEvent;
import cc.lvjia.wings.client.event.EmptyOffHandPresentEvent;
import cc.lvjia.wings.util.Access;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;

public final class WingsHooksClient {
    private static final ThreadLocal<AbstractClientPlayer> RENDERING_PLAYER = new ThreadLocal<>();
    private static int selectedItemSlot = 0;

    private WingsHooksClient() {
    }

    public static float onGetCameraEyeHeight(Entity entity, float eyeHeight) {
        GetCameraEyeHeightEvent event = GetCameraEyeHeightEvent.create(entity, eyeHeight);
        ClientEventHandler.onGetCameraEyeHeight(event);
        return event.getValue();
    }

    public static float onComputeCameraRoll(float delta) {
        return ClientEventHandler.computeCameraRoll(delta);
    }

    public static void onSetPlayerRotationAngles(AvatarRenderState state, PlayerModel model) {
        AbstractClientPlayer player = resolvePlayer(state);
        if (player != null) {
            try {
                ClientEventHandler
                        .onAnimatePlayerModel(new AnimatePlayerModelEvent(player, model, state.ageInTicks, state.xRot));
            } finally {
                RENDERING_PLAYER.remove();
            }
        } else {
            RENDERING_PLAYER.remove();
        }
    }

    public static void onExtractPlayerRenderState(AbstractClientPlayer player, AvatarRenderState state) {
        RENDERING_PLAYER.set(player);
    }

    public static void onApplyPlayerRotations(AvatarRenderState state, PoseStack matrixStack) {
        try {
            AbstractClientPlayer player = resolvePlayer(state);
            if (player != null) {
                float delta = state.ageInTicks - player.tickCount;
                ClientEventHandler.onApplyRotations(new ApplyPlayerRotationsEvent(player, matrixStack, delta));
            }
        } finally {
            RENDERING_PLAYER.remove();
        }
    }

    private static @Nullable AbstractClientPlayer resolvePlayer(@Nullable AvatarRenderState state) {
        AbstractClientPlayer player = RENDERING_PLAYER.get();
        if (player != null) {
            return player;
        }
        if (state != null) {
            Minecraft minecraft = Minecraft.getInstance();
            ClientLevel level = minecraft.level;
            if (level != null) {
                Entity entity = level.getEntity(state.id);
                if (entity instanceof AbstractClientPlayer found) {
                    return found;
                }
            }
        }
        return null;
    }

    public static boolean onCheckRenderEmptyHand(boolean isMainHand, AbstractClientPlayer player, InteractionHand hand,
            ItemStack itemStack, ItemStack itemStackMainHand) {
        if (isMainHand) {
            return true;
        }
        if (!(player instanceof LocalPlayer) || hand != InteractionHand.OFF_HAND) {
            return false;
        }
        return canRenderEmptyOffhand(itemStack, itemStackMainHand);
    }

    public static boolean shouldRenderEmptyOffhand(LocalPlayer player, ItemStack offHandItem, ItemStack mainHandItem) {
        if (!canRenderEmptyOffhand(offHandItem, mainHandItem)) {
            return false;
        }
        EmptyOffHandPresentEvent event = new EmptyOffHandPresentEvent(player);
        ClientEventHandler.onEmptyOffHandPresentEvent(event);
        return event.isAllowed();
    }

    public static boolean canRenderEmptyOffhand(ItemStack offHandItem, ItemStack mainHandItem) {
        return offHandItem.isEmpty() && !Holder.OPTIFINE_PRESENT && !isMap(mainHandItem);
    }

    public static boolean onCheckDoReequipAnimation(ItemStack from, ItemStack to, int slot) {
        boolean fromEmpty = from.isEmpty();
        boolean toEmpty = to.isEmpty();
        boolean isOffHand = slot == -1;
        if (toEmpty && isOffHand) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) {
                return true;
            }
            boolean fromMap = isMap(GetItemStackMainHand.invoke(mc.gameRenderer.itemInHandRenderer));
            boolean toMap = isMap(player.getMainHandItem());
            if (fromMap || toMap) {
                return fromMap != toMap;
            }
            if (fromEmpty) {
                EmptyOffHandPresentEvent ev = new EmptyOffHandPresentEvent(player);
                ClientEventHandler.onEmptyOffHandPresentEvent(ev);
                return !ev.isAllowed();
            }
        }
        if (fromEmpty || toEmpty) {
            return fromEmpty != toEmpty;
        }
        boolean hasSlotChange = !isOffHand && selectedItemSlot != (selectedItemSlot = slot);
        return hasSlotChange || !ItemStack.matches(from, to);
    }

    private static boolean isMap(ItemStack stack) {
        return stack.getItem() instanceof MapItem;
    }

    private static final class GetItemStackMainHand {
        private static final MethodHandle MH = Access.getter(ItemInHandRenderer.class)
                .name("f_109300_", "mainHandItem")
                .type(ItemStack.class);

        private GetItemStackMainHand() {
        }

        private static ItemStack invoke(ItemInHandRenderer instance) {
            try {
                return (ItemStack) MH.invokeExact(instance);
            } catch (Throwable t) {
                throw Access.rethrow(t);
            }
        }
    }

    private static final class Holder {
        private static final boolean OPTIFINE_PRESENT;

        static {
            boolean present;
            try {
                Class.forName("optifine.ZipResourceProvider");
                present = true;
            } catch (ClassNotFoundException e) {
                present = false;
            }
            OPTIFINE_PRESENT = present;
        }
    }
}
