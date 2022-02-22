package com.github.phantazmnetwork.api.instance;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import org.jetbrains.annotations.NotNull;
/**
 * Loads usable {@link Instance}s.
 */
public interface InstanceLoader {

    /**
     * Loads an {@link Instance}.
     * @param instanceManager The {@link InstanceManager} to register the {@link Instance} with
     * @param subPaths Paths used to identify the {@link Instance}
     * @return A new {@link Instance}
     */
    @NotNull Instance loadInstance(@NotNull InstanceManager instanceManager, @NotNull String... subPaths);

}
