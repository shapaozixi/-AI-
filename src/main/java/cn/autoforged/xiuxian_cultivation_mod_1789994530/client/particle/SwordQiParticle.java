package cn.autoforged.xiuxian_cultivation_mod_1789994530.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * 剑气粒子的客户端实现：<b>蓝色、零重力、极短寿命</b>。
 *
 * <p>三个关键参数直接对应需求：
 * <ul>
 *   <li>{@link #LIFETIME_TICKS} = 4 —— 剑气速度 0.95 格/tick，4 tick 的存活期意味着
 *       最老的粒子离剑气本体最多约 <b>3.8 格</b>，拖尾不会拖出一长条；</li>
 *   <li>{@code gravity = 0} + {@code hasPhysics = false} —— 粒子既不自由落体也不被方块推挤，
 *       稳稳悬在生成点，不会「下坠」；</li>
 *   <li>{@code setColor(...)} —— 固定为剑气蓝。</li>
 * </ul>
 */
public class SwordQiParticle extends TextureSheetParticle {

    /**
     * 存活刻数（tick）。
     * 拖尾长度 ≈ 剑气速度 × 该值 = 0.95 × 4 ≈ 3.8 格，满足「前后粒子相差不超过三格左右」。
     * 想更短就调小，想更长就调大。
     */
    private static final int LIFETIME_TICKS = 4;

    /** 剑气主色（RGB 0~1）：偏青的亮蓝。 */
    private static final float COLOR_R = 0.42F;
    private static final float COLOR_G = 0.74F;
    private static final float COLOR_B = 1.00F;

    protected SwordQiParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.lifetime = LIFETIME_TICKS;
        this.gravity = 0.0F;          // 零重力：绝不下坠
        this.hasPhysics = false;      // 不被方块碰撞推挤
        this.friction = 1.0F;         // 不再衰减速度（本来也没有初速度）
        this.quadSize = 0.16F;
        this.alpha = 1.0F;
        this.setColor(COLOR_R, COLOR_G, COLOR_B);
        this.pickSprite(sprites);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        // 半透明精灵表：带 alpha 混合，弧刃边缘能自然融进背景
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        // 随寿命线性收缩：刚生成时最大最亮，临近消散时缩到 40%，看起来像刃光在收束
        float remain = 1.0F - (float) this.age / (float) this.lifetime;
        return this.quadSize * (0.4F + 0.6F * remain);
    }

    /** 粒子工厂：由 NeoForge 在客户端注册，负责生成 {@link SwordQiParticle}。 */
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new SwordQiParticle(level, x, y, z, this.sprites);
        }
    }
}
