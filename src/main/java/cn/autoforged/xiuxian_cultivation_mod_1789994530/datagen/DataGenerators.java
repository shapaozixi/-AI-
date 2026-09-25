package cn.autoforged.xiuxian_cultivation_mod_1789994530.datagen;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = XiuxianCultivationMod.MODID)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(),
                new ModItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(),
                new ModRecipeProvider(packOutput, event.getLookupProvider()));
    }
}
