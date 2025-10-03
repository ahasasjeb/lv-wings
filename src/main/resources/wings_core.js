function initializeCoreMod() {
Java.type('net.minecraftforge.coremod.api.ASMAPI').loadFile('easycorelib.js')

easycore.include('me')

var PlayerEntity = net.minecraft.world.entity.player.Player,
    LivingEntity = net.minecraft.world.entity.LivingEntity,
    Entity = net.minecraft.world.entity.Entity,
    WingsHooks = com.toni.wings.server.asm.WingsHooks,
    WingsHooksClient = com.toni.wings.server.asm.WingsHooksClient,
    ResourceLocation = net.minecraft.resources.ResourceLocation,
    ServerPlayNetHandler = net.minecraft.server.network.ServerGamePacketListenerImpl,
    ServerPlayerEntity = net.minecraft.server.level.ServerPlayer,
    CPlayerPacket = net.minecraft.network.protocol.game.ServerboundMovePlayerPacket,
    LivingEntity = net.minecraft.world.entity.LivingEntity,
    ActiveRenderInfo = net.minecraft.client.Camera,
    FirstPersonRenderer = net.minecraft.client.renderer.ItemInHandRenderer,
    AbstractClientPlayerEntity = net.minecraft.client.player.AbstractClientPlayer,
    ClientPlayerEntity = net.minecraft.client.player.LocalPlayer,
    ItemStack = net.minecraft.world.item.ItemStack,
    PlayerModel = net.minecraft.client.model.PlayerModel,
    MatrixStack = com.mojang.blaze3d.vertex.PoseStack

/**
 * Enable flying pose for wings
 */
easycore.inMethod(PlayerEntity.m_7594_()) // updatePose
    .atFirst(invokevirtual(PlayerEntity.m_21255_(), boolean)).append( // isElytraFlying
        aload(0),
        swap,
        invokestatic(WingsHooks.onFlightCheck(PlayerEntity, boolean), boolean)
    )

/**
 * Add exhaustion for winged flight
 */
easycore.inMethod(PlayerEntity.m_36378_(double, double, double)) // addMovementStat
    .atLast(invokestatic(java.lang.Math.round(float), int)).append(
        aload(0),
        dload(1),
        dload(3),
        dload(5),
        invokestatic(WingsHooks.onAddFlown(PlayerEntity, double, double, double))
    )

/**
 * Use flying speed for player movement validation
 */
easycore.inMethod(ServerPlayNetHandler.m_5682_(CPlayerPacket)) // processPlayer
    .atEach(invokevirtual(ServerPlayerEntity.m_21255_(), boolean)).append( // isElytraFlying
        aload(0),
        getfield(ServerPlayNetHandler.f_9743_, ServerPlayerEntity), // player
        swap,
        invokestatic(WingsHooks.onFlightCheck(PlayerEntity, boolean), boolean)
    )

/**
 * Add GetCameraEyeHeightEvent
 */
easycore.inMethod(ActiveRenderInfo.m_90565_()) // interpolateHeight
    .atFirst(invokevirtual(Entity.m_20236_())).append( // getEyeHeight
        aload(0),
        getfield(ActiveRenderInfo.f_90551_, Entity), // interpolateHeight
        swap,
        invokestatic(WingsHooks.onGetCameraEyeHeight(Entity, float), float)
    )

/**
 * Add smooth body rotation while flying
 */
easycore.inMethod(LivingEntity.m_5632_(float, float), float) // updateDistance
    .atFirst().prepend(
        aload(0),
        fload(1),
        invokestatic(WingsHooks.onUpdateBodyRotation(LivingEntity, float), boolean),
        ifeq(L0 = label()),
        bipush(0),
        i2f,
        freturn,
        L0
    )

/**
 * Add GetLivingHeadLimitEvent, rotate body with head rotation at limit
 */
easycore.inMethod(Entity.m_19884_(double, double)) // rotateTowards
    .atLast(_return).prepend(
        aload(0),
        dup,
        fload(6),
        //d2f,
        invokestatic(WingsHooksClient.onTurn(Entity, float))
    )

/**
 * Make offhand always render
 */
easycore.inMethod(FirstPersonRenderer.m_109371_(
        AbstractClientPlayerEntity,
        float,
        float,
        net.minecraft.world.InteractionHand,
        float,
        ItemStack,
        float,
        MatrixStack,
        net.minecraft.client.renderer.MultiBufferSource,
        int
        )) // renderItemInFirstPerson
    .atFirst(iload(11)).after(ifeq).append(
        aload(0),
        getfield(FirstPersonRenderer.f_109300_, ItemStack), // itemStackMainhand
        invokestatic(WingsHooksClient.onCheckRenderEmptyHand(boolean, ItemStack), boolean)
    )

/**
 * Replace reequip logic to control visibility of offhand, existing implementation left as dead code
 */
easycore.inMethod(net.minecraftforge.client.ForgeHooksClient.shouldCauseReequipAnimation(ItemStack, ItemStack, int), boolean)
    .atFirst().prepend(
        aload(0),
        aload(1),
        iload(2),
        invokestatic(WingsHooksClient.onCheckDoReequipAnimation(ItemStack, ItemStack, int), boolean),
        ireturn
    )

/**
 * Add AnimatePlayerModelEvent
 */
easycore.inMethod(PlayerModel.m_6973_(LivingEntity, float, float, float, float, float)) // setRotationAngles
    .atFirst(invokespecial(net.minecraft.client.model.HumanoidModel.m_6973_(LivingEntity, float, float, float, float, float))).append(
        aload(1),
        aload(0),
        fload(4),
        fload(6),
        invokestatic(WingsHooksClient.onSetPlayerRotationAngles(LivingEntity, PlayerModel, float, float))
    )

/**
 * Add ApplyPlayerRotationsEvent
 */
easycore.inMethod(net.minecraft.client.renderer.entity.player.PlayerRenderer.m_7523_(AbstractClientPlayerEntity, MatrixStack, float, float, float)) // applyRotations
    .atLast(_return).prepend(
        aload(1),
        aload(2),
        fload(5),
        invokestatic(WingsHooksClient.onApplyPlayerRotations(AbstractClientPlayerEntity, MatrixStack, float))
    )

/**
 * Don't treat being in fall_flying pose and not elytra flying as swimming when flying
 */
easycore.inMethod(LivingEntity.m_6067_()) // isActualySwimming
    .atFirst(invokevirtual(LivingEntity.m_21255_(), boolean)).append( // isElytraFlying
        aload(0),
        swap,
        invokestatic(WingsHooks.onFlightCheck(LivingEntity, boolean), boolean)
    )

/**
 * Disable crouching while flying
 */
easycore.inMethod(ClientPlayerEntity.m_8107_()) // livingTick
    .atFirst(invokevirtual(ClientPlayerEntity.m_6069_())).append( // isSwimming
        aload(0),
        swap,
        invokestatic(WingsHooks.onFlightCheck(LivingEntity, boolean), boolean)
    )

return easycore.build()
}
