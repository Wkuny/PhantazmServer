package org.phantazm.zombies.map.shop.predicate.logic;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.predicate.PredicateBase;
import org.phantazm.zombies.map.shop.predicate.ShopPredicate;

import java.util.List;

@Model("zombies.map.shop.predicate.xor")
public class XorPredicate extends PredicateBase<XorPredicate.Data> {
    private final List<ShopPredicate> predicates;

    @FactoryMethod
    public XorPredicate(@NotNull Data data, @Child("predicates") List<ShopPredicate> predicates) {
        super(data);
        this.predicates = List.copyOf(predicates);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        boolean result = false;
        for (ShopPredicate predicate : predicates) {
            result = result ^ predicate.canInteract(interaction);
        }

        return result;
    }

    @DataObject
    public record Data(@NotNull @ChildPath("predicates") List<String> paths) {
    }
}
