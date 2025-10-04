function initializeCoreMod() {
Java.type('net.minecraftforge.coremod.api.ASMAPI').loadFile('easycorelib.js')
//net/minecraftforge/coremod/api/ASMAPI.java

easycore.include('me', 'com')

var Player = net.minecraft.world.entity.player.Player,
    LivingEntity = net.minecraft.world.entity.LivingEntity,
    Entity = net.minecraft.world.entity.Entity,
    WingsHooks = com.toni.wings.server.asm.WingsHooks,
    WingsHooksClient = com.toni.wings.server.asm.WingsHooksClient,
    ResourceLocation = net.minecraft.resources.ResourceLocation,
    ServerGamePacketListenerImpl = net.minecraft.server.network.ServerGamePacketListenerImpl,
    ServerPlayer = net.minecraft.server.level.ServerPlayer,
    ServerboundMovePlayerPacket = net.minecraft.network.protocol.game.ServerboundMovePlayerPacket,
    Camera = net.minecraft.client.Camera,
    ItemInHandRenderer = net.minecraft.client.renderer.ItemInHandRenderer,
    AbstractClientPlayer = net.minecraft.client.player.AbstractClientPlayer,
    LocalPlayer = net.minecraft.client.player.LocalPlayer,
    RemotePlayer = net.minecraft.client.player.RemotePlayer,
    ItemStack = net.minecraft.world.item.ItemStack,
    PlayerModel = net.minecraft.client.model.PlayerModel,
    PoseStack = com.mojang.blaze3d.vertex.PoseStack

/**
 * Enable flying pose for wings
 */
/*easycore.inMethod(Player.m_7594_()) // updatePose
    .atFirst(invokevirtual(Player.isFallFlying(), boolean)).append( // isElytraFlying
        aload(0),
        swap,
        invokestatic(WingsHooks.onFlightCheck(Player, boolean), boolean)
    )
*/
/**
 * Add exhaustion for winged flight
 */
easycore.inMethod(ServerPlayer.checkMovementStatistics(double, double, double)) // addMovementStat//checkMovementStatistics
    .atLast(invokestatic(java.lang.Math.round(float), int)).append(
        aload(0),
        dload(1),
        dload(3),
        dload(5),
        invokestatic(WingsHooks.onAddFlown(Player, double, double, double))
    )

/**
 * Use flying speed for player movement validation
 */
easycore.inMethod(ServerGamePacketListenerImpl.handleMovePlayer(ServerboundMovePlayerPacket)) // processPlayer
    .atEach(invokevirtual(ServerPlayer.isFallFlying(), boolean)).append( // isElytraFlying//isFallFlying
        aload(0),
        getfield(ServerGamePacketListenerImpl.player, ServerPlayer), // player
        swap,
        invokestatic(WingsHooks.onFlightCheck(Player, boolean), boolean)
    )

/**
 * Add GetCameraEyeHeightEvent
 */
easycore.inMethod(Camera.tick()) // interpolateHeight
    .atFirst(invokevirtual(Entity.getEyeHeight())).append( // getEyeHeight
        aload(0),
        getfield(Camera.entity, Entity), // interpolateHeight
        swap,
        invokestatic(WingsHooks.onGetCameraEyeHeight(Entity, float), float)
    )

/**
 * Add smooth body rotation while flying
 */
easycore.inMethod(LivingEntity.tickHeadTurn(float, float), float) // updateDistance
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
/*easycore.inMethod(Entity.m_19884_(double, double)) // rotateTowards
    .atLast(_return).prepend(
        aload(0),
        dup,
        dload(4),
        d2f,
        invokestatic(WingsHooksClient.onTurn(Entity, float))
    )
*/
/**
 * Make offhand always render
 */
easycore.inMethod(ItemInHandRenderer.renderArmWithItem(
        AbstractClientPlayer,
        float,
        float,
        net.minecraft.world.InteractionHand,
        float,
        ItemStack,
        float,
        PoseStack,
        net.minecraft.client.renderer.MultiBufferSource,
        int
        )) // renderItemInFirstPerson
    .atFirst(iload(11)).after(ifeq).append(
        aload(0),
        getfield(ItemInHandRenderer.mainHandItem, ItemStack), // itemStackMainhand
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
easycore.inMethod(PlayerModel.setupAnim(net.minecraft.client.renderer.entity.state.PlayerRenderState)) // setRotationAngles
    .atLast(_return).prepend(
        aload(1),
        aload(0),
        invokestatic(WingsHooksClient.onSetPlayerRotationAngles(net.minecraft.client.renderer.entity.state.PlayerRenderState, PlayerModel))
    )

/**
 * Track player for render state callbacks
 */
easycore.inMethod(net.minecraft.client.renderer.entity.player.PlayerRenderer.extractRenderState(
        AbstractClientPlayer,
        net.minecraft.client.renderer.entity.state.PlayerRenderState,
        float
    ))
    .atFirst().prepend(
        aload(1),
        aload(2),
        invokestatic(WingsHooksClient.onExtractPlayerRenderState(AbstractClientPlayer, net.minecraft.client.renderer.entity.state.PlayerRenderState))
    )

/**
 * Add ApplyPlayerRotationsEvent
 */
easycore.inMethod(net.minecraft.client.renderer.entity.player.PlayerRenderer.setupRotations(
        net.minecraft.client.renderer.entity.state.PlayerRenderState,
        PoseStack,
        float,
        float
    )) // applyRotations
    .atLast(_return).prepend(
        aload(1),
        aload(2),
        invokestatic(WingsHooksClient.onApplyPlayerRotations(net.minecraft.client.renderer.entity.state.PlayerRenderState, PoseStack))
    )

/**
 * Don't treat being in fall_flying pose and not elytra flying as swimming when flying
 */
easycore.inMethod(LivingEntity.isVisuallySwimming()) // isActualySwimming
    .atFirst(invokevirtual(LivingEntity.isFallFlying(), boolean)).append( // isElytraFlying
        aload(0),
        swap,
        invokestatic(WingsHooks.onFlightCheck(LivingEntity, boolean), boolean)
    )

/**
 * Disable crouching while flying
 */
/*easycore.inMethod(RemotePlayer.m_8107_()) // livingTick
    .atFirst(invokevirtual(RemotePlayer.m_6069_())).append( // isSwimming
        aload(0),
        swap,
        invokestatic(WingsHooks.onFlightCheck(LivingEntity, boolean), boolean)
    )
*/
return easycore.build()
}
