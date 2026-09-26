package cn.autoforged.xiuxian_cultivation_mod_1789994530.client;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.client.hud.CultivationHudOverlay;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.client.particle.SwordQiParticle;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.client.render.SwordQiRenderer;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.entity.ModEntities;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.particle.ModParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = XiuxianCultivationMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ModClientEvents {

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        // 注册在饥饿/饱和度条之上，实现「真气值在饱和度上方显示」
        event.registerAbove(
                VanillaGuiLayers.FOOD_LEVEL,
                CultivationHudOverlay.HUD_ID,
                CultivationHudOverlay.INSTANCE);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 剑气弹射物：外观由粒子承担，渲染器只占位（详见 SwordQiRenderer 注释）
        event.registerEntityRenderer(ModEntities.SWORD_QI.get(), SwordQiRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        // 剑气粒子：蓝色 / 零重力 / 4 tick 短寿命（见 SwordQiParticle）
        event.registerSpriteSet(ModParticles.SWORD_QI.get(), SwordQiParticle.Provider::new);
    }
}
