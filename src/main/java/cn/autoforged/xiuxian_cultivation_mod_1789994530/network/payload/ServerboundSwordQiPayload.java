package cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端 -> 服务端：左键「空挥」（没有命中任何实体）时发送，请求释放剑气。
 * 命中实体的情况由服务端 AttackEntityEvent 处理，两条路径不会重复触发。
 */
public record ServerboundSwordQiPayload() implements CustomPacketPayload {

    public static final ServerboundSwordQiPayload INSTANCE = new ServerboundSwordQiPayload();

    public static final CustomPacketPayload.Type<ServerboundSwordQiPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(XiuxianCultivationMod.MODID, "sword_qi_swing"));

    public static final StreamCodec<ByteBuf, ServerboundSwordQiPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
