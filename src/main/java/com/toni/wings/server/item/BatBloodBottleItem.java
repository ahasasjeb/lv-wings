package com.toni.wings.server.item;

import com.toni.wings.server.apparatus.FlightApparatus;
import com.toni.wings.server.effect.WingsEffects;
import com.toni.wings.server.flight.Flights;
import com.toni.wings.server.sound.WingsSounds;
import javax.annotation.Nonnull;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BatBloodBottleItem extends Item {
    public BatBloodBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(@Nonnull ItemStack stack, @Nonnull Level world, @Nonnull LivingEntity living) {
        ItemStack result = super.finishUsingItem(stack, world, living);

    if (!world.isClientSide() && living instanceof ServerPlayer player) {
            if (removeWings(player)) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), WingsSounds.ITEM_ARMOR_EQUIP_WINGS.get(), SoundSource.PLAYERS, 1.0F, 0.8F);
            }
        }

        return result;
    }

    public static boolean removeWings(Player player) {
        return WingsEffects.WINGS.isBound() && player.removeEffect(WingsEffects.WINGS);
    }

    public static boolean removeWings(ServerPlayer player, FlightApparatus wings) {
        boolean changed = Flights.get(player).filter(flight -> flight.getWing() == wings).isPresent();
        return changed && WingsEffects.WINGS.isBound() && player.removeEffect(WingsEffects.WINGS);
    }
}
