package cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端 -> 服务端：左键「空挥」（没有命中任何实体）时发送，请求释放剑气。
 * 命中实体的情况由服务端 AttackEntityEvent 处理，两条路径不会重复触发。
 *
 * <p><b>为什么要带上朝向</b>：客户端站着不动只转动视角时，位置/朝向同步包并不是每 tick
 * 都发给服务端，服务端读到的 {@code getLookAngle()} 可能已经过期，结果剑气方向看起来
 * 是「随机」的。这里把发射瞬间客户端真实的 yaw/pitch 一并送过去，服务端直接采用。
 *
 * @param yaw   发射瞬间玩家的水平朝向（度）
 * @param pitch 发射瞬间玩家的俯仰角（度）
 */
public record ServerboundSwordQiPayload(float yaw, float pitch) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerboundSwordQiPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(XiuxianCultivationMod.MODID, "sword_qi_swing"));

    public static final StreamCodec<ByteBuf, ServerboundSwordQiPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT, ServerboundSwordQiPayload::yaw,
                    ByteBufCodecs.FLOAT, ServerboundSwordQiPayload::pitch,
                    ServerboundSwordQiPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
