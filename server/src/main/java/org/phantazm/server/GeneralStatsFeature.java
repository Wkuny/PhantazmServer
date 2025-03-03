package org.phantazm.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerSpawnEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.stats.general.GeneralDatabase;
import org.phantazm.stats.general.SQLGeneralDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.time.ZonedDateTime;
import java.util.concurrent.Executor;

public final class GeneralStatsFeature {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralStatsFeature.class);
    private static GeneralDatabase generalDatabase;

    private GeneralStatsFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull Executor executor, @NotNull DataSource dataSource) {
        generalDatabase = new SQLGeneralDatabase(executor, dataSource);

        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, GeneralStatsFeature::onPlayerSpawn);
    }

    private static void onPlayerSpawn(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) {
            return;
        }

        generalDatabase.handleJoin(event.getPlayer().getUuid(), ZonedDateTime.now())
            .whenComplete((ignored, throwable) -> {
                if (throwable != null) {
                    LOGGER.warn("Failed to update join times for {}", event.getPlayer().getUuid(), throwable);
                }
            });
    }

}
