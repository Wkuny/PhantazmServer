package org.phantazm.stats.zombies;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jooq.impl.SQLDataType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

public class JooqZombiesSQLFetcher implements ZombiesSQLFetcher {

    @Override
    public void synchronizeZombiesPlayerMapStats(@NotNull Connection connection,
            @NotNull ZombiesPlayerMapStats mapStats) {
        using(connection).insertInto(table("zombies_player_map_stats"), field("player_uuid"), field("map_key"),
                        field("games_played"), field("wins"), field("best_time"), field("rounds_survived"), field("kills"),
                        field("knocks"), field("deaths"), field("revives"), field("regular_shots"), field("headshots"))
                .values(mapStats.getPlayerUUID().toString(), mapStats.getMapKey().asString(), mapStats.getGamesPlayed(),
                        mapStats.getWins(), mapStats.getBestTime().orElse(null), mapStats.getRoundsSurvived(),
                        mapStats.getKills(), mapStats.getKnocks(), mapStats.getDeaths(), mapStats.getRevives(),
                        mapStats.getRegularShots(), mapStats.getHeadshots()).onDuplicateKeyUpdate()
                .set(field("games_played"), field("games_played", SQLDataType.INTEGER).plus(mapStats.getGamesPlayed()))
                .set(field("wins"), field("wins", SQLDataType.INTEGER).plus(mapStats.getGamesPlayed()))
                .set(field("best_time"), mapStats.getBestTime().isPresent()
                                         ? least(field("best_time"), mapStats.getBestTime().get())
                                         : field("best_time", SQLDataType.BIGINT)).set(field("rounds_survived"),
                        field("rounds_survived", SQLDataType.INTEGER).plus(mapStats.getRoundsSurvived()))
                .set(field("kills"), field("kills", SQLDataType.INTEGER).plus(mapStats.getRoundsSurvived()))
                .set(field("knocks"), field("knocks", SQLDataType.INTEGER).plus(mapStats.getKnocks()))
                .set(field("deaths"), field("deaths", SQLDataType.INTEGER).plus(mapStats.getDeaths()))
                .set(field("revives"), field("revives", SQLDataType.INTEGER).plus(mapStats.getRevives()))
                .set(field("regular_shots"),
                        field("regular_shots", SQLDataType.INTEGER).plus(mapStats.getRegularShots()))
                .set(field("headshots"), field("headshots", SQLDataType.INTEGER).plus(mapStats.getHeadshots()))
                .execute();
    }

    @Override
    public @NotNull List<BestTime> getBestTimes(@NotNull Connection connection, @NotNull Key mapKey)
            throws SQLException {
        List<BestTime> bestTimes = new ArrayList<>();
        try (ResultSet resultSet = using(connection).select(field("player_uuid"), field("best_time"))
                .from(table("zombies_player_map_stats")).where(field("map_key").eq(mapKey.asString()))
                .and(field("best_time").isNotNull()).orderBy(field("best_time")).fetchResultSet()) {
            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("player_uuid"));
                long bestTime = resultSet.getLong("best_time");
                bestTimes.add(new BestTime(uuid, bestTime));
            }
        }

        return bestTimes;
    }
}
