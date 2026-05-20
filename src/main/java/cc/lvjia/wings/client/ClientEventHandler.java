package cc.lvjia.wings.client;

import cc.lvjia.wings.client.audio.WingsSound;
import cc.lvjia.wings.client.asm.AnimatePlayerModelEvent;
import cc.lvjia.wings.client.asm.ApplyPlayerRotationsEvent;
import cc.lvjia.wings.client.asm.GetCameraEyeHeightEvent;
import cc.lvjia.wings.client.event.EmptyOffHandPresentEvent;
import cc.lvjia.wings.client.flight.FlightView;
import cc.lvjia.wings.client.flight.FlightViews;
import cc.lvjia.wings.server.flight.Flights;
import cc.lvjia.wings.util.MathH;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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

import java.util.Map;
import java.util.WeakHashMap;

public final class ClientEventHandler {
    private static ResourceKey<Level> lastPlayerDimension;
    private static CameraType lastCameraType = CameraType.FIRST_PERSON;
    private static float smoothedCameraRoll;
    private static final Map<Player, FlightPoseAngles> FLIGHT_POSE_ANGLES = new WeakHashMap<>();

    private ClientEventHandler() {
    }

    public static void register() {
        ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> onEntityJoinWorld(entity));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!client.isPaused() && client.level != null) {
                for (Player player : client.level.players()) {
                    if (player instanceof AbstractClientPlayer clientPlayer) {
                        Flights.get(clientPlayer).ifPresent(flight -> flight.tick(clientPlayer));
                        FlightViews.get(clientPlayer).ifPresent(FlightView::tick);
                    }
                }
            }
            onClientTick();
        });
    }

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

    public static void onApplyRotations(ApplyPlayerRotationsEvent event) {
        Flights.ifPlayer(event.getEntity(), (player, flight) -> {
            PoseStack matrixStack = event.getMatrixStack();
            float delta = event.getDelta();
            float amt = flight.getFlyingAmount(delta);
            if (shouldApplyFlightPose(player, amt)) {
                FlightPoseAngles angles = getFlightPoseAngles(player, flight.isFlying(), amt, delta);
                matrixStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(amt, 0.0F, angles.roll)));
                matrixStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(amt, 0.0F, angles.pitch)));
                matrixStack.translate(0.0D, -1.2D * MathH.easeInOut(amt), 0.0D);
            }
        });
    }

    public static void onGetCameraEyeHeight(GetCameraEyeHeightEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LocalPlayer) {
            FlightViews.get((LocalPlayer) entity)
                    .ifPresent(flight -> flight.tickEyeHeight(event.getValue(), event::setValue));
        }
    }

    public static float computeCameraRoll(float delta) {
        Minecraft mc = Minecraft.getInstance();
        CameraType cameraType = mc.options.getCameraType();
        if (cameraType != lastCameraType) {
            lastCameraType = cameraType;
            smoothedCameraRoll = 0.0F;
        }
        if (!cameraType.isFirstPerson()) {
            smoothedCameraRoll = 0.0F;
            return 0.0F;
        }

        Entity cameraEntity = mc.getCameraEntity();
        if (cameraEntity == null) {
            smoothedCameraRoll = 0.0F;
            return 0.0F;
        }

        final boolean[] handled = {false};
        final float[] rollOut = {0.0F};
        Flights.ifPlayer(cameraEntity, (player, flight) -> {
            handled[0] = true;
            float amt = flight.getFlyingAmount(delta);
            if (player.isSpectator() || amt <= 0.0F) {
                smoothedCameraRoll = 0.0F;
                FLIGHT_POSE_ANGLES.remove(player);
                return;
            }

            FlightPoseAngles angles = getFlightPoseAngles(player, flight.isFlying(), amt, delta);
            float targetRoll = Mth.lerp(amt, 0.0F, -angles.roll * 0.25F);
            if (!Float.isFinite(targetRoll)) {
                targetRoll = 0.0F;
            }
            targetRoll = Mth.clamp(targetRoll, -35.0F, 35.0F);
            smoothedCameraRoll = Mth.approachDegrees(smoothedCameraRoll, targetRoll, 8.0F);
            if (!flight.isFlying() && Math.abs(smoothedCameraRoll) < 0.01F) {
                smoothedCameraRoll = 0.0F;
            }
            rollOut[0] = smoothedCameraRoll;
        });
        if (!handled[0]) {
            smoothedCameraRoll = 0.0F;
            return 0.0F;
        }
        return rollOut[0];
    }

    public static void onEmptyOffHandPresentEvent(EmptyOffHandPresentEvent event) {
        Flights.get(event.getPlayer()).ifPresent(flight -> {
            if (flight.isFlying()) {
                event.allow();
            }
        });
    }

    public static void onEntityJoinWorld(Entity entity) {
        Flights.ifPlayer(entity, Player::isLocalPlayer,
                (player, flight) -> Minecraft.getInstance().getSoundManager().play(new WingsSound(player, flight)));
    }

    public static void onClientTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            lastPlayerDimension = null;
            smoothedCameraRoll = 0.0F;
            FLIGHT_POSE_ANGLES.clear();
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

    private static FlightPoseAngles getFlightPoseAngles(Player player, boolean flying, float amount, float delta) {
        FlightPoseAngles angles = FLIGHT_POSE_ANGLES.computeIfAbsent(player, ignored -> new FlightPoseAngles());
        if (flying) {
            angles.roll = getBodyYawRoll(player, delta);
            angles.pitch = -Mth.lerp(delta, player.xRotO, player.getXRot()) - 90.0F;
        } else if (amount <= 0.0F) {
            FLIGHT_POSE_ANGLES.remove(player);
        }
        return angles;
    }

    private static final class FlightPoseAngles {
        private float roll;
        private float pitch;
    }
}
