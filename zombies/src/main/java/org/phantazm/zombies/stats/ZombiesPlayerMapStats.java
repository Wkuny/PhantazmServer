package org.phantazm.zombies.stats;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface ZombiesPlayerMapStats {

    @NotNull UUID getPlayerUUID();

    @NotNull Key getMapKey();

    int getGamesPlayed();

    void setGamesPlayed(int gamesPlayed);

    int getWins();

    void setWins(int wins);

    int getRoundsSurvived();

    void setRoundsSurvived(int roundsSurvived);

    int getKills();

    void setKills(int kills);

    int getKnocks();

    void setKnocks(int knocks);

    int getDeaths();

    void setDeaths(int deaths);

    int getRevives();

    void setRevives(int revives);

    int getRegularShots();

    void setRegularShots(int regularShots);

    int getHeadshots();

    void setHeadshots(int headshots);

}
