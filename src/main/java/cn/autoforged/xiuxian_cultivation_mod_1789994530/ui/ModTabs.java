package cn.autoforged.xiuxian_cultivation_mod_1789994530.ui;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, XiuxianCultivationMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CULTIVATION_TAB =
            CREATIVE_TABS.register("cultivation", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + XiuxianCultivationMod.MODID))
                    .withTabsBefore(CreativeModeTabs.INGREDIENTS)
                    .icon(() -> ModItems.BODY_TEMPERING_PILL.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BODY_TEMPERING_PILL.get());
                        // 三本功法秘籍
                        output.accept(ModItems.LOCK_ON_MANUAL.get());
                        output.accept(ModItems.QI_ENHANCE_MANUAL.get());
                        output.accept(ModItems.WEAPON_ENHANCE_MANUAL.get());
                    })
                    .build());

    private ModTabs() {
    }
}
