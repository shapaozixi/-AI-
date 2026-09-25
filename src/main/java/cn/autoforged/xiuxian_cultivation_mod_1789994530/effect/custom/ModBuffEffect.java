package cn.autoforged.xiuxian_cultivation_mod_1789994530.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 通用的功能性 buff 效果：具体逻辑由 {@code CultivationHelper} 在玩家 tick 中处理，
 * 这里只提供效果定义、名称与图标颜色。
 */
public class ModBuffEffect extends MobEffect {
    public ModBuffEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
