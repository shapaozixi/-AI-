package cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 服务端 -> 客户端：同步攻击增强模式状态。
 *
 * @param active             模式是否开启
 * @param percentBasisPoints 每次攻击消耗真气上限的比例（万分比，100 = 1%）
 */
public record ClientboundQiEnhanceSyncPayload(
        boolean active,
        int percentBasisPoints) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientboundQiEnhanceSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(XiuxianCultivationMod.MODID, "qi_enhance_sync"));

    public static final StreamCodec<ByteBuf, ClientboundQiEnhanceSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, ClientboundQiEnhanceSyncPayload::active,
                    ByteBufCodecs.VAR_INT, ClientboundQiEnhanceSyncPayload::percentBasisPoints,
                    ClientboundQiEnhanceSyncPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
