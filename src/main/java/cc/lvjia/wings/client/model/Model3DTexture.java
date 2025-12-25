package cc.lvjia.wings.client.model;

import cc.lvjia.wings.client.renderer.SodiumBypassVertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;

import java.lang.reflect.*;
import java.util.EnumSet;
import java.util.Objects;

// replace reflection with mixin 实际：改不了一点，贼TM麻烦
public final class Model3DTexture extends ModelPart.Cube {
    private static final Field POLYGONS_FIELD = findPolygonsField();
    private final int width;
    private final int height;
    private final float u1;
    private final float v1;
    private final float u2;
    private final float v2;

    private Model3DTexture(
            float posX, float posY, float posZ,
            int width, int height,
            float u1, float v1,
            float u2, float v2
    ) {
        super(0, 0, posX, posY, posZ, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, false, 64.0F, 64.0F, EnumSet.noneOf(Direction.class));
        this.width = width;
        this.height = height;
        this.u1 = u1;
        this.v1 = v1;
        this.u2 = u2;
        this.v2 = v2;
        int faceCount = 2 + 2 * width + 2 * height;
        Object polygonsOld = Objects.requireNonNull(getPolygons(this));
        Class<?> polygonClass = polygonsOld.getClass().getComponentType();
        Field verticesField = findArrayField(polygonClass, "vertices");
        Class<?> vertexArrayClass = verticesField.getType();
        Class<?> vertexClass = vertexArrayClass.getComponentType();
        Constructor<?> vertexCtor;
        try {
            vertexCtor = vertexClass.getDeclaredConstructor(float.class, float.class, float.class, float.class, float.class);
            vertexCtor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Model3DTexture: cannot access vertex constructor (float,float,float,float,float)", e);
        }
        Constructor<?> polygonCtor;
        try {
            polygonCtor = polygonClass.getDeclaredConstructor(vertexArrayClass, float.class, float.class, float.class, float.class, float.class, float.class, boolean.class, Direction.class);
            polygonCtor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Model3DTexture: cannot access polygon constructor signature", e);
        }
        Object[] polygons = (Object[]) Array.newInstance(polygonClass, faceCount);
        int[] quadIndex = {0};
        float x0 = this.minX;
        float x1 = (this.minX + this.width);
        float y0 = this.minY;
        float y1 = (this.minY + this.height);
        float z0 = this.minZ;
        float z1 = (this.minZ + 1);
        FaceAdder faces = (fx0, fy0, fz0, fx1, fy1, fz1, fu1, fv1, fu2, fv2, normal) -> {
            Object vertices = Array.newInstance(vertexClass, 4);
            try {
                boolean v = normal.getAxis().isVertical();
                Array.set(vertices, 0, vertexCtor.newInstance(fx1, fy0, fz0, 0.0F, 0.0F));
                Array.set(vertices, 1, vertexCtor.newInstance(fx0, fy0, v ? fz0 : fz1, 0.0F, 0.0F));
                Array.set(vertices, 2, vertexCtor.newInstance(fx0, fy1, fz1, 0.0F, 0.0F));
                Array.set(vertices, 3, vertexCtor.newInstance(fx1, fy1, v ? fz1 : fz0, 0.0F, 0.0F));
                polygons[quadIndex[0]++] = polygonCtor.newInstance(vertices, fu1, fv1, fu2, fv2, 64.0F, 64.0F, false, normal);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Model3DTexture: failed to construct polygon for normal " + normal, e);
            }
        };
        faces.add(x0, y0, z0, x1, y1, z0, this.u1, this.v1, this.u2, this.v2, Direction.NORTH);
        faces.add(x0, y1, z1, x1, y0, z1, this.u1, this.v2, this.u2, this.v1, Direction.SOUTH);
        float f5 = 0.5F * (this.u1 - this.u2) / this.width;
        float f6 = 0.5F * (this.v1 - this.v2) / this.height;
        for (int k = 0; k < this.width; k++) {
            float f7 = x0 + k;
            float f8 = this.u1 + (this.u2 - this.u1) * ((float) k / this.width) - f5;
            faces.add(f7, y0, z0, f7, y1, z1, f8, this.v1, f8, this.v2, Direction.WEST);
        }
        for (int k = 0; k < this.width; k++) {
            float f8 = this.u1 + (this.u2 - this.u1) * ((float) k / this.width) - f5;
            float f9 = x0 + (k + 1);
            faces.add(f9, y1, z0, f9, y0, z1, f8, this.v2, f8, this.v1, Direction.EAST);
        }
        for (int k = 0; k < this.height; k++) {
            float f8 = this.v1 + (this.v2 - this.v1) * ((float) k / this.height) - f6;
            float f9 = y0 + (k + 1);
            faces.add(x0, f9, z0, x1, f9, z1, this.u1, f8, this.u2, f8, Direction.UP);
        }
        for (int k = 0; k < this.height; k++) {
            float f7 = y0 + k;
            float f8 = this.v1 + (this.v2 - this.v1) * ((float) k / this.height) - f6;
            faces.add(x1, f7, z0, x0, f7, z1, this.u2, f8, this.u1, f8, Direction.DOWN);
        }
        setPolygons(this, polygons);
    }

    public static ModelPart.Cube create(
            float posX, float posY, float posZ,
            int width, int height,
            int u, int v,
            int textureWidth, int textureHeight
    ) {
        ModelPart.Cube cube = new Model3DTexture(
                posX, posY, posZ,
                width, height,
                (float) u, (float) v,
                (float) (u + width), (float) (v + height)
        );
        return cube;
    }

    private static Field findPolygonsField() {
        for (Field field : ModelPart.Cube.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (field.getType().isArray()) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new IllegalStateException("Unable to locate ModelPart.Cube polygons field");
    }

    private static Field findArrayField(Class<?> owner, String debugName) {
        for (Field field : owner.getDeclaredFields()) {
            if (field.getType().isArray()) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new IllegalStateException("Unable to locate array field '" + debugName + "' on " + owner.getName());
    }

    private static Object getPolygons(ModelPart.Cube cube) {
        try {
            return POLYGONS_FIELD.get(cube);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Model3DTexture: failed to read ModelPart.Cube polygons via reflection", e);
        }
    }

    private static void setPolygons(ModelPart.Cube cube, Object value) {
        try {
            POLYGONS_FIELD.set(cube, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Model3DTexture: failed to set ModelPart.Cube polygons via reflection", e);
        }
    }

    @Override
    public void compile(PoseStack.Pose pose, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        // Wrap buffer to bypass Sodium's vertex writer optimization
        VertexConsumer safeBuffer = Objects.requireNonNull(buffer, "vertex consumer");
        super.compile(pose, SodiumBypassVertexConsumer.wrap(safeBuffer), packedLight, packedOverlay, color);
    }

    interface FaceAdder {
        void add(float x, float y, float z, float x2, float y2, float z2, float u1, float v1, float u2, float v2, Direction normal);
    }
}
