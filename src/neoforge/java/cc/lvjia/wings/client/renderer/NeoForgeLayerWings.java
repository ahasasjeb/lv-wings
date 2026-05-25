package cc.lvjia.wings.client.renderer;

import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.client.NeoForgeClientProxy;
import cc.lvjia.wings.client.flight.FlightViews;
import cc.lvjia.wings.mixin.client.LivingEntityRendererAccessor;
import cc.lvjia.wings.client.model.ModelWingsAvian;
import cc.lvjia.wings.client.model.ModelWingsInsectoid;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;

public final class NeoForgeLayerWings extends RenderLayer<AvatarRenderState, PlayerModel> {

    public static final @NonNull ModelLayerLocation INSECTOID_WINGS = layer("insectoid_wings");
    public static final @NonNull ModelLayerLocation AVIAN_WINGS = layer("avian_wings");

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

    private static @NonNull ModelLayerLocation layer(@NonNull String name) {
        return layer(name, "main");
    }

    private static @NonNull ModelLayerLocation layer(@NonNull String name, @NonNull String layer) {
        return Objects.requireNonNull(new ModelLayerLocation(WingsMod.locate(name), layer), "model layer");
    }

    @Override
    public void submit(@NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, int packedLight,
            @NonNull AvatarRenderState state, float limbSwing, float limbSwingAmount) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        if (!(level.getEntity(state.id) instanceof AbstractClientPlayer player)) {
            return;
        }

        if (player.isInvisible()) {
            return;
        }

        FlightViews.get(player).ifPresent(flight -> {
            flight.tick();
            flight.ifFormPresent(form -> {
                float delta = Mth.clamp(state.ageInTicks - player.tickCount, 0.0F, 1.0F);
                poseStack.pushPose();
                if (state.isCrouching) {
                    poseStack.translate(0.0D, 0.2D, 0.0D);
                }
                ModelPart body = Objects.requireNonNull(this.getParentModel().body, "player body");
                body.translateAndRotate(poseStack);
                submitNodeCollector.submitCustomGeometry(poseStack, form.getRenderType(), (pose, buffer) -> {
                    PoseStack.Pose safePose = Objects.requireNonNull(pose, "pose");
                    VertexConsumer safeBuffer = Objects.requireNonNull(buffer, "vertex consumer");
                    PoseStack renderStack = new PoseStack();
                    PoseStack.Pose renderPose = Objects.requireNonNull(renderStack.last(), "pose");
                    renderPose.pose().set(safePose.pose());
                    renderPose.normal().set(safePose.normal());
                    form.render(renderStack, SodiumBypassVertexConsumer.wrap(safeBuffer), packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F, delta);
                });
                poseStack.popPose();
            });
        });
    }

}
