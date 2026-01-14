package com.toni.wings.client;

import com.mojang.logging.LogUtils;
import com.toni.wings.WingsMod;
import com.toni.wings.client.apparatus.WingForm;
import com.toni.wings.client.renderer.LayerCapeWings;
import com.toni.wings.client.renderer.LayerWings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.annotation.Nonnull;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

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
        Stream.concat(manager.getSkinMap().values().stream(), manager.renderers.values().stream())
                .filter(LivingEntityRenderer.class::isInstance)
                .map(r -> (LivingEntityRenderer<?, ?>) r)
                .filter(render -> render.getModel() instanceof HumanoidModel<?>)
                .unordered()
                .distinct()
                .forEach(render -> {
                    if (render instanceof PlayerRenderer playerRenderer) {
                        replaceCapeLayer(playerRenderer);
                    }
                    ModelPart body = ((HumanoidModel<?>) render.getModel()).body;
                    @SuppressWarnings("unchecked") LivingEntityRenderer<LivingEntity, HumanoidModel<LivingEntity>> livingRender = (LivingEntityRenderer<LivingEntity, HumanoidModel<LivingEntity>>) render;
                    livingRender.addLayer(new LayerWings(livingRender, (player, stack) -> {
                        if (player.isCrouching()) {
                            stack.translate(0.0D, 0.2D, 0.0D);
                        }
                        body.translateAndRotate(stack);
                    }));
                });
    }

    private void replaceCapeLayer(PlayerRenderer renderer) {
        List<?> layers = findLayers(renderer);
        if (layers == null) {
            return;
        }

        boolean vanillaCapeRemoved = false;
        boolean hasCustomCape = false;

        try {
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
        } catch (UnsupportedOperationException ex) {
            LOGGER.warn("Cannot modify player renderer layers; skipping cape replacement", ex);
            return;
        }

        if (vanillaCapeRemoved && !hasCustomCape) {
            renderer.addLayer(new LayerCapeWings(renderer));
        }
    }

    private List<?> findLayers(LivingEntityRenderer<?, ?> renderer) {
        // Try to locate the layers list across subclasses and obfuscated names to support vanilla and Embeddium.
        for (Class<?> cls = renderer.getClass(); cls != null; cls = cls.getSuperclass()) {
            for (String name : new String[]{"layers", "f_115291_", "field_115291"}) {
                try {
                    var field = cls.getDeclaredField(name);
                    field.setAccessible(true);
                    Object value = field.get(renderer);
                    if (value instanceof List<?> list) {
                        return list;
                    }
                } catch (NoSuchFieldException ignored) {
                    // keep searching
                } catch (ReflectiveOperationException ex) {
                    LOGGER.warn("Failed accessing renderer layers via '{}' on {}", name, cls.getName(), ex);
                    return null;
                }
            }
        }
        LOGGER.warn("Unable to locate player renderer layers; skipping cape replacement");
        return null;
    }
}
