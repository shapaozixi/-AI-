package cn.autoforged.xiuxian_cultivation_mod_1789994530.entity;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.cultivation.CultivationHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * [武器强化] 剑气弹射物：一道贴地平扫的弯月形弧刃，外观完全由原版粒子构成。
 *
 * <p>行为：
 * <ul>
 *   <li><b>穿透实体</b>：沿途每个生物各受一次伤害，剑气不停留；</li>
 *   <li><b>穿透方块</b>：自身不受方块阻挡，但会<b>沿途破坏</b>可破坏的方块；</li>
 *   <li><b>体积联动</b>：发射时消耗的真气越多，月牙越大、破坏力越强；</li>
 *   <li>飞满最大射程后消散。</li>
 * </ul>
 */
public class SwordQiProjectile extends Projectile {

    /** 飞行速度（格/tick）。刻意偏快，突出「凌厉」的手感。 */
    private static final double SPEED = 0.95D;
    /** 命中判定额外的膨胀半径。 */
    private static final double HIT_INFLATE = 1.0D;

    // ===== 外观：原版粒子构成的「贴地平扫弯月」 =====
    /** 沿弧采样段数。 */
    private static final int ARC_SEGMENTS = 16;
    /** 外弧基准半径（格），实际半径 = 基准 × scale。 */
    private static final double ARC_BASE_RADIUS = 2.4D;
    /** 月牙最厚处相对外弧半径的比例（中间最厚、两端收成刀尖）。 */
    private static final double ARC_THICKNESS_RATIO = 0.30D;
    /** 每一段沿厚度方向填几颗粒子。 */
    private static final int ARC_FILL_PER_SEGMENT = 3;
    /** 每几 tick 撒一次粒子。 */
    private static final int ARC_EMIT_INTERVAL = 1;

    // ===== 体积联动 =====
    /** 体积缩放下限。 */
    private static final double SCALE_MIN = 0.70D;
    /** 体积缩放上限。 */
    private static final double SCALE_MAX = 2.60D;
    /** 每消耗 100 点真气带来的额外缩放。 */
    private static final double SCALE_PER_100_QI = 0.55D;

    // ===== 沿途破坏 =====
    /** 每几 tick 尝试破坏一次沿途方块，避免逐 tick 破坏造成卡顿。 */
    private static final int TERRAIN_BREAK_INTERVAL = 2;
    /**
     * 剑气破坏地形的真气折算系数。
     * 比「真气强化冲刺」的破坏弱一些，否则一路飞过会把地表整片推平。
     */
    private static final double TERRAIN_QI_FACTOR = 0.30D;

    private float damage = 1.0F;
    private double maxRange = 12.0D;
    private double traveled = 0.0D;
    /** 发射时消耗的真气总量：同时决定月牙大小与破坏力。 */
    private double qiSpent = 0.0D;
    /** 由 {@link #qiSpent} 推出的体积缩放。 */
    private double scale = 1.0D;
    /** 已被本发剑气打过的实体，避免同一目标被重复扣血。 */
    private final Set<UUID> hitEntities = new HashSet<>();

    public SwordQiProjectile(EntityType<? extends SwordQiProjectile> type, Level level) {
        super(type, level);
    }

    /**
     * 由玩家发射。
     *
     * @param qiSpent 本次发射消耗的真气总量（决定体积与破坏力）
     */
    public SwordQiProjectile(Level level, LivingEntity owner, Vec3 direction, float damage,
                             double maxRange, double qiSpent) {
        this(ModEntities.SWORD_QI.get(), level);
        this.setOwner(owner);
        this.damage = damage;
        this.maxRange = maxRange;
        this.qiSpent = qiSpent;
        // 消耗真气越多 → 月牙越大（体积联动）
        this.scale = Math.max(SCALE_MIN,
                Math.min(SCALE_MAX, 1.0D + qiSpent / 100.0D * SCALE_PER_100_QI));

        Vec3 dir = direction.normalize();
        this.setPos(owner.getX(), owner.getEyeY() - 0.25D, owner.getZ());
        this.setDeltaMovement(dir.scale(SPEED));
        this.setYRot((float) Math.toDegrees(Math.atan2(dir.x, dir.z)));
        this.setXRot((float) Math.toDegrees(-Math.asin(dir.y)));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // 无需同步字段：伤害/射程/体积只在服务端推进，客户端只看飞行轨迹与粒子
    }

