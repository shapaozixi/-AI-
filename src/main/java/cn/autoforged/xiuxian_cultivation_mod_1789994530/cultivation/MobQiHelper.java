package cn.autoforged.xiuxian_cultivation_mod_1789994530.cultivation;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.attachment.MobQiData;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.attachment.ModAttachments;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 生物真气系统：敌对生物与中立生物按概率拥有真气、按玩家 1/10 速度回复、
 * 真气满后在当前位置释放参数化粒子特效「破体进化」、默认开启真气强化。
 */
public final class MobQiHelper {
    /** 敌对生物拥有真气的概率 */
    public static final double QI_CHANCE = 0.2D;
    /** 敌对生物真气回复速度相对玩家的倍率（1/10） */
    public static final double MOB_REGEN_FACTOR = 0.1D;
    /** 敌对生物初始只拥有真气上限的 1/5 */
    public static final double MOB_INITIAL_FRACTION = 0.2D;
    /** 敌对生物真气强化每次攻击消耗真气上限的比例 */
    public static final double MOB_ENHANCE_PERCENT = 0.05D;
    /** 进化时基础数值的增量：ADD_MULTIPLIED_BASE +2 即最终 ×3 */
    public static final double EVOLUTION_BONUS = 2.0D;

    private MobQiHelper() {
    }

    /** 敌对生物真气上限：由自身生命、护甲与攻击决定。 */
    public static double getMaxQi(LivingEntity mob) {
        double health = mob.getMaxHealth();
        double armor = mob.getArmorValue();
        AttributeInstance attack = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        double attackValue = attack == null ? 0.0D : attack.getValue();
        return Math.max(0.0D, health * 10.0D + armor * 50.0D + attackValue * 40.0D);
    }

    /**
     * 是否允许拥有真气：只有敌对生物与中立生物可以，友善生物（动物等）不可以。
     * 中立生物由 {@link NeutralMob} 标识（末影人、僵尸猪灵、狼、蜜蜂、北极熊、铁傀儡等）。
     */
    public static boolean canHaveQi(LivingEntity mob) {
        return mob instanceof Enemy || mob instanceof NeutralMob;
    }

    /** 生物加入世界时按概率赋予真气；已有数据（含从存档加载）不重复赋予。 */
    public static void tryGiveQi(LivingEntity mob) {
        if (!canHaveQi(mob)) {
            return;
        }
        if (mob.hasData(ModAttachments.MOB_QI.get())) {
            return;
        }
        if (mob.getRandom().nextDouble() >= QI_CHANCE) {
            return;
        }
        MobQiData data = new MobQiData();
        data.hasQi = true;
        mob.setData(ModAttachments.MOB_QI.get(), data);
        data.qi = getMaxQi(mob) * MOB_INITIAL_FRACTION;
    }

    /** 无视概率直接赋予真气（用于淬体试炼召唤的坚守者等必定拥有真气的敌人）。 */
    public static void forceQi(LivingEntity mob) {
        MobQiData data;
        if (mob.hasData(ModAttachments.MOB_QI.get())) {
            data = mob.getData(ModAttachments.MOB_QI.get());
        } else {
            data = new MobQiData();
            mob.setData(ModAttachments.MOB_QI.get(), data);
        }
        data.hasQi = true;
        data.enhanceActive = true;
        double maxQi = getMaxQi(mob);
        if (data.qi <= 0.0D) {
            data.qi = maxQi * MOB_INITIAL_FRACTION;
        } else {
            data.qi = Math.min(maxQi, data.qi);
        }
    }

    /** 每 tick：恢复真气，真气满则进化一次，并把真气同步给客户端用于显示真气条。非敌对/中立生物不处理。 */
    public static void tick(LivingEntity mob) {
        if (!canHaveQi(mob) || !mob.hasData(ModAttachments.MOB_QI.get())) {
            return;
        }
        MobQiData data = mob.getData(ModAttachments.MOB_QI.get());
        if (!data.hasQi) {
            return;
        }
        double maxQi = getMaxQi(mob);
        double regenPerSecond = mob.getMaxHealth()
                * CultivationHelper.QI_REGEN_PER_HEALTH_PER_SECOND * MOB_REGEN_FACTOR;
        if (data.qi < maxQi) {
            data.qi = Math.min(maxQi, data.qi + regenPerSecond / 20.0D);
        }
        if (!data.evolved && data.qi >= maxQi) {
            evolve(mob, data);
        }
        if (data.evolved) {
            // 幂等：实体重载后属性修饰符可能丢失，这里补回
            applyEvolutionModifiers(mob);
        }
        // 每 10 tick 同步一次真气值给客户端（真气条显示），避免每 tick 发包。
        if (mob.tickCount % 10 == 0) {
            mob.syncData(ModAttachments.MOB_QI.get());
        }
    }

