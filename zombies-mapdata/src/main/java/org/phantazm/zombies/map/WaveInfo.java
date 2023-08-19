package org.phantazm.zombies.map;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.mapper.annotation.Default;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Defines a wave.
 */
public record WaveInfo(long delayTicks, @NotNull List<SpawnInfo> spawns, @NotNull ConfigList spawnActions) {
    /**
     * Creates a new instance of this record.
     *
     * @param delayTicks the number of ticks until this wave triggers, measured from the start of the round (if this is
     *                   the first wave), or from the end of the previous wave (if this is not the first wave).
     * @param spawns     the mobs to spawn on this wave
     */
    public WaveInfo {
        Objects.requireNonNull(spawns);
    }

    @Default("spawnActions")
    public static @NotNull ConfigElement defaultSpawnActions() {
        return ConfigList.of();
    }
}
