package cc.lvjia.wings.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = WingsMod.ID)
public final class ClientEventHandler {
    private ClientEventHandler() {
    }

    @SubscribeEvent
    public static void onAnimatePlayerModel(AnimatePlayerModelEvent event) {
        Player player = event.getEntity();
        Flights.get(player).ifPresent(flight -> {
            float delta = event.getTicksExisted() - player.tickCount;
            float amt = flight.getFlyingAmount(delta);
            if (amt == 0.0F) {
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
            if (amt > 0.0F) {
                float roll = MathH.lerpDegrees(
                    player.yBodyRotO - player.yRotO,
                    player.yBodyRot - player.getYRot(),
                    delta
                );
                float pitch = -MathH.lerpDegrees(player.xRotO, player.getXRot(), delta) - 90.0F;
                matrixStack.mulPose(Axis.ZP.rotationDegrees(MathH.lerpDegrees(0.0F, roll, amt)));
                matrixStack.mulPose(Axis.XP.rotationDegrees(MathH.lerpDegrees(0.0F, pitch, amt)));
                matrixStack.translate(0.0D, -1.2D * MathH.easeInOut(amt), 0.0D);
            }
        });
    }

    @SubscribeEvent
    public static void onGetCameraEyeHeight(GetCameraEyeHeightEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LocalPlayer) {
            FlightViews.get((LocalPlayer) entity).ifPresent(flight ->
                flight.tickEyeHeight(event.getValue(), event::setValue)
            );
        }
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        Flights.ifPlayer(event.getCamera().getEntity(), (player, flight) -> {
            float delta = (float) event.getPartialTick();
            float amt = flight.getFlyingAmount(delta);
            if (amt > 0.0F) {
                float roll = MathH.lerpDegrees(
                    player.yBodyRotO - player.yRotO,
                    player.yBodyRot - player.getYRot(),
                    delta
                );
                event.setRoll(MathH.lerpDegrees(0.0F, -roll * 0.25F, amt));
            }
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
        Flights.ifPlayer(event.getEntity(), Player::isLocalPlayer, (player, flight) ->
            Minecraft.getInstance().getSoundManager().play(new WingsSound(player, flight))
        );
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player entity = event.getEntity();
        if (entity instanceof AbstractClientPlayer player) {
            FlightViews.get(player).ifPresent(FlightView::tick);
        }
    }
}
