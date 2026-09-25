package cn.autoforged.xiuxian_cultivation_mod_1789994530.cultivation;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.attachment.CultivationData;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.attachment.ModAttachments;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.config.ModCommonConfig;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.effect.ModEffects;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.entity.SwordQiProjectile;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload.ClientboundCultivationSyncPayload;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload.ClientboundQiEnhanceSyncPayload;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.skill.SkillType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

/**
 * 修仙体系核心逻辑：境界经验、突破、属性加成、淬体试炼、真气。
 */
public final class CultivationHelper {
    /** 凡人境层数（0..3） */
    public static final int MORTAL_TIERS = 4;
    /** 突破到第 n 层所需经验，下标为目标层 */
    public static final int[] MORTAL_EXP_REQUIRED = {0, 100, 1000, 10000};
    /** 炼体境层数（0..3） */
    public static final int BODY_TIERS = 3;
    /** 突破到炼体第 n 层所需经验，下标为目标层 */
    public static final int[] BODY_EXP_REQUIRED = {0, 10000, 20000, 40000};
    /** 每行走 100 格获得 1 点修炼经验 */
    public static final int WALK_BLOCKS_PER_EXP = 100;
    /** 每次行走奖励经验 */
    public static final int WALK_EXP_GAIN = 1;
    /** 经验获取倍率（较快 2x） */
    public static final double EXP_MULTIPLIER = 2.0D;
    /** 淬体试炼持续时间（30 秒） */
    public static final int GATE_DURATION_TICKS = 600;
    /** 每抵挡 1 点伤害消耗的真气：30 点真气抵挡 2 点伤害 */
    public static final double QI_COST_PER_DAMAGE = 15.0D;
    /** 真气回复速率：生命值×5/秒 */
    public static final double QI_REGEN_PER_HEALTH_PER_SECOND = 5.0D;
    /** 真气强化：每消耗 20 点真气，本次攻击伤害 +1 */
    public static final double QI_PER_BONUS_DAMAGE = 20.0D;
    /** 真气强化：每 50 点真气回复 1 格饱食度与 1 点饱和度 */
    public static final double QI_PER_FOOD_POINT = 50.0D;
    /** 真气强化攻击粒子：消耗 10 点真气即出现特效 */
    public static final double QI_FX_MIN_CONSUME = 10.0D;
    /** 真气强化攻击粒子：消耗 1000 点真气时特效达到最强 */
    public static final double QI_FX_MAX_CONSUME = 1000.0D;
    /** 真气强化攻击粒子：外圈末地烛圆环的最小半径 */
    public static final double QI_FX_RING_MIN_RADIUS = 0.5D;
    /** 真气强化攻击粒子：外圈末地烛圆环的最大半径（消耗真气越多半径越大，而不是叠加更多特效） */
    public static final double QI_FX_RING_MAX_RADIUS = 2.5D;
    /** 真气强化攻击粒子：末地烛圆环的采样点数 */
    public static final int QI_FX_RING_POINTS = 20;
    /** 真气强化攻击粒子：消耗真气比例达到该值后，在大圆环内再叠加一个小圆环 */
    public static final double QI_FX_INNER_RING_RATIO = 0.5D;
    /** 冲刺：单点默认消耗 10 点真气 */
    public static final double DASH_MIN_COST = 10.0D;
    /** 冲刺：连点时每次递增的真气消耗 */
    public static final double DASH_COST_STEP = 5.0D;
    /** 冲刺：单次最高消耗 30 点真气 */
    public static final double DASH_MAX_COST = 30.0D;
    /** 冲刺连点判定窗口（tick），超过则重置连点计数 */
    public static final int DASH_COMBO_WINDOW_TICKS = 10;
    /** 攻击增强每次攻击消耗真气上限比例的可调档位（蹲下按 V 循环） */
    public static final double[] ENHANCE_PERCENT_STEPS =
            {0.01D, 0.02D, 0.03D, 0.05D, 0.10D, 0.20D, 0.30D, 0.50D, 1.00D};

    // ==================== 功法技能参数 ====================
    /** [真气强化] 技能：每次攻击固定消耗真气上限的 5% */
    public static final double SKILL_ENHANCE_PERCENT = 0.05D;
    /** [真气强化] 技能：每消耗 15 点真气，攻击力 +1 */
    public static final double QI_PER_BONUS_DAMAGE_SKILL = 15.0D;
    /** [真气强化] 技能：buff 每秒消耗 10 点真气 */
    public static final double QI_ENHANCE_DRAIN_PER_SECOND = 10.0D;
    /** [真气强化] 技能 buff 的刷新时长（tick） */
    public static final int QI_ENHANCE_BUFF_REFRESH_TICKS = 40;
    /** [武器强化] 技能：真气回复增加「攻击力 × 5 / 秒」 */
    public static final double WEAPON_ENHANCE_REGEN_ATTACK_MULTIPLIER = 5.0D;
    /** [武器强化] 技能 buff 持续时长（tick），30 秒 */
    public static final int WEAPON_ENHANCE_DURATION_TICKS = 600;
    /** [武器强化] 剑气射程（格） */
    public static final double SWORD_QI_RANGE = 12.0D;
    /** [武器强化] 每次挥出剑气额外消耗的真气 */
    public static final double SWORD_QI_QI_COST = 15.0D;
    /** 冲刺特效最短间隔（tick），限制短时间内频繁触发粒子造成卡顿 */
    public static final int DASH_FX_INTERVAL_TICKS = 10;
    /** 破坏地形：最低需要消耗的真气，低于此值不破坏 */
    public static final double TERRAIN_MIN_QI = 5.0D;
    /** 破坏地形：基准半径 */
    public static final int TERRAIN_BASE_RADIUS = 1;
    /** 破坏地形：每多消耗多少真气半径 +1 */
    public static final double TERRAIN_QI_PER_RADIUS = 60.0D;
    /** 破坏地形：最大半径 */
    public static final int TERRAIN_MAX_RADIUS = 4;
    /** 破坏地形：单次最多破坏的方块数，避免卡顿 */
    public static final int TERRAIN_MAX_BLOCKS = 48;
    /** 破坏地形：硬度超过该值的方块（黑曜石等）不破坏 */
    public static final float TERRAIN_MAX_HARDNESS = 50.0F;

