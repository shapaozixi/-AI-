package cn.autoforged.xiuxian_cultivation_mod_1789994530.datagen;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, XiuxianCultivationMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.BODY_TEMPERING_PILL.get());
        // 所有功法秘籍当前共用同一套模型与贴图（统一模型，见需求）
        skillBookModel(ModItems.LOCK_ON_MANUAL.get());
        skillBookModel(ModItems.QI_ENHANCE_MANUAL.get());
        skillBookModel(ModItems.WEAPON_ENHANCE_MANUAL.get());
    }

    /**
     * 生成一个指向统一贴图 {@code item/skill_book} 的平面物品模型。
     * 三本秘籍的 JSON 内容完全一致，只换成各自的文件名，从而共用同一张图。
     */
    private void skillBookModel(Item item) {
        withExistingParent(key(item).getPath(), mcLoc("item/generated"))
                .texture("layer0", modLoc("item/skill_book"));
    }
}
