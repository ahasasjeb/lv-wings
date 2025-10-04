package com.toni.wings.server.net;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.Objects;

public class ClientMessageContext extends MessageContext {
    public ClientMessageContext(CustomPayloadEvent.Context context) {
        super(context);
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }

    public Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    public ClientLevel getWorld() {
        return Objects.requireNonNull(this.getMinecraft().level);
    }

    public Player getPlayer() {
        return Objects.requireNonNull(this.getMinecraft().player);
    }
}
