package com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class AroundEndFinder implements DirectionalEntityFinder {

    public record Data(double range) implements Keyed {

        public static final Key SERIAL_KEY
                = Key.key(Namespaces.PHANTAZM, "gun.entity_finder.directional.around_end");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                return new Data(element.getNumberOrThrow("range").doubleValue());
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) {
                ConfigNode node = new LinkedConfigNode(1);
                node.putNumber("range", data.range());

                return node;
            }
        };
    }

    private final Data data;

    public AroundEndFinder(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull Collection<Entity> findEntities(@NotNull Instance instance, @NotNull Pos start,
                                                    @NotNull Point end) {
        return instance.getNearbyEntities(end, data.range());
    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }
}
