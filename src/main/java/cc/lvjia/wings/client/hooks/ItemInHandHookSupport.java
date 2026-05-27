package cc.lvjia.wings.client.hooks;

import cc.lvjia.wings.util.Access;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;

import org.jspecify.annotations.NonNull;

import java.lang.invoke.MethodHandle;

public final class ItemInHandHookSupport {
    private static int selectedItemSlot = 0;

    private ItemInHandHookSupport() {
    }

    public static boolean canRenderEmptyOffhand(boolean isMainHand, AbstractClientPlayer player, InteractionHand hand,
                                                ItemStack offHandItem, ItemStack mainHandItem) {
        if (isMainHand) {
            return true;
        }
        if (!(player instanceof LocalPlayer) || hand != InteractionHand.OFF_HAND) {
            return false;
        }
        return canRenderEmptyOffhand(offHandItem, mainHandItem);
    }

    public static boolean canRenderEmptyOffhand(ItemStack offHandItem, ItemStack mainHandItem) {
        return offHandItem.isEmpty() && !OptiFineHolder.PRESENT && !isMap(mainHandItem);
    }

    public static boolean canRenderEmptyOffhand(ItemStack mainHandItem) {
        return !OptiFineHolder.PRESENT && !isMap(mainHandItem);
    }

    public static boolean onCheckDoReequipAnimation(ItemStack from, ItemStack to, int slot,
                                                    EmptyOffhandAllowance emptyOffhandAllowance, NormalReequipRule normalReequipRule) {
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
                return !emptyOffhandAllowance.isAllowed(player);
            }
        }
        if (fromEmpty || toEmpty) {
            return fromEmpty != toEmpty;
        }
        boolean hasSlotChange = !isOffHand && selectedItemSlot != (selectedItemSlot = slot);
        return normalReequipRule.shouldReequip(from, to, hasSlotChange);
    }

    private static boolean isMap(ItemStack stack) {
        return stack.getItem() instanceof MapItem;
    }

    @FunctionalInterface
    public interface EmptyOffhandAllowance {
        boolean isAllowed(LocalPlayer player);
    }

    @FunctionalInterface
    public interface NormalReequipRule {
        boolean shouldReequip(@NonNull ItemStack from, @NonNull ItemStack to, boolean hasSlotChange);
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

    private static final class OptiFineHolder {
        private static final boolean PRESENT;

        static {
            boolean present;
            try {
                Class.forName("optifine.ZipResourceProvider");
                present = true;
            } catch (ClassNotFoundException e) {
                present = false;
            }
            PRESENT = present;
        }
    }
}
