package cc.lvjia.wings.server.worldgen;

import cc.lvjia.wings.server.config.VeinSettings;
import cc.lvjia.wings.server.config.WingsOreConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class OreGenerationHandler {
    private static final Block FAIRY_DUST_BLOCK = Blocks.GLOWSTONE;
    private static final Block AMETHYST_BLOCK = Blocks.AMETHYST_BLOCK;
    private static final long FAIRY_DUST_SALT = 0x57A1EEDL;
    private static final long AMETHYST_SALT = 0xA6E7715L;
    private static final int MAX_CHUNKS_PER_TICK = 2;
    private static final int MAX_BLOCK_CHANGES_PER_CHUNK = 128;
    private static final int ORE_PLACEMENT_FLAGS =
            Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS;

    private static final Map<ResourceKey<Level>, Deque<ChunkPos>> PENDING_CHUNKS = new HashMap<>();
    private static final Map<ResourceKey<Level>, Set<Long>> PENDING_CHUNK_KEYS = new HashMap<>();

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
        enqueue(level, chunkPos);
    }

    public static void onServerTick(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            processQueue(level, MAX_CHUNKS_PER_TICK);
        }
    }

    private static void enqueue(ServerLevel level, ChunkPos chunkPos) {
        ResourceKey<Level> dimension = level.dimension();
        Set<Long> queuedKeys = PENDING_CHUNK_KEYS.computeIfAbsent(dimension, ignored -> new HashSet<>());
        long packed = chunkPos.pack();
        if (!queuedKeys.add(packed)) {
            return;
        }

        PENDING_CHUNKS.computeIfAbsent(dimension, ignored -> new ArrayDeque<>()).addLast(chunkPos);
    }

    private static void processQueue(ServerLevel level, int maxChunks) {
        ResourceKey<Level> dimension = level.dimension();
        Deque<ChunkPos> queue = PENDING_CHUNKS.get(dimension);
        if (queue == null || queue.isEmpty()) {
            return;
        }

        Set<Long> queuedKeys = PENDING_CHUNK_KEYS.get(dimension);
        int processed = 0;
        while (processed < maxChunks) {
            ChunkPos chunkPos = queue.pollFirst();
            if (chunkPos == null) {
                break;
            }

            if (queuedKeys != null) {
                queuedKeys.remove(chunkPos.pack());
            }

            generateInChunk(level, chunkPos);
            processed++;
        }

        if (queue.isEmpty()) {
            PENDING_CHUNKS.remove(dimension);
            PENDING_CHUNK_KEYS.remove(dimension);
        }
    }

    private static void generateInChunk(ServerLevel level, ChunkPos chunkPos) {
        if (!level.hasChunk(chunkPos.x(), chunkPos.z())) {
            return;
        }

        int[] remainingBudget = new int[]{MAX_BLOCK_CHANGES_PER_CHUNK};
        generateOre(level, chunkPos, WingsOreConfig.FAIRY_DUST, FAIRY_DUST_BLOCK.defaultBlockState(), FAIRY_DUST_SALT, remainingBudget);
        generateOre(level, chunkPos, WingsOreConfig.AMETHYST, AMETHYST_BLOCK.defaultBlockState(), AMETHYST_SALT, remainingBudget);
    }

    private static void generateOre(ServerLevel level, ChunkPos chunkPos, VeinSettings settings, BlockState oreState, long salt,
                                    int[] remainingBudget) {
        if (remainingBudget[0] <= 0) {
            return;
        }

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
            if (remainingBudget[0] <= 0) {
                break;
            }

            int startX = minX + random.nextInt(16);
            int startY = minHeight + random.nextInt(heightSpan);
            int startZ = minZ + random.nextInt(16);
            placeVein(level, random, startX, startY, startZ, size, oreState, minX, maxX, minZ, maxZ, remainingBudget);
        }
    }

    private static void placeVein(ServerLevel level, RandomSource random, int startX, int startY, int startZ, int size,
                                  BlockState oreState, int minX, int maxX, int minZ, int maxZ, int[] remainingBudget) {
        int x = startX;
        int y = startY;
        int z = startZ;
        int minY = level.getMinY();
        int maxY = level.getMaxY();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int i = 0; i < size && remainingBudget[0] > 0; i++) {
            x = clamp(x, minX, maxX);
            y = clamp(y, minY, maxY);
            z = clamp(z, minZ, maxZ);
            cursor.set(x, y, z);

            if (isReplaceable(level.getBlockState(cursor))) {
                if (level.setBlock(cursor, oreState, ORE_PLACEMENT_FLAGS)) {
                    remainingBudget[0]--;
                }
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
