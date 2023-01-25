package org.phantazm.mob.goal;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.skill.Skill;
import org.phantazm.mob.target.LastHitSelector;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

import java.util.Collection;
import java.util.Objects;

@Model("mob.goal.melee_attack")
public class MeleeAttackGoal implements ProximaGoal {
    private final Data data;
    private final Collection<Skill> skills;
    private final LastHitSelector<LivingEntity> lastHitSelector;
    private final ProximaEntity entity;
    private long lastAttackTime = 0L;

    @FactoryMethod
    public MeleeAttackGoal(@NotNull Data data, @NotNull @Child("skills") Collection<Skill> skills,
            @NotNull @Child("last_hit_selector") LastHitSelector<LivingEntity> lastHitSelector,
            @NotNull ProximaEntity entity) {
        this.data = Objects.requireNonNull(data, "data");
        this.skills = Objects.requireNonNull(skills, "skills");
        this.lastHitSelector = Objects.requireNonNull(lastHitSelector, "lastHitSelector");
        this.entity = Objects.requireNonNull(entity, "entity");
    }

    @Override
    public boolean shouldStart() {
        if ((System.currentTimeMillis() - lastAttackTime) / MinecraftServer.TICK_MS >= data.cooldown()) {
            Entity target = entity.getTargetEntity();
            if (target == null) {
                return false;
            }

            return entity.getDistanceSquared(target) <= data.range * data.range;
        }

        return false;
    }

    @Override
    public void start() {
        Entity target = entity.getTargetEntity();
        if (target == null) {
            return;
        }

        entity.attack(target, true);
        if (target instanceof LivingEntity livingEntity) {
            lastHitSelector.setLastHit(livingEntity);

            for (Skill skill : skills) {
                skill.use();
            }
        }

        lastAttackTime = System.currentTimeMillis();
    }

    @Override
    public boolean shouldEnd() {
        return true;
    }

    @DataObject
    public record Data(long cooldown,
                       double range,
                       @NotNull @ChildPath("skills") Collection<String> skillPaths,
                       @NotNull @ChildPath("last_hit_selector") String lastHitSelectorPath) {

        public Data {
            Objects.requireNonNull(lastHitSelectorPath, "lastHitSelectorPath");
        }

    }
}
