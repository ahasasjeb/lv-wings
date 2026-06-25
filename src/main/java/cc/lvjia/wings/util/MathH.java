package cc.lvjia.wings.util;

import net.minecraft.util.Mth;

// 数学工具：角度变换、线性插值、三角函数映射（避免依赖 Apache Commons）
public final class MathH {
    public static final float PI = (float) Math.PI;
    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0D);
    private static final float RAD_TO_DEG = (float) (180.0D / Math.PI);

    private MathH() {
    }

    public static float toRadians(float degrees) {
        return degrees * DEG_TO_RAD;
    }

    public static float toDegrees(float radians) {
        return radians * RAD_TO_DEG;
    }

    // 标准线性插值，t 超界时 clamp 到 [0,1]
    public static float lerp(float a, float b, float t) {
        return t <= 0.0F ? a : t >= 1.0F ? b : a + (b - a) * t;
    }

    // 角度插值：自动处理 360° 环绕
    public static float lerpDegrees(float a, float b, float t) {
        return a + t * getDifference(a, b, 360.0F);
    }

    public static double lerpDegrees(double a, double b, double t) {
        return a + t * getDifference(a, b, 360.0D);
    }

    private static float getDifference(float a, float b, float rot) {
        return mod(b - a + rot / 2.0F, rot) - rot / 2.0F;
    }

    private static double getDifference(double a, double b, double rot) {
        return mod(b - a + rot / 2.0D, rot) - rot / 2.0D;
    }

    private static float mod(float a, float b) {
        return (a % b + b) % b;
    }

    private static double mod(double a, double b) {
        return (a % b + b) % b;
    }

    // 缓入缓出函数：-cos(π*t)/2 + 1/2，用于翅膀展开/收起的平滑过渡
    public static float easeInOut(float t) {
        return -(Mth.cos(PI * t) - 1.0F) / 2.0F;
    }

    // 值域映射：将 x 从 [domainMin, domainMax] 线性映射到 [rangeMin, rangeMax]
    public static float transform(float x, float domainMin, float domainMax, float rangeMin, float rangeMax) {
        if (x <= domainMin) {
            return rangeMin;
        }
        if (x >= domainMax) {
            return rangeMax;
        }
        return (rangeMax - rangeMin) * (x - domainMin) / (domainMax - domainMin) + rangeMin;
    }
}
