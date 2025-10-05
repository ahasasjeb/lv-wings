package com.toni.wings.server.flight;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.event.AttachCapabilitiesEvent;
import net.neoforged.bus.api.Event;

public final class AttachFlightCapabilityEvent extends Event {
    private final AttachCapabilitiesEvent<Entity> event;

    private final Flight instance;

    private AttachFlightCapabilityEvent(AttachCapabilitiesEvent<Entity> event, Flight instance) {
        this.event = event;
        this.instance = instance;
    }

    public Entity getObject() {
        return this.event.getObject();
    }

    public void addCapability(ResourceLocation key, ICapabilityProvider cap) {
        this.event.addCapability(key, cap);
    }

    public Flight getInstance() {
        return this.instance;
    }

    public static AttachFlightCapabilityEvent create(AttachCapabilitiesEvent<Entity> event, Flight instance) {
        return new AttachFlightCapabilityEvent(event, instance);
    }
}
