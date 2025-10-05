package com.toni.wings.client;

import com.mojang.logging.LogUtils;
import com.toni.wings.WingsMod;
import com.toni.wings.client.apparatus.WingForm;
import com.toni.wings.client.renderer.LayerCapeWings;
import com.toni.wings.client.renderer.LayerWings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static net.neoforged.fml.util.ObfuscationReflectionHelper.getPrivateValue;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class ReloadListener implements ResourceManagerReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();

    /*@SubscribeEvent
    public static void onModelBakeEvent(RenderLivingEvent.Pre event) {
        if(event.getEntity().hasEffect(WingsEffects.WINGS))
    }*/

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
            //WingForm.register(WingsMod.METALLIC_WINGS, ClientProxy.createAvianWings(WingsMod.WINGS.getKey(WingsMod.METALLIC_WINGS)));
        }

        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher manager = mc.getEntityRenderDispatcher();
    Stream<PlayerRenderer> skinRenderers = manager.getSkinMap().values().stream()
        .filter(PlayerRenderer.class::isInstance)
        .map(PlayerRenderer.class::cast);
    Stream<PlayerRenderer> otherRenderers = manager.renderers.values().stream()
                .filter(PlayerRenderer.class::isInstance)
                .map(PlayerRenderer.class::cast);

        Stream.concat(skinRenderers, otherRenderers)
                .unordered()
                .distinct()
                .forEach(this::augmentPlayerRenderer);
    }

    private void augmentPlayerRenderer(PlayerRenderer renderer) {
        replaceCapeLayer(renderer);
        ensureWingsLayer(renderer);
    }

    private void ensureWingsLayer(PlayerRenderer renderer) {
        List<?> layers = getLayers(renderer);
        if (layers == null) {
            return;
        }
        boolean hasLayer = layers.stream().anyMatch(LayerWings.class::isInstance);
        if (!hasLayer) {
            renderer.addLayer(new LayerWings(renderer));
        }
    }

    private void replaceCapeLayer(PlayerRenderer renderer) {
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

    private List<?> getLayers(PlayerRenderer renderer) {
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
}
