package cc.lvjia.wings.client;

import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.client.audio.WingsSound;
import cc.lvjia.wings.client.flight.FlightView;
import cc.lvjia.wings.client.flight.FlightViews;
import cc.lvjia.wings.server.asm.AnimatePlayerModelEvent;
import cc.lvjia.wings.server.asm.ApplyPlayerRotationsEvent;
import cc.lvjia.wings.server.asm.EmptyOffHandPresentEvent;
import cc.lvjia.wings.server.asm.GetCameraEyeHeightEvent;
import cc.lvjia.wings.server.flight.Flights;
import cc.lvjia.wings.util.MathH;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = WingsMod.ID)
public final class ClientEventHandler {
    private static ResourceKey<Level> lastPlayerDimension;
    private static CameraType lastCameraType = CameraType.FIRST_PERSON;
    private static float smoothedCameraRoll;

    private ClientEventHandler() {
    }

    @SubscribeEvent
    public static void onAnimatePlayerModel(AnimatePlayerModelEvent event) {
        Player player = event.getEntity();
        Flights.get(player).ifPresent(flight -> {
            float delta = event.getTicksExisted() - player.tickCount;
            float amt = flight.getFlyingAmount(delta);
            if (!shouldApplyFlightPose(player, amt)) {
                return;
            }
            PlayerModel model = event.getModel();
            float pitch = event.getPitch();
            model.head.xRot = MathH.toRadians(MathH.lerp(pitch, pitch / 4.0F - 90.0F, amt));
            model.leftArm.xRot = MathH.lerp(model.leftArm.xRot, -3.2F, amt);
            model.rightArm.xRot = MathH.lerp(model.rightArm.xRot, -3.2F, amt);
            model.leftLeg.xRot = MathH.lerp(model.leftLeg.xRot, 0.0F, amt);
            model.rightLeg.xRot = MathH.lerp(model.rightLeg.xRot, 0.0F, amt);
            model.hat.xRot = 0;
            model.hat.yRot = 0;
            model.hat.zRot = 0;
            model.leftSleeve.xRot = 0;
            model.leftSleeve.yRot = 0;
            model.leftSleeve.zRot = 0;
            model.rightSleeve.xRot = 0;
            model.rightSleeve.yRot = 0;
            model.rightSleeve.zRot = 0;
            model.leftPants.xRot = 0;
            model.leftPants.yRot = 0;
            model.leftPants.zRot = 0;
            model.rightPants.xRot = 0;
            model.rightPants.yRot = 0;
            model.rightPants.zRot = 0;
            model.jacket.xRot = 0;
            model.jacket.yRot = 0;
            model.jacket.zRot = 0;
        });
    }

    @SubscribeEvent
    public static void onApplyRotations(ApplyPlayerRotationsEvent event) {
        Flights.ifPlayer(event.getEntity(), (player, flight) -> {
            PoseStack matrixStack = event.getMatrixStack();
            float delta = event.getDelta();
            float amt = flight.getFlyingAmount(delta);
            if (shouldApplyFlightPose(player, amt)) {
                float roll = getBodyYawRoll(player, delta);
                float pitch = -Mth.lerp(delta, player.xRotO, player.getXRot()) - 90.0F;
                matrixStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(amt, 0.0F, roll)));
                matrixStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(amt, 0.0F, pitch)));
                matrixStack.translate(0.0D, -1.2D * MathH.easeInOut(amt), 0.0D);
            }
        });
    }

    @SubscribeEvent
    public static void onGetCameraEyeHeight(GetCameraEyeHeightEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LocalPlayer) {
            FlightViews.get((LocalPlayer) entity)
                    .ifPresent(flight -> flight.tickEyeHeight(event.getValue(), event::setValue));
        }
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        CameraType cameraType = mc.options.getCameraType();
        if (cameraType != lastCameraType) {
            lastCameraType = cameraType;
            smoothedCameraRoll = 0.0F;
        }
        if (!cameraType.isFirstPerson()) {
            event.setRoll(0.0F);
            return;
        }

        Entity cameraEntity = mc.getCameraEntity();
        if (cameraEntity == null) {
            return;
        }

        Flights.ifPlayer(cameraEntity, (player, flight) -> {
            float delta = (float) event.getPartialTick();
            float amt = flight.getFlyingAmount(delta);
            if (!flight.isFlying() || player.isSpectator() || amt <= 0.0F) {
                smoothedCameraRoll = 0.0F;
                event.setRoll(0.0F);
                return;
            }

            float roll = getBodyYawRoll(player, delta);
            float targetRoll = Mth.lerp(amt, 0.0F, -roll * 0.25F);
            if (!Float.isFinite(targetRoll)) {
                targetRoll = 0.0F;
            }
            targetRoll = Mth.clamp(targetRoll, -35.0F, 35.0F);
            smoothedCameraRoll = Mth.approachDegrees(smoothedCameraRoll, targetRoll, 8.0F);
            event.setRoll(smoothedCameraRoll);
        });
    }

    @SubscribeEvent
    public static void onEmptyOffHandPresentEvent(EmptyOffHandPresentEvent event) {
        Flights.get(event.getPlayer()).ifPresent(flight -> {
            if (flight.isFlying()) {
                event.allow();
            }
        });
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        Flights.ifPlayer(event.getEntity(), Player::isLocalPlayer,
                (player, flight) -> Minecraft.getInstance().getSoundManager().play(new WingsSound(player, flight)));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player entity = event.getEntity();
        if (entity instanceof AbstractClientPlayer player) {
            FlightViews.get(player).ifPresent(FlightView::tick);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            lastPlayerDimension = null;
            smoothedCameraRoll = 0.0F;
            return;
        }
        ResourceKey<Level> current = player.level().dimension();
        if (current != lastPlayerDimension) {
            lastPlayerDimension = current;
            FlightViews.invalidate(player);
        }
    }

    private static boolean shouldApplyFlightPose(Player player, float amount) {
        return amount > 0.0F && !player.isSpectator();
    }

    private static float getBodyYawRoll(Player player, float delta) {
        float diffO = Mth.wrapDegrees(player.yBodyRotO - player.yRotO);
        float diff = Mth.wrapDegrees(player.yBodyRot - player.getYRot());
        return Mth.rotLerp(delta, diffO, diff);
    }
}
