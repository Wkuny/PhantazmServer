package org.phantazm.mob2.validator;

import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.ChildPath;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;

import java.util.ArrayList;
import java.util.List;

public class AndValidator implements ValidatorComponent {
    private final List<ValidatorComponent> validators;

    @FactoryMethod
    public AndValidator(@NotNull @Child("validators") List<ValidatorComponent> validators) {
        this.validators = validators;
    }

    @DataObject
    public record Data(@NotNull @ChildPath("validators") List<String> validators) {
    }

    @Override
    public @NotNull Validator apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        List<Validator> validators = new ArrayList<>(this.validators.size());
        for (ValidatorComponent component : this.validators) {
            validators.add(component.apply(mob, injectionStore));
        }

        return new Internal(validators);
    }

    private record Internal(List<Validator> validators) implements Validator {
        @Override
        public boolean valid(@NotNull Entity entity) {
            for (Validator validator : validators) {
                if (!validator.valid(entity)) {
                    return false;
                }
            }

            return true;
        }
    }
}
