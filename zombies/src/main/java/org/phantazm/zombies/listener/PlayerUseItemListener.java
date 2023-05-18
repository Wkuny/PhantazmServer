package org.phantazm.zombies.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerUseItemListener extends ZombiesPlayerEventListener<PlayerUseItemEvent> {

    private final PlayerRightClickListener rightClickListener;

    public PlayerUseItemListener(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers,
            @NotNull PlayerRightClickListener rightClickListener) {
        super(instance, zombiesPlayers);
        this.rightClickListener = Objects.requireNonNull(rightClickListener, "rightClickListener");
    }

    @Override
    public void accept(@NotNull ZombiesPlayer zombiesPlayer, @NotNull PlayerUseItemEvent event) {
        if (event.getHand() == Player.Hand.MAIN) {
            rightClickListener.onRightClick(zombiesPlayer, event.getPlayer().getHeldSlot());
        }
    }
}
