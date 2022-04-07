package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.vector.Vec3F;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an object that has collision. Each Solid is a single AABB (axis-aligned bounding box) specified by two
 * points (the "minimum" point and "maximum" point, with the region between encompassing the bounds). Platform-specific
 * bindings must generally create their own implementation of this interface.
 */
public interface Solid {
    /**
     * Retrieves the minimum floating-point vector for this solid. Each component must range from [0.0, 1.0] and be
     * smaller than or equal to each equivalent component in the vector returned by {@code getMax()}.
     * @return the minimum vector
     */
    @NotNull Vec3F getMin();

    /**
     * Retrieves the maximum floating-point vector for this solid. Each component must range from [0.0, 1.0] and be
     * greater than or equal to each equivalent component in the vector returned by {@code getMin()}.
     * @return the minimum vector
     */
    @NotNull Vec3F getMax();

    @NotNull Iterable<Solid> getComponents();

    boolean overlaps(double x, double y, double z, double width, double height, double depth);
}
