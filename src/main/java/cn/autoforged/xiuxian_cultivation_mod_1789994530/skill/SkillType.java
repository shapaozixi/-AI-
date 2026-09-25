package cn.autoforged.xiuxian_cultivation_mod_1789994530.skill;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import net.minecraft.network.chat.Component;

/**
 * 功法技能类型。
 *
 * <p>id 同时承担两个角色：技能槽（Z/X/C）里存储的值，以及「已习得位掩码」
 * （{@code CultivationData.learnedSkills}）中的位号；0 一律表示「空」。
 *
 * <p>[重建说明] 原源码包中缺少本文件（有三个类 import 它却找不到），
 * 此处依据调用方用到的成员（{@code byId} / {@code bit()} / {@code displayName()} /
 * {@code values()} / {@code ALL_MASK}）以及语言文件中已存在的键名
 * {@code skill.<modid>.<key>} 精确还原。
 */
public enum SkillType {
    /** 锁定：锁定视线内生物，冲刺可瞬移到其面前，并持续注视目标。 */
    LOCK_ON(1, "lock_on"),
    /** 真气强化：攻击与位移按真气消耗量破坏地形。 */
    QI_ENHANCE(2, "qi_enhance_skill"),
    /** 武器强化：真气回复提升；手持剑/斧左键即可挥出剑气。 */
    WEAPON_ENHANCE(3, "weapon_enhance");

    /** 全部功法的位掩码（位 1、2、3）。 */
    public static final int ALL_MASK = 0b1110;

    private final int id;
    private final String langKey;

    SkillType(int id, String langKey) {
        this.id = id;
        this.langKey = langKey;
    }

    /** 功法 id（1/2/3）；技能槽里存的就是它，0 表示空槽。 */
    public int id() {
        return this.id;
    }

    /** 本功法在 learnedSkills 位掩码中占的位。 */
    public int bit() {
        return 1 << this.id;
    }

    /** 功法显示名，走语言文件 {@code skill.<modid>.<key>}。 */
    public Component displayName() {
        return Component.translatable("skill." + XiuxianCultivationMod.MODID + "." + this.langKey);
    }

    /** 按 id 取功法；传入 0 或未知 id 时返回 null（调用方据此判断空槽）。 */
    public static SkillType byId(int id) {
        for (SkillType skill : values()) {
            if (skill.id == id) {
                return skill;
            }
        }
        return null;
    }
}
