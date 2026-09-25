package cn.autoforged.xiuxian_cultivation_mod_1789994530.client;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.attachment.MobQiData;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.attachment.ModAttachments;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.config.ModCommonConfig;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.cultivation.MobQiHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;

/**
 * 在拥有真气的生物头顶显示真气数值条（用带颜色的竖条字符拼出）。
 * 通过 {@link RenderNameTagEvent} 强制渲染名牌实现，可在配置中关闭。
 */
@EventBusSubscriber(modid = XiuxianCultivationMod.MODID, value = Dist.CLIENT)
public class QiBarOverlay {
    private static final int SEGMENTS = 10;

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!ModCommonConfig.CONFIG.entityQiBarEnabled.get()) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        double qi;
        double maxQi;
        if (living instanceof Player player) {
            // 玩家只有本地缓存同步了自己的真气，其他玩家不同步
            if (player != Minecraft.getInstance().player) {
                return;
            }
            qi = ClientCultivationData.getQi();
            maxQi = player.getMaxHealth() * 10.0D
                    + player.getArmorValue() * 50.0D + ClientCultivationData.getBonusMaxQi();
        } else {
            if (!living.hasData(ModAttachments.MOB_QI.get())) {
                return;
            }
            MobQiData data = living.getData(ModAttachments.MOB_QI.get());
            if (!data.hasQi) {
                return;
            }
            qi = data.qi;
            maxQi = MobQiHelper.getMaxQi(living);
        }
        if (maxQi <= 0.0D) {
            return;
        }
        event.setContent(buildBar(qi, maxQi,
                living.hasCustomName() ? event.getOriginalContent() : null));
        event.setCanRender(TriState.TRUE);
    }

    private static Component buildBar(double qi, double maxQi, Component original) {
        MutableComponent line = Component.empty();
        if (original != null) {
            line.append(original).append(" ");
        }
        double ratio = Math.max(0.0D, Math.min(1.0D, qi / maxQi));
        int filled = (int) Math.round(ratio * SEGMENTS);
        for (int i = 0; i < SEGMENTS; i++) {
            line.append(Component.literal("|")
                    .withStyle(i < filled ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY));
        }
        line.append(Component.literal(" " + Math.round(qi) + "/" + Math.round(maxQi))
                .withStyle(ChatFormatting.WHITE));
        return line;
    }
}
