package cc.lvjia.wings.server.net;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * 模组网络消息的统一标记接口。
 * <p>
 * 这里继承 {@link CustomPacketPayload}，便于在网络注册与发送时统一约束类型。
 */
public interface Message extends CustomPacketPayload {
}
