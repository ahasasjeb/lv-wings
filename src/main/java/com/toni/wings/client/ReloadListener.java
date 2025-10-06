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
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;

import javax.annotation.Nonnull;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static net.minecraftforge.fml.util.ObfuscationReflectionHelper.getPrivateValue;

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

        if (layers == null) {
            if (failure != null) {
                LOGGER.warn("Failed to access player renderer layers; skipping cape replacement", failure);
            }
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
            renderer.addLayer(new LayerCapeWings(renderer));
        }
    }
}
