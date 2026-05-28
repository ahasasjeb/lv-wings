package cc.lvjia.wings.client;

import cc.lvjia.wings.client.flight.FlightPoseSupport;
import cc.lvjia.wings.client.flight.FlightViews;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.Flights;
import cc.lvjia.wings.util.MathH;
import cc.lvjia.wings.util.function.FloatConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class ClientEventHandlerSupport {
    private static ResourceKey<Level> lastPlayerDimension;
    private static CameraType lastCameraType = CameraType.FIRST_PERSON;
    private static float smoothedCameraRoll;

    private ClientEventHandlerSupport() {
    }

    public static void applyFlightPose(Player player, PlayerModel model, float ticksExisted, float pitch) {
        Flight flight = Flights.get(player);
        float delta = ticksExisted - player.tickCount;
        float amount = flight.getFlyingAmount(delta);
        if (!FlightPoseSupport.shouldApplyFlightPose(player, amount)) {
            return;
        }

        model.head.xRot = MathH.toRadians(MathH.lerp(pitch, pitch / 4.0F - 90.0F, amount));
        model.leftArm.xRot = MathH.lerp(model.leftArm.xRot, -3.2F, amount);
        model.rightArm.xRot = MathH.lerp(model.rightArm.xRot, -3.2F, amount);
        model.leftLeg.xRot = MathH.lerp(model.leftLeg.xRot, 0.0F, amount);
        model.rightLeg.xRot = MathH.lerp(model.rightLeg.xRot, 0.0F, amount);
        resetOuterModelParts(model);
    }

    public static void applyPlayerRotations(Entity entity, PoseStack matrixStack, float delta) {
        Flights.ifPlayer(entity, (player, flight) -> {
            float amount = flight.getFlyingAmount(delta);
            if (FlightPoseSupport.shouldApplyFlightPose(player, amount)) {
                FlightPoseSupport.FlightPoseAngles angles = FlightPoseSupport.getFlightPoseAngles(player,
                        flight.isFlying(), amount, delta);
                matrixStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(amount, 0.0F, angles.roll())));
                matrixStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(amount, 0.0F, angles.pitch())));
                matrixStack.translate(0.0D, -1.2D * MathH.easeInOut(amount), 0.0D);
            }
        });
    }

    public static void tickCameraEyeHeight(Entity entity, float value, FloatConsumer setter) {
        if (entity instanceof LocalPlayer player) {
            FlightViews.get(player).ifPresent(flight -> flight.tickEyeHeight(value, setter));
        }
    }

    public static float computeCameraRoll(float delta) {
        Minecraft minecraft = Minecraft.getInstance();
        CameraType cameraType = minecraft.options.getCameraType();
        if (cameraType != lastCameraType) {
            lastCameraType = cameraType;
            smoothedCameraRoll = 0.0F;
        }
        if (!cameraType.isFirstPerson()) {
            smoothedCameraRoll = 0.0F;
            return 0.0F;
        }

        Entity cameraEntity = minecraft.getCameraEntity();
        if (!(cameraEntity instanceof Player player)) {
            smoothedCameraRoll = 0.0F;
            return 0.0F;
        }

        Flight flight = Flights.get(player);
        float amount = flight.getFlyingAmount(delta);
        if (player.isSpectator() || amount <= 0.0F) {
            smoothedCameraRoll = 0.0F;
            FlightPoseSupport.clear(player);
            return 0.0F;
        }

        FlightPoseSupport.FlightPoseAngles angles = FlightPoseSupport.getFlightPoseAngles(player,
                flight.isFlying(), amount, delta);
        float targetRoll = Mth.lerp(amount, 0.0F, -angles.roll() * 0.25F);
        if (!Float.isFinite(targetRoll)) {
            targetRoll = 0.0F;
        }
        targetRoll = Mth.clamp(targetRoll, -35.0F, 35.0F);
        smoothedCameraRoll = Mth.approachDegrees(smoothedCameraRoll, targetRoll, 8.0F);
        if (!flight.isFlying() && Math.abs(smoothedCameraRoll) < 0.01F) {
            smoothedCameraRoll = 0.0F;
        }
        return smoothedCameraRoll;
    }

    public static void tickClientDimension() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            lastPlayerDimension = null;
            smoothedCameraRoll = 0.0F;
            FlightPoseSupport.clearAll();
            return;
        }
        ResourceKey<Level> current = player.level().dimension();
        if (current != lastPlayerDimension) {
            lastPlayerDimension = current;
            FlightViews.invalidate(player);
        }
    }

    private static void resetOuterModelParts(PlayerModel model) {
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
    }
}
