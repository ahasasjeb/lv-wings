package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class EntityRenderDispatcher implements ResourceManagerReloadListener {
    private static final RenderType SHADOW_RENDER_TYPE = RenderType.entityShadow(ResourceLocation.withDefaultNamespace("textures/misc/shadow.png"));
    private static final float MAX_SHADOW_RADIUS = 32.0F;
    private static final float SHADOW_POWER_FALLOFF_Y = 0.5F;
    private Map<EntityType<?>, EntityRenderer<?, ?>> renderers = ImmutableMap.of();
    /**
     * lists the various player skin types with their associated Renderer class instances.
     */
    private Map<PlayerSkin.Model, EntityRenderer<? extends Player, ?>> playerRenderers = Map.of();
    public final TextureManager textureManager;
    private Level level;
    public Camera camera;
    private Quaternionf cameraOrientation;
    public Entity crosshairPickEntity;
    private final ItemModelResolver itemModelResolver;
    private final MapRenderer mapRenderer;
    private final BlockRenderDispatcher blockRenderDispatcher;
    private final ItemInHandRenderer itemInHandRenderer;
    private final Font font;
    public final Options options;
    private final Supplier<EntityModelSet> entityModels;
    private final EquipmentAssetManager equipmentAssets;
    private boolean shouldRenderShadow = true;
    private boolean renderHitBoxes;

    public <E extends Entity> int getPackedLightCoords(E entity, float partialTicks) {
        return this.getRenderer(entity).getPackedLightCoords(entity, partialTicks);
    }

    public EntityRenderDispatcher(
        Minecraft minecraft,
        TextureManager textureManager,
        ItemModelResolver itemModelResolver,
        ItemRenderer itemRenderer,
        MapRenderer mapRenderer,
        BlockRenderDispatcher blockRenderDispatcher,
        Font font,
        Options options,
        Supplier<EntityModelSet> entityModels,
        EquipmentAssetManager equipmentModels
    ) {
        this.textureManager = textureManager;
        this.itemModelResolver = itemModelResolver;
        this.mapRenderer = mapRenderer;
        this.itemInHandRenderer = new ItemInHandRenderer(minecraft, this, itemRenderer, itemModelResolver);
        this.blockRenderDispatcher = blockRenderDispatcher;
        this.font = font;
        this.options = options;
        this.entityModels = entityModels;
        this.equipmentAssets = equipmentModels;
    }

    public <T extends Entity> EntityRenderer<? super T, ?> getRenderer(T entity) {
        if (entity instanceof AbstractClientPlayer abstractclientplayer) {
            PlayerSkin.Model playerskin$model = abstractclientplayer.getSkin().model();
            EntityRenderer<? extends Player, ?> entityrenderer = this.playerRenderers.get(playerskin$model);
            return (EntityRenderer<? super T, ?>)(entityrenderer != null ? entityrenderer : this.playerRenderers.get(PlayerSkin.Model.WIDE));
        } else {
            return (EntityRenderer<? super T, ?>)this.renderers.get(entity.getType());
        }
    }

    public void prepare(Level level, Camera activeRenderInfo, Entity entity) {
        this.level = level;
        this.camera = activeRenderInfo;
        this.cameraOrientation = activeRenderInfo.rotation();
        this.crosshairPickEntity = entity;
    }

    public void overrideCameraOrientation(Quaternionf cameraOrientation) {
        this.cameraOrientation = cameraOrientation;
    }

    public void setRenderShadow(boolean renderShadow) {
        this.shouldRenderShadow = renderShadow;
    }

    public void setRenderHitBoxes(boolean debugBoundingBox) {
        this.renderHitBoxes = debugBoundingBox;
    }

    public boolean shouldRenderHitBoxes() {
        return this.renderHitBoxes;
    }

    public <E extends Entity> boolean shouldRender(E entity, Frustum frustum, double camX, double camY, double camZ) {
        EntityRenderer<? super E, ?> entityrenderer = this.getRenderer(entity);
        return entityrenderer.shouldRender(entity, frustum, camX, camY, camZ);
    }

    public <E extends Entity> void render(
        E entity, double xOffset, double yOffset, double zOffset, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight
    ) {
        EntityRenderer<? super E, ?> entityrenderer = this.getRenderer(entity);
        this.render(entity, xOffset, yOffset, zOffset, partialTick, poseStack, bufferSource, packedLight, entityrenderer);
    }

    private <E extends Entity, S extends EntityRenderState> void render(
        E entity,
        double xOffset,
        double yOffset,
        double zOffset,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int packedLight,
        EntityRenderer<? super E, S> renderer
    ) {
        try {
            S s = renderer.createRenderState(entity, partialTick);
            Vec3 vec3 = renderer.getRenderOffset(s);
            double d3 = xOffset + vec3.x();
            double d0 = yOffset + vec3.y();
            double d1 = zOffset + vec3.z();
            poseStack.pushPose();
            poseStack.translate(d3, d0, d1);
            renderer.render(s, poseStack, bufferSource, packedLight);
            if (s.displayFireAnimation) {
                this.renderFlame(poseStack, bufferSource, s, Mth.rotationAroundAxis(Mth.Y_AXIS, this.cameraOrientation, new Quaternionf()));
            }

            if (entity instanceof Player) {
                poseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
            }

            if (this.options.entityShadows().get() && this.shouldRenderShadow && !s.isInvisible) {
                float f = renderer.getShadowRadius(s);
                if (f > 0.0F) {
                    double d2 = s.distanceToCameraSq;
                    float f1 = (float)((1.0 - d2 / 256.0) * (double)renderer.getShadowStrength(s));
                    if (f1 > 0.0F) {
                        renderShadow(poseStack, bufferSource, s, f1, partialTick, this.level, Math.min(f, 32.0F));
                    }
                }
            }

            if (!(entity instanceof Player)) {
                poseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
            }

            if (this.renderHitBoxes && !s.isInvisible && !Minecraft.getInstance().showOnlyReducedInfo()) {
                renderHitbox(poseStack, bufferSource.getBuffer(RenderType.lines()), entity, partialTick, 1.0F, 1.0F, 1.0F);
            }

            poseStack.popPose();
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering entity in world");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being rendered");
            entity.fillCrashReportCategory(crashreportcategory);
            CrashReportCategory crashreportcategory1 = crashreport.addCategory("Renderer details");
            crashreportcategory1.setDetail("Assigned renderer", renderer);
            crashreportcategory1.setDetail("Location", CrashReportCategory.formatLocation(this.level, xOffset, yOffset, zOffset));
            crashreportcategory1.setDetail("Delta", partialTick);
            throw new ReportedException(crashreport);
        }
    }

    private static void renderServerSideHitbox(PoseStack poseStack, Entity p_entity, MultiBufferSource bufferSource) {
        Entity entity = getServerSideEntity(p_entity);
        if (entity == null) {
            DebugRenderer.renderFloatingText(poseStack, bufferSource, "Missing", p_entity.getX(), p_entity.getBoundingBox().maxY + 1.5, p_entity.getZ(), -65536);
        } else {
            poseStack.pushPose();
            poseStack.translate(entity.getX() - p_entity.getX(), entity.getY() - p_entity.getY(), entity.getZ() - p_entity.getZ());
            renderHitbox(poseStack, bufferSource.getBuffer(RenderType.lines()), entity, 1.0F, 0.0F, 1.0F, 0.0F);
            ShapeRenderer.renderVector(poseStack, bufferSource.getBuffer(RenderType.lines()), new Vector3f(), entity.getDeltaMovement(), -256);
            poseStack.popPose();
        }
    }

    @Nullable
    private static Entity getServerSideEntity(Entity entity) {
        IntegratedServer integratedserver = Minecraft.getInstance().getSingleplayerServer();
        if (integratedserver != null) {
            ServerLevel serverlevel = integratedserver.getLevel(entity.level().dimension());
            if (serverlevel != null) {
                return serverlevel.getEntity(entity.getId());
            }
        }

        return null;
    }

    private static void renderHitbox(
        PoseStack poseStack, VertexConsumer buffer, Entity p_entity, float red, float green, float blue, float alpha
    ) {
        AABB aabb = p_entity.getBoundingBox().move(-p_entity.getX(), -p_entity.getY(), -p_entity.getZ());
        ShapeRenderer.renderLineBox(poseStack, buffer, aabb, green, blue, alpha, 1.0F);
        if (p_entity.isMultipartEntity()) {
            double d0 = -Mth.lerp((double)red, p_entity.xOld, p_entity.getX());
            double d1 = -Mth.lerp((double)red, p_entity.yOld, p_entity.getY());
            double d2 = -Mth.lerp((double)red, p_entity.zOld, p_entity.getZ());

            for (net.neoforged.neoforge.entity.PartEntity<?> enderdragonpart : p_entity.getParts()) {
                poseStack.pushPose();
                double d3 = d0 + Mth.lerp((double)red, enderdragonpart.xOld, enderdragonpart.getX());
                double d4 = d1 + Mth.lerp((double)red, enderdragonpart.yOld, enderdragonpart.getY());
                double d5 = d2 + Mth.lerp((double)red, enderdragonpart.zOld, enderdragonpart.getZ());
                poseStack.translate(d3, d4, d5);
                ShapeRenderer.renderLineBox(
                    poseStack,
                    buffer,
                    enderdragonpart.getBoundingBox().move(-enderdragonpart.getX(), -enderdragonpart.getY(), -enderdragonpart.getZ()),
                    0.25F,
                    1.0F,
                    0.0F,
                    1.0F
                );
                poseStack.popPose();
            }
        }

        if (p_entity instanceof LivingEntity) {
            float f1 = 0.01F;
            ShapeRenderer.renderLineBox(
                poseStack,
                buffer,
                aabb.minX,
                (double)(p_entity.getEyeHeight() - 0.01F),
                aabb.minZ,
                aabb.maxX,
                (double)(p_entity.getEyeHeight() + 0.01F),
                aabb.maxZ,
                1.0F,
                0.0F,
                0.0F,
                1.0F
            );
        }

        Entity entity = p_entity.getVehicle();
        if (entity != null) {
            float f = Math.min(entity.getBbWidth(), p_entity.getBbWidth()) / 2.0F;
            float f2 = 0.0625F;
            Vec3 vec3 = entity.getPassengerRidingPosition(p_entity).subtract(p_entity.position());
            ShapeRenderer.renderLineBox(
                poseStack,
                buffer,
                vec3.x - (double)f,
                vec3.y,
                vec3.z - (double)f,
                vec3.x + (double)f,
                vec3.y + 0.0625,
                vec3.z + (double)f,
                1.0F,
                1.0F,
                0.0F,
                1.0F
            );
        }

        ShapeRenderer.renderVector(
            poseStack, buffer, new Vector3f(0.0F, p_entity.getEyeHeight(), 0.0F), p_entity.getViewVector(red).scale(2.0), -16776961
        );
    }

    private void renderFlame(PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderState renderState, Quaternionf quaternion) {
        TextureAtlasSprite textureatlassprite = ModelBakery.FIRE_0.sprite();
        TextureAtlasSprite textureatlassprite1 = ModelBakery.FIRE_1.sprite();
        poseStack.pushPose();
        float f = renderState.boundingBoxWidth * 1.4F;
        poseStack.scale(f, f, f);
        float f1 = 0.5F;
        float f2 = 0.0F;
        float f3 = renderState.boundingBoxHeight / f;
        float f4 = 0.0F;
        poseStack.mulPose(quaternion);
        poseStack.translate(0.0F, 0.0F, 0.3F - (float)((int)f3) * 0.02F);
        float f5 = 0.0F;
        int i = 0;
        VertexConsumer vertexconsumer = bufferSource.getBuffer(Sheets.cutoutBlockSheet());

        for (PoseStack.Pose posestack$pose = poseStack.last(); f3 > 0.0F; i++) {
            TextureAtlasSprite textureatlassprite2 = i % 2 == 0 ? textureatlassprite : textureatlassprite1;
            float f6 = textureatlassprite2.getU0();
            float f7 = textureatlassprite2.getV0();
            float f8 = textureatlassprite2.getU1();
            float f9 = textureatlassprite2.getV1();
            if (i / 2 % 2 == 0) {
                float f10 = f8;
                f8 = f6;
                f6 = f10;
            }

            fireVertex(posestack$pose, vertexconsumer, -f1 - 0.0F, 0.0F - f4, f5, f8, f9);
            fireVertex(posestack$pose, vertexconsumer, f1 - 0.0F, 0.0F - f4, f5, f6, f9);
            fireVertex(posestack$pose, vertexconsumer, f1 - 0.0F, 1.4F - f4, f5, f6, f7);
            fireVertex(posestack$pose, vertexconsumer, -f1 - 0.0F, 1.4F - f4, f5, f8, f7);
            f3 -= 0.45F;
            f4 -= 0.45F;
            f1 *= 0.9F;
            f5 -= 0.03F;
        }

        poseStack.popPose();
    }

    private static void fireVertex(
        PoseStack.Pose matrixEntry, VertexConsumer buffer, float x, float y, float z, float texU, float texV
    ) {
        buffer.addVertex(matrixEntry, x, y, z)
            .setColor(-1)
            .setUv(texU, texV)
            .setUv1(0, 10)
            .setLight(240)
            .setNormal(matrixEntry, 0.0F, 1.0F, 0.0F);
    }

    private static void renderShadow(
        PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderState renderState, float shadowStrength, float partialTick, LevelReader level, float size
    ) {
        float f = Math.min(shadowStrength / 0.5F, size);
        int i = Mth.floor(renderState.x - (double)size);
        int j = Mth.floor(renderState.x + (double)size);
        int k = Mth.floor(renderState.y - (double)f);
        int l = Mth.floor(renderState.y);
        int i1 = Mth.floor(renderState.z - (double)size);
        int j1 = Mth.floor(renderState.z + (double)size);
        PoseStack.Pose posestack$pose = poseStack.last();
        VertexConsumer vertexconsumer = bufferSource.getBuffer(SHADOW_RENDER_TYPE);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int k1 = i1; k1 <= j1; k1++) {
            for (int l1 = i; l1 <= j; l1++) {
                blockpos$mutableblockpos.set(l1, 0, k1);
                ChunkAccess chunkaccess = level.getChunk(blockpos$mutableblockpos);

                for (int i2 = k; i2 <= l; i2++) {
                    blockpos$mutableblockpos.setY(i2);
                    float f1 = shadowStrength - (float)(renderState.y - (double)blockpos$mutableblockpos.getY()) * 0.5F;
                    renderBlockShadow(
                        posestack$pose, vertexconsumer, chunkaccess, level, blockpos$mutableblockpos, renderState.x, renderState.y, renderState.z, size, f1
                    );
                }
            }
        }
    }

    private static void renderBlockShadow(
        PoseStack.Pose pose,
        VertexConsumer buffer,
        ChunkAccess chunk,
        LevelReader level,
        BlockPos pos,
        double x,
        double y,
        double z,
        float size,
        float weight
    ) {
        BlockPos blockpos = pos.below();
        BlockState blockstate = chunk.getBlockState(blockpos);
        if (blockstate.getRenderShape() != RenderShape.INVISIBLE && level.getMaxLocalRawBrightness(pos) > 3) {
            if (blockstate.isCollisionShapeFullBlock(chunk, blockpos)) {
                VoxelShape voxelshape = blockstate.getShape(chunk, blockpos);
                if (!voxelshape.isEmpty()) {
                    float f = LightTexture.getBrightness(level.dimensionType(), level.getMaxLocalRawBrightness(pos));
                    float f1 = weight * 0.5F * f;
                    if (f1 >= 0.0F) {
                        if (f1 > 1.0F) {
                            f1 = 1.0F;
                        }

                        int i = ARGB.color(Mth.floor(f1 * 255.0F), 255, 255, 255);
                        AABB aabb = voxelshape.bounds();
                        double d0 = (double)pos.getX() + aabb.minX;
                        double d1 = (double)pos.getX() + aabb.maxX;
                        double d2 = (double)pos.getY() + aabb.minY;
                        double d3 = (double)pos.getZ() + aabb.minZ;
                        double d4 = (double)pos.getZ() + aabb.maxZ;
                        float f2 = (float)(d0 - x);
                        float f3 = (float)(d1 - x);
                        float f4 = (float)(d2 - y);
                        float f5 = (float)(d3 - z);
                        float f6 = (float)(d4 - z);
                        float f7 = -f2 / 2.0F / size + 0.5F;
                        float f8 = -f3 / 2.0F / size + 0.5F;
                        float f9 = -f5 / 2.0F / size + 0.5F;
                        float f10 = -f6 / 2.0F / size + 0.5F;
                        shadowVertex(pose, buffer, i, f2, f4, f5, f7, f9);
                        shadowVertex(pose, buffer, i, f2, f4, f6, f7, f10);
                        shadowVertex(pose, buffer, i, f3, f4, f6, f8, f10);
                        shadowVertex(pose, buffer, i, f3, f4, f5, f8, f9);
                    }
                }
            }
        }
    }

    private static void shadowVertex(
        PoseStack.Pose pose, VertexConsumer consumer, int color, float offsetX, float offsetY, float offsetZ, float u, float v
    ) {
        Vector3f vector3f = pose.pose().transformPosition(offsetX, offsetY, offsetZ, new Vector3f());
        consumer.addVertex(vector3f.x(), vector3f.y(), vector3f.z(), color, u, v, OverlayTexture.NO_OVERLAY, 15728880, 0.0F, 1.0F, 0.0F);
    }

    /**
     * World sets this RenderManager's worldObj to the world provided
     */
    public void setLevel(@Nullable Level level) {
        this.level = level;
        if (level == null) {
            this.camera = null;
        }
    }

    public double distanceToSqr(Entity entity) {
        return this.camera.getPosition().distanceToSqr(entity.position());
    }

    public double distanceToSqr(double x, double y, double z) {
        return this.camera.getPosition().distanceToSqr(x, y, z);
    }

    public Quaternionf cameraOrientation() {
        return this.cameraOrientation;
    }

    public ItemInHandRenderer getItemInHandRenderer() {
        return this.itemInHandRenderer;
    }

    public Map<PlayerSkin.Model, EntityRenderer<? extends Player, ?>> getSkinMap() {
        return java.util.Collections.unmodifiableMap(playerRenderers);
    }

    @Override
    public void onResourceManagerReload(ResourceManager p_174004_) {
        EntityRendererProvider.Context entityrendererprovider$context = new EntityRendererProvider.Context(
            this, this.itemModelResolver, this.mapRenderer, this.blockRenderDispatcher, p_174004_, this.entityModels.get(), this.equipmentAssets, this.font
        );
        this.renderers = EntityRenderers.createEntityRenderers(entityrendererprovider$context);
        this.playerRenderers = EntityRenderers.createPlayerRenderers(entityrendererprovider$context);
        net.neoforged.fml.ModLoader.postEvent(new net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers(renderers, playerRenderers, entityrendererprovider$context));
    }
}
