package com.github.phantazmnetwork.zombies.game.map.shop.predicate.logic;

import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.PredicateBase;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.ShopPredicate;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.map.shop.predicate.not")
public class NotPredicate extends PredicateBase<NotPredicate.Data> {
    @ProcessorMethod
    public static ConfigProcessor<NotPredicate.Data> processor() {
        return new ConfigProcessor<>() {
            @Override
            public Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                String predicate = element.getStringOrThrow("predicate");
                return new Data(predicate);
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data data) {
                return ConfigNode.of("predicate", data.predicate);
            }
        };
    }

    private final ShopPredicate predicate;

    @FactoryMethod
    public NotPredicate(@NotNull Data data, @DataName("predicate") ShopPredicate predicate) {
        super(data);
        this.predicate = Objects.requireNonNull(predicate, "predicate");
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        return !predicate.canInteract(interaction);
    }

    @DataObject
    public record Data(@NotNull @DataPath("predicate") String predicate) {
    }
}
