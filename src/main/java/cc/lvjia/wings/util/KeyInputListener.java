package cc.lvjia.wings.util;

import com.google.common.collect.ImmutableListMultimap;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

public final class KeyInputListener {
    private final ImmutableListMultimap<KeyMapping, Runnable> bindings;

    private KeyInputListener(ImmutableListMultimap<KeyMapping, Runnable> bindings) {
        this.bindings = bindings;
    }

    public static Builder builder() {
        return new BuilderRoot();
    }

    public void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> this.bindings.asMap().entrySet().stream()
                .filter(e -> e.getKey().consumeClick())
                .flatMap(e -> e.getValue().stream())
                .forEach(Runnable::run));
    }

    public interface Builder {
        CategoryBuilder category(KeyMapping.Category category);

        KeyInputListener build();
    }

    public interface CategoryBuilder extends Builder {
        BindingBuilder key(String desc, int keyCode);
    }

    public static final class BuilderRoot implements Builder {
        private final ImmutableListMultimap.Builder<KeyMapping, Runnable> bindings = ImmutableListMultimap.builder();

        @Override
        public CategoryBuilder category(KeyMapping.Category category) {
            return new CategoryBuilderRoot(this, category);
        }

        @Override
        public KeyInputListener build() {
            return new KeyInputListener(this.bindings.build());
        }
    }

    private static abstract class ChildBuilder<P extends Builder> implements Builder {
        final P parent;

        private ChildBuilder(P parent) {
            this.parent = parent;
        }

        @Override
        public final CategoryBuilder category(KeyMapping.Category category) {
            return this.parent.category(category);
        }

        @Override
        public final KeyInputListener build() {
            return this.parent.build();
        }
    }

    private static final class CategoryBuilderRoot extends ChildBuilder<BuilderRoot> implements CategoryBuilder {
        private final KeyMapping.Category category;

        private CategoryBuilderRoot(BuilderRoot delegate, KeyMapping.Category category) {
            super(delegate);
            this.category = category;
        }

        @Override
        public BindingBuilder key(String desc, int keyCode) {
            KeyMapping binding = KeyMappingHelper.registerKeyMapping(new KeyMapping(desc, InputConstants.Type.KEYSYM, keyCode, this.category));
            return new BindingBuilder(this, binding);
        }
    }

    public static final class BindingBuilder extends ChildBuilder<CategoryBuilderRoot> implements CategoryBuilder {
        private final KeyMapping binding;

        private BindingBuilder(CategoryBuilderRoot delegate, KeyMapping binding) {
            super(delegate);
            this.binding = binding;
        }

        public BindingBuilder onPress(Runnable runnable) {
            this.parent.parent.bindings.put(this.binding, runnable);
            return this;
        }

        @Override
        public BindingBuilder key(String desc, int keyCode) {
            return this.parent.key(desc, keyCode);
        }
    }
}
