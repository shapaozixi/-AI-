package cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 服务端 -> 客户端：同步修炼数据用于 HUD 显示。maxQi = 属性计算 + bonusMaxQi。 */
public record ClientboundCultivationSyncPayload(
        int mortalTier,
        int mortalExp,
        int bodyTier,
        int bodyExp,
        int qi,
        boolean gatePassed,
        int bonusMaxQi,
        int learnedSkills,
        int skillSlot0,
        int skillSlot1,
        int skillSlot2) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientboundCultivationSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(XiuxianCultivationMod.MODID, "cultivation_sync"));

    // StreamCodec.composite 最多 6 个分量，故此处手工编解码 11 个字段。
    public static final StreamCodec<ByteBuf, ClientboundCultivationSyncPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        ByteBufCodecs.VAR_INT.encode(buf, payload.mortalTier());
                        ByteBufCodecs.VAR_INT.encode(buf, payload.mortalExp());
                        ByteBufCodecs.VAR_INT.encode(buf, payload.bodyTier());
                        ByteBufCodecs.VAR_INT.encode(buf, payload.bodyExp());
                        ByteBufCodecs.VAR_INT.encode(buf, payload.qi());
                        ByteBufCodecs.BOOL.encode(buf, payload.gatePassed());
                        ByteBufCodecs.VAR_INT.encode(buf, payload.bonusMaxQi());
                        ByteBufCodecs.VAR_INT.encode(buf, payload.learnedSkills());
                        ByteBufCodecs.VAR_INT.encode(buf, payload.skillSlot0());
                        ByteBufCodecs.VAR_INT.encode(buf, payload.skillSlot1());
                        ByteBufCodecs.VAR_INT.encode(buf, payload.skillSlot2());
                    },
                    buf -> new ClientboundCultivationSyncPayload(
                            ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.BOOL.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf)));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
