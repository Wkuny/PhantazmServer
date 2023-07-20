package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.target.TargetSelector;

import java.util.Objects;

@Model("mob.skill.push")
@Cache(false)
public class PushEntitySkill implements Skill {
    private final Data data;
    private final TargetSelector<Object> selector;

    @FactoryMethod
    public PushEntitySkill(@NotNull Data data, @NotNull @Child("selector") TargetSelector<Object> selector) {
        this.data = Objects.requireNonNull(data, "data");
        this.selector = Objects.requireNonNull(selector, "selector");
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        selector.selectTarget(self).ifPresent(livingEntity -> {
            if (livingEntity instanceof Iterable<?> iterable) {
                Iterable<LivingEntity> entityIterable = (Iterable<LivingEntity>)iterable;
                for (LivingEntity entity : entityIterable) {
                    setVelocity(entity, self.entity());
                }
            }
            else if (livingEntity instanceof LivingEntity living) {
                setVelocity(living, self.entity());
            }
        });
    }

    private void setVelocity(Entity target, Entity self) {
        Vec diff = target.getPosition().sub(self.getPosition()).direction();
        target.setVelocity(target.getVelocity().add(diff.mul(data.power)));
    }

    @DataObject
    public record Data(@NotNull @ChildPath("selector") String selector, double power) {
    }
}
