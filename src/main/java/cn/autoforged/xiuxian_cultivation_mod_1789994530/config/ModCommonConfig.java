package cn.autoforged.xiuxian_cultivation_mod_1789994530.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 通用配置。可在 .minecraft/config/&lt;modid&gt;-common.toml 中修改。
 */
public final class ModCommonConfig {
    public static final ModCommonConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    static {
        Pair<ModCommonConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(ModCommonConfig::new);
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    /** [锁定] 功法可锁定生物的最大距离（格），默认 100。 */
    public final ModConfigSpec.IntValue lockMaxDistance;
    /** 是否在拥有真气的生物头顶显示真气数值条。 */
    public final ModConfigSpec.BooleanValue entityQiBarEnabled;

    private ModCommonConfig(ModConfigSpec.Builder builder) {
        builder.push("skill");
        lockMaxDistance = builder
                .comment("Maximum distance (blocks) that the Lock-On skill can lock a creature, default 100")
                .translation("config.xiuxian_cultivation_mod_1789994530.lock_max_distance")
                .defineInRange("lockMaxDistance", 100, 1, 512);
        builder.pop();

        builder.push("display");
        entityQiBarEnabled = builder
                .comment("Render a qi value bar above creatures that have qi")
                .translation("config.xiuxian_cultivation_mod_1789994530.entity_qi_bar")
                .define("entityQiBarEnabled", true);
        builder.pop();
    }
}
