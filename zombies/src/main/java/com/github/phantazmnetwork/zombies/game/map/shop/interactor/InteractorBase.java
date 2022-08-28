package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class InteractorBase<TData> implements ShopInteractor {
    protected final TData data;

    public InteractorBase(@NotNull TData data) {
        this.data = Objects.requireNonNull(data, "data");
    }
}
