package com.toni.wings.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiFunction;

public final class Access {
    private Access() {
    }

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static <T> NamingVirtualHandleBuilder<T> virtual(Class<T> refc) {
        return new NamingVirtualHandleBuilder<>(refc);
    }

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

    public static final class NamingVirtualHandleBuilder<T> extends NamingBuilder<T, VirtualHandleBuilder<T>> {
        private NamingVirtualHandleBuilder(Class<T> refc) {
            super(VirtualHandleBuilder::new, refc);
        }

        @Override
        public VirtualHandleBuilder<T> name(String name, String... others) {
            return super.name(name, others);
        }
    }

    public static final class VirtualHandleBuilder<T> extends HandleBuilder<T> {
        private final ObjectArrayList<Class<?>> ptypes;

        private VirtualHandleBuilder(Class<T> refc, ObjectArrayList<String> names) {
            super(refc, names);
            this.ptypes = new ObjectArrayList<Class<?>>(new Class<?>[4], false) {
                // create a trim that preserves type
                @Override
                public void trim() {
                    this.a = ObjectArrays.trim(this.a, this.size);
                }
            };
        }

        public VirtualHandleBuilder<T> ptype(Class<?> ptype) {
            this.ptypes.add(ptype);
            return this;
        }

        public VirtualHandleBuilder<T> ptypes(Class<?>... ptypes) {
            this.ptypes.addElements(this.ptypes.size(), ptypes);
            return this;
        }

        public <R> MethodHandle rtype(Class<R> rtype) {
            this.ptypes.trim();
            return find(this.refc, this.names, MethodType.methodType(rtype, this.ptypes.elements()));
        }

        private static MethodHandle find(Class<?> refc, ObjectArrayList<String> names, MethodType type) {
            Class<?>[] parameterTypes = type.parameterArray();
            for (ObjectListIterator<String> it = names.iterator(); ; ) {
                String name = it.next();
                try {
                    Method m = refc.getDeclaredMethod(name, parameterTypes);
                    m.setAccessible(true);
                    if (m.getReturnType() != type.returnType()) {
                        throw new NoSuchMethodException("Method return type mismatch: expected " + type.returnType().getName() + ", but found " + m.getReturnType().getName());
                    }
                    return LOOKUP.unreflect(m);
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    if (!it.hasNext()) {
                        StringBuilder errorMessage = new StringBuilder();
                        errorMessage.append("Unable to find accessible method with the following criteria:\n");
                        errorMessage.append("  Class: ").append(refc.getName()).append("\n");
                        errorMessage.append("  Method names tried: ").append(names).append("\n");
                        errorMessage.append("  Parameter types: ").append(java.util.Arrays.toString(parameterTypes)).append("\n");
                        errorMessage.append("  Return type: ").append(type.returnType().getName()).append("\n");
                        errorMessage.append("  Last attempted method: '").append(name).append("'\n");
                        if (e instanceof NoSuchMethodException) {
                            errorMessage.append("  Reason: Method not found or return type mismatch");
                        } else {
                            errorMessage.append("  Reason: Illegal access to method");
                        }
                        throw new RuntimeException(errorMessage.toString(), e);
                    }
                }
            }
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
