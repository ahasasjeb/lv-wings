package cc.lvjia.wings.util;

import com.google.common.collect.ImmutableListMultimap;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

@SuppressWarnings("null")
public final class KeyInputListener {
    private final @NonNull ImmutableListMultimap<@NonNull KeyMapping, @NonNull Runnable> bindings;

    private KeyInputListener(@NonNull ImmutableListMultimap<@NonNull KeyMapping, @NonNull Runnable> bindings) {
        this.bindings = Objects.requireNonNull(bindings, "bindings");
    }

    public static @NonNull Builder builder() {
        return new BuilderRoot();
    }

    public void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> this.bindings.asMap().entrySet().stream()
                .filter(e -> e.getKey().consumeClick())
                .flatMap(e -> e.getValue().stream())
                .forEach(Runnable::run));
    }

    public interface Builder {
        @NonNull CategoryBuilder category(KeyMapping.Category category);

        @NonNull KeyInputListener build();
    }

    public interface CategoryBuilder extends Builder {
        @NonNull BindingBuilder key(@NonNull String desc, int keyCode);
    }

    public static final class BuilderRoot implements Builder {
        private final ImmutableListMultimap.Builder<@NonNull KeyMapping, @NonNull Runnable> bindings =
                ImmutableListMultimap.builder();

        @Override
        public @NonNull CategoryBuilder category(KeyMapping.Category category) {
            return new CategoryBuilderRoot(this, category);
        }

        @Override
        public @NonNull KeyInputListener build() {
            return new KeyInputListener(this.bindings.build());
        }
    }

    private static abstract class ChildBuilder<P extends Builder> implements Builder {
        final @NonNull P parent;

        private ChildBuilder(@NonNull P parent) {
            this.parent = Objects.requireNonNull(parent, "parent");
        }

        @Override
        public final @NonNull CategoryBuilder category(KeyMapping.Category category) {
            return this.parent.category(category);
        }

        @Override
        public final @NonNull KeyInputListener build() {
            return this.parent.build();
        }
    }

    private static final class CategoryBuilderRoot extends ChildBuilder<BuilderRoot> implements CategoryBuilder {
        private final KeyMapping.Category category;

        private CategoryBuilderRoot(@NonNull BuilderRoot delegate, KeyMapping.Category category) {
            super(delegate);
            this.category = Objects.requireNonNull(category, "category");
        }

        @Override
        public @NonNull BindingBuilder key(@NonNull String desc, int keyCode) {
            KeyMapping binding = KeyMappingHelper
                    .registerKeyMapping(new KeyMapping(Objects.requireNonNull(desc, "description"),
                            InputConstants.Type.KEYSYM, keyCode, this.category));
            return new BindingBuilder(this, binding);
        }
    }

    public static final class BindingBuilder extends ChildBuilder<CategoryBuilderRoot> implements CategoryBuilder {
        private final @NonNull KeyMapping binding;

        private BindingBuilder(@NonNull CategoryBuilderRoot delegate, @NonNull KeyMapping binding) {
            super(delegate);
            this.binding = Objects.requireNonNull(binding, "binding");
        }

        public @NonNull BindingBuilder onPress(@NonNull Runnable runnable) {
            this.parent.parent.bindings.put(this.binding, Objects.requireNonNull(runnable, "runnable"));
            return this;
        }

        @Override
        public @NonNull BindingBuilder key(@NonNull String desc, int keyCode) {
            return this.parent.key(desc, keyCode);
        }
    }
}
