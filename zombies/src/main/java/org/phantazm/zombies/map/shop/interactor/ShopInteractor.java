package org.phantazm.zombies.map.shop.interactor;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

public interface ShopInteractor extends Tickable {
    boolean handleInteraction(@NotNull PlayerInteraction interaction);

    @Override
    default void tick(long time) {

    }

    default void initialize(@NotNull Shop shop) {
    }

    static boolean handle(@NotNull Iterable<? extends ShopInteractor> interactors,
            @NotNull PlayerInteraction interaction) {
        boolean res = true;
        for (ShopInteractor interactor : interactors) {
            res &= interactor.handleInteraction(interaction);
        }

        return res;
    }

    static void tick(@NotNull Iterable<? extends ShopInteractor> interactors, long time) {
        for (ShopInteractor interactor : interactors) {
            interactor.tick(time);
        }
    }

    static void initialize(@NotNull Iterable<? extends ShopInteractor> interactors, @NotNull Shop shop) {
        for (ShopInteractor interactor : interactors) {
            interactor.initialize(shop);
        }
    }
}
