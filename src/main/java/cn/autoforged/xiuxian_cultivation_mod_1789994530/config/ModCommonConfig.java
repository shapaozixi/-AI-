package cn.autoforged.xiuxian_cultivation_mod_1789994530.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 通用配置。可在 .minecraft/config/&lt;modid&gt;-common.toml 中修改。
 *
 * <p>改完保存后重进世界（或执行 /reload）即可生效。
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

    // ==================== 功法 ====================

    /** [锁定] 功法可锁定生物的最大距离（格），默认 100。 */
    public final ModConfigSpec.IntValue lockMaxDistance;

    // ==================== 显示 ====================

    /** 是否在拥有真气的生物头顶显示真气数值条。 */
    public final ModConfigSpec.BooleanValue entityQiBarEnabled;

    // ==================== 破坏地形 ====================

    /** [武器强化] 剑气飞行途中是否破坏方块。设 false 就完全不动地形。 */
    public final ModConfigSpec.BooleanValue swordQiBreakBlocks;
    /** [真气强化] 近战攻击是否破坏方块。 */
    public final ModConfigSpec.BooleanValue meleeBreakBlocks;
    /**
     * 破坏范围与真气的比例：每消耗这么多真气，破坏半径 +1 格。
     * 调小 = 破坏更猛；调大 = 破坏更保守。
     */
    public final ModConfigSpec.DoubleValue terrainQiPerRadius;
    /** 破坏半径上限（格），防止把地图犁穿。 */
    public final ModConfigSpec.IntValue terrainMaxRadius;
    /** 单次破坏的方块数上限，防止低配机卡顿。 */
    public final ModConfigSpec.IntValue terrainMaxBlocks;
    /** 剑气破坏的强度系数：剑气用「消耗真气 × 该系数」去算破坏半径。调小则剑气破坏更弱。 */
    public final ModConfigSpec.DoubleValue swordQiTerrainFactor;

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

        builder.push("terrain");
        swordQiBreakBlocks = builder
                .comment("Whether sword qi destroys blocks along its flight path.",
                         "Set to false if you don't want your builds damaged by sword qi.")
                .translation("config.xiuxian_cultivation_mod_1789994530.sword_qi_break_blocks")
                .define("swordQiBreakBlocks", true);

        meleeBreakBlocks = builder
                .comment("Whether melee attacks destroy blocks while Qi Enhancement is active.")
                .translation("config.xiuxian_cultivation_mod_1789994530.melee_break_blocks")
                .define("meleeBreakBlocks", true);

        terrainQiPerRadius = builder
                .comment("How much qi is needed per +1 block of destruction radius.",
                         "Smaller = much more destructive. Default 45.")
                .translation("config.xiuxian_cultivation_mod_1789994530.terrain_qi_per_radius")
                .defineInRange("terrainQiPerRadius", 45.0D, 1.0D, 10000.0D);

        terrainMaxRadius = builder
                .comment("Maximum destruction radius in blocks. Default 6.")
                .translation("config.xiuxian_cultivation_mod_1789994530.terrain_max_radius")
                .defineInRange("terrainMaxRadius", 6, 1, 32);

        terrainMaxBlocks = builder
                .comment("Maximum number of blocks destroyed per event - lower this if you get lag spikes. Default 96.")
                .translation("config.xiuxian_cultivation_mod_1789994530.terrain_max_blocks")
                .defineInRange("terrainMaxBlocks", 96, 1, 4096);

        swordQiTerrainFactor = builder
                .comment("Sword qi destruction strength factor. Radius is computed from",
                         "(qi spent x this factor). Smaller = weaker sword qi destruction. Default 0.30.")
                .translation("config.xiuxian_cultivation_mod_1789994530.sword_qi_terrain_factor")
                .defineInRange("swordQiTerrainFactor", 0.30D, 0.0D, 10.0D);
        builder.pop();
    }
}
