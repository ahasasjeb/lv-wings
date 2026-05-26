package cc.lvjia.wings.client.renderer;

import cc.lvjia.wings.client.NeoForgeClientProxy;
import cc.lvjia.wings.client.model.ModelWingsAvian;
import cc.lvjia.wings.client.model.ModelWingsInsectoid;
import cc.lvjia.wings.mixin.client.LivingEntityRendererAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;

public final class NeoForgeLayerWings extends RenderLayer<AvatarRenderState, PlayerModel> {

    public static final @NonNull ModelLayerLocation INSECTOID_WINGS = WingsLayerRenderer.layer("insectoid_wings");
    public static final @NonNull ModelLayerLocation AVIAN_WINGS = WingsLayerRenderer.layer("avian_wings");

    public NeoForgeLayerWings(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(Objects.requireNonNull(parent, "parent"));
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(NeoForgeLayerWings::initLayers);
        modBus.addListener(NeoForgeLayerWings::addLayers);
    }

    public static void initLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(INSECTOID_WINGS, ModelWingsInsectoid::createBodyLayer);
        event.registerLayerDefinition(AVIAN_WINGS, ModelWingsAvian::createBodyLayer);
    }

    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        NeoForgeClientProxy.registerWingForms(event.getEntityModels());
        for (var skin : event.getSkins()) {
            AvatarRenderer<?> playerRenderer = event.getPlayerRenderer(skin);
            if (playerRenderer != null) {
                augmentPlayerRenderer(playerRenderer, event.getEntityModels());
            }
            AvatarRenderer<?> mannequinRenderer = event.getMannequinRenderer(skin);
            if (mannequinRenderer != null) {
                augmentPlayerRenderer(mannequinRenderer, event.getEntityModels());
            }
        }
    }

    private static void augmentPlayerRenderer(AvatarRenderer<?> renderer, EntityModelSet modelSet) {
        Objects.requireNonNull(renderer, "renderer");
        Objects.requireNonNull(modelSet, "model set");
        List<?> layers = ((LivingEntityRendererAccessor<?, ?, ?>) renderer).wings$getLayers();
        layers.removeIf(layer -> layer instanceof NeoForgeLayerCapeWings || layer instanceof CapeLayer);
        renderer.addLayer(new NeoForgeLayerCapeWings(renderer, modelSet));
        if (layers.stream().noneMatch(NeoForgeLayerWings.class::isInstance)) {
            renderer.addLayer(new NeoForgeLayerWings(renderer));
        }
    }

    @Override
    public void submit(@NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, int packedLight,
                       @NonNull AvatarRenderState state, float limbSwing, float limbSwingAmount) {
        WingsLayerRenderer.submitWings(poseStack, submitNodeCollector, packedLight, state, this.getParentModel());
    }

}
