package com.herobrot.heroslevels.criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SkillCriterion extends SimpleCriterionTrigger<SkillCriterion.Conditions> {

    @Override
    public @NotNull Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player, String skillName, int skillLevel) {
        this.trigger(player, conditions -> conditions.matches(skillName, skillLevel));
    }

    public record Conditions(Optional<ContextAwarePredicate> player, String skillName, int skillLevel) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                        Codec.STRING.fieldOf("skill_name").forGetter(Conditions::skillName),
                        Codec.INT.fieldOf("skill_level").forGetter(Conditions::skillLevel)
                )
                .apply(instance, Conditions::new));

        public boolean matches(String skillName, int skillLevel) {
            if (!skillName.equals(this.skillName)) {
                return false;
            }
            return skillLevel == this.skillLevel;
        }
    }
}