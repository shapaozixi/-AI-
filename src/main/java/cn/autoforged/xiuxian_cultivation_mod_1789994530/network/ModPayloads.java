package cn.autoforged.xiuxian_cultivation_mod_1789994530.network;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.client.ClientCultivationData;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.cultivation.CultivationHelper;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload.ClientboundCultivationSyncPayload;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload.ClientboundQiEnhanceSyncPayload;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload.ServerboundBreakthroughPayload;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload.ServerboundDashPayload;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload.ServerboundLockPayload;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload.ServerboundSkillAssignPayload;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload.ServerboundSkillUsePayload;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload.ServerboundSwordQiPayload;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload.ServerboundToggleQiEnhancePayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = XiuxianCultivationMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModPayloads {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                ClientboundCultivationSyncPayload.TYPE,
                ClientboundCultivationSyncPayload.STREAM_CODEC,
                ModPayloads::handleSync);
        registrar.playToClient(
                ClientboundQiEnhanceSyncPayload.TYPE,
                ClientboundQiEnhanceSyncPayload.STREAM_CODEC,
                ModPayloads::handleEnhanceSync);
        registrar.playToServer(
                ServerboundToggleQiEnhancePayload.TYPE,
                ServerboundToggleQiEnhancePayload.STREAM_CODEC,
                ModPayloads::handleToggleEnhance);
        registrar.playToServer(
                ServerboundBreakthroughPayload.TYPE,
                ServerboundBreakthroughPayload.STREAM_CODEC,
                ModPayloads::handleBreakthrough);
        registrar.playToServer(
                ServerboundDashPayload.TYPE,
                ServerboundDashPayload.STREAM_CODEC,
                ModPayloads::handleDash);
        registrar.playToServer(
                ServerboundSkillUsePayload.TYPE,
                ServerboundSkillUsePayload.STREAM_CODEC,
                ModPayloads::handleSkillUse);
        registrar.playToServer(
                ServerboundSkillAssignPayload.TYPE,
                ServerboundSkillAssignPayload.STREAM_CODEC,
                ModPayloads::handleSkillAssign);
        registrar.playToServer(
                ServerboundLockPayload.TYPE,
                ServerboundLockPayload.STREAM_CODEC,
                ModPayloads::handleLock);
        registrar.playToServer(
                ServerboundSwordQiPayload.TYPE,
                ServerboundSwordQiPayload.STREAM_CODEC,
                ModPayloads::handleSwordQiSwing);
    }

    private static void handleSync(ClientboundCultivationSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientCultivationData.update(
                payload.mortalTier(),
                payload.mortalExp(),
                payload.bodyTier(),
                payload.bodyExp(),
                payload.qi(),
                payload.gatePassed(),
                payload.bonusMaxQi(),
                payload.learnedSkills(),
                payload.skillSlot0(),
                payload.skillSlot1(),
                payload.skillSlot2()));
    }

    private static void handleEnhanceSync(ClientboundQiEnhanceSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientCultivationData.updateEnhance(
                payload.active(),
                payload.percentBasisPoints()));
    }

    private static void handleToggleEnhance(ServerboundToggleQiEnhancePayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer serverPlayer) {
            if (payload.adjust()) {
                CultivationHelper.cycleEnhancePercent(serverPlayer);
            } else {
                CultivationHelper.toggleAttackEnhance(serverPlayer);
            }
        }
    }

    private static void handleBreakthrough(ServerboundBreakthroughPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer serverPlayer) {
            CultivationHelper.onBreakthroughKey(serverPlayer);
        }
    }

    private static void handleDash(ServerboundDashPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer serverPlayer) {
            CultivationHelper.performDash(serverPlayer);
        }
    }

    private static void handleSkillUse(ServerboundSkillUsePayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer serverPlayer) {
            CultivationHelper.useSkill(serverPlayer, payload.slot());
        }
    }

    private static void handleSkillAssign(ServerboundSkillAssignPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer serverPlayer) {
            CultivationHelper.assignSkill(serverPlayer, payload.slot(), payload.skillId());
        }
    }

    private static void handleLock(ServerboundLockPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer serverPlayer) {
            CultivationHelper.tryLockTarget(serverPlayer);
        }
    }

    /** 左键空挥 → 释放剑气（与命中实体时走的是同一个技能入口）。 */
    private static void handleSwordQiSwing(ServerboundSwordQiPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer serverPlayer) {
            CultivationHelper.fireSwordQi(serverPlayer);
        }
    }
}
