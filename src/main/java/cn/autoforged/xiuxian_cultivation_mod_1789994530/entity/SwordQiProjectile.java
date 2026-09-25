package cn.autoforged.xiuxian_cultivation_mod_1789994530.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * [武器强化] 剑气弹射物：一道半圆弧形的蓝色剑气（模型由 SwordQiRenderer 现画）。
 *
 * <p>按需求实现的行为：
 * <ul>
 *   <li><b>穿透实体</b>：沿途每个生物都被扫到并受到一次伤害，但剑气不停留、不消失；</li>
 *   <li><b>穿透方块</b>：不受方块阻挡，也不会因撞墙消失；</li>
 *   <li>飞满最大射程后自动消失。</li>
 * </ul>
 */
public class SwordQiProjectile extends Projectile {

    /** 每 tick 飞行速度（格/tick）。原为 0.85，快到只剩残影，放慢到 0.5 便于看清。 */
    private static final double SPEED = 0.5D;
    /** 命中判定额外的膨胀半径，让弧面更容易扫到生物。 */
    private static final double HIT_INFLATE = 0.9D;

    // ===== 剑气外观：由原版粒子构成的「拱形」光弧 =====
    /**
     * 弧上采样段数。段数越多弧越连续，但粒子数线性增长（性能开关）。
     * 参考图里那道弧横跨约 8-10 格，弧长变大后需要更多采样点才不会显得断断续续。
     */
    private static final int ARC_SEGMENTS = 24;
    /** 弧的半径（格）。3.0 对应弧宽约 6 格，接近参考图的体量。 */
    private static final double ARC_RADIUS = 3.0D;
    /**
     * 每隔多少 tick 撒一次粒子。
     * 1 = 每 tick 都撒（最密最好看，但粒子数翻倍）；
     * 2 = 隔 tick 撒（省一半开销，视觉上几乎看不出差别）。卡顿时优先调这个。
     */
    private static final int ARC_EMIT_INTERVAL = 2;

    private float damage = 1.0F;
    private double maxRange = 12.0D;
    private double traveled = 0.0D;
    /** 已被本发剑气打过的实体，避免同一目标被重复扣血。 */
    private final Set<UUID> hitEntities = new HashSet<>();

    public SwordQiProjectile(EntityType<? extends SwordQiProjectile> type, Level level) {
        super(type, level);
    }

    /** 由玩家发射：给定方向、伤害与最大射程。 */
    public SwordQiProjectile(Level level, LivingEntity owner, Vec3 direction, float damage, double maxRange) {
        this(ModEntities.SWORD_QI.get(), level);
        this.setOwner(owner);
        this.damage = damage;
        this.maxRange = maxRange;

        Vec3 dir = direction.normalize();
        this.setPos(owner.getX(), owner.getEyeY() - 0.25D, owner.getZ());
        this.setDeltaMovement(dir.scale(SPEED));
        // 让弧面朝向与飞行方向一致
        this.setYRot((float) Math.toDegrees(Math.atan2(dir.x, dir.z)));
        this.setXRot((float) Math.toDegrees(-Math.asin(dir.y)));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // 无需同步字段：伤害/射程只在服务端推进，客户端只看飞行轨迹
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
            AABB sweep = this.getBoundingBox().inflate(HIT_INFLATE);
            for (Entity candidate : this.level().getEntities(this, sweep)) {
                if (candidate == this.getOwner() || !(candidate instanceof LivingEntity living)) {
                    continue;
                }
                if (living.isAlive() && this.hitEntities.add(living.getUUID())) {
                    living.hurt(this.damageSources().magic(), this.damage);
                }
            }
        }

        // 外观：由原版粒子构成的拱形光弧。只在客户端生成（服务端撒粒子没有意义，还浪费带宽）。
        if (this.level().isClientSide() && this.tickCount % ARC_EMIT_INTERVAL == 0) {
            emitArcParticles();
        }

        // 飞满射程即消散
        if (this.traveled >= this.maxRange) {
            this.discard();
        }
    }

    /**
     * 沿「拱形弧」铺一圈原版粒子，构成剑气外观（对应参考图：白色十字粒子勾弧 + 粉紫光晕）。
     *
     * <p>做法：用飞行方向算出三个正交基向量（前 / 右 / 上），再在「右-上」平面内
     * 按 0~π 采样半圆 —— 中间最高、两端最低，即一道拱形弧。把每个采样点换算到世界坐标
     * 后各撒两颗粒子：
     * <ul>
     *   <li>{@link ParticleTypes#FIREWORK} —— 白色十字闪光，负责勾出弧线轮廓；</li>
     *   <li>{@link ParticleTypes#DRAGON_BREATH} —— 粉紫色雾气，负责柔和的发光底色。</li>
     * </ul>
     *
     * <p>两者都是原版粒子，不引入任何额外依赖或贴图。
     */
    private void emitArcParticles() {
        Vec3 forward = this.getDeltaMovement();
        if (forward.lengthSqr() < 1.0E-6D) {
            return;
        }
        forward = forward.normalize();

        // 以世界上方向为参照构造正交基；正上/正下飞行时退化，换一个参照向量
        Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = forward.cross(worldUp);
        if (right.lengthSqr() < 1.0E-6D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }
        Vec3 up = right.cross(forward).normalize();

        Vec3 base = this.position();
        for (int i = 0; i <= ARC_SEGMENTS; i++) {
            double t = (double) i / ARC_SEGMENTS;
            double angle = Math.PI * t;                        // 0 → π
            double offsetRight = -Math.cos(angle) * ARC_RADIUS; // 左端 → 右端
            double offsetUp = Math.sin(angle) * ARC_RADIUS;     // 中间最高，两端贴地
            Vec3 p = base.add(right.scale(offsetRight)).add(up.scale(offsetUp));

            this.level().addParticle(ParticleTypes.FIREWORK, p.x, p.y, p.z, 0.0D, 0.0D, 0.0D);
            this.level().addParticle(ParticleTypes.DRAGON_BREATH, p.x, p.y, p.z, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.damage = tag.getFloat("Damage");
        this.maxRange = tag.getDouble("MaxRange");
        this.traveled = tag.getDouble("Traveled");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Damage", this.damage);
        tag.putDouble("MaxRange", this.maxRange);
        tag.putDouble("Traveled", this.traveled);
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
