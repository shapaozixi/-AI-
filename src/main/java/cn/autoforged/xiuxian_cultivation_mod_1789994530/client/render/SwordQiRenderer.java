package cn.autoforged.xiuxian_cultivation_mod_1789994530.client.render;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.entity.SwordQiProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * [武器强化] 剑气渲染器：用程序化几何画出一道<b>半圆弧形的蓝色光弧</b>。
 *
 * <p>不使用任何贴图 —— 顶点写入 POSITION_COLOR 格式的 {@link RenderType#lightning()}，
 * 自带发光混合，所以弧面看上去像发光的剑气。
 *
 * <p>坐标系说明：实体渲染时 PoseStack 已应用实体朝向（局部 Z+ 为前方），
 * 因此直接在 XY 平面画弧即可得到"横在飞行方向上"的月牙剑气。
 */
public class SwordQiRenderer extends EntityRenderer<SwordQiProjectile> {

    /** 半圆分成多少段（段数越多越圆滑）。 */
    private static final int SEGMENTS = 24;
    /** 弧的内半径（格）。 */
    private static final float INNER_RADIUS = 0.55F;
    /** 弧的外半径（格）。 */
    private static final float OUTER_RADIUS = 1.15F;
    /** 外缘额外描一圈更淡的光晕，做出边缘渐隐感。 */
    private static final float HALO_SCALE = 1.22F;

    /**
     * 实体渲染器必须返回一个有效贴图。本渲染器纯用几何顶点（POSITION_COLOR），
     * 不采样贴图，但返回 {@code null} 会踩到渲染管线的空指针，故给一个必定存在的原版贴图占位。
     */
    private static final ResourceLocation PLACEHOLDER_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/particle/particles.png");

    private static final int CORE_R = 130;
    private static final int CORE_G = 205;
    private static final int CORE_B = 255;
    private static final int HALO_R = 60;
    private static final int HALO_G = 140;
    private static final int HALO_B = 255;

    public SwordQiRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(SwordQiProjectile entity) {
        // 纯几何渲染，不采样贴图；仅返回占位以避免 null 贴图。
        return PLACEHOLDER_TEXTURE;
    }

    @Override
    public void render(SwordQiProjectile entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // 外圈淡光晕
        for (int i = 0; i < SEGMENTS; i++) {
            float a0 = angleAt(i);
            float a1 = angleAt(i + 1);
            emitQuad(consumer, matrix, a0, a1,
                    INNER_RADIUS * HALO_SCALE, OUTER_RADIUS * HALO_SCALE,
                    HALO_R, HALO_G, HALO_B, 90);
        }
        // 内圈实心弧（剑气本体）
        for (int i = 0; i < SEGMENTS; i++) {
            float a0 = angleAt(i);
            float a1 = angleAt(i + 1);
            emitQuad(consumer, matrix, a0, a1,
                    INNER_RADIUS, OUTER_RADIUS,
                    CORE_R, CORE_G, CORE_B, 215);
        }

        poseStack.popPose();
    }

    /** 第 i 段对应的角度（从 -90° 扫到 +90°，即右半圆）。 */
    private static float angleAt(int i) {
        return (float) Math.toRadians(-90.0 + 180.0 * i / SEGMENTS);
    }

    /** 把一段弧做成一个梯形四边形（内弧两点 + 外弧两点）。 */
    private static void emitQuad(VertexConsumer consumer, Matrix4f matrix,
                                 float a0, float a1,
                                 float innerRadius, float outerRadius,
                                 int r, int g, int b, int a) {
        float c0 = (float) Math.cos(a0);
        float s0 = (float) Math.sin(a0);
        float c1 = (float) Math.cos(a1);
        float s1 = (float) Math.sin(a1);

        consumer.addVertex(matrix, c0 * innerRadius, s0 * innerRadius, 0.0F).setColor(r, g, b, a);
        consumer.addVertex(matrix, c0 * outerRadius, s0 * outerRadius, 0.0F).setColor(r, g, b, a);
        consumer.addVertex(matrix, c1 * outerRadius, s1 * outerRadius, 0.0F).setColor(r, g, b, a);
        consumer.addVertex(matrix, c1 * innerRadius, s1 * innerRadius, 0.0F).setColor(r, g, b, a);
    }
}
