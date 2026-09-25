package cn.autoforged.xiuxian_cultivation_mod_1789994530.client;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * [锁定] 客户端平滑视角跟随。
 *
 * <p>设计要点（对应「跟随生物极其抖动」的修复）：
 * <ul>
 *   <li>不再由服务端每 tick 硬发 LookAt 包强制转向 —— 那会和玩家的鼠标输入
 *       每 tick 争夺控制权，表现为剧烈抖动；</li>
 *   <li>改为客户端自己按固定比例（{@link #FOLLOW_FACTOR}）向目标角度<b>插值逼近</b>，
 *       眼睛看到的是连续平滑的转动，且不会产生位置偏移。</li>
 * </ul>
 *
 * <p>每 tick 执行：{@code setYRot}/{@code setXRot} 是 tick 级别的朝向，
 * 渲染时游戏会自动在上一 tick 与本 tick 之间插值，所以 20Hz 更新依然是平滑的。
 */
@EventBusSubscriber(modid = XiuxianCultivationMod.MODID, value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.GAME)
public final class ClientLockOnHandler {

    /**
     * 每 tick 向目标角度逼近的比例。
     * 越大越"跟手"但越生硬；越小越平滑但越迟钝。
     * 0.35 大约是「约 0.2 秒内追上」的手感。
     */
    private static final float FOLLOW_FACTOR = 0.35F;

    private ClientLockOnHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        // 打开 GUI / 暂停时不抢视角，避免菜单里镜头乱转
        if (minecraft.screen != null || minecraft.isPaused()) {
            return;
        }
        Entity target = ClientLockOnState.getTarget();
        if (target == null) {
            return;
        }

        Vec3 eye = minecraft.player.getEyePosition();
        Vec3 toTarget = target.getEyePosition().subtract(eye);
        double horizontal = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
        if (horizontal < 1.0E-4D && Math.abs(toTarget.y) < 1.0E-4D) {
            return;   // 和目标重合，没有可用的方向
        }

        // Minecraft 的朝向约定：
        //   yaw   = atan2(-dx, dz)，0 指向 +Z（南），顺时针为正
        //   pitch = -atan2(dy, 水平距离)，正值为向下看
        float wantYaw = (float) Math.toDegrees(Math.atan2(-toTarget.x, toTarget.z));
        float wantPitch = (float) Math.toDegrees(-Math.atan2(toTarget.y, horizontal));

        minecraft.player.setYRot(approach(minecraft.player.getYRot(), wantYaw, FOLLOW_FACTOR));
        minecraft.player.setXRot(approach(minecraft.player.getXRot(), wantPitch, FOLLOW_FACTOR));
    }

    /**
     * 角度插值：先取最短转向路径（处理 ±180° 环绕），再按比例逼近。
     *
     * @param current 当前角度（度）
     * @param target  目标角度（度）
     * @param factor  逼近比例，0~1
     * @return 插值后的角度
     */
    private static float approach(float current, float target, float factor) {
        float delta = Mth.wrapDegrees(target - current);
        return current + delta * factor;
    }
}
