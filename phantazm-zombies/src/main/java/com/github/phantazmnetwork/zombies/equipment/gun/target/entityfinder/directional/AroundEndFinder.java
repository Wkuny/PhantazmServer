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
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Finds entities around the end of a shot.
 */
public class AroundEndFinder implements DirectionalEntityFinder {

    /**
     * Data for an {@link AroundEndFinder}.
     * @param range The range to search for entities
     */
    public record Data(double range) implements Keyed {

        /**
         * The serial {@link Key} for this {@link Data}.
         */
        public static final Key SERIAL_KEY
                = Key.key(Namespaces.PHANTAZM, "gun.entity_finder.directional.around_end");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                double range = element.getNumberOrThrow("range").doubleValue();
                if (range < 0) {
                    throw new ConfigProcessException("range must be greater than or equal to 0");
                }

                return new Data(range);
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

    /**
     * Creates a new {@link AroundEndFinder}.
     * @param data The {@link Data} for the {@link AroundEndFinder}
     */
    public AroundEndFinder(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull Collection<LivingEntity> findEntities(@NotNull Instance instance, @NotNull Pos start,
                                                          @NotNull Point end) {
        Collection<Entity> entities = instance.getNearbyEntities(end, data.range());
        Collection<LivingEntity> livingEntities = new ArrayList<>(entities.size());
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity) {
                livingEntities.add(livingEntity);
            }
        }

        return livingEntities;
    }

}
