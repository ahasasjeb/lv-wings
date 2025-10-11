package cc.lvjia.wings.client;

import com.mojang.logging.LogUtils;
import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.client.apparatus.WingForm;
import cc.lvjia.wings.client.renderer.LayerCapeWings;
import cc.lvjia.wings.client.renderer.LayerWings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static net.neoforged.fml.util.ObfuscationReflectionHelper.getPrivateValue;

import javax.annotation.Nonnull;

public class ReloadListener implements ResourceManagerReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onResourceManagerReload(@Nonnull ResourceManager rm) {

        if(WingForm.isEmpty()){
            WingForm.register(WingsMod.ANGEL_WINGS, ClientProxy.createAvianWings(WingsMod.WINGS.getKey(WingsMod.ANGEL_WINGS)));
            WingForm.register(WingsMod.PARROT_WINGS, ClientProxy.createAvianWings(WingsMod.WINGS.getKey(WingsMod.PARROT_WINGS)));
            WingForm.register(WingsMod.BAT_WINGS, ClientProxy.createAvianWings(WingsMod.WINGS.getKey(WingsMod.BAT_WINGS)));
            WingForm.register(WingsMod.BLUE_BUTTERFLY_WINGS, ClientProxy.createInsectoidWings(WingsMod.WINGS.getKey(WingsMod.BLUE_BUTTERFLY_WINGS)));
            WingForm.register(WingsMod.DRAGON_WINGS, ClientProxy.createAvianWings(WingsMod.WINGS.getKey(WingsMod.DRAGON_WINGS)));
            WingForm.register(WingsMod.EVIL_WINGS, ClientProxy.createAvianWings(WingsMod.WINGS.getKey(WingsMod.EVIL_WINGS)));
            WingForm.register(WingsMod.FAIRY_WINGS, ClientProxy.createInsectoidWings(WingsMod.WINGS.getKey(WingsMod.FAIRY_WINGS)));
            WingForm.register(WingsMod.FIRE_WINGS, ClientProxy.createAvianWings(WingsMod.WINGS.getKey(WingsMod.FIRE_WINGS)));
            WingForm.register(WingsMod.MONARCH_BUTTERFLY_WINGS, ClientProxy.createInsectoidWings(WingsMod.WINGS.getKey(WingsMod.MONARCH_BUTTERFLY_WINGS)));
            WingForm.register(WingsMod.SLIME_WINGS, ClientProxy.createInsectoidWings(WingsMod.WINGS.getKey(WingsMod.SLIME_WINGS)));
            WingForm.register(WingsMod.LVJIA_SUPER_WINGS, ClientProxy.createEndPortalWings(WingsMod.WINGS.getKey(WingsMod.LVJIA_SUPER_WINGS)));
        }

        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher manager = mc.getEntityRenderDispatcher();
        Stream<AvatarRenderer<?>> playerRenderers = manager.getPlayerRenderers().values().stream()
            .filter(AvatarRenderer.class::isInstance)
            .map(AvatarRenderer.class::cast);
        Stream<AvatarRenderer<?>> mannequinRenderers = manager.getMannequinRenderers().values().stream()
            .filter(AvatarRenderer.class::isInstance)
            .map(AvatarRenderer.class::cast);
        Stream<AvatarRenderer<?>> otherRenderers = getRendererMap(manager).values().stream()
            .filter(AvatarRenderer.class::isInstance)
            .map(AvatarRenderer.class::cast);

        Stream.concat(Stream.concat(playerRenderers, mannequinRenderers), otherRenderers)
                .unordered()
                .distinct()
                .forEach(this::augmentPlayerRenderer);
    }

    private void augmentPlayerRenderer(AvatarRenderer<?> renderer) {
        replaceCapeLayer(renderer);
        ensureWingsLayer(renderer);
    }

    private void ensureWingsLayer(AvatarRenderer<?> renderer) {
        List<?> layers = getLayers(renderer);
        if (layers == null) {
            return;
        }
        boolean hasLayer = layers.stream().anyMatch(LayerWings.class::isInstance);
        if (!hasLayer) {
            renderer.addLayer(new LayerWings(renderer));
        }
    }

    private void replaceCapeLayer(AvatarRenderer<?> renderer) {
        List<?> layers = getLayers(renderer);
        if (layers == null) {
            return;
        }
        boolean vanillaCapeRemoved = false;
        boolean hasCustomCape = false;
        Iterator<?> iterator = layers.iterator();
        while (iterator.hasNext()) {
            Object layer = iterator.next();
            if (layer instanceof LayerCapeWings) {
                hasCustomCape = true;
                continue;
            }
            if (layer instanceof CapeLayer) {
                iterator.remove();
                vanillaCapeRemoved = true;
            }
        }
        if (vanillaCapeRemoved && !hasCustomCape) {
            renderer.addLayer(new LayerCapeWings(renderer, Minecraft.getInstance().getEntityModels()));
        }
    }

    private List<?> getLayers(AvatarRenderer<?> renderer) {
        List<?> layers = null;
        RuntimeException failure = null;
        for (String name : new String[]{"layers", "f_115291_"}) {
            try {
                layers = getPrivateValue(LivingEntityRenderer.class, renderer, name);
                if (layers != null) {
                    break;
                }
            } catch (RuntimeException ex) {
                failure = ex;
            }
        }
        if (layers == null && failure != null) {
            LOGGER.warn("Failed to access player renderer layers; skipping cape replacement", failure);
        }
        return layers;
    }

    private Map<?, ?> getRendererMap(EntityRenderDispatcher dispatcher) {
        RuntimeException failure = null;
        for (String name : new String[]{"renderers", "f_173940_"}) {
            try {
                Map<?, ?> map = getPrivateValue(EntityRenderDispatcher.class, dispatcher, name);
                if (map != null) {
                    return map;
                }
            } catch (RuntimeException ex) {
                failure = ex;
            }
        }
        if (failure != null) {
            LOGGER.warn("Failed to access entity renderer map; player layer updates may be incomplete", failure);
        }
        return Map.of();
    }
}
