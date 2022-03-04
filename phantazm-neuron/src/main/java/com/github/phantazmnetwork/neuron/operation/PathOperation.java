package com.github.phantazmnetwork.neuron.operation;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a potentially ongoing pathfinding operation.
 */
public interface PathOperation {
    enum State {
        IN_PROGRESS,
        SUCCEEDED,
        FAILED
    }

    void step();

    @NotNull State getState();

    @NotNull PathContext getContext();

    @NotNull PathResult getResult();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    default boolean isComplete() {
        return getState() != State.IN_PROGRESS;
    }

    default @NotNull PathResult runToCompletion() {
        while(!isComplete()) {
            step();
        }

        return getResult();
    }
}
