package cc.lvjia.wings.server.asm;

import net.minecraft.world.entity.Entity;

public final class GetCameraEyeHeightEvent {
    private final Entity entity;

    private float value;

    private GetCameraEyeHeightEvent(Entity entity) {
        this.entity = entity;
    }

    public static GetCameraEyeHeightEvent create(Entity entity, float eyeHeight) {
        GetCameraEyeHeightEvent ev = new GetCameraEyeHeightEvent(entity);
        ev.setValue(eyeHeight);
        return ev;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public float getValue() {
        return this.value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