    @Override
    public void tick() {
        super.tick();

        Vec3 motion = this.getDeltaMovement();
        Vec3 next = this.position().add(motion);
        // 直接位移，不做方块碰撞检测 —— 剑气穿透方块
        this.setPos(next.x, next.y, next.z);
        this.traveled += motion.length();

        if (!this.level().isClientSide()) {
            // 沿途破坏方块（穿透不代表无害）
            if (this.level() instanceof ServerLevel serverLevel
                    && this.tickCount % TERRAIN_BREAK_INTERVAL == 0) {
                Player owner = this.getOwner() instanceof Player p ? p : null;
                CultivationHelper.breakTerrainAround(serverLevel, this.position(),
                        this.qiSpent * TERRAIN_QI_FACTOR, owner);
            }

            // 命中判定：弧面扫到谁就伤害谁，同一目标只扣一次
            AABB sweep = this.getBoundingBox().inflate(HIT_INFLATE * this.scale);
            for (Entity candidate : this.level().getEntities(this, sweep)) {
                if (candidate == this.getOwner() || !(candidate instanceof LivingEntity living)) {
                    continue;
                }
                if (living.isAlive() && this.hitEntities.add(living.getUUID())) {
                    living.hurt(this.damageSources().magic(), this.damage);
                }
            }
        } else {
            // 外观：原版粒子构成的贴地平扫月牙
            if (this.tickCount % ARC_EMIT_INTERVAL == 0) {
                emitArcParticles();
            }
        }

        // 飞满射程即消散
        if (this.traveled >= this.maxRange) {
            this.discard();
        }
    }

    /**
     * 沿「贴地平扫的弯月」铺粒子。
     *
     * <p>三个正交基：飞行方向算出水平右向量 {@code right}，再把飞行方向压进水平面得到
     * {@code flatForward} —— 弧就在这两个向量张成的<b>水平面</b>内展开，所以看起来是一道横劈
     * 出去的弧刃，而不是一堵竖着的弧墙。
     *
     * <p>厚度沿弧线按 {@code sin} 分布：中间最厚、两端收成刀尖。外缘一层用
     * {@link ParticleTypes#FIREWORK}（白色十字）勾出锋利刃口，内层用
     * {@link ParticleTypes#DRAGON_BREATH}（粉紫）填充光晕。
     */
    private void emitArcParticles() {
        Vec3 forward = this.getDeltaMovement();
        if (forward.lengthSqr() < 1.0E-6D) {
            return;
        }
        forward = forward.normalize();

        Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = forward.cross(worldUp);
        if (right.lengthSqr() < 1.0E-6D) {
            // 垂直向上/下飞行时叉乘退化，随便给一个水平方向
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }
        Vec3 flatForward = worldUp.cross(right).normalize();
        if (flatForward.dot(forward) < 0.0D) {
            flatForward = flatForward.scale(-1.0D);      // 保证凸面朝前
        }

        double outerRadius = ARC_BASE_RADIUS * this.scale;
        double maxThickness = outerRadius * ARC_THICKNESS_RATIO;
        Vec3 base = this.position();

        for (int i = 0; i <= ARC_SEGMENTS; i++) {
            double t = (double) i / ARC_SEGMENTS;
            double angle = Math.PI * t;                             // 0 → π
            double thickness = maxThickness * Math.sin(Math.PI * t);
            if (thickness < 1.0E-4D) {
                continue;                                           // 刀尖处自然收束
            }

            for (int k = 0; k < ARC_FILL_PER_SEGMENT; k++) {
                double fill = (k + 0.5D) / ARC_FILL_PER_SEGMENT;
                double radius = outerRadius - thickness * fill;
                double offsetRight = -Math.cos(angle) * radius;
                double offsetForward = Math.sin(angle) * radius;
                Vec3 p = base
                        .add(right.scale(offsetRight))
                        .add(flatForward.scale(offsetForward));

                boolean edge = fill < 0.34D;
                this.level().addParticle(
                        edge ? ParticleTypes.FIREWORK : ParticleTypes.DRAGON_BREATH,
                        p.x, p.y, p.z, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.damage = tag.getFloat("Damage");
        this.maxRange = tag.getDouble("MaxRange");
        this.traveled = tag.getDouble("Traveled");
        this.qiSpent = tag.getDouble("QiSpent");
        this.scale = tag.getDouble("Scale");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Damage", this.damage);
        tag.putDouble("MaxRange", this.maxRange);
        tag.putDouble("Traveled", this.traveled);
        tag.putDouble("QiSpent", this.qiSpent);
        tag.putDouble("Scale", this.scale);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSqr) {
        return distanceSqr < 4096.0D;
    }
}
