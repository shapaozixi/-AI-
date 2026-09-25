package cn.autoforged.xiuxian_cultivation_mod_1789994530.item;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.skill.SkillType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(XiuxianCultivationMod.MODID);

    public static final DeferredItem<BodyTemperingPillItem> BODY_TEMPERING_PILL = ITEMS.registerItem(
            "body_tempering_pill",
            BodyTemperingPillItem::new,
            new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));

    // ==================== 功法秘籍（每个功法一个物品，消耗品） ====================

    /** 锁定功法秘籍：右键使用后习得 [锁定]。 */
    public static final DeferredItem<SkillBookItem> LOCK_ON_MANUAL = ITEMS.registerItem(
            "lock_on_manual",
            properties -> new SkillBookItem(SkillType.LOCK_ON, properties),
            new Item.Properties().stacksTo(16).rarity(Rarity.RARE));

    /** 真气强化功法秘籍：右键使用后习得 [真气强化]。 */
    public static final DeferredItem<SkillBookItem> QI_ENHANCE_MANUAL = ITEMS.registerItem(
            "qi_enhance_manual",
            properties -> new SkillBookItem(SkillType.QI_ENHANCE, properties),
            new Item.Properties().stacksTo(16).rarity(Rarity.RARE));

    /** 武器强化功法秘籍：右键使用后习得 [武器强化]。 */
    public static final DeferredItem<SkillBookItem> WEAPON_ENHANCE_MANUAL = ITEMS.registerItem(
            "weapon_enhance_manual",
            properties -> new SkillBookItem(SkillType.WEAPON_ENHANCE, properties),
            new Item.Properties().stacksTo(16).rarity(Rarity.RARE));

    private ModItems() {
    }
}
