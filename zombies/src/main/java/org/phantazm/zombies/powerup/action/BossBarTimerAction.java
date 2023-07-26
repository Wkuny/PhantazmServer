package org.phantazm.zombies.powerup.action;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.CancellableState;
import org.phantazm.commons.MathUtils;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicate;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@Model("zombies.powerup.action.boss_bar_timer")
@Cache(false)
public class BossBarTimerAction implements Supplier<PowerupAction> {
    private final Data data;
    private final Instance instance;
    private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;
    private final TickFormatter tickFormatter;

    @FactoryMethod
    public BossBarTimerAction(@NotNull Data data, @NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap,
            @NotNull @Child("tick_formatter") TickFormatter tickFormatter) {
        this.data = data;
        this.instance = instance;
        this.playerMap = playerMap;
        this.tickFormatter = Objects.requireNonNull(tickFormatter, "tickFormatter");
    }

    @Override
    public PowerupAction get() {
        return new Action(data, instance, playerMap, tickFormatter);
    }

    private static class Action implements PowerupAction {
        private final Data data;
        private final Instance instance;
        private final DeactivationPredicate predicate;
        private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;
        private final TickFormatter tickFormatter;
        private final UUID id;

        private long startTime = -1;
        private BossBar bossBar;

        private Action(Data data, Instance instance, Map<? super UUID, ? extends ZombiesPlayer> playerMap,
                TickFormatter tickFormatter) {
            this.data = data;
            this.instance = instance;
            this.predicate = new DeactivationPredicate() {
                @Override
                public void activate(long time) {

                }

                @Override
                public boolean shouldDeactivate(long time) {
                    return (time - startTime) / MinecraftServer.TICK_MS >= data.duration;
                }
            };
            this.playerMap = playerMap;
            this.tickFormatter = tickFormatter;
            this.id = UUID.randomUUID();
        }

        @Override
        public void tick(long time) {
            BossBar bossBar = this.bossBar;
            if (startTime < 0 || bossBar == null) {
                return;
            }

            long elapsedTime = (time - startTime) / MinecraftServer.TICK_MS;
            bossBar.name(createBossBarName(time));
            bossBar.progress((float)MathUtils.clamp(1D - ((float)elapsedTime / (float)data.duration), 0, 1));
        }

        @Override
        public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
            this.startTime = System.currentTimeMillis();

            BossBar bossBar = BossBar.bossBar(createBossBarName(time), 1.0F, data.color, data.overlay);
            instance.showBossBar(bossBar);

            this.bossBar = bossBar;

            for (ZombiesPlayer zombiesPlayer : playerMap.values()) {
                zombiesPlayer.registerCancellable(CancellableState.named(id, () -> {
                }, () -> zombiesPlayer.getPlayer().ifPresent(actualPlayer -> actualPlayer.hideBossBar(bossBar))), true);
            }
        }

        private Component createBossBarName(long time) {
            long delta = time - startTime;
            long asTicks = delta * MinecraftServer.TICK_MS;
            long remainingTicks = data.duration - asTicks;

            TagResolver timePlaceholder = Placeholder.unparsed("time", tickFormatter.format(remainingTicks));
            return MiniMessage.miniMessage().deserialize(data.format, timePlaceholder);
        }

        @Override
        public void deactivate(@NotNull ZombiesPlayer player) {
            BossBar bossBar = this.bossBar;
            if (bossBar == null) {
                return;
            }

            for (ZombiesPlayer zombiesPlayer : playerMap.values()) {
                zombiesPlayer.removeCancellable(id);
            }

            MinecraftServer.getBossBarManager().destroyBossBar(bossBar);
            this.bossBar = null;
        }

        @Override
        public @NotNull DeactivationPredicate deactivationPredicate() {
            return predicate;
        }
    }

    @DataObject
    public record Data(long duration,
                       @NotNull String format,
                       @NotNull BossBar.Color color,
                       @NotNull BossBar.Overlay overlay,
                       @NotNull @ChildPath("tick_formatter") String tickFormatter) {
    }
}
