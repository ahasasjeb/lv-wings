package com.toni.wings.server.dreamcatcher;

import com.toni.wings.WingsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WingsMod.ID)
public final class InSomniableEventHandler {
    private InSomniableEventHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer && !player.isCreative()) {
            Level world = event.getLevel();
            BlockPos pos = event.getPos();
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (block == Blocks.NOTE_BLOCK && world.isEmptyBlock(pos.above()) &&
                world.mayInteract(player, pos) &&
                !player.blockActionRestricted(world, pos, ((ServerPlayer) player).gameMode.getGameModeForPlayer())
            ) {
                InSomniableCapability.getInSomniable(player).ifPresent(inSomniable ->
                    inSomniable.onPlay(world, player, pos, state.getValue(NoteBlock.NOTE))
                );
            }
        }
    }
}
