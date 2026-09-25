package cn.autoforged.xiuxian_cultivation_mod_1789994530.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/**
 * 客户端本地的「锁定目标」状态。
 *
 * <p>由 {@link cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload.ClientboundLockTargetPayload}
 * 更新，被 {@link ClientLockOnHandler} 每 tick 读取来做平滑视角跟随。
 *
 * <p>纯客户端字段，不需要同步回服务端。
 */
public final class ClientLockOnState {

    /** 当前锁定目标的实体 ID；-1 表示没有锁定。 */
    private static int targetEntityId = -1;

    private ClientLockOnState() {
    }

    public static void setTarget(int entityId) {
        targetEntityId = entityId;
    }

    public static void clear() {
        targetEntityId = -1;
    }

    public static boolean isLocked() {
        return targetEntityId >= 0;
    }

    /**
     * 取当前锁定目标实体。
     *
     * @return 目标实体；未锁定、已切换维度或目标已消失时返回 {@code null}
     */
    public static Entity getTarget() {
        if (targetEntityId < 0) {
            return null;
        }
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        Entity entity = level.getEntity(targetEntityId);
        return (entity != null && entity.isAlive()) ? entity : null;
    }
}
