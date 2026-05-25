package cc.lvjia.wings.util;

import net.minecraft.nbt.Tag;

/**
 * NBT 序列化/反序列化适配器。
 * <p>
 * 用于将业务对象与 Minecraft 的 {@link Tag} 之间互相转换。
 */
public interface NBTSerializer<T, N extends Tag> {
    /**
     * 将对象序列化为 NBT。
     */
    N serialize(T instance);

    /**
     * 从 NBT 反序列化对象。
     */
    T deserialize(N compound);
}
