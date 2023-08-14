package org.phantazm.mob2.selector;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Keys;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;

public class SelfTargetSelector implements SelectorComponent {
    @Override
    public @NotNull Selector apply(@NotNull InjectionStore injectionStore) {
        return new SelfSelector(injectionStore.get(Keys.MOB_KEY));
    }

    private record SelfSelector(Mob self) implements Selector {
        @Override
        public @NotNull Target select() {
            return Target.entities(self);
        }
    }
}
