package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A {@link Predicate} operating on three integer values.
 */
@FunctionalInterface
public interface Vec3IPredicate extends Predicate<Vec3I> {
    /**
     * Tests this predicate with the provided 3D integer vector.
     * @param x the x-component
     * @param y the y-component
     * @param z the z-component
     * @return true if this test passes, false otherwise
     */
    boolean testVector(int x, int y, int z);

    @Override
    default boolean test(@NotNull Vec3I vec3I) {
        Objects.requireNonNull(vec3I, "vec3I");
        return testVector(vec3I.getX(), vec3I.getY(), vec3I.getZ());
    }
}
