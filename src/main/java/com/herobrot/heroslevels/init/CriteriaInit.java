package com.herobrot.heroslevels.init;

import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.criteria.LevelCriterion;
import com.herobrot.heroslevels.criteria.SkillCriterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CriteriaInit {

    public static final DeferredRegister<CriterionTrigger<?>> CRITERIA =
            DeferredRegister.create(Registries.TRIGGER_TYPE, HerosLevels.MOD_ID);

    public static final Supplier<LevelCriterion> LEVEL_UP = CRITERIA.register("level", LevelCriterion::new);
    public static final Supplier<SkillCriterion> SKILL_UP = CRITERIA.register("skill", SkillCriterion::new);
}