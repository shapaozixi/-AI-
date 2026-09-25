package cn.autoforged.xiuxian_cultivation_mod_1789994530.attachment;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, XiuxianCultivationMod.MODID);

    public static final Supplier<AttachmentType<CultivationData>> CULTIVATION =
            ATTACHMENTS.register("cultivation", () -> AttachmentType
                    .serializable(CultivationData::new)
                    .build());

    /** 生物的真气数据（仅拥有真气的敌对/中立生物才会挂载），同步给客户端用于显示真气条。 */
    public static final Supplier<AttachmentType<MobQiData>> MOB_QI =
            ATTACHMENTS.register("mob_qi", () -> AttachmentType
                    .serializable(MobQiData::new)
                    .sync(MobQiData.STREAM_CODEC)
                    .build());

    private ModAttachments() {
    }
}
