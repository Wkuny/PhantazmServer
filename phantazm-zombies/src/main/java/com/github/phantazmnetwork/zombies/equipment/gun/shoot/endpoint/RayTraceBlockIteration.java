package com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint;

import com.github.phantazmnetwork.api.RayUtils;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

public class RayTraceBlockIteration implements BlockIteration {

    public record Data() implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.block_iteration.ray_trace");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) {
                return new Data();
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) {
                return new LinkedConfigNode(0);
            }
        };
    }

    private final Data data;

    public RayTraceBlockIteration(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @SuppressWarnings("UnstableApiUsage")
    public @NotNull Optional<Vec> findEnd(@NotNull Instance instance, @NotNull Entity shooter, @NotNull Pos start,
                                          @NotNull Iterator<Point> it) {
        Point blockLocation = null;
        Block block = null;

        while (it.hasNext()) {
            blockLocation = it.next();
            block = instance.getBlock(blockLocation);

            Shape shape = block.registry().collisionShape();
            if (!shape.relativeEnd().isZero()) {
                Optional<Vec> intersection = RayUtils.getIntersectionPosition(shape, blockLocation, start);

                if (intersection.isPresent()) {
                    return intersection;
                }
            }
        }

        if (block != null) {
            return RayUtils.rayTrace(new BoundingBox(1D, 1D, 1D),
                    blockLocation.add(0.5D, 0D, 0.5D), start);
        }

        return Optional.empty();
    }

}
