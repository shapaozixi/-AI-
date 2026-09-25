package cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端 -> 服务端：按下 V 键时发送。
 *
 * @param adjust true 表示蹲下按 V，调整消耗比例；false 表示切换攻击增强模式开关
 */
public record ServerboundToggleQiEnhancePayload(boolean adjust) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerboundToggleQiEnhancePayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(XiuxianCultivationMod.MODID, "toggle_qi_enhance"));

    public static final StreamCodec<ByteBuf, ServerboundToggleQiEnhancePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, ServerboundToggleQiEnhancePayload::adjust,
                    ServerboundToggleQiEnhancePayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
