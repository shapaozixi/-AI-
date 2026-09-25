package cn.autoforged.xiuxian_cultivation_mod_1789994530.client;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

/** 客户端按键绑定：V 切换/调整真气强化，O 突破（开启淬体试炼），R 真气冲刺，
 * Z/X/C 释放功法技能，G 锁定生物，Shift+L 打开技能配置界面。 */
@EventBusSubscriber(modid = XiuxianCultivationMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ModKeyMappings {

    public static final String KEY_CATEGORY = "key.categories." + XiuxianCultivationMod.MODID;

    public static final Lazy<KeyMapping> QI_ENHANCE_KEY = Lazy.of(() -> new KeyMapping(
            "key." + XiuxianCultivationMod.MODID + ".qi_enhance",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            KEY_CATEGORY));

    public static final Lazy<KeyMapping> BREAKTHROUGH_KEY = Lazy.of(() -> new KeyMapping(
            "key." + XiuxianCultivationMod.MODID + ".breakthrough",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            KEY_CATEGORY));

    public static final Lazy<KeyMapping> DASH_KEY = Lazy.of(() -> new KeyMapping(
            "key." + XiuxianCultivationMod.MODID + ".dash",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            KEY_CATEGORY));

    public static final Lazy<KeyMapping> SKILL_Z_KEY = Lazy.of(() -> new KeyMapping(
            "key." + XiuxianCultivationMod.MODID + ".skill_z",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            KEY_CATEGORY));

    public static final Lazy<KeyMapping> SKILL_X_KEY = Lazy.of(() -> new KeyMapping(
            "key." + XiuxianCultivationMod.MODID + ".skill_x",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            KEY_CATEGORY));

    public static final Lazy<KeyMapping> SKILL_C_KEY = Lazy.of(() -> new KeyMapping(
            "key." + XiuxianCultivationMod.MODID + ".skill_c",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            KEY_CATEGORY));

    public static final Lazy<KeyMapping> LOCK_KEY = Lazy.of(() -> new KeyMapping(
            "key." + XiuxianCultivationMod.MODID + ".lock",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            KEY_CATEGORY));

    public static final Lazy<KeyMapping> SKILL_MENU_KEY = Lazy.of(() -> new KeyMapping(
            "key." + XiuxianCultivationMod.MODID + ".skill_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_L,
            KEY_CATEGORY));

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(QI_ENHANCE_KEY.get());
        event.register(BREAKTHROUGH_KEY.get());
        event.register(DASH_KEY.get());
        event.register(SKILL_Z_KEY.get());
        event.register(SKILL_X_KEY.get());
        event.register(SKILL_C_KEY.get());
        event.register(LOCK_KEY.get());
        event.register(SKILL_MENU_KEY.get());
    }
}
