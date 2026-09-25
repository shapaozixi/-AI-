package cn.autoforged.xiuxian_cultivation_mod_1789994530.entity;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** 实体类型注册。 */
public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, XiuxianCultivationMod.MODID);

    /** [武器强化] 剑气弹射物。 */
    public static final DeferredHolder<EntityType<?>, EntityType<SwordQiProjectile>> SWORD_QI =
            ENTITIES.register("sword_qi", () -> EntityType.Builder
                    // 尺寸略大于视觉弧面，保证命中判定覆盖得住
                    .<SwordQiProjectile>of(SwordQiProjectile::new, MobCategory.MISC)
                    .sized(0.9F, 0.9F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("sword_qi"));

    private ModEntities() {
    }
}
