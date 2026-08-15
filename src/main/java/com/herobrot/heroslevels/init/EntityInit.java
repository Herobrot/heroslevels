package com.herobrot.heroslevels.init;

import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.entity.LevelExperienceOrbEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, HerosLevels.MOD_ID);

    public static final Supplier<EntityType<LevelExperienceOrbEntity>> LEVEL_EXPERIENCE_ORB = ENTITIES.register("level_experience_orb",
            () -> EntityType.Builder.<LevelExperienceOrbEntity>of(LevelExperienceOrbEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(6)
                    .updateInterval(20)
                    .build("level_experience_orb"));
}