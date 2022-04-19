package com.github.phantazmnetwork.neuron.bindings.minestom.chunk;

import com.github.phantazmnetwork.commons.HashStrategies;
import com.github.phantazmnetwork.commons.minestom.vector.VecUtils;
import com.github.phantazmnetwork.commons.vector.Vec3F;
import com.github.phantazmnetwork.neuron.bindings.minestom.solid.SolidProvider;
import com.github.phantazmnetwork.neuron.world.Solid;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A custom extension of {@link DynamicChunk} that enables fast, asynchronous access to {@link Solid} objects
 * representing collision bounds.
 */
@SuppressWarnings("UnstableApiUsage")
public class NeuralChunk extends DynamicChunk {
    private static final Map<Shape, Solid[]> SPLIT_MAP = new Object2ObjectOpenCustomHashMap<>(HashStrategies
            .identity());

    private final IntSet tallSolids;

    public NeuralChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ);
        this.tallSolids = new IntOpenHashSet(8);
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        super.setBlock(x, y, z, block);

        Shape shape = block.registry().collisionShape();
        int index = ChunkUtils.getBlockIndex(x, y, z);
        if(shape.relativeEnd().y() > 1) {
            tallSolids.add(index);
        }
        else {
            tallSolids.remove(index);
        }
    }

    public @Nullable Solid getSolid(int x, int y, int z) {
        int index = ChunkUtils.getBlockIndex(x, y, z);
        if(tallSolids.contains(index)) {
            return getSplitFor(getBlock(x, y, z))[0];
        }

        int belowIndex = ChunkUtils.getBlockIndex(x, y - 1, z);
        if(tallSolids.contains(belowIndex)) {
            return getSplitFor(getBlock(x, y - 1, z))[1];
        }

        Block block = getBlock(x, y, z);
        if(block.isSolid()) {
            return SolidProvider.fromShape(block.registry().collisionShape());
        }

        return null;
    }

    private Solid[] getSplitFor(Block block) {
        Shape tallShape = block.registry().collisionShape();
        return SPLIT_MAP.computeIfAbsent(tallShape, key -> {
            Point start = key.relativeStart();
            Point end = key.relativeEnd();

            return new Solid[] {
                    SolidProvider.fromPoints(VecUtils.toFloat(start), Vec3F.ofDouble(start.x(), 1, start.z())),
                    SolidProvider.fromPoints(Vec3F.ofDouble(start.x(), 0, start.z()), Vec3F.ofDouble(start.x(),
                            end.y() - 1, start.z()))
            };
        });
    }
}
