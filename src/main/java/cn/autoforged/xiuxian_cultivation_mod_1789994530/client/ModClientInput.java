package cn.autoforged.xiuxian_cultivation_mod_1789994530.client;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload.ServerboundBreakthroughPayload;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload.ServerboundDashPayload;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload.ServerboundLockPayload;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload.ServerboundSkillUsePayload;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload.ServerboundSwordQiPayload;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload.ServerboundToggleQiEnhancePayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 客户端输入处理：检测 V（真气强化）、O（突破）、R（冲刺）、Z/X/C（功法技能）、
 * G（锁定）、Shift+L（技能配置），以及左键空挥（剑气）。
 */
@EventBusSubscriber(modid = XiuxianCultivationMod.MODID, value = Dist.CLIENT)
public class ModClientInput {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        while (ModKeyMappings.QI_ENHANCE_KEY.get().consumeClick()) {
            // 蹲下按 V = 调整消耗比例，否则切换真气强化模式开关
            PacketDistributor.sendToServer(new ServerboundToggleQiEnhancePayload(minecraft.player.isShiftKeyDown()));
        }
        while (ModKeyMappings.BREAKTHROUGH_KEY.get().consumeClick()) {
            PacketDistributor.sendToServer(ServerboundBreakthroughPayload.INSTANCE);
        }
        while (ModKeyMappings.DASH_KEY.get().consumeClick()) {
            PacketDistributor.sendToServer(ServerboundDashPayload.INSTANCE);
        }
        while (ModKeyMappings.SKILL_Z_KEY.get().consumeClick()) {
            PacketDistributor.sendToServer(new ServerboundSkillUsePayload(0));
        }
        while (ModKeyMappings.SKILL_X_KEY.get().consumeClick()) {
            PacketDistributor.sendToServer(new ServerboundSkillUsePayload(1));
        }
        while (ModKeyMappings.SKILL_C_KEY.get().consumeClick()) {
            PacketDistributor.sendToServer(new ServerboundSkillUsePayload(2));
        }
        while (ModKeyMappings.LOCK_KEY.get().consumeClick()) {
            PacketDistributor.sendToServer(ServerboundLockPayload.INSTANCE);
        }
        while (ModKeyMappings.SKILL_MENU_KEY.get().consumeClick()) {
            // Shift+L 打开技能配置界面
            if (minecraft.player.isShiftKeyDown()) {
                minecraft.setScreen(new SkillConfigScreen());
            }
        }
    }

    /**
     * 左键「空挥」：手上没打到任何实体时触发（仅客户端），请求服务端释放剑气，
     * 从而实现「即使空挥也会发出剑气」。命中实体的情况走服务端 AttackEntityEvent，
     * 两条路径互斥，不会重复发射。
     */
    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        PacketDistributor.sendToServer(ServerboundSwordQiPayload.INSTANCE);
    }
}
