package cc.lvjia.wings.server.item;

import cc.lvjia.wings.server.apparatus.FlightApparatus;
import cc.lvjia.wings.server.sound.WingsSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("null")
public class BatBloodBottleItem extends Item {
    public BatBloodBottleItem(Properties properties) {
        super(properties);
    }

    public static boolean removeWings(Player player) {
        return WingsBottleActions.removeWings(player);
    }

    public static boolean removeWings(ServerPlayer player, FlightApparatus wings) {
        return WingsBottleActions.removeWings(player, wings);
    }

    @Override
    public @NonNull ItemStack finishUsingItem(@NonNull ItemStack stack, @NonNull Level world,
            @NonNull LivingEntity living) {
        ItemStack result = super.finishUsingItem(stack, world, living);

        if (!world.isClientSide() && living instanceof ServerPlayer player) {
            if (removeWings(player)) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        WingsSounds.ITEM_ARMOR_EQUIP_WINGS.get(), SoundSource.PLAYERS, 1.0F, 0.8F);
            }
        }

        return result;
    }
}
