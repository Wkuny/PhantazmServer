package org.phantazm.zombies.player.state;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.scoreboard.BelowNameTag;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.server.scoreboard.TabList;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.tick.Activable;
import org.phantazm.commons.MiniMessageUtils;
import org.phantazm.core.inventory.InventoryAccessRegistry;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.map.MapSettingsInfo;
import org.phantazm.zombies.player.ZombiesPlayerMeta;
import org.phantazm.zombies.player.state.context.AlivePlayerStateContext;

import java.util.*;

public class BasicAliveStateActivable implements Activable {
    private final AlivePlayerStateContext context;
    private final Instance instance;
    private final InventoryAccessRegistry accessRegistry;
    private final PlayerView playerView;
    private final ZombiesPlayerMeta meta;
    private final MapSettingsInfo settings;
    private final Sidebar sidebar;
    private final TabList tabList;
    private final BelowNameTag belowNameTag;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private long healTicks = 0;

    private int lastTickHp = -1;

    public BasicAliveStateActivable(@NotNull AlivePlayerStateContext context, @NotNull Instance instance,
        @NotNull InventoryAccessRegistry accessRegistry, @NotNull PlayerView playerView,
        @NotNull ZombiesPlayerMeta meta, @NotNull MapSettingsInfo settings, @NotNull Sidebar sidebar,
        @NotNull TabList tabList, @NotNull BelowNameTag belowNameTag) {
        this.context = Objects.requireNonNull(context);
        this.instance = Objects.requireNonNull(instance);
        this.accessRegistry = Objects.requireNonNull(accessRegistry);
        this.playerView = Objects.requireNonNull(playerView);
        this.meta = Objects.requireNonNull(meta);
        this.settings = Objects.requireNonNull(settings);
        this.sidebar = Objects.requireNonNull(sidebar);
        this.tabList = Objects.requireNonNull(tabList);
        this.belowNameTag = Objects.requireNonNull(belowNameTag);
    }

    @Override
    public void start() {
        playerView.getPlayer().ifPresent(player -> {
            player.setInvisible(false);
            player.setAllowFlying(false);
            player.setFlying(false);
            player.setGameMode(GameMode.ADVENTURE);
            sidebar.addViewer(player);
            tabList.addViewer(player);
            belowNameTag.addViewer(player);

            player.getAttribute(Attributes.HEAL_TICKS).setBaseValue(settings.healTicks());
            player.getAttribute(Attributes.REVIVE_TICKS).setBaseValue(settings.baseReviveTicks());
        });

        if (context.isRevive()) {
            playerView.getPlayer().ifPresent(player -> {
                player.setNextHurtTick(MinecraftServer.currentTick() + settings.reviveInvulnerabilityTicks());
            });

            // TODO: theoretical memleaks here
            playerView.getDisplayName().thenAccept(displayName -> {
                TagResolver[] tagResolvers = getTagResolvers(displayName);

                if (meta.isInGame()) {
                    playerView.getPlayer().ifPresent(player -> player.sendMessage(
                        miniMessage.deserialize(settings.reviveMessageToRevivedFormat(), tagResolvers)));
                }

                Set<Player> players = new HashSet<>(instance.getPlayers());
                playerView.getPlayer().ifPresent(players::remove);
                Audience filteredAudience = PacketGroupingAudience.of(players);

                filteredAudience.sendMessage(
                    miniMessage.deserialize(settings.reviveMessageToOthersFormat(), tagResolvers));
            });

            Point point = context.reviveLocation();
            if (point != null) {
                instance.playSound(settings.reviveSound(), point.x(), point.y(), point.z());
            } else {
                playerView.getPlayer().ifPresent(player -> {
                    Pos location = player.getPosition();
                    instance.playSound(settings.reviveSound(), location.x(), location.y(), location.z());
                });
            }
        }

        accessRegistry.switchAccess(InventoryKeys.ALIVE_ACCESS);
    }

    @Override
    public void tick(long time) {
        ++healTicks;
        playerView.getPlayer().ifPresent(player -> {
            int playerHealTicks = (int) player.getAttributeValue(Attributes.HEAL_TICKS);
            if (playerHealTicks >= 0) {
                if (healTicks >= playerHealTicks) {
                    player.setHealth(player.getHealth() + 1F);
                    healTicks = 0;
                }
            }

            int currentHp = (int) Math.floor(player.getHealth());
            if (currentHp != lastTickHp) {
                belowNameTag.updateScore(player, currentHp);
                lastTickHp = currentHp;
            }
        });
    }

    @Override
    public void end() {
        playerView.getPlayer().ifPresent(player -> {
            player.setInvisible(false);
            player.setAllowFlying(false);
            player.setFlying(false);
            player.setGameMode(GameMode.ADVENTURE);
            player.clearEffects();
            sidebar.addViewer(player);
            tabList.addViewer(player);
            belowNameTag.addViewer(player);
        });
    }

    private TagResolver[] getTagResolvers(@NotNull Component displayName) {
        boolean reviverPresent = context.reviverName() != null;
        List<TagResolver> tagResolvers = new ArrayList<>();
        tagResolvers.add(Placeholder.component("revived", displayName));
        tagResolvers.add(MiniMessageUtils.optional("reviver_present", reviverPresent));
        if (reviverPresent) {
            tagResolvers.add(Placeholder.component("reviver", context.reviverName()));
        }

        return tagResolvers.toArray(TagResolver[]::new);
    }

}
