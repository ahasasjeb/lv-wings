package cc.lvjia.wings.server.dreamcatcher;

import cc.lvjia.wings.server.item.WingsItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;

public final class InSomniable {
    private State state;

    public InSomniable() {
        this(new SearchState());
    }

    private InSomniable(State state) {
        this.state = state;
    }

    public void onPlay(Level world, Player player, BlockPos pos, int note) {
        this.state = this.state.onPlay(world, player, pos, note);
    }

    public void clone(InSomniable other) {
        this.state = other.state.copy();
    }

    private interface State {
        State onPlay(Level world, Player player, BlockPos pos, int note);

        State copy();

        void ifSearching(IntConsumer consumer);
    }

    private static final class SearchState implements State {
        private final int[] mask = {
                0xBFBE,
                0xFFFD,
                0xFFFF,
                0xCD43,
                0xFFFF,
                0x7EFF,
                0xFFFF,
                0xF7FF,
                0xFBFF
        };

        private final List<@NonNull String> members = List.of(
                "wings.dreamcatcher.jiu",
                "wings.dreamcatcher.sua",
                "wings.dreamcatcher.siyeon",
                "wings.dreamcatcher.handong",
                "wings.dreamcatcher.yoohyeon",
                "wings.dreamcatcher.dami",
                "wings.dreamcatcher.gahyeon");

        private int state;

        private SearchState() {
            this(0x1FFFE);
        }

        private SearchState(int state) {
            this.state = state;
        }

        @Override
        public State onPlay(Level world, Player player, BlockPos pos, int note) {
            if (note >= 6 && note <= 14 && ((this.state = (this.state | this.mask[note - 6]) << 1) & 0x20000) == 0) {
                @NonNull ItemLike rewardItem = Objects.requireNonNull(WingsItems.ANGEL_WINGS_BOTTLE.get(),
                        "angel wings bottle");
                @NonNull String member = this.members.get(world.getRandom().nextInt(this.members.size()));
                ItemStack stack = new ItemStack(rewardItem);
                stack.<Component>set(customNameComponent(), Component.translatable(member));
                ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5D, pos.getY() + 1.25D, pos.getZ() + 0.5D, stack);
                entity.setDefaultPickUpDelay();
                world.addFreshEntity(entity);
                return InSomniacState.INSTANCE;
            }
            return this;
        }

        @Override
        public State copy() {
            return new SearchState(this.state);
        }

        @Override
        public void ifSearching(IntConsumer consumer) {
            consumer.accept(this.state);
        }
    }

    private static DataComponentType<Component> customNameComponent() {
        return DataComponents.CUSTOM_NAME;
    }

    private static final class InSomniacState implements State {
        private static final State INSTANCE = new InSomniacState();

        @Override
        public State onPlay(Level world, Player player, BlockPos pos, int note) {
            return this;
        }

        @Override
        public State copy() {
            return this;
        }

        @Override
        public void ifSearching(IntConsumer consumer) {
        }
    }

    public static final class Serializer {
        private static final String SEARCH_STATE = "SearchState";

        public void serialize(InSomniable instance, ValueOutput output) {
            instance.state.ifSearching(state -> output.putInt(SEARCH_STATE, state));
        }

        public InSomniable deserialize(ValueInput input) {
            State state = input.getInt(SEARCH_STATE)
                    .map(value -> (State) new SearchState(value))
                    .orElse(InSomniacState.INSTANCE);
            return new InSomniable(state);
        }
    }
}
