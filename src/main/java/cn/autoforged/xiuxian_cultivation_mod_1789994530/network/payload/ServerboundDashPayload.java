package cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 客户端 -> 服务端：真气强化期间按下冲刺按键（R）时发送。 */
public record ServerboundDashPayload() implements CustomPacketPayload {

    public static final ServerboundDashPayload INSTANCE = new ServerboundDashPayload();

    public static final CustomPacketPayload.Type<ServerboundDashPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(XiuxianCultivationMod.MODID, "dash"));

    public static final StreamCodec<ByteBuf, ServerboundDashPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