    /** 该生物是否拥有真气（已挂载且 hasQi）。 */
    public static boolean hasQi(LivingEntity mob) {
        if (!canHaveQi(mob) || !mob.hasData(ModAttachments.MOB_QI.get())) {
            return false;
        }
        return mob.getData(ModAttachments.MOB_QI.get()).hasQi;
    }

    /**
     * 拥有真气的生物用真气抵挡伤害：与玩家同比例（15 真气抵挡 1 点），
     * 真气不足时按余量部分抵挡。
     *
     * @return 本次实际抵挡的伤害量
     */
    public static float absorbDamageWithQi(LivingEntity mob, float damage) {
        if (damage <= 0.0F || !hasQi(mob)) {
            return 0.0F;
        }
        MobQiData data = mob.getData(ModAttachments.MOB_QI.get());
        double affordable = data.qi / CultivationHelper.QI_COST_PER_DAMAGE;
        float blocked = (float) Math.min((double) damage, affordable);
        if (blocked <= 0.0F) {
            return 0.0F;
        }
        data.qi = Math.max(0.0D, data.qi - blocked * CultivationHelper.QI_COST_PER_DAMAGE);
        return blocked;
    }

    private static void evolve(LivingEntity mob, MobQiData data) {
        data.evolved = true;
        applyEvolutionModifiers(mob);
        mob.setHealth(mob.getMaxHealth());
        // 进化：在当前位置释放参数化粒子特效并播放空灵震撼的音效（不再召唤雷电）
        if (mob.level() instanceof ServerLevel serverLevel) {
            spawnEvolutionFeedback(serverLevel, mob);
        }
    }

    /**
     * 进化特效：以生物为中心的参数化上升螺旋（由函数直接算出每个粒子坐标），
     * 点数固定、单次释放，开销很低；配合底部魂火环与「晶鸣 + 龙吼」音效，
     * 营造破体淬炼的空灵震撼感。
     */
    private static void spawnEvolutionFeedback(ServerLevel level, LivingEntity mob) {
        double cx = mob.getX();
        double baseY = mob.getY() + 0.1D;
        double cz = mob.getZ();
        final int points = 48;
        final double turns = 3.0D;
        final double radius = 0.9D;
        final double height = 2.5D;
        for (int i = 0; i < points; i++) {
            double t = (double) i / (double) (points - 1);
            double angle = turns * 2.0D * Math.PI * t;
            double r = radius * (1.0D - t * 0.7D);
            level.sendParticles(ParticleTypes.END_ROD,
                    cx + Math.cos(angle) * r, baseY + height * t, cz + Math.sin(angle) * r,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        for (int i = 0; i < 12; i++) {
            double angle = 2.0D * Math.PI * i / 12.0D;
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    cx + Math.cos(angle) * 0.7D, baseY, cz + Math.sin(angle) * 0.7D,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        level.playSound(null, cx, mob.getY(), cz,
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.2F, 1.6F);
        level.playSound(null, cx, mob.getY(), cz,
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 0.7F, 1.2F);
        level.playSound(null, cx, mob.getY(), cz,
                SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 0.9F, 0.7F);
    }

    /** 将所有基础战斗数值乘以 3（生命/攻击/护甲）。 */
    private static void applyEvolutionModifiers(LivingEntity mob) {
        applyEvolutionModifier(mob, Attributes.MAX_HEALTH, "mob_evolution_health");
        applyEvolutionModifier(mob, Attributes.ATTACK_DAMAGE, "mob_evolution_attack");
        applyEvolutionModifier(mob, Attributes.ARMOR, "mob_evolution_armor");
    }

    private static void applyEvolutionModifier(LivingEntity mob, Holder<Attribute> attribute, String name) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(XiuxianCultivationMod.MODID, name);
        if (instance.hasModifier(id)) {
            return;
        }
        instance.addPermanentModifier(new AttributeModifier(
                id, EVOLUTION_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    /** 敌对生物攻击时消耗真气追加伤害（默认开启真气强化）。 */
    public static float applyEnhancement(LivingEntity mob, float damage) {
        if (damage <= 0.0F) {
            return damage;
        }
        if (!canHaveQi(mob) || !mob.hasData(ModAttachments.MOB_QI.get())) {
            return damage;
        }
        MobQiData data = mob.getData(ModAttachments.MOB_QI.get());
        if (!data.hasQi || !data.enhanceActive) {
            return damage;
        }
        double cost = Math.min(getMaxQi(mob) * MOB_ENHANCE_PERCENT, data.qi);
        double bonus = cost / CultivationHelper.QI_PER_BONUS_DAMAGE;
        if (bonus <= 0.0D) {
            return damage;
        }
        data.qi = Math.max(0.0D, data.qi - cost);
        return damage + (float) bonus;
    }
}
