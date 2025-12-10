package cc.lvjia.wings.server.asm;

import cc.lvjia.wings.util.Access;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.neoforged.neoforge.common.NeoForge;

import java.lang.invoke.MethodHandle;

public final class WingsHooksClient {
    private static final ThreadLocal<AbstractClientPlayer> RENDERING_PLAYER = new ThreadLocal<>();
    private static int selectedItemSlot = 0;

    private WingsHooksClient() {
    }

    public static void onSetPlayerRotationAngles(AvatarRenderState state, PlayerModel model) {
        AbstractClientPlayer player = resolvePlayer(state);
        if (player != null) {
            try {
                NeoForge.EVENT_BUS.post(new AnimatePlayerModelEvent(player, model, state.ageInTicks, state.xRot));
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
        AbstractClientPlayer player = resolvePlayer(state);
        if (player != null) {
            float delta = state.ageInTicks - player.tickCount;
            NeoForge.EVENT_BUS.post(new ApplyPlayerRotationsEvent(player, matrixStack, delta));
        }
    }

    private static AbstractClientPlayer resolvePlayer(AvatarRenderState state) {
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

    public static void onTurn(Entity entity, float deltaYaw) {
        if (entity instanceof LivingEntity living) {
            float theta = Mth.wrapDegrees(living.getYRot() - living.yBodyRot);
            GetLivingHeadLimitEvent ev = GetLivingHeadLimitEvent.create(living);
            NeoForge.EVENT_BUS.post(ev);
            float limit = ev.getHardLimit();
            if (theta < -limit || theta > limit) {
                living.yBodyRot += deltaYaw;
                living.yBodyRotO += deltaYaw;
            }
        }
    }

    public static boolean onCheckRenderEmptyHand(boolean isMainHand, ItemStack itemStackMainHand) {
        return isMainHand || !Holder.OPTIFUCK && !isMap(itemStackMainHand);
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
                NeoForge.EVENT_BUS.post(ev);
                return !ev.isAllowed();
            }
        }
        if (fromEmpty || toEmpty) {
            return fromEmpty != toEmpty;
        }
        boolean hasSlotChange = !isOffHand && selectedItemSlot != (selectedItemSlot = slot);
        return from.getItem().shouldCauseReequipAnimation(from, to, hasSlotChange);
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
        private static final boolean OPTIFUCK;

        static {
            boolean present;
            try {
                Class.forName("optifine.ZipResourceProvider");
                present = true;
            } catch (ClassNotFoundException thankGod) {
                present = false;
            }
            OPTIFUCK = present;
        }
    }
}
