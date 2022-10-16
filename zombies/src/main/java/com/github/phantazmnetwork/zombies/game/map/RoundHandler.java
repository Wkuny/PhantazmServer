package com.github.phantazmnetwork.zombies.game.map;

import net.minestom.server.Tickable;

public interface RoundHandler extends Tickable {
    int roundCount();

    int currentRoundIndex();

    void setCurrentRound(int roundIndex);

    Round currentRound();

    boolean hasEnded();
}
