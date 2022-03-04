package com.github.phantazmnetwork.neuron.engine;

import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.operation.PathResult;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;

public interface Pathfinder {
    @NotNull Future<PathResult> pathfind(@NotNull Agent agent, int startX, int startY, int startZ, int endX, int endY,
                                         int endZ);
}
