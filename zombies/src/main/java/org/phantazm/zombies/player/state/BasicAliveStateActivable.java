package org.phantazm.zombies.player.state;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.server.scoreboard.TabList;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Activable;
import org.phantazm.commons.MiniMessageUtils;
import org.phantazm.core.inventory.InventoryAccessRegistry;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.map.MapSettingsInfo;
import org.phantazm.zombies.player.state.context.AlivePlayerStateContext;

import java.util.*;

public class BasicAliveStateActivable implements Activable {
    private static final TagResolver[] EMPTY_TAG_RESOLVER_ARRAY = new TagResolver[0];
    private final AlivePlayerStateContext context;
    private final Instance instance;
    private final InventoryAccessRegistry accessRegistry;
    private final PlayerView playerView;
    private final MapSettingsInfo settings;
    private final Sidebar sidebar;
    private final TabList tabList;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private long lastHeal;

    public BasicAliveStateActivable(@NotNull AlivePlayerStateContext context, @NotNull Instance instance,
            @NotNull InventoryAccessRegistry accessRegistry, @NotNull PlayerView playerView,
            @NotNull MapSettingsInfo settings, @NotNull Sidebar sidebar, @NotNull TabList tabList) {
        this.context = Objects.requireNonNull(context, "context");
        this.instance = Objects.requireNonNull(instance, "instance");
        this.accessRegistry = Objects.requireNonNull(accessRegistry, "accessRegistry");
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.sidebar = Objects.requireNonNull(sidebar, "sidebar");
        this.tabList = Objects.requireNonNull(tabList, "tabList");
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
        });

        if (context.isRevive()) {
            Set<Player> players = new HashSet<>(instance.getPlayers());
            playerView.getPlayer().ifPresent(players::remove);
            Audience instanceAudience = PacketGroupingAudience.of(players);

            playerView.getDisplayName().thenAccept(displayName -> {
                TagResolver[] tagResolvers = getTagResolvers(displayName);
                playerView.getPlayer().ifPresent(player -> {
                    player.sendMessage(miniMessage.deserialize(settings.reviveMessageToRevivedFormat(), tagResolvers));
                });
                instanceAudience.sendMessage(
                        miniMessage.deserialize(settings.reviveMessageToOthersFormat(), tagResolvers));
            });

            Point knockLocation = context.reviveLocation();
            if (knockLocation != null) {
                instanceAudience.playSound(settings.reviveSound(), knockLocation.x(), knockLocation.y(),
                        knockLocation.z());
            }
        }

        accessRegistry.switchAccess(InventoryKeys.ALIVE_ACCESS);
    }

    @Override
    public void tick(long time) {
        playerView.getPlayer().ifPresent(player -> {
            if ((time - lastHeal) / MinecraftServer.TICK_MS >= (int)player.getAttributeValue(Attributes.HEAL_TICKS)) {
                player.setHealth(player.getHealth() + 1F);
                lastHeal = time;
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
        });

        accessRegistry.switchAccess(null);
    }

    private TagResolver[] getTagResolvers(@NotNull Component displayName) {
        boolean reviverPresent = context.reviverName() != null;
        List<TagResolver> tagResolvers = new ArrayList<>();
        tagResolvers.add(Placeholder.component("revived", displayName));
        tagResolvers.add(MiniMessageUtils.optional("reviver_present", reviverPresent));
        if (reviverPresent) {
            tagResolvers.add(Placeholder.component("reviver", context.reviverName()));
        }

        return tagResolvers.toArray(EMPTY_TAG_RESOLVER_ARRAY);
    }

}
