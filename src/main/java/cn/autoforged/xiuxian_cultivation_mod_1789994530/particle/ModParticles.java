package cn.autoforged.xiuxian_cultivation_mod_1789994530.particle;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 本模组的粒子类型注册。
 *
 * <p>为什么要自定义粒子（而不是直接用原版粒子）：
 * <ul>
 *   <li><b>寿命可控</b> —— 原版粒子寿命由游戏写死（约 1 秒），
 *       剑气飞得快，1 秒的拖尾会拉出十几格长；自定义粒子能把它压到 4 tick；</li>
 *   <li><b>无重力</b> —— 原版粒子大多会下坠，剑气粒子应该稳稳悬在原位；</li>
 *   <li><b>颜色可控</b> —— 想要精确的「剑气蓝」，原版粒子只有固定的几种颜色。</li>
 * </ul>
 */
public final class ModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, XiuxianCultivationMod.MODID);

    /**
     * 剑气粒子：蓝色、零重力、极短寿命。
     *
     * <p>{@code overrideLimiter = false} 表示照常受粒子数量限制；设 true 会绕过限制（不推荐）。
     * 具体外观与寿命见客户端实现 {@code client.particle.SwordQiParticle}。
     */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SWORD_QI =
            PARTICLES.register("sword_qi", () -> new SimpleParticleType(false));

    private ModParticles() {
    }
}
