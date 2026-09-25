package cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端 -> 服务端：在技能配置界面里把某个功法装配到技能槽。
 *
 * @param slot    技能槽下标（0/1/2 对应 Z/X/C）
 * @param skillId 功法技能 id（0 表示清空）
 */
public record ServerboundSkillAssignPayload(int slot, int skillId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerboundSkillAssignPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(XiuxianCultivationMod.MODID, "skill_assign"));

    public static final StreamCodec<ByteBuf, ServerboundSkillAssignPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ServerboundSkillAssignPayload::slot,
                    ByteBufCodecs.VAR_INT, ServerboundSkillAssignPayload::skillId,
                    ServerboundSkillAssignPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
