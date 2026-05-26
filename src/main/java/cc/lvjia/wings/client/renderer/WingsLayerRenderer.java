package cc.lvjia.wings.client.renderer;

import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.client.flight.FlightViews;
import cc.lvjia.wings.server.apparatus.FlightApparatus;
import cc.lvjia.wings.server.flight.Flights;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public final class WingsLayerRenderer {
    private WingsLayerRenderer() {
    }

    public static @NonNull ModelLayerLocation layer(@NonNull String name) {
        return layer(name, "main");
    }

    public static @NonNull ModelLayerLocation layer(@NonNull String name, @NonNull String layer) {
        return Objects.requireNonNull(new ModelLayerLocation(WingsMod.locate(name), layer), "model layer");
    }

    public static void submitWings(@NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector,
                                   int packedLight, @NonNull AvatarRenderState state, @NonNull PlayerModel parentModel) {
        AbstractClientPlayer player = resolvePlayer(state);
        if (player == null || player.isInvisible()) {
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
                ModelPart body = Objects.requireNonNull(parentModel.body, "player body");
                body.translateAndRotate(poseStack);
                submitNodeCollector.submitCustomGeometry(poseStack, form.getRenderType(), (pose, buffer) -> {
                    PoseStack.Pose safePose = Objects.requireNonNull(pose, "pose");
                    VertexConsumer safeBuffer = Objects.requireNonNull(buffer, "vertex consumer");
                    PoseStack renderStack = new PoseStack();
                    PoseStack.Pose renderPose = Objects.requireNonNull(renderStack.last(), "pose");
                    renderPose.pose().set(safePose.pose());
                    renderPose.normal().set(safePose.normal());
                    form.render(
                            renderStack,
                            SodiumBypassVertexConsumer.wrap(safeBuffer),
                            packedLight,
                            OverlayTexture.NO_OVERLAY,
                            1.0F,
                            1.0F,
                            1.0F,
                            1.0F,
                            delta);
                });
                poseStack.popPose();
            });
        });
    }

    public static void submitCape(@NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector,
                                  int packedLight, @NonNull AvatarRenderState state, @NonNull HumanoidModel<AvatarRenderState> model) {
        if (state.isInvisible || !state.showCape) {
            return;
        }

        AbstractClientPlayer player = resolvePlayer(state);
        if (player == null || hasVisibleWings(player)) {
            return;
        }

        PlayerSkin skin = state.skin;
        ClientAsset.Texture capeTexture = skin.cape();
        if (capeTexture == null) {
            return;
        }

        poseStack.pushPose();
        submitNodeCollector.submitModel(
                model,
                state,
                poseStack,
                RenderTypes.entitySolid(capeTexture.texturePath()),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor,
                null);
        poseStack.popPose();
    }

    private static AbstractClientPlayer resolvePlayer(AvatarRenderState state) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level != null && level.getEntity(state.id) instanceof AbstractClientPlayer player) {
            return player;
        }
        return null;
    }

    private static boolean hasVisibleWings(AbstractClientPlayer player) {
        FlightApparatus wing = Flights.get(player).getWing();
        return wing != WingsMod.NONE && wing != WingsMod.WINGLESS;
    }
}
