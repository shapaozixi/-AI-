package cn.autoforged.xiuxian_cultivation_mod_1789994530.attachment;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.UUID;

/**
 * 玩家修炼数据。可变对象，修改后由 NeoForge 在存档时整体序列化。
 */
public class CultivationData implements INBTSerializable<CompoundTag> {
    /** 攻击增强消耗比例的默认值 */
    public static final double DEFAULT_ENHANCE_PERCENT = 0.01D;
    /** 默认未习得任何功法（0）。功法改为通过「功法秘籍」物品学习，见 SkillType / SkillBookItem。 */
    public static final int DEFAULT_LEARNED_SKILLS = 0;
    /** 默认技能槽装配：Z=真气强化(2)、X=武器强化(3)、C=锁定(1)。 */
    public static final int[] DEFAULT_SKILL_SLOTS = {2, 3, 1};
    /** 凡人境层数 0..3 */
    public int mortalTier = 0;
    /** 凡人境当前经验 */
    public int mortalExp = 0;
    /** 炼体境层数 0..3 */
    public int bodyTier = 0;
    /** 炼体境当前经验 */
    public int bodyExp = 0;
    /** 当前真气值 */
    public double qi = 0.0D;
    /** 是否已通过凡人第3层的淬体试炼 */
    public boolean gatePassed = false;
    /** 淬体试炼是否进行中 */
    public boolean gateActive = false;
    /** 淬体试炼剩余 tick */
    public int gateTicks = 0;
    /** 攻击增强模式是否开启 */
    public boolean attackEnhanceMode = false;
    /** 攻击增强每次攻击消耗真气上限的比例（0.01 = 1%） */
    public double attackEnhancePercent = DEFAULT_ENHANCE_PERCENT;
    /** 击杀拥有真气的敌对生物累积的额外真气上限 */
    public double bonusMaxQi = 0.0D;
    /** 行走累计距离（未满100的余量） */
    public double walkAccumulator = 0.0D;
    /** 上一 tick 的 X 坐标（用于行走距离计算） */
    public double lastX = Double.NaN;
    /** 上一 tick 的 Z 坐标 */
    public double lastZ = Double.NaN;
    /** 同步节流计数 */
    public int syncTimer = 0;
    /** 上次冲刺的游戏刻（不持久化） */
    public long lastDashTick = -1000L;
    /** 上次挥出剑气的游戏刻（不持久化），用于出手冷却、避免连点叠出多道剑气 */
    public long lastSwordQiTick = -1000L;
    /** 连续冲刺的次数（不持久化），用于递增真气消耗 */
    public int dashStreak = 0;
    /** 已习得功法技能的位掩码 */
    public int learnedSkills = DEFAULT_LEARNED_SKILLS;
    /** 技能槽 0/1/2 分别对应 Z/X/C，值为 SkillType.id，0 表示未装配 */
    public final int[] skillSlots = {2, 3, 1};
    /** [锁定] 当前锁定的生物 UUID（无则为 null） */
    public UUID lockedTarget = null;
    /** 锁定生物所在维度（维度 id 字符串） */
    public String lockedTargetDimension = "";
    /** 上次冲刺特效释放的游戏刻（不持久化），用于限制每 10 tick 最多一次 */
    public long lastDashFxTick = -1000L;

    public void copyFrom(CultivationData other) {
        this.mortalTier = other.mortalTier;
        this.mortalExp = other.mortalExp;
        this.bodyTier = other.bodyTier;
        this.bodyExp = other.bodyExp;
        this.qi = other.qi;
        this.gatePassed = other.gatePassed;
        this.gateActive = other.gateActive;
        this.gateTicks = other.gateTicks;
        this.attackEnhanceMode = other.attackEnhanceMode;
        this.attackEnhancePercent = other.attackEnhancePercent;
        this.bonusMaxQi = other.bonusMaxQi;
        this.walkAccumulator = other.walkAccumulator;
        this.learnedSkills = other.learnedSkills;
        System.arraycopy(other.skillSlots, 0, this.skillSlots, 0, this.skillSlots.length);
        this.lockedTarget = other.lockedTarget;
        this.lockedTargetDimension = other.lockedTargetDimension;
        this.lastX = Double.NaN;
        this.lastZ = Double.NaN;
        this.lastDashTick = -1000L;
        this.lastDashFxTick = -1000L;
        this.dashStreak = 0;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("MortalTier", this.mortalTier);
        tag.putInt("MortalExp", this.mortalExp);
        tag.putInt("BodyTier", this.bodyTier);
        tag.putInt("BodyExp", this.bodyExp);
        tag.putDouble("Qi", this.qi);
        tag.putBoolean("GatePassed", this.gatePassed);
        tag.putBoolean("GateActive", this.gateActive);
        tag.putInt("GateTicks", this.gateTicks);
        tag.putBoolean("AttackEnhanceMode", this.attackEnhanceMode);
        tag.putDouble("AttackEnhancePercent", this.attackEnhancePercent);
        tag.putDouble("BonusMaxQi", this.bonusMaxQi);
        tag.putDouble("WalkAccumulator", this.walkAccumulator);
        tag.putInt("LearnedSkills", this.learnedSkills);
        tag.putInt("SkillSlot0", this.skillSlots[0]);
        tag.putInt("SkillSlot1", this.skillSlots[1]);
        tag.putInt("SkillSlot2", this.skillSlots[2]);
        if (this.lockedTarget != null) {
            tag.putUUID("LockedTarget", this.lockedTarget);
            tag.putString("LockedTargetDimension", this.lockedTargetDimension);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.mortalTier = tag.getInt("MortalTier");
        this.mortalExp = tag.getInt("MortalExp");
        this.bodyTier = tag.getInt("BodyTier");
        this.bodyExp = tag.getInt("BodyExp");
        this.qi = tag.getDouble("Qi");
        this.gatePassed = tag.getBoolean("GatePassed");
        this.gateActive = tag.getBoolean("GateActive");
        this.gateTicks = tag.getInt("GateTicks");
        this.attackEnhanceMode = tag.getBoolean("AttackEnhanceMode");
        this.attackEnhancePercent = tag.contains("AttackEnhancePercent")
                ? tag.getDouble("AttackEnhancePercent") : DEFAULT_ENHANCE_PERCENT;
        this.bonusMaxQi = tag.getDouble("BonusMaxQi");
        this.walkAccumulator = tag.getDouble("WalkAccumulator");
        this.learnedSkills = tag.contains("LearnedSkills")
                ? tag.getInt("LearnedSkills") : DEFAULT_LEARNED_SKILLS;
        this.skillSlots[0] = tag.contains("SkillSlot0") ? tag.getInt("SkillSlot0") : DEFAULT_SKILL_SLOTS[0];
        this.skillSlots[1] = tag.contains("SkillSlot1") ? tag.getInt("SkillSlot1") : DEFAULT_SKILL_SLOTS[1];
        this.skillSlots[2] = tag.contains("SkillSlot2") ? tag.getInt("SkillSlot2") : DEFAULT_SKILL_SLOTS[2];
        if (tag.hasUUID("LockedTarget")) {
            this.lockedTarget = tag.getUUID("LockedTarget");
            this.lockedTargetDimension = tag.getString("LockedTargetDimension");
        } else {
            this.lockedTarget = null;
            this.lockedTargetDimension = "";
        }
        this.lastX = Double.NaN;
        this.lastZ = Double.NaN;
    }
}
