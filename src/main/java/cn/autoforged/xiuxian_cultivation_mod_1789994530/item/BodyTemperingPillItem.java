package cn.autoforged.xiuxian_cultivation_mod_1789994530.item;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.cultivation.CultivationHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 炼体丹：服用后获得修炼经验。尚未开启炼体境时转化为凡人境经验。
 */
public class BodyTemperingPillItem extends Item {
    /** 基础经验（最终会乘以全局经验倍率）。数值已下调以放缓发育速度。 */
    public static final int PILL_EXP = 500;

    public BodyTemperingPillItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            CultivationHelper.addCultivationExp(serverPlayer, PILL_EXP);
            int granted = (int) Math.ceil(PILL_EXP * CultivationHelper.EXP_MULTIPLIER);
            serverPlayer.displayClientMessage(
                    Component.translatable(CultivationHelper.key("message.pill_used"), granted), true);
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
