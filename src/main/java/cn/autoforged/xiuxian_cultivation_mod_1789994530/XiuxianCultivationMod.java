package cn.autoforged.xiuxian_cultivation_mod_1789994530;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.attachment.ModAttachments;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.config.ModCommonConfig;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.effect.ModEffects;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.entity.ModEntities;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.item.ModItems;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.particle.ModParticles;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.ui.ModTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(XiuxianCultivationMod.MODID)
public class XiuxianCultivationMod {
    public static final String MODID = "xiuxian_cultivation_mod_1789994530";

    public XiuxianCultivationMod(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModAttachments.ATTACHMENTS.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModTabs.CREATIVE_TABS.register(modEventBus);
        ModParticles.PARTICLES.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, ModCommonConfig.CONFIG_SPEC);
    }
}
