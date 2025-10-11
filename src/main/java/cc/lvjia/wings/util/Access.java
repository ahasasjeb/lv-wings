package cc.lvjia.wings.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.function.BiFunction;

public final class Access {
    private Access() {
    }

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static <T> NamingGetterHandleBuilder<T> getter(Class<T> refc) {
        return new NamingGetterHandleBuilder<>(refc);
    }

    private static abstract class NamingBuilder<T, F> {
        private final BiFunction<Class<T>, ObjectArrayList<String>, F> factory;

        private final Class<T> refc;

        private NamingBuilder(BiFunction<Class<T>, ObjectArrayList<String>, F> factory, Class<T> refc) {
            this.factory = factory;
            this.refc = refc;
        }

        F name(String name, String... others) {
            ObjectArrayList<String> names = ObjectArrayList.wrap(new String[others.length + 1], 0);
            names.add(name);
            names.addElements(names.size(), others);
            return this.factory.apply(this.refc, names);
        }
    }

    private static abstract class HandleBuilder<T> {
        final Class<T> refc;

        final ObjectArrayList<String> names;

        private HandleBuilder(Class<T> refc, ObjectArrayList<String> names) {
            this.refc = refc;
            this.names = names;
        }
    }

    public static final class NamingGetterHandleBuilder<T> extends NamingBuilder<T, GetterHandleBuilder<T>> {
        private NamingGetterHandleBuilder(Class<T> refc) {
            super(GetterHandleBuilder::new, refc);
        }

        @Override
        public GetterHandleBuilder<T> name(String name, String... others) {
            return super.name(name, others);
        }
    }

    public static final class GetterHandleBuilder<T> extends HandleBuilder<T> {
        private GetterHandleBuilder(Class<T> refc, ObjectArrayList<String> names) {
            super(refc, names);
        }

        public <R> MethodHandle type(Class<R> type) {
            return find(this.refc, this.names, MethodType.methodType(type, this.refc));
        }

        private static MethodHandle find(Class<?> refc, ObjectArrayList<String> names, MethodType type) {
            for (ObjectListIterator<String> it = names.iterator(); ; ) {
                String name = it.next();
                try {
                    Field f = refc.getDeclaredField(name);
                    f.setAccessible(true);
                    if (f.getType() != type.returnType()) {
                        throw new NoSuchFieldException("Field type mismatch: expected " + type.returnType().getName() + ", but found " + f.getType().getName());
                    }
                    return LOOKUP.unreflectGetter(f);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    if (!it.hasNext()) {
                        StringBuilder errorMessage = new StringBuilder();
                        errorMessage.append("Unable to find accessible field with the following criteria:\n");
                        errorMessage.append("  Class: ").append(refc.getName()).append("\n");
                        errorMessage.append("  Field names tried: ").append(names).append("\n");
                        errorMessage.append("  Expected field type: ").append(type.returnType().getName()).append("\n");
                        errorMessage.append("  Last attempted field: '").append(name).append("'\n");
                        if (e instanceof NoSuchFieldException) {
                            errorMessage.append("  Reason: Field not found or type mismatch");
                        } else {
                            errorMessage.append("  Reason: Illegal access to field");
                        }
                        throw new RuntimeException(errorMessage.toString(), e);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> RuntimeException rethrow(Throwable t) throws T {
        //noinspection unchecked
        throw (T) t;
    }
}
