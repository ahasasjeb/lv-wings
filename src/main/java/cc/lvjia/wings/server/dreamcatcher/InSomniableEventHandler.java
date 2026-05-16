package cc.lvjia.wings.server.dreamcatcher;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class InSomniableEventHandler {
    private InSomniableEventHandler() {
    }

    public static void register() {
        AttackBlockCallback.EVENT.register((player, level, hand, pos, side) -> {
            onLeftClickBlock(player, level, pos);
            return InteractionResult.PASS;
        });
    }

    public static void onLeftClickBlock(Player player, Level world, BlockPos pos) {
        if (player instanceof ServerPlayer && !player.isCreative()) {
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
