package com.toni.wings.client.renderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class PortalWingRenderType {

    public static final ResourceLocation PORTAL_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/end_portal.png");

    private static final RenderType PORTAL = RenderType.endPortal();
    private static final RenderType SHADER_FALLBACK = RenderType.entityTranslucent(PORTAL_TEXTURE);
    private static final boolean IRIS_PRESENT = ModList.get().isLoaded("iris") || ModList.get().isLoaded("oculus");
    private static final String IRIS_API_CLASS = "net.irisshaders.iris.api.v0.IrisApi";

    private static Object irisApiInstance;
    private static Method irisShaderStatusMethod;

    private PortalWingRenderType() {
    }

    public static RenderType get() {
        return isShaderPackActive() ? SHADER_FALLBACK : PORTAL;
    }

    private static boolean isShaderPackActive() {
        if (!IRIS_PRESENT) {
            return false;
        }
        try {
            ensureIrisApi();
            if (irisApiInstance == null || irisShaderStatusMethod == null) {
                return false;
            }
            Object result = irisShaderStatusMethod.invoke(irisApiInstance);
            return result instanceof Boolean && (Boolean) result;
        } catch (IllegalAccessException | InvocationTargetException ex) {
            return false;
        }
    }

    private static void ensureIrisApi() {
        if (irisShaderStatusMethod != null && irisApiInstance != null) {
            return;
        }
        try {
            Class<?> apiClass = Class.forName(IRIS_API_CLASS);
            Method getInstance = apiClass.getMethod("getInstance");
            Method isShaderPackInUse = apiClass.getMethod("isShaderPackInUse");
            Object instance = getInstance.invoke(null);
            irisApiInstance = instance;
            irisShaderStatusMethod = isShaderPackInUse;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            irisApiInstance = null;
            irisShaderStatusMethod = null;
        }
    }
}
