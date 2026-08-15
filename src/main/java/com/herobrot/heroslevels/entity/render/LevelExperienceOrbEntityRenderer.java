package com.herobrot.heroslevels.entity.render;

import com.herobrot.heroslevels.entity.LevelExperienceOrbEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class LevelExperienceOrbEntityRenderer extends EntityRenderer<LevelExperienceOrbEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/experience_orb.png");
    private static final RenderType LAYER = RenderType.itemEntityTranslucentCull(TEXTURE);

    public LevelExperienceOrbEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.15f;
        this.shadowStrength = 0.75f;
    }

    @Override
    protected int getBlockLightLevel(@NotNull LevelExperienceOrbEntity entity, @NotNull BlockPos pos) {
        return Mth.clamp(super.getBlockLightLevel(entity, pos) + 7, 0, 15);
    }

    @Override
    public void render(@NotNull LevelExperienceOrbEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        int j = entity.getOrbSize() * 100;
        float h = (float) (0) / 64.0F;
        float k = (float) (16) / 64.0F;
        float l = (float) (j / 4 * 16) / 64.0F;
        float m = (float) (j / 4 * 16 + 16) / 64.0F;
        float r = ((float) entity.tickCount + partialTicks) / 2.0F;

        int s = Math.max(80, (int) ((Mth.sin(r + 0.0F) + 1.0F) * 0.5F * 255.0F));
        int u = Math.max(100, (int) ((Mth.sin(r + (float) (Math.PI * 4.0 / 3.0)) + 1.0F) * 0.1F * 255.0F));

        poseStack.translate(0.0F, 0.1F, 0.0F);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(0.3F, 0.3F, 0.3F);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(LAYER);
        PoseStack.Pose entry = poseStack.last();

        vertex(vertexConsumer, entry, -0.5F, -0.25F, u, s, h, m, packedLight);
        vertex(vertexConsumer, entry, 0.5F, -0.25F, u, s, k, m, packedLight);
        vertex(vertexConsumer, entry, 0.5F, 0.75F, u, s, k, l, packedLight);
        vertex(vertexConsumer, entry, -0.5F, 0.75F, u, s, h, l, packedLight);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose matrix, float x, float y, int red, int green, float u, float v, int light) {
        vertexConsumer.addVertex(matrix, x, y, 0.0F)
                .setColor(red, green, 255, 128)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(matrix, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull LevelExperienceOrbEntity entity) {
        return TEXTURE;
    }
}