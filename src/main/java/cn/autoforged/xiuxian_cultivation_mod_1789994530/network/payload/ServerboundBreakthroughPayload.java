package cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 客户端 -> 服务端：按下突破按键（O）时发送。 */
public record ServerboundBreakthroughPayload() implements CustomPacketPayload {

    public static final ServerboundBreakthroughPayload INSTANCE = new ServerboundBreakthroughPayload();

    public static final CustomPacketPayload.Type<ServerboundBreakthroughPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(XiuxianCultivationMod.MODID, "breakthrough"));

    public static final StreamCodec<ByteBuf, ServerboundBreakthroughPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
