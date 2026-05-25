package cc.lvjia.wings.client.flight;

public final class RotationAngles {
    private float x;
    private float y;
    private float z;

    public RotationAngles set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public RotationAngles set(double x, double y, double z) {
        return this.set((float) x, (float) y, (float) z);
    }

    public RotationAngles set(RotationAngles angles) {
        return this.set(angles.x, angles.y, angles.z);
    }

    public RotationAngles add(double x, double y, double z) {
        this.x += (float) x;
        this.y += (float) y;
        this.z += (float) z;
        return this;
    }

    public float x() {
        return this.x;
    }

    public float y() {
        return this.y;
    }

    public float z() {
        return this.z;
    }
}
