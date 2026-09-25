package cn.autoforged.xiuxian_cultivation_mod_1789994530.attachment;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * 生物的真气数据。只有被随机赋予真气的敌对/中立生物才会挂载此附件。
 * 客户端显示真气数值条需要同步，故同时提供 {@link #STREAM_CODEC}。
 */
public class MobQiData implements INBTSerializable<CompoundTag> {
    /** 该生物是否拥有真气 */
    public boolean hasQi = false;
    /** 当前真气值 */
    public double qi = 0.0D;
    /** 是否已经完成过一次进化（每个生物只能进化一次） */
    public boolean evolved = false;
    /** 真气强化是否开启（敌对生物默认开启） */
    public boolean enhanceActive = true;

    /** 附件同步到客户端的编解码器（4 个字段，不超过 composite 上限 6）。 */
    public static final StreamCodec<ByteBuf, MobQiData> STREAM_CODEC =
            StreamCodec.<ByteBuf, MobQiData, Boolean, Double, Boolean, Boolean>composite(
                    ByteBufCodecs.BOOL, data -> data.hasQi,
                    ByteBufCodecs.DOUBLE, data -> data.qi,
                    ByteBufCodecs.BOOL, data -> data.evolved,
                    ByteBufCodecs.BOOL, data -> data.enhanceActive,
                    (hasQi, qi, evolved, enhanceActive) -> {
                        MobQiData data = new MobQiData();
                        data.hasQi = hasQi;
                        data.qi = qi;
                        data.evolved = evolved;
                        data.enhanceActive = enhanceActive;
                        return data;
                    });

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("HasQi", this.hasQi);
        tag.putDouble("Qi", this.qi);
        tag.putBoolean("Evolved", this.evolved);
        tag.putBoolean("EnhanceActive", this.enhanceActive);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.hasQi = tag.getBoolean("HasQi");
        this.qi = tag.getDouble("Qi");
        this.evolved = tag.getBoolean("Evolved");
        this.enhanceActive = !tag.contains("EnhanceActive") || tag.getBoolean("EnhanceActive");
    }
}
