package cn.autoforged.xiuxian_cultivation_mod_1789994530.item;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.cultivation.CultivationHelper;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.skill.SkillType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 功法秘籍（消耗品）：右键使用后永久习得对应功法。
 *
 * <p>学习成功才消耗物品；若玩家已习得该功法则不消耗，只给出提示。
 * 每个功法对应一个独立物品，但当前阶段它们共用同一套模型与贴图。
 */
public class SkillBookItem extends Item {

    private final SkillType skill;

    public SkillBookItem(SkillType skill, Properties properties) {
        super(properties);
        this.skill = skill;
    }

    /** 这本秘籍记载的功法。 */
    public SkillType skill() {
        return this.skill;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // 学会返回 true；已习得返回 false（此时不消耗）
            if (CultivationHelper.learnSkill(serverPlayer, this.skill)) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
