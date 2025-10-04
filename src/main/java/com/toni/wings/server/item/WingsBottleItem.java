package com.toni.wings.server.item;

import com.toni.wings.server.apparatus.FlightApparatus;
import com.toni.wings.server.effect.WingsEffects;
import com.toni.wings.server.flight.Flight;
import com.toni.wings.server.flight.Flights;
import com.toni.wings.server.sound.WingsSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import javax.annotation.Nonnull;

public class WingsBottleItem extends Item {
    private final FlightApparatus wings;

    public WingsBottleItem(Properties properties, FlightApparatus wings) {
        super(properties);
        this.wings = wings;
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack finishUsingItem(@Nonnull ItemStack stack, @Nonnull Level world, @Nonnull LivingEntity living) {
        ItemStack result = super.finishUsingItem(stack, world, living);

        if (!world.isClientSide && living instanceof ServerPlayer player) {
            giveWing(player, this.wings);
            world.playSound(null, player.getX(), player.getY(), player.getZ(), WingsSounds.ITEM_ARMOR_EQUIP_WINGS.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        return result;
    }

    public static boolean giveWing(ServerPlayer player, FlightApparatus wings) {
        boolean changed = Flights.get(player).filter(flight -> {
            if (flight.getWing() != wings) {
                flight.setWing(wings, Flight.PlayerSet.ofAll());
                return true;
            }
            return false;
        }).isPresent();
        WingsEffects.WINGS.getHolder().ifPresent(holder -> player.addEffect(new MobEffectInstance(holder, MobEffectInstance.INFINITE_DURATION, 0, true, false)));
        return changed;
    }

}
