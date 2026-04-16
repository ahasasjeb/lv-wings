package cc.lvjia.wings.server.worldgen;

import cc.lvjia.wings.server.config.VeinSettings;
import cc.lvjia.wings.server.config.WingsOreConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.ChunkEvent;

public final class OreGenerationHandler {
    private static final Block FAIRY_DUST_BLOCK = Blocks.GLOWSTONE;
    private static final Block AMETHYST_BLOCK = Blocks.AMETHYST_BLOCK;
    private static final long FAIRY_DUST_SALT = 0x57A1EEDL;
    private static final long AMETHYST_SALT = 0xA6E7715L;

    private OreGenerationHandler() {
    }

    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!event.isNewChunk() || level.dimension() != Level.OVERWORLD) {
            return;
        }

        ChunkPos chunkPos = event.getChunk().getPos();
        // NeoForge marks this event before FULL status; world edits are deferred to avoid deadlocks.
        level.getServer().execute(() -> generateInChunk(level, chunkPos));
    }

    private static void generateInChunk(ServerLevel level, ChunkPos chunkPos) {
        if (!level.hasChunk(chunkPos.x(), chunkPos.z())) {
            return;
        }

        generateOre(level, chunkPos, WingsOreConfig.FAIRY_DUST, FAIRY_DUST_BLOCK.defaultBlockState(), FAIRY_DUST_SALT);
        generateOre(level, chunkPos, WingsOreConfig.AMETHYST, AMETHYST_BLOCK.defaultBlockState(), AMETHYST_SALT);
    }

    private static void generateOre(ServerLevel level, ChunkPos chunkPos, VeinSettings settings, BlockState oreState, long salt) {
        int count = settings.getCount();
        int size = settings.getSize();
        int minHeight = Math.max(settings.getMinHeight(), level.getMinY());
        int maxHeight = Math.min(settings.getMaxHeight(), level.getMaxY());

        if (count <= 0 || size <= 0 || maxHeight < minHeight) {
            return;
        }

        int heightSpan = maxHeight - minHeight + 1;
        int minX = chunkPos.getMinBlockX();
        int maxX = chunkPos.getMaxBlockX();
        int minZ = chunkPos.getMinBlockZ();
        int maxZ = chunkPos.getMaxBlockZ();
        RandomSource random = RandomSource.create(level.getSeed() ^ chunkPos.pack() ^ salt);

        for (int i = 0; i < count; i++) {
            int startX = minX + random.nextInt(16);
            int startY = minHeight + random.nextInt(heightSpan);
            int startZ = minZ + random.nextInt(16);
            placeVein(level, random, startX, startY, startZ, size, oreState, minX, maxX, minZ, maxZ);
        }
    }

    private static void placeVein(ServerLevel level, RandomSource random, int startX, int startY, int startZ, int size,
                                  BlockState oreState, int minX, int maxX, int minZ, int maxZ) {
        int x = startX;
        int y = startY;
        int z = startZ;
        int minY = level.getMinY();
        int maxY = level.getMaxY();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int i = 0; i < size; i++) {
            x = clamp(x, minX, maxX);
            y = clamp(y, minY, maxY);
            z = clamp(z, minZ, maxZ);
            cursor.set(x, y, z);

            if (isReplaceable(level.getBlockState(cursor))) {
                level.setBlock(cursor, oreState, Block.UPDATE_CLIENTS);
            }

            x += random.nextInt(3) - 1;
            y += random.nextInt(3) - 1;
            z += random.nextInt(3) - 1;
        }
    }

    private static boolean isReplaceable(BlockState state) {
        return state.is(BlockTags.STONE_ORE_REPLACEABLES) || state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
