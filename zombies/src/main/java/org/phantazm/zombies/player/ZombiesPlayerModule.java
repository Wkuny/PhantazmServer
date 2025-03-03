package org.phantazm.zombies.player;

import com.github.steanky.element.core.annotation.Depend;
import com.github.steanky.element.core.annotation.Memoize;
import com.github.steanky.element.core.dependency.DependencyModule;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.server.scoreboard.TabList;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.EquipmentCreator;
import org.phantazm.core.equipment.EquipmentHandler;
import org.phantazm.core.inventory.InventoryAccessRegistry;
import org.phantazm.core.player.PlayerView;
import org.phantazm.stats.zombies.ZombiesPlayerMapStats;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.TransactionModifierSource;
import org.phantazm.zombies.kill.PlayerKills;
import org.phantazm.zombies.leaderboard.BestTimeLeaderboard;
import org.phantazm.commons.flag.Flaggable;
import org.phantazm.zombies.player.action_bar.ZombiesPlayerActionBar;
import org.phantazm.zombies.player.state.PlayerStateKey;
import org.phantazm.zombies.player.state.PlayerStateSwitcher;
import org.phantazm.zombies.player.state.ZombiesPlayerState;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Depend
@Memoize
public class ZombiesPlayerModule implements DependencyModule {
    private final PlayerView playerView;
    private final ZombiesPlayerMeta meta;
    private final PlayerCoins coins;
    private final PlayerKills kills;
    private final EquipmentHandler equipmentHandler;
    private final EquipmentCreator equipmentCreator;
    private final ZombiesPlayerActionBar actionBar;
    private final InventoryAccessRegistry profileSwitcher;
    private final PlayerStateSwitcher stateSwitcher;
    private final Map<PlayerStateKey<?>, Function<?, ? extends ZombiesPlayerState>> stateFunctions;
    private final Sidebar sidebar;
    private final TabList tabList;
    private final TransactionModifierSource playerTransactionModifierSource;
    private final TransactionModifierSource compositeTransactionModifierSource;
    private final Flaggable flaggable;
    private final ZombiesPlayerMapStats stats;
    private final BestTimeLeaderboard leaderboard;

    public ZombiesPlayerModule(@NotNull PlayerView playerView, @NotNull ZombiesPlayerMeta meta,
        @NotNull PlayerCoins coins, @NotNull PlayerKills kills, @NotNull EquipmentHandler equipmentHandler,
        @NotNull EquipmentCreator equipmentCreator, @NotNull ZombiesPlayerActionBar actionBar,
        @NotNull InventoryAccessRegistry profileSwitcher, @NotNull PlayerStateSwitcher stateSwitcher,
        @NotNull Map<PlayerStateKey<?>, Function<?, ? extends ZombiesPlayerState>> stateFunctions,
        @NotNull Sidebar sidebar, @NotNull TabList tabList,
        @NotNull TransactionModifierSource mapTransactionModifierSource,
        @NotNull TransactionModifierSource playerTransactionModifierSource, @NotNull Flaggable flaggable,
        @NotNull ZombiesPlayerMapStats stats, @NotNull BestTimeLeaderboard leaderboard) {
        this.playerView = Objects.requireNonNull(playerView);
        this.meta = Objects.requireNonNull(meta);
        this.coins = Objects.requireNonNull(coins);
        this.kills = Objects.requireNonNull(kills);
        this.equipmentHandler = Objects.requireNonNull(equipmentHandler);
        this.equipmentCreator = Objects.requireNonNull(equipmentCreator);
        this.actionBar = Objects.requireNonNull(actionBar);
        this.profileSwitcher = Objects.requireNonNull(profileSwitcher);
        this.stateSwitcher = Objects.requireNonNull(stateSwitcher);
        this.stateFunctions = Map.copyOf(stateFunctions);
        this.sidebar = Objects.requireNonNull(sidebar);
        this.tabList = Objects.requireNonNull(tabList);
        this.playerTransactionModifierSource =
            Objects.requireNonNull(playerTransactionModifierSource);
        this.compositeTransactionModifierSource =
            TransactionModifierSource.compositeView(mapTransactionModifierSource, playerTransactionModifierSource);
        this.flaggable = Objects.requireNonNull(flaggable);
        this.stats = Objects.requireNonNull(stats);
        this.leaderboard = Objects.requireNonNull(leaderboard);
    }

    public @NotNull ZombiesPlayerMeta getMeta() {
        return meta;
    }

    public @NotNull PlayerCoins getCoins() {
        return coins;
    }

    public @NotNull PlayerKills getKills() {
        return kills;
    }

    public @NotNull EquipmentHandler getEquipmentHandler() {
        return equipmentHandler;
    }

    public @NotNull EquipmentCreator getEquipmentCreator() {
        return equipmentCreator;
    }

    public @NotNull InventoryAccessRegistry getInventoryAccessRegistry() {
        return profileSwitcher;
    }

    public @NotNull ZombiesPlayerActionBar getActionBar() {
        return actionBar;
    }

    public @NotNull PlayerStateSwitcher getStateSwitcher() {
        return stateSwitcher;
    }

    public @NotNull Map<PlayerStateKey<?>, Function<?, ? extends ZombiesPlayerState>> getStateFunctions() {
        return stateFunctions;
    }

    public @NotNull PlayerView getPlayerView() {
        return playerView;
    }

    public @NotNull Sidebar getSidebar() {
        return sidebar;
    }

    public @NotNull TabList getTabList() {
        return tabList;
    }

    @Depend("zombies.dependency.player.modifiers")
    public @NotNull TransactionModifierSource playerTransactionModifiers() {
        return playerTransactionModifierSource;
    }

    @Depend("zombies.dependency.player.composite_modifiers")
    public @NotNull TransactionModifierSource compositeTransactionModifiers() {
        return compositeTransactionModifierSource;
    }

    public @NotNull Flaggable flags() {
        return flaggable;
    }

    public @NotNull ZombiesPlayerMapStats getStats() {
        return stats;
    }

    public @NotNull BestTimeLeaderboard getLeaderboard() {
        return leaderboard;
    }
}
