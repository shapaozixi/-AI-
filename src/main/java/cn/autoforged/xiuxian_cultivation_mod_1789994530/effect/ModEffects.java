package cn.autoforged.xiuxian_cultivation_mod_1789994530.effect;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.effect.custom.ModBuffEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** 功法 buff 状态效果注册。 */
public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, XiuxianCultivationMod.MODID);

    /** [真气强化] buff：持续期间攻击/位移按真气消耗破坏地形，每秒消耗真气。 */
    public static final DeferredHolder<MobEffect, MobEffect> QI_ENHANCEMENT =
            EFFECTS.register("qi_enhancement", () -> new ModBuffEffect(MobEffectCategory.BENEFICIAL, 0x33E5FF));

    /** [武器强化] buff：持续期间真气回复提升，手持武器攻击挥出剑气。 */
    public static final DeferredHolder<MobEffect, MobEffect> WEAPON_ENHANCEMENT =
            EFFECTS.register("weapon_enhancement", () -> new ModBuffEffect(MobEffectCategory.BENEFICIAL, 0xB060FF));

    private ModEffects() {
    }
}
