package org.phantazm.zombies.powerup2.action;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.coin.TransactionModifierSource;
import org.phantazm.zombies.powerup2.predicate.DeactivationPredicate;
import org.phantazm.zombies.powerup2.predicate.DeactivationPredicateComponent;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.Objects;
import java.util.function.Supplier;

@Model("zombies.powerup.action.map_transaction_modifier.add")
@Cache(false)
public class MapTransactionModifierAddAction implements PowerupActionComponent {
    private final Data data;
    private final DeactivationPredicateComponent deactivationPredicate;

    @FactoryMethod
    public MapTransactionModifierAddAction(@NotNull Data data,
            @NotNull @Child("deactivation_predicate") DeactivationPredicateComponent deactivationPredicate) {
        this.data = data;
        this.deactivationPredicate = deactivationPredicate;
    }

    @Override
    public @NotNull PowerupAction apply(@NotNull ZombiesScene scene) {
        return new Action(data, deactivationPredicate.apply(scene),
                scene.getMap().mapObjects().module().modifierSource());
    }

    @DataObject
    public record Data(@NotNull @ChildPath("deactivation_predicate") String deactivationPredicate,
                       @NotNull Key modifierGroup,
                       @NotNull Component displayName,
                       int amount,
                       int priority) {
    }

    private static class Action extends MapTransactionAction {
        private final Data data;

        public Action(Data data, DeactivationPredicate deactivationPredicate,
                TransactionModifierSource transactionModifierSource) {
            super(deactivationPredicate, transactionModifierSource, data.modifierGroup);
            this.data = data;
        }

        @Override
        protected @NotNull Transaction.Modifier getModifier() {
            return new Transaction.Modifier() {
                @Override
                public @NotNull Component getDisplayName() {
                    return data.displayName;
                }

                @Override
                public int modify(int coins) {
                    return data.amount + coins;
                }

                @Override
                public int getPriority() {
                    return data.priority;
                }
            };
        }
    }
}
