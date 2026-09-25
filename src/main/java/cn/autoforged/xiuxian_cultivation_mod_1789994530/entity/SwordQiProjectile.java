package cn.autoforged.xiuxian_cultivation_mod_1789994530.entity;

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

    /** 每 tick 飞行速度（格/tick）。 */
    private static final double SPEED = 0.85D;
    /** 命中判定额外的膨胀半径，让弧面更容易扫到生物。 */
    private static final double HIT_INFLATE = 0.9D;

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

        // 飞满射程即消散
        if (this.traveled >= this.maxRange) {
            this.discard();
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
