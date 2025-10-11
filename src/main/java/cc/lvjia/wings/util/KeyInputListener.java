package cc.lvjia.wings.util;

import com.google.common.collect.ImmutableListMultimap;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;

public final class KeyInputListener {
    private static final java.util.List<KeyMapping> KEY_MAPPINGS = new java.util.ArrayList<>();
    private static final java.util.Set<KeyMapping.Category> KEY_CATEGORIES = new java.util.LinkedHashSet<>();

    private final ImmutableListMultimap<KeyMapping, Runnable> bindings;

    private KeyInputListener(ImmutableListMultimap<KeyMapping, Runnable> bindings) {
        this.bindings = bindings;
    }

    @SubscribeEvent
    public void onKey(InputEvent.Key event) {
        this.bindings.asMap().entrySet().stream()
            .filter(e -> e.getKey().consumeClick())
            .flatMap(e -> e.getValue().stream())
            .forEach(Runnable::run);
    }

    public static Builder builder() {
        return new BuilderRoot();
    }

    public interface Builder {
        CategoryBuilder category(KeyMapping.Category category);

        KeyInputListener build();
    }

    public interface CategoryBuilder extends Builder {
        BindingBuilder key(String desc, IKeyConflictContext context, KeyModifier modifier, int keyCode);
    }

    public static final class BuilderRoot implements Builder {
        private final ImmutableListMultimap.Builder<KeyMapping, Runnable> bindings;

        private BuilderRoot() {
            this(ImmutableListMultimap.builder());
        }

        private BuilderRoot(ImmutableListMultimap.Builder<KeyMapping, Runnable> bindings) {
            this.bindings = bindings;
        }

        @Override
        public CategoryBuilder category(KeyMapping.Category category) {
            KEY_CATEGORIES.add(category);
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
        public BindingBuilder key(String desc, IKeyConflictContext context, KeyModifier modifier, int keyCode) {
            KeyMapping binding = new KeyMapping(desc, context, modifier, InputConstants.Type.KEYSYM, keyCode, this.category);
            KEY_MAPPINGS.add(binding);
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
        public BindingBuilder key(String desc, IKeyConflictContext context, KeyModifier modifier, int keyCode) {
            return this.parent.key(desc, context, modifier, keyCode);
        }
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        KEY_CATEGORIES.forEach(event::registerCategory);
        KEY_MAPPINGS.forEach(event::register);
    }
}
