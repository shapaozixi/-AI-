package cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 服务端 -> 客户端：告知「当前锁定的目标实体 ID」。
 *
 * <p>为什么需要它：视角跟随如果由服务端每 tick 硬发
 * {@code ClientboundPlayerLookAtPacket} 强制转向，会与玩家自己的鼠标输入
 * 每 tick 打架，表现为「视线剧烈抖动」。
 *
 * <p>所以改为：服务端只告诉客户端「锁了谁」，客户端每 tick 自己做平滑插值转向。
 * 锁定/解锁各发一次即可，不需要每 tick 重复发送。
 *
 * @param targetEntityId 目标实体 ID；{@code -1} 表示解除锁定
 */
public record ClientboundLockTargetPayload(int targetEntityId) implements CustomPacketPayload {

    /** 解除锁定的哨兵值。 */
    public static final int NO_TARGET = -1;

    public static final CustomPacketPayload.Type<ClientboundLockTargetPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(XiuxianCultivationMod.MODID, "lock_target"));

    public static final StreamCodec<ByteBuf, ClientboundLockTargetPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ClientboundLockTargetPayload::targetEntityId,
                    ClientboundLockTargetPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