    private CultivationHelper() {
    }

    public static CultivationData get(Player player) {
        return player.getData(ModAttachments.CULTIVATION.get());
    }

    public static String key(String suffix) {
        return XiuxianCultivationMod.MODID + "." + suffix;
    }

    // ==================== 经验与突破 ====================

    public static void addCultivationExp(Player player, int baseAmount) {
        CultivationData data = get(player);
        routeExp(player, data, baseAmount);
        if (player instanceof ServerPlayer serverPlayer) {
            sync(serverPlayer);
        }
    }

    private static void routeExp(Player player, CultivationData data, int baseAmount) {
        if (baseAmount <= 0) {
            return;
        }
        int amount = (int) Math.ceil(baseAmount * EXP_MULTIPLIER);
        if (!data.gatePassed) {
            addMortalExp(player, data, amount);
        } else {
            addBodyExp(player, data, amount);
        }
    }

    private static void addMortalExp(Player player, CultivationData data, int amount) {
        // 凡人第 3 层经验封顶：达到后不再获得凡人经验，转而进入淬体试炼
        if (data.mortalTier >= 3) {
            return;
        }
        data.mortalExp += amount;
        while (data.mortalTier < 3) {
            int next = data.mortalTier + 1;
            int required = MORTAL_EXP_REQUIRED[next];
            if (data.mortalExp < required) {
                break;
            }
            data.mortalExp -= required;
            data.mortalTier = next;
            onBreakthrough(player, data, false);
            if (next == 3) {
                // 第 3 层经验封顶，清空余量并开启淬体试炼
                data.mortalExp = 0;
                break;
            }
        }
    }

    private static void addBodyExp(Player player, CultivationData data, int amount) {
        if (data.bodyTier >= 3) {
            return;
        }
        data.bodyExp += amount;
        while (data.bodyTier < 3) {
            int next = data.bodyTier + 1;
            int required = BODY_EXP_REQUIRED[next];
            if (data.bodyExp < required) {
                break;
            }
            data.bodyExp -= required;
            data.bodyTier = next;
            onBreakthrough(player, data, true);
        }
    }

    private static void onBreakthrough(Player player, CultivationData data, boolean body) {
        applyAttributeBonuses(player, data);
        if (player instanceof ServerPlayer serverPlayer) {
            int tier = body ? data.bodyTier : data.mortalTier;
            breakthroughFeedback(serverPlayer, body, tier);
        }
    }

    // ==================== 属性加成 ====================

    public static void applyAttributeBonuses(Player player, CultivationData data) {
        for (int tier = 1; tier <= data.mortalTier && tier < MORTAL_EXP_REQUIRED.length; tier++) {
            applyMortalBonus(player, tier);
        }
        for (int tier = 1; tier <= data.bodyTier && tier < BODY_EXP_REQUIRED.length; tier++) {
            applyBodyBonus(player, tier);
        }
    }

    private static void applyMortalBonus(Player player, int tier) {
        switch (tier) {
            case 1 -> {
                addModifier(player, Attributes.MAX_HEALTH, "mortal_t1_health", 5.0D, AttributeModifier.Operation.ADD_VALUE, true);
                addModifier(player, Attributes.ATTACK_DAMAGE, "mortal_t1_fist", 2.0D, AttributeModifier.Operation.ADD_VALUE, false);
            }
            case 2 -> {
                addModifier(player, Attributes.MAX_HEALTH, "mortal_t2_health", 10.0D, AttributeModifier.Operation.ADD_VALUE, true);
                addModifier(player, Attributes.ATTACK_DAMAGE, "mortal_t2_fist", 3.0D, AttributeModifier.Operation.ADD_VALUE, false);
                addModifier(player, Attributes.MOVEMENT_SPEED, "mortal_t2_speed", 0.5D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);
            }
            case 3 -> {
                addModifier(player, Attributes.MAX_HEALTH, "mortal_t3_health", 10.0D, AttributeModifier.Operation.ADD_VALUE, true);
                addModifier(player, Attributes.ATTACK_DAMAGE, "mortal_t3_fist", 3.0D, AttributeModifier.Operation.ADD_VALUE, false);
                addModifier(player, Attributes.MOVEMENT_SPEED, "mortal_t3_speed", 0.5D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);
            }
            default -> {
            }
        }
    }

    private static void applyBodyBonus(Player player, int tier) {
        switch (tier) {
            case 1 -> {
                addModifier(player, Attributes.MAX_HEALTH, "body_t1_health", 30.0D, AttributeModifier.Operation.ADD_VALUE, true);
                addModifier(player, Attributes.ATTACK_DAMAGE, "body_t1_fist", 10.0D, AttributeModifier.Operation.ADD_VALUE, false);
                addModifier(player, Attributes.MOVEMENT_SPEED, "body_t1_speed", 1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);
            }
            case 2 -> {
                addModifier(player, Attributes.MAX_HEALTH, "body_t2_health", 30.0D, AttributeModifier.Operation.ADD_VALUE, true);
                addModifier(player, Attributes.ATTACK_DAMAGE, "body_t2_fist", 10.0D, AttributeModifier.Operation.ADD_VALUE, false);
                addModifier(player, Attributes.ARMOR, "body_t2_armor", 5.0D, AttributeModifier.Operation.ADD_VALUE, false);
            }
            case 3 -> {
                addModifier(player, Attributes.MAX_HEALTH, "body_t3_health", 50.0D, AttributeModifier.Operation.ADD_VALUE, true);
                addModifier(player, Attributes.ATTACK_DAMAGE, "body_t3_fist", 10.0D, AttributeModifier.Operation.ADD_VALUE, false);
                addModifier(player, Attributes.ARMOR, "body_t3_armor", 10.0D, AttributeModifier.Operation.ADD_VALUE, false);
            }
            default -> {
            }
        }
    }

