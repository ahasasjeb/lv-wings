package com.toni.wings.server.item;

import com.toni.wings.server.apparatus.FlightApparatus;
import com.toni.wings.server.effect.WingsEffects;
import com.toni.wings.server.flight.Flight;
import com.toni.wings.server.flight.Flights;
import com.toni.wings.server.sound.WingsSounds;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public class WingsBottleItem extends Item {
    private final FlightApparatus wings;

    public WingsBottleItem(Properties properties, FlightApparatus wings) {
        super(properties);
        this.wings = wings;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity living) {
        if (living instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) living;
            CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
            giveWing(player, this.wings);
            world.playSound(null, player.getX(), player.getY(), player.getZ(), WingsSounds.ITEM_ARMOR_EQUIP_WINGS.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        if (living instanceof Player) {
            Player player = (Player) living;
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        if (stack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }
        if (living instanceof Player && !((Player) living).getAbilities().instabuild) {
            ItemStack emptyBottle = new ItemStack(Items.GLASS_BOTTLE);
            Player player = (Player) living;
            if (!player.getInventory().add(emptyBottle)) {
                player.drop(emptyBottle, false);
            }
        }
        return stack;
    }

    public static boolean giveWing(ServerPlayer player, FlightApparatus wings) {
        boolean changed = Flights.get(player).filter(flight -> {
            if (flight.getWing() != wings) {
                flight.setWing(wings, Flight.PlayerSet.ofAll());
                return true;
            }
            return false;
        }).isPresent();
        player.addEffect(new MobEffectInstance(WingsEffects.WINGS.get(), Integer.MAX_VALUE, 0, true, false));
        return changed;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 40;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(world, player, hand);
    }
}
