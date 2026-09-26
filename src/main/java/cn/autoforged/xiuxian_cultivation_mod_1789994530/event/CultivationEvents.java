package cn.autoforged.xiuxian_cultivation_mod_1789994530.event;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.attachment.CultivationData;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.attachment.ModAttachments;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.cultivation.CultivationHelper;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.cultivation.MobQiHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = XiuxianCultivationMod.MODID)
public class CultivationEvents {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && !serverPlayer.level().isClientSide()) {
            CultivationHelper.tick(serverPlayer);
        }
    }

    /** 挖掘经验：每点硬度 1 点经验。 */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide()) {
            return;
        }
        BlockState state = event.getState();
        float hardness = state.getDestroySpeed(player.level(), event.getPos());
        if (hardness <= 0.0F) {
            return;
        }
        CultivationHelper.addCultivationExp(player, Math.max(1, (int) Math.floor(hardness)));
    }

    /** 击杀经验：目标最大生命/10 + 护甲/2；击杀拥有真气的敌对生物额外获得其真气上限 1/5 的真气上限。 */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide() || event.getEntity() instanceof Player) {
            return;
        }
        LivingEntity victim = event.getEntity();
        Entity killer = event.getSource().getEntity();
        if (killer instanceof ServerPlayer serverPlayer) {
            double health = victim.getMaxHealth();
            double armor = victim.getArmorValue();
            int exp = (int) Math.ceil(health / 10.0D + armor / 2.0D);
            CultivationHelper.addCultivationExp(serverPlayer, Math.max(exp, 1));
            if (victim.hasData(ModAttachments.MOB_QI.get())) {
                CultivationHelper.addBonusMaxQi(serverPlayer, MobQiHelper.getMaxQi(victim) * MobQiHelper.MOB_INITIAL_FRACTION);
            }
        }
    }

    /** 真气护体：玩家与拥有真气的生物都用当前真气抵挡护甲削减后的伤害（15 真气抵 1 点）。 */
    @SubscribeEvent
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        float damage = event.getNewDamage();
        if (damage <= 0.0F) {
            return;
        }
        if (entity instanceof Player player) {
            float blocked = CultivationHelper.absorbDamageWithQi(player, damage);
            if (blocked > 0.0F) {
                event.setNewDamage(Math.max(0.0F, damage - blocked));
                if (player instanceof ServerPlayer serverPlayer) {
                    CultivationHelper.sync(serverPlayer);
                }
            }
        } else {
            float blocked = MobQiHelper.absorbDamageWithQi(entity, damage);
            if (blocked > 0.0F) {
                event.setNewDamage(Math.max(0.0F, damage - blocked));
            }
        }
    }

    /** 真气强化：玩家开启后造成的伤害会消耗真气并追加伤害；敌对生物默认开启真气强化。 */
    @SubscribeEvent
    public static void onLivingDealDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (attacker == null || attacker == event.getEntity()) {
            return;
        }
        float damage = event.getNewDamage();
        if (attacker instanceof ServerPlayer serverPlayer) {
            CultivationHelper.QiEnhanceOutcome outcome = CultivationHelper.applyQiEnhancement(serverPlayer, damage);
            if (outcome.enhanced()) {
                event.setNewDamage(outcome.damage());
                // 消耗 10 点真气起才显示攻击特效，消耗越多越强
                if (outcome.consumedQi() >= CultivationHelper.QI_FX_MIN_CONSUME) {
                    CultivationHelper.spawnQiEnhanceFeedback(serverPlayer, event.getEntity(), outcome.consumedQi());
                }
                // [真气强化] 功法 buff：攻击按真气消耗量破坏地形（并额外放大破坏范围）
                if (CultivationHelper.isQiEnhanceSkillActive(serverPlayer)
                        && serverPlayer.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    CultivationHelper.breakTerrainAround(serverLevel, event.getEntity().position(),
                            outcome.consumedQi() * CultivationHelper.QI_ENHANCE_TERRAIN_MULTIPLIER, serverPlayer);
                }
                CultivationHelper.sync(serverPlayer);
            }
        } else if (attacker instanceof LivingEntity living) {
            float enhanced = MobQiHelper.applyEnhancement(living, damage);
            if (enhanced > damage) {
                event.setNewDamage(enhanced);
            }
        }
    }

    /** [武器强化] 功法：手持武器攻击时挥出剑气（远距离伤害 + 破坏地形 + 额外真气消耗）。 */
    @SubscribeEvent
    public static void onAttackEntity(net.neoforged.neoforge.event.entity.player.AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && !serverPlayer.level().isClientSide()) {
            CultivationHelper.fireSwordQi(serverPlayer);
        }
    }

    /** 敌对生物加入世界时按概率获得真气（存档加载的已有生物不重新赋予）。 */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || event.loadedFromDisk()) {
            return;
        }
        if (event.getEntity() instanceof LivingEntity living) {
            MobQiHelper.tryGiveQi(living);
        }
    }

    /** 敌对/中立生物真气回复，满真气后释放粒子特效进化。 */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        if (entity instanceof LivingEntity living
                && (entity instanceof Enemy || entity instanceof NeutralMob)) {
            MobQiHelper.tick(living);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            CultivationData data = CultivationHelper.get(serverPlayer);
            CultivationHelper.applyAttributeBonuses(serverPlayer, data);
            CultivationHelper.sync(serverPlayer);
            CultivationHelper.syncEnhance(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();
        CultivationData oldData = original.getData(ModAttachments.CULTIVATION.get());
        CultivationData newData = newPlayer.getData(ModAttachments.CULTIVATION.get());
        newData.copyFrom(oldData);
        if (event.isWasDeath()) {
            // 死亡后淬体试炼需要重来
            newData.gateActive = false;
            newData.gateTicks = 0;
            newData.qi = 0.0D;
            newData.lockedTarget = null;
            newData.lockedTargetDimension = "";
        }
        CultivationHelper.applyAttributeBonuses(newPlayer, newData);
    }
}
