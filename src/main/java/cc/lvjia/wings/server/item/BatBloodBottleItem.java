package cc.lvjia.wings.server.item;

import cc.lvjia.wings.server.apparatus.FlightApparatus;
import cc.lvjia.wings.server.effect.WingsEffects;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.FlightAnimationState;
import cc.lvjia.wings.server.flight.FlightSpeedAntiCheat;
import cc.lvjia.wings.server.flight.Flights;
import cc.lvjia.wings.server.sound.WingsSounds;
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

    public static boolean removeWings(Player player) {
        boolean removed = WingsEffects.WINGS.isBound() && player.removeEffect(WingsEffects.WINGS);
        if (removed) {
            clearFlightState(player);
        }
        return removed;
    }

    public static boolean removeWings(ServerPlayer player, FlightApparatus wings) {
        boolean changed = Flights.get(player).filter(flight -> flight.getWing() == wings).isPresent();
        if (!changed || !WingsEffects.WINGS.isBound() || !player.removeEffect(WingsEffects.WINGS)) {
            return false;
        }
        clearFlightState(player);
        return true;
    }

    private static void clearFlightState(Player player) {
        Flights.get(player).ifPresent(flight -> {
            Flight.PlayerSet players = player.level().isClientSide() ? Flight.PlayerSet.empty() : Flight.PlayerSet.ofAll();
            flight.setIsFlying(false, players);
            flight.setWing(FlightApparatus.NONE, players);
            flight.setTimeFlying(0);
            flight.setAnimationState(FlightAnimationState.IDLE);
        });
        if (player instanceof ServerPlayer serverPlayer) {
            FlightSpeedAntiCheat.clear(serverPlayer);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity living) {
        ItemStack result = super.finishUsingItem(stack, world, living);

        if (!world.isClientSide() && living instanceof ServerPlayer player) {
            if (removeWings(player)) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), WingsSounds.ITEM_ARMOR_EQUIP_WINGS.get(), SoundSource.PLAYERS, 1.0F, 0.8F);
            }
        }

        return result;
    }
}
