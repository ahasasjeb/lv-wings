package com.toni.wings.server.asm;

import net.minecraft.client.player.LocalPlayer;
import net.neoforged.bus.api.Event;

public final class EmptyOffHandPresentEvent extends Event {
    private final LocalPlayer player;
    private boolean allowed;

    public EmptyOffHandPresentEvent(LocalPlayer player) {
        this.player = player;
    }

    public LocalPlayer getPlayer() {
        return this.player;
    }

    public void allow() {
        this.allowed = true;
    }

    public boolean isAllowed() {
        return this.allowed;
    }
}
