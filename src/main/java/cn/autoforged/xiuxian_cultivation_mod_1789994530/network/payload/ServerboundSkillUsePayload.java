package cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 客户端 -> 服务端：按下 Z/X/C 技能键时发送，携带技能槽下标（0/1/2）。 */
public record ServerboundSkillUsePayload(int slot) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerboundSkillUsePayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(XiuxianCultivationMod.MODID, "skill_use"));

    public static final StreamCodec<ByteBuf, ServerboundSkillUsePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ServerboundSkillUsePayload::slot,
                    ServerboundSkillUsePayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
