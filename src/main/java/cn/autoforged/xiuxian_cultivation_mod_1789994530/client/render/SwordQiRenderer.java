package cn.autoforged.xiuxian_cultivation_mod_1789994530.client.render;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.entity.SwordQiProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * [武器强化] 剑气渲染器。
 *
 * <p><b>当前方案：剑气外观完全由原版粒子构成</b>，见
 * {@link SwordQiProjectile#tick()} 里每 tick 调用的 {@code emitArcParticles()} —— 它沿一道拱形弧
 * 铺 {@code ParticleTypes.FIREWORK}（白色十字）与 {@code ParticleTypes.DRAGON_BREATH}（粉紫光晕）。
 *
 * <p>因此这里<b>刻意不绘制任何几何体</b>，否则会在粒子之外再叠一层实心弧，出现双重影像。
 * 本类的存在只为一个目的：把实体注册到渲染管线（实体没有渲染器不会被渲染，粒子仍会正常生成）。
 *
 * <p><b>如果将来粒子开销过大</b>（一次挥砍约产生 12 段 × 2 颗 × 飞行 tick 数），
 * 可以把粒子关掉，改为在本类的 {@code render} 里用几何顶点或实例化渲染重建同一个拱形弧——
 * 参考 Photon（Unity 风格粒子/拖尾 + GPU instancing）的思路：
 * 一次性 GPU 批量绘制比逐个粒子实体便宜得多。
 */
public class SwordQiRenderer extends EntityRenderer<SwordQiProjectile> {

    /**
     * 实体渲染器必须返回一个有效贴图。本渲染器不绘制任何东西、也不采样贴图，
     * 但返回 {@code null} 会踩到渲染管线的空指针，故给一个必定存在的原版贴图占位。
     */
    private static final ResourceLocation PLACEHOLDER_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/particle/particles.png");

    public SwordQiRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(SwordQiProjectile entity) {
        return PLACEHOLDER_TEXTURE;
    }

    @Override
    public void render(SwordQiProjectile entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        // 外观由粒子承担，这里不绘制几何体（原因见类注释）。
    }
}