    private static void addModifier(Player player, Holder<Attribute> attribute, String name, double amount,
                                    AttributeModifier.Operation operation, boolean heal) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(XiuxianCultivationMod.MODID, name);
        if (instance.hasModifier(id)) {
            return;
        }
        instance.addPermanentModifier(new AttributeModifier(id, amount, operation));
        if (heal) {
            player.heal((float) amount);
        }
    }

    // ==================== 真气 ====================

    /**
     * 真气上限：只由最大生命与护甲决定（不再受攻击伤害影响，避免手持不同武器时上限飘忽不定）。
     */
    public static double getMaxQi(Player player) {
        double health = player.getMaxHealth();
        double armor = player.getArmorValue();
        double bonus = get(player).bonusMaxQi;
        return Math.max(0.0D, health * 10.0D + armor * 50.0D + bonus);
    }

    public static double getQiRegenPerSecond(Player player) {
        return player.getMaxHealth() * QI_REGEN_PER_HEALTH_PER_SECOND;
    }

    /** 击杀拥有真气的敌对生物，永久提高玩家真气上限。 */
    public static void addBonusMaxQi(ServerPlayer player, double amount) {
        if (amount <= 0.0D) {
            return;
        }
        get(player).bonusMaxQi += amount;
        sync(player);
    }

    /**
     * 真气护体：按 {@link #QI_COST_PER_DAMAGE} 的比例，用当前真气尽可能多地抵挡护甲削减后的伤害。
     * 真气不足以挡住全部伤害时按剩余真气抵挡一部分。
     *
     * @return 本次实际抵挡的伤害量
     */
    public static float absorbDamageWithQi(Player player, float damage) {
        if (damage <= 0.0F) {
            return 0.0F;
        }
        CultivationData data = get(player);
        double affordable = data.qi / QI_COST_PER_DAMAGE;
        float blocked = (float) Math.min((double) damage, affordable);
        if (blocked <= 0.0F) {
            return 0.0F;
        }
        data.qi = Math.max(0.0D, data.qi - blocked * QI_COST_PER_DAMAGE);
        return blocked;
    }

    // ==================== 真气强化模式 ====================

    /** 切换真气强化模式（按 V）。 */
    public static void toggleAttackEnhance(ServerPlayer player) {
        CultivationData data = get(player);
        data.attackEnhanceMode = !data.attackEnhanceMode;
        Level level = player.level();
        if (data.attackEnhanceMode) {
            player.displayClientMessage(Component.translatable(key("message.enhance_on"), percentText(data.attackEnhancePercent)), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.4F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.6D, 0.8D, 0.6D, 0.02D);
            }
        } else {
            player.displayClientMessage(Component.translatable(key("message.enhance_off")), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 1.2F);
        }
        syncEnhance(player);
        sync(player);
    }

    /** 调整攻击增强每次攻击消耗的真气比例（蹲下按 V，循环档位）。 */
    public static void cycleEnhancePercent(ServerPlayer player) {
        CultivationData data = get(player);
        int index = 0;
        for (int i = 0; i < ENHANCE_PERCENT_STEPS.length; i++) {
            if (Math.abs(ENHANCE_PERCENT_STEPS[i] - data.attackEnhancePercent) < 1.0E-6D) {
                index = i;
                break;
            }
        }
        index = (index + 1) % ENHANCE_PERCENT_STEPS.length;
        data.attackEnhancePercent = ENHANCE_PERCENT_STEPS[index];
        player.displayClientMessage(Component.translatable(key("message.enhance_cost"), percentText(data.attackEnhancePercent)), true);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 1.0F, 1.6F);
        syncEnhance(player);
    }

    /** 真气强化一次攻击的结果：强化后伤害 + 本次实际消耗的真气。 */
    public record QiEnhanceOutcome(float damage, double consumedQi) {
        public boolean enhanced() {
            return consumedQi > 0.0D;
        }
    }

    /**
     * 真气强化：模式下每次攻击消耗「真气上限 × 设定比例」的真气，
     * 每 {@link #QI_PER_BONUS_DAMAGE} 点消耗增加 1 点伤害；真气不足时按余量部分强化。
     * 若已通过 [真气强化] 功法开启 buff，则改为固定消耗真气上限 5%，每 15 点真气 +1 伤害。
     *
     * @return 强化后伤害与实际消耗的真气（未开启、无真气或伤害非正时原样返回）
     */
    public static QiEnhanceOutcome applyQiEnhancement(ServerPlayer player, float damage) {
        if (damage <= 0.0F) {
            return new QiEnhanceOutcome(damage, 0.0D);
        }
        CultivationData data = get(player);
        if (isQiEnhanceSkillActive(player)) {
            double desired = getMaxQi(player) * SKILL_ENHANCE_PERCENT;
            double cost = Math.min(desired, data.qi);
            double bonus = cost / QI_PER_BONUS_DAMAGE_SKILL;
            if (bonus <= 0.0D) {
                return new QiEnhanceOutcome(damage, 0.0D);
            }
            data.qi = Math.max(0.0D, data.qi - cost);
            return new QiEnhanceOutcome(damage + (float) bonus, cost);
        }
        if (!data.attackEnhanceMode) {
            return new QiEnhanceOutcome(damage, 0.0D);
        }
        double desired = getMaxQi(player) * data.attackEnhancePercent;
        double cost = Math.min(desired, data.qi);
        double bonus = cost / QI_PER_BONUS_DAMAGE;
        if (bonus <= 0.0D) {
            return new QiEnhanceOutcome(damage, 0.0D);
        }
        data.qi = Math.max(0.0D, data.qi - cost);
        return new QiEnhanceOutcome(damage + (float) bonus, cost);
    }

    /** [真气强化] 功法 buff 是否生效。 */
    public static boolean isQiEnhanceSkillActive(Player player) {
        return player.hasEffect(ModEffects.QI_ENHANCEMENT);
    }

    /** [武器强化] 功法 buff 是否生效。 */
    public static boolean isWeaponEnhanceActive(Player player) {
        return player.hasEffect(ModEffects.WEAPON_ENHANCEMENT);
    }

    /**
     * 真气强化命中特效：目标处烟花星爆，外圈是一圈<b>竖直</b>的末地烛圆环——
     * 圆面法线取玩家→目标的水平朝向，所以圆是立起来正对玩家的，而不再平铺在地面上。
     * 消耗真气越多，只放大圆环半径（不叠加更多特效）；消耗足够多时最多再叠一个内圈小圆环。
     */
    public static void spawnQiEnhanceFeedback(ServerPlayer player, Entity target, double consumedQi) {
        Level level = player.level();
        if (level instanceof ServerLevel serverLevel) {
            double y = target.getY() + target.getBbHeight() * 0.5D;
            double ratio = (consumedQi - QI_FX_MIN_CONSUME) / (QI_FX_MAX_CONSUME - QI_FX_MIN_CONSUME);
            ratio = Math.max(0.0D, Math.min(1.0D, ratio));
            double spread = 0.3D + 0.4D * ratio;
            int stars = 8 + (int) Math.round(ratio * 16.0D);
            serverLevel.sendParticles(ParticleTypes.FIREWORK,
                    target.getX(), y, target.getZ(), stars, spread, spread, spread, 0.0D);
            // 圆的朝向：玩家 → 目标的水平方向，使圆面竖直且正对玩家
            Vec3 facing = horizontalFacing(player, target);
            double outerRadius = QI_FX_RING_MIN_RADIUS + ratio * (QI_FX_RING_MAX_RADIUS - QI_FX_RING_MIN_RADIUS);
            spawnVerticalEndRodRing(serverLevel, target.getX(), y, target.getZ(), facing, outerRadius, QI_FX_RING_POINTS);
            if (ratio >= QI_FX_INNER_RING_RATIO) {
                double innerRadius = outerRadius * 0.5D;
                spawnVerticalEndRodRing(serverLevel, target.getX(), y, target.getZ(), facing, innerRadius,
                        Math.max(6, QI_FX_RING_POINTS / 2));
            }
        }
        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.8F, 1.3F);
    }

    /** 玩家到目标的水平朝向（单位向量，仅 x/z）；两者水平重叠时退化为玩家视线的水平方向。 */
    private static Vec3 horizontalFacing(Player player, Entity target) {
        double dx = target.getX() - player.getX();
        double dz = target.getZ() - player.getZ();
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 1.0E-4D) {
            Vec3 look = player.getLookAngle();
            double lookLength = Math.sqrt(look.x * look.x + look.z * look.z);
            if (lookLength < 1.0E-4D) {
                return new Vec3(0.0D, 0.0D, 1.0D);
            }
            return new Vec3(look.x / lookLength, 0.0D, look.z / lookLength);
        }
        return new Vec3(dx / length, 0.0D, dz / length);
    }

    /**
     * 在竖直平面内撒一圈末地烛粒子：圆面法线为水平向量 {@code facing}，
     * 用「右向量 × 上向量」张成竖直平面，因此圆环立起来而不是铺在地上。
     */
    private static void spawnVerticalEndRodRing(ServerLevel level, double cx, double cy, double cz,
                                                Vec3 facing, double radius, int points) {
        double rightX = -facing.z;
        double rightZ = facing.x;
        for (int i = 0; i < points; i++) {
            double angle = 2.0D * Math.PI * i / points;
            double horizontal = Math.cos(angle) * radius;
            double vertical = Math.sin(angle) * radius;
            double x = cx + rightX * horizontal;
            double y = cy + vertical;
            double z = cz + rightZ * horizontal;
            level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static Component percentText(double percent) {
        return Component.literal(Math.round(percent * 100.0D) + "%");
    }

    // ==================== 每 tick 逻辑 ====================

    public static void tick(ServerPlayer player) {
        CultivationData data = get(player);
        handleWalkExp(player, data);
        handleGateTrial(player, data);
        handleQiRegen(player, data);
        handleQiEnhance(player, data);
        handleLockOnGaze(player, data);
        data.syncTimer++;
        if (data.syncTimer >= 5) {
            data.syncTimer = 0;
            sync(player);
        }
    }

    /**
     * [锁定] 视角跟随：锁定有效期间，每 tick 强制玩家视角注视目标（硬锁定）。
     *
     * <p>服务端先设置自身旋转以保持权威，再用 {@link ClientboundPlayerLookAtPacket}
     * 让客户端转向目标——这个包只改朝向、不重置位置，因此不会引起位置抖动。
     */
    private static void handleLockOnGaze(ServerPlayer player, CultivationData data) {
        if (data.lockedTarget == null) {
            return;
        }
        LivingEntity target = getLockedTarget(player);
        if (target == null) {
            return;
        }
        faceEntity(player, target);
        player.connection.send(new ClientboundPlayerLookAtPacket(
                EntityAnchorArgument.Anchor.EYES, target, EntityAnchorArgument.Anchor.EYES));
    }

    private static void handleWalkExp(Player player, CultivationData data) {
        double x = player.getX();
        double z = player.getZ();
        if (!Double.isNaN(data.lastX)) {
            double dx = x - data.lastX;
            double dz = z - data.lastZ;
            double distance = Math.sqrt(dx * dx + dz * dz);
            // 过滤传送造成的大位移
            if (distance > 0.0D && distance < 10.0D) {
                data.walkAccumulator += distance;
                while (data.walkAccumulator >= WALK_BLOCKS_PER_EXP) {
                    data.walkAccumulator -= WALK_BLOCKS_PER_EXP;
                    routeExp(player, data, WALK_EXP_GAIN);
                }
            }
        }
        data.lastX = x;
        data.lastZ = z;
    }

    private static void handleQiRegen(Player player, CultivationData data) {
        double maxQi = getMaxQi(player);
        if (maxQi <= 0.0D) {
            data.qi = 0.0D;
            return;
        }
        double regenPerSecond = getQiRegenPerSecond(player);
        // [武器强化] buff：额外增加「攻击力×5/秒」的真气回复
        if (isWeaponEnhanceActive(player)) {
            regenPerSecond += player.getAttributeValue(Attributes.ATTACK_DAMAGE)
                    * WEAPON_ENHANCE_REGEN_ATTACK_MULTIPLIER;
        }
        data.qi += regenPerSecond / 20.0D;
        if (data.qi > maxQi) {
            data.qi = maxQi;
        }
        if (data.qi < 0.0D) {
            data.qi = 0.0D;
        }
    }

    /**
     * 真气强化期间的持续效果：消耗真气回复饱食度/饱和度，并在玩家身边显示蓝色粒子。
     */
    private static void handleQiEnhance(ServerPlayer player, CultivationData data) {
        // [真气强化] 功法 buff：每秒抽 10 点真气维持，真气不足则 buff 消失
        if (player.hasEffect(ModEffects.QI_ENHANCEMENT)) {
            if (data.qi >= QI_ENHANCE_DRAIN_PER_SECOND) {
                data.qi -= QI_ENHANCE_DRAIN_PER_SECOND / 20.0D;
                if (player.tickCount % 20 == 0) {
                    player.addEffect(new MobEffectInstance(ModEffects.QI_ENHANCEMENT,
                            QI_ENHANCE_BUFF_REFRESH_TICKS, 0, false, true, true));
                }
            } else {
                player.removeEffect(ModEffects.QI_ENHANCEMENT);
                player.displayClientMessage(Component.translatable(key("message.qi_buff_exhausted")), true);
            }
        }
        if (!data.attackEnhanceMode) {
            return;
        }
        // 50 点真气 = 1 格饱食度 + 1 点饱和度
        FoodData food = player.getFoodData();
        boolean foodMissing = food.needsFood() || food.getSaturationLevel() < food.getFoodLevel();
        if (foodMissing && data.qi >= QI_PER_FOOD_POINT) {
            data.qi -= QI_PER_FOOD_POINT;
            food.eat(1, 0.5F);
        }
        // 蓝色粒子表明已进入真气强化模式
        if (player.tickCount % 5 == 0 && player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    player.getX(), player.getY() + 1.0D, player.getZ(),
                    10, 0.5D, 0.8D, 0.5D, 0.01D);
        }
    }

    /**
     * 真气强化期间的冲刺：每点真气冲刺 1 格，单点默认 10 点，连点最高 30 点。
     * 若已通过 [锁定] 锁定生物，则改为瞬移到该生物面前。
     */
    public static void performDash(ServerPlayer player) {
        CultivationData data = get(player);
        if (!data.attackEnhanceMode && !isQiEnhanceSkillActive(player)) {
            player.displayClientMessage(Component.translatable(key("message.dash_need_enhance")), true);
            return;
        }
        if (player.isPassenger()) {
            return;
        }
        long now = player.level().getGameTime();
        if (now - data.lastDashTick <= DASH_COMBO_WINDOW_TICKS) {
            data.dashStreak++;
        } else {
            data.dashStreak = 0;
        }
        data.lastDashTick = now;
        double cost = Math.min(DASH_MIN_COST + data.dashStreak * DASH_COST_STEP, DASH_MAX_COST);
        if (data.qi < cost) {
            player.displayClientMessage(Component.translatable(key("message.dash_no_qi")), true);
            return;
        }
        Vec3 start = player.position();
        LivingEntity locked = getLockedTarget(player);
        Vec3 target;
        if (locked != null) {
            // 瞬移到锁定生物面前：从生物指向玩家的水平方向退开 2 格
            Vec3 away = new Vec3(start.x - locked.getX(), 0.0D, start.z - locked.getZ());
            if (away.lengthSqr() < 1.0E-4D) {
                Vec3 look = player.getLookAngle();
                away = new Vec3(look.x, 0.0D, look.z);
                if (away.lengthSqr() < 1.0E-4D) {
                    away = new Vec3(0.0D, 0.0D, 1.0D);
                }
            }
            away = away.normalize();
            target = new Vec3(locked.getX() + away.x * 2.0D, locked.getY(), locked.getZ() + away.z * 2.0D);
        } else {
            Vec3 look = player.getLookAngle();
            Vec3 dir = new Vec3(look.x, look.y, look.z);
            if (dir.lengthSqr() < 1.0E-6D) {
                return;
            }
            dir = dir.normalize();
            Vec3 end = start.add(dir.scale(cost));
            BlockHitResult hit = player.level().clip(new ClipContext(start.add(0.0D, 0.1D, 0.0D), end,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            target = end;
            if (hit.getType() != HitResult.Type.MISS) {
                target = hit.getLocation().subtract(dir.scale(0.5D));
            }
        }
        double distance = start.distanceTo(target);
        data.qi -= cost;
        player.teleportTo(target.x, target.y, target.z);
        player.resetFallDistance();
        if (locked != null) {
            faceEntity(player, locked);
        }
        // [真气强化] buff 期间：位移按真气消耗量破坏地形
        if (isQiEnhanceSkillActive(player) && player.level() instanceof ServerLevel serverLevel) {
            breakTerrainAlong(serverLevel, start, target, cost, player);
        }
        dashFeedback(player, data, start, target, distance);
        sync(player);
    }

    /** 让玩家转向目标生物。 */
    private static void faceEntity(Player player, Entity target) {
        Vec3 d = target.position().subtract(player.position());
        double horizontal = Math.sqrt(d.x * d.x + d.z * d.z);
        float yaw = (float) (Math.toDegrees(Math.atan2(d.z, d.x))) - 90.0F;
        float pitch = (float) (-Math.toDegrees(Math.atan2(d.y, horizontal)));
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.setYHeadRot(yaw);
    }

    /**
     * 冲刺特效：沿航迹的末地烛拖尾 + 终点处形如飞机音爆云的白色「蒸汽锥」。
     * 特效最多每 {@link #DASH_FX_INTERVAL_TICKS} tick 显示一次，避免短时间连点导致卡顿；
     * 蒸汽锥大小随实际移动距离扩大或缩小。
     */
    private static void dashFeedback(ServerPlayer player, CultivationData data, Vec3 from, Vec3 to, double distance) {
        long now = player.level().getGameTime();
        if (now - data.lastDashFxTick < DASH_FX_INTERVAL_TICKS) {
            return;
        }
        data.lastDashFxTick = now;
        Level level = player.level();
        if (level instanceof ServerLevel serverLevel) {
            Vec3 dir = to.subtract(from);
            if (dir.lengthSqr() < 1.0E-6D) {
                dir = player.getLookAngle();
            }
            dir = dir.normalize();
            int steps = Math.max(1, (int) (distance / 2.0D));
            for (int i = 0; i <= steps; i++) {
                double t = (double) i / (double) steps;
                double x = from.x + (to.x - from.x) * t;
                double y = from.y + (to.y - from.y) * t + 0.4D;
                double z = from.z + (to.z - from.z) * t;
                serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 2, 0.1D, 0.1D, 0.1D, 0.0D);
            }
            // 距离越长，蒸汽锥越大（0.5x ~ 2.0x）
            double scale = Math.max(0.5D, Math.min(2.0D, distance / 10.0D));
            spawnVaporCone(serverLevel, to, dir, scale);
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, to.x, to.y + 0.5D, to.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.7F, 1.8F);
    }

    /**
     * 飞机音爆云（蒸汽锥）：以冲刺方向为轴，用少量白色圆环拼出「前窄后宽」的锥形云雾，
     * 形如飞行器突破音障时产生的水汽锥。粒子数固定，避免随冲刺距离增长而增加负担。
     */
    private static void spawnVaporCone(ServerLevel level, Vec3 center, Vec3 dir, double scale) {
        Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = dir.cross(up);
        if (right.lengthSqr() < 1.0E-6D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        }
        right = right.normalize();
        Vec3 realUp = right.cross(dir).normalize();
        final int rings = 5;
        final int pointsPerRing = 10;
        final double coneLength = 2.4D * scale;
        final double baseRadius = 1.1D * scale;
        for (int r = 0; r < rings; r++) {
            double t = (r + 1.0D) / rings;
            double radius = baseRadius * t;
            double along = coneLength * (1.0D - t);
            Vec3 ringCenter = center.add(dir.scale(along));
            for (int i = 0; i < pointsPerRing; i++) {
                double angle = 2.0D * Math.PI * i / pointsPerRing;
                Vec3 point = ringCenter
                        .add(right.scale(Math.cos(angle) * radius))
                        .add(realUp.scale(Math.sin(angle) * radius));
                level.sendParticles(ParticleTypes.WHITE_SMOKE, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        level.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 0.5D, center.z, 12, 0.6D, 0.5D, 0.6D, 0.02D);
    }

    // ==================== 功法技能 ====================

    /** 玩家是否已习得某功法技能。 */
    public static boolean isSkillLearned(CultivationData data, SkillType skill) {
        return (data.learnedSkills & skill.bit()) != 0;
    }

    /**
     * 习得一门功法（供「功法秘籍」物品调用）。
     *
     * @return true = 本次学会（调用方应消耗秘籍）；false = 早已学会，不消耗
     */
    public static boolean learnSkill(ServerPlayer player, SkillType skill) {
        CultivationData data = get(player);
        if (isSkillLearned(data, skill)) {
            player.displayClientMessage(
                    Component.translatable(key("message.skill_already_learned"), skill.displayName()), false);
            return false;
        }
        data.learnedSkills |= skill.bit();
        player.displayClientMessage(
                Component.translatable(key("message.skill_learned"), skill.displayName()), false);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.5F);
        sync(player);
        return true;
    }

    /** 使用技能槽（0/1/2 对应 Z/X/C）中装配的功法。 */
    public static void useSkill(ServerPlayer player, int slot) {
        CultivationData data = get(player);
        if (slot < 0 || slot >= data.skillSlots.length) {
            return;
        }
        SkillType skill = SkillType.byId(data.skillSlots[slot]);
        if (skill == null) {
            player.displayClientMessage(Component.translatable(key("message.skill_empty")), true);
            return;
        }
        if (!isSkillLearned(data, skill)) {
            player.displayClientMessage(Component.translatable(key("message.skill_locked"), skill.displayName()), true);
            return;
        }
        switch (skill) {
            case LOCK_ON -> tryLockTarget(player);
            case QI_ENHANCE -> toggleQiEnhanceBuff(player, data);
            case WEAPON_ENHANCE -> toggleWeaponEnhanceBuff(player, data);
        }
    }

    /** 把技能装配到技能槽；skillId 为 0 表示清空。 */
    public static void assignSkill(ServerPlayer player, int slot, int skillId) {
        CultivationData data = get(player);
        if (slot < 0 || slot >= data.skillSlots.length) {
            return;
        }
        SkillType skill = SkillType.byId(skillId);
        if (skillId != 0 && (skill == null || !isSkillLearned(data, skill))) {
            return;
        }
        data.skillSlots[slot] = skillId;
        sync(player);
    }

    /** 切换 [真气强化] 功法 buff。 */
    private static void toggleQiEnhanceBuff(ServerPlayer player, CultivationData data) {
        if (player.hasEffect(ModEffects.QI_ENHANCEMENT)) {
            player.removeEffect(ModEffects.QI_ENHANCEMENT);
            player.displayClientMessage(Component.translatable(key("message.qi_buff_off")), true);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 1.2F);
        } else {
            player.addEffect(new MobEffectInstance(ModEffects.QI_ENHANCEMENT,
                    QI_ENHANCE_BUFF_REFRESH_TICKS, 0, false, true, true));
            player.displayClientMessage(Component.translatable(key("message.qi_buff_on")), true);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.4F);
        }
    }

    /** 切换 [武器强化] 功法 buff。 */
    private static void toggleWeaponEnhanceBuff(ServerPlayer player, CultivationData data) {
        if (player.hasEffect(ModEffects.WEAPON_ENHANCEMENT)) {
            player.removeEffect(ModEffects.WEAPON_ENHANCEMENT);
            player.displayClientMessage(Component.translatable(key("message.weapon_buff_off")), true);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            player.addEffect(new MobEffectInstance(ModEffects.WEAPON_ENHANCEMENT,
                    WEAPON_ENHANCE_DURATION_TICKS, 0, false, true, true));
            player.displayClientMessage(Component.translatable(key("message.weapon_buff_on")), true);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 0.9F);
        }
    }

    /**
     * [锁定]：沿视线射线寻找生物并锁定（最大距离由配置 lockMaxDistance 控制，默认 100 格）。
     */
    public static void tryLockTarget(ServerPlayer player) {
        CultivationData data = get(player);
        if (!isSkillLearned(data, SkillType.LOCK_ON)) {
            player.displayClientMessage(Component.translatable(key("message.skill_locked"),
                    SkillType.LOCK_ON.displayName()), true);
            return;
        }
        double maxDistance = ModCommonConfig.CONFIG.lockMaxDistance.get();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        if (look.lengthSqr() < 1.0E-6D) {
            return;
        }
        look = look.normalize();
        Vec3 end = eye.add(look.scale(maxDistance));
        Entity best = null;
        double bestDistance = Double.MAX_VALUE;
        AABB search = new AABB(eye, end).inflate(1.5D);
        for (Entity candidate : player.level().getEntities(player, search)) {
            if (candidate == player || !(candidate instanceof LivingEntity)) {
                continue;
            }
            Optional<Vec3> hit = candidate.getBoundingBox().inflate(0.5D).clip(eye, end);
            if (hit.isPresent()) {
                double d = eye.distanceToSqr(hit.get());
                if (d < bestDistance) {
                    bestDistance = d;
                    best = candidate;
                }
            }
        }
        if (best != null) {
            data.lockedTarget = best.getUUID();
            data.lockedTargetDimension = player.level().dimension().location().toString();
            player.displayClientMessage(Component.translatable(key("message.lock_on"), best.getDisplayName()), true);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.6F);
        } else {
            data.lockedTarget = null;
            data.lockedTargetDimension = "";
            player.displayClientMessage(Component.translatable(key("message.lock_none")), true);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.6F, 1.8F);
        }
        sync(player);
    }

    /** 取当前锁定的生物；目标已死或不在同一维度则自动解除并返回 null。 */
    public static LivingEntity getLockedTarget(ServerPlayer player) {
        CultivationData data = get(player);
        if (data.lockedTarget == null || !(player.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        String dimension = serverLevel.dimension().location().toString();
        if (!dimension.equals(data.lockedTargetDimension)) {
            return null;
        }
        Entity entity = serverLevel.getEntity(data.lockedTarget);
        if (entity instanceof LivingEntity living && living.isAlive()) {
            return living;
        }
        data.lockedTarget = null;
        data.lockedTargetDimension = "";
        return null;
    }

    /**
     * [武器强化] 剑气：向前挥出一道<b>剑气弹射物</b>（半圆弧蓝色，渲染见 SwordQiRenderer）。
     *
     * <p>弹射物穿透沿途生物造成伤害、不被方块阻挡，飞满 {@link #SWORD_QI_RANGE} 格后消散。
     * 触发方式改为「左键即可」：空挥由客户端发包触发，命中实体走 AttackEntityEvent，
     * 两条路径互斥、不会重复发射。
     */
    public static void fireSwordQi(ServerPlayer player) {
        if (!isWeaponEnhanceActive(player)) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SwordItem) && !(stack.getItem() instanceof AxeItem)) {
            return;
        }
        Vec3 dir = player.getLookAngle();
        if (dir.lengthSqr() < 1.0E-6D) {
            return;
        }
        CultivationData data = get(player);
        if (data.qi < SWORD_QI_QI_COST) {
            return;
        }
        data.qi -= SWORD_QI_QI_COST;

        if (player.level() instanceof ServerLevel serverLevel) {
            float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            SwordQiProjectile projectile = new SwordQiProjectile(serverLevel, player, dir, damage, SWORD_QI_RANGE);
            serverLevel.addFreshEntity(projectile);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.2F);
        }
        sync(player);
    }

    /** 点到射线的最近点（用于剑气命中判定）。 */
    private static Vec3 closestPointOnRay(Vec3 origin, Vec3 direction, Vec3 point) {
        double t = point.subtract(origin).dot(direction);
        if (t < 0.0D) {
            t = 0.0D;
        }
        return origin.add(direction.scale(t));
    }

    // ==================== 地形破坏（[真气强化] buff） ====================

    /** 在中心点周围按真气消耗量破坏地形。 */
    public static void breakTerrainAround(ServerLevel level, Vec3 center, double qiConsumed, Player player) {
        if (qiConsumed < TERRAIN_MIN_QI) {
            return;
        }
        int radius = terrainRadius(qiConsumed);
        BlockPos origin = BlockPos.containing(center);
        int broken = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-radius, -radius, -radius), origin.offset(radius, radius, radius))) {
            if (broken >= TERRAIN_MAX_BLOCKS) {
                break;
            }
            if (tryBreakBlock(level, pos.immutable(), player)) {
                broken++;
            }
        }
    }

    /** 沿位移路径按真气消耗量破坏地形。 */
    public static void breakTerrainAlong(ServerLevel level, Vec3 from, Vec3 to, double qiConsumed, Player player) {
        if (qiConsumed < TERRAIN_MIN_QI) {
            return;
        }
        double distance = from.distanceTo(to);
        int steps = Math.max(1, (int) Math.ceil(distance / 2.0D));
        int radius = Math.min(2, terrainRadius(qiConsumed));
        int totalBroken = 0;
        for (int i = 0; i <= steps; i++) {
            if (totalBroken >= TERRAIN_MAX_BLOCKS) {
                break;
            }
            double t = (double) i / (double) steps;
            Vec3 point = from.add(to.subtract(from).scale(t));
            BlockPos origin = BlockPos.containing(point);
            for (BlockPos pos : BlockPos.betweenClosed(
                    origin.offset(-radius, -radius, -radius), origin.offset(radius, radius, radius))) {
                if (totalBroken >= TERRAIN_MAX_BLOCKS) {
                    break;
                }
                if (tryBreakBlock(level, pos.immutable(), player)) {
                    totalBroken++;
                }
            }
        }
    }

    private static int terrainRadius(double qiConsumed) {
        return (int) Math.min(TERRAIN_MAX_RADIUS,
                TERRAIN_BASE_RADIUS + qiConsumed / TERRAIN_QI_PER_RADIUS);
    }

    private static boolean tryBreakBlock(ServerLevel level, BlockPos pos, Player player) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0.0F || hardness > TERRAIN_MAX_HARDNESS) {
            return false;
        }
        level.destroyBlock(pos, false, player);
        return true;
    }


    // ==================== 凡人第3层淬体试炼 ====================

    /** 突破按键（O）：凡人第 3 层后手动开启淬体试炼。 */
    public static void onBreakthroughKey(ServerPlayer player) {
        CultivationData data = get(player);
        if (data.gatePassed) {
            player.displayClientMessage(Component.translatable(key("message.breakthrough_done")), true);
            return;
        }
        if (data.mortalTier < 3) {
            player.displayClientMessage(Component.translatable(key("message.breakthrough_need_tier")), true);
            return;
        }
        if (data.gateActive) {
            player.displayClientMessage(Component.translatable(key("message.gate_running")), true);
            return;
        }
        startGateTrial(player, data);
        sync(player);
    }

    private static void handleGateTrial(ServerPlayer player, CultivationData data) {
        if (data.mortalTier < 3 || data.gatePassed || !data.gateActive) {
            return;
        }
        data.gateTicks--;
        if (data.gateTicks % 20 == 0 && data.gateTicks > 0) {
            int seconds = (data.gateTicks + 19) / 20;
            player.displayClientMessage(Component.translatable(key("message.gate_progress"), seconds), true);
        }
        applyGateTrialEffects(player, Math.max(data.gateTicks, 1));
        if (data.gateTicks <= 0) {
            data.gateActive = false;
            data.gatePassed = true;
            player.displayClientMessage(Component.translatable(key("message.gate_complete")), false);
            feedbackEffect(player, false, 3);
        }
    }

    private static void startGateTrial(ServerPlayer player, CultivationData data) {
        data.gateActive = true;
        data.gateTicks = GATE_DURATION_TICKS;
        applyGateTrialEffects(player, GATE_DURATION_TICKS);
        player.displayClientMessage(Component.translatable(key("message.gate_start")), false);
        Level level = player.level();
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 0.6F);
        summonTrialWarden(player);
    }

    /** 淬体试炼开始时在玩家周围召唤一位必定拥有真气的坚守者。 */
    private static void summonTrialWarden(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos origin = player.blockPosition();
        BlockPos spawnPos = null;
        for (int attempt = 0; attempt < 8 && spawnPos == null; attempt++) {
            double angle = Math.toRadians(attempt * 45.0D);
            BlockPos candidate = origin.offset(
                    (int) Math.round(Math.cos(angle) * 3.0D), 0, (int) Math.round(Math.sin(angle) * 3.0D));
            if (isFreeSpace(serverLevel, candidate)) {
                spawnPos = candidate;
            }
        }
        if (spawnPos == null) {
            spawnPos = origin;
        }
        Warden warden = EntityType.WARDEN.spawn(serverLevel, spawnPos, MobSpawnType.TRIGGERED);
        if (warden != null) {
            MobQiHelper.forceQi(warden);
        }
    }

    private static boolean isFreeSpace(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
    }

    /** 中毒20、凋零20、饥饿255，各撑满 30 秒。丢失效果会被立刻补回，防止用牛奶取巧。 */
    private static void applyGateTrialEffects(Player player, int duration) {
        ensureEffect(player, MobEffects.POISON, 19, duration);
        ensureEffect(player, MobEffects.WITHER, 19, duration);
        ensureEffect(player, MobEffects.HUNGER, 254, duration);
    }

    private static void ensureEffect(Player player, Holder<MobEffect> effect, int amplifier, int duration) {
        if (duration <= 0) {
            return;
        }
        MobEffectInstance current = player.getEffect(effect);
        if (current == null || current.getDuration() < duration - 20) {
            player.addEffect(new MobEffectInstance(effect, duration, amplifier, false, true, true));
        }
    }

    // ==================== 反馈 ====================

    private static void breakthroughFeedback(ServerPlayer player, boolean body, int tier) {
        feedbackEffect(player, body, tier);
        Component realm = Component.translatable(body ? key("realm.body") : key("realm.mortal"));
        player.displayClientMessage(Component.translatable(key("message.breakthrough"), realm, tier), false);
    }

    private static void feedbackEffect(ServerPlayer player, boolean body, int tier) {
        Level level = player.level();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    player.getX(), player.getY() + 1.0D, player.getZ(), 45, 0.6D, 0.9D, 0.6D, 0.15D);
            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    player.getX(), player.getY() + 1.2D, player.getZ(), 60, 0.8D, 1.0D, 0.8D, 0.5D);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, body ? 1.3F : 1.0F);
    }

    // ==================== 网络同步 ====================

    public static void sync(ServerPlayer player) {
        CultivationData data = get(player);
        PacketDistributor.sendToPlayer(player, new ClientboundCultivationSyncPayload(
                data.mortalTier,
                data.mortalExp,
                data.bodyTier,
                data.bodyExp,
                (int) Math.round(data.qi),
                data.gatePassed,
                (int) Math.round(data.bonusMaxQi),
                data.learnedSkills,
                data.skillSlots[0],
                data.skillSlots[1],
                data.skillSlots[2]));
    }

    /** 同步攻击增强模式状态到客户端（供 HUD 显示）。 */
    public static void syncEnhance(ServerPlayer player) {
        CultivationData data = get(player);
        PacketDistributor.sendToPlayer(player, new ClientboundQiEnhanceSyncPayload(
                data.attackEnhanceMode,
                (int) Math.round(data.attackEnhancePercent * 10000.0D)));
    }
}
