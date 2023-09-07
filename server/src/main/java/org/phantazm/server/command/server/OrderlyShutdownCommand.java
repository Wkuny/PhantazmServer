package org.phantazm.server.command.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.permission.Permission;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.command.PermissionLockedCommand;
import org.phantazm.core.event.PlayerJoinLobbyEvent;
import org.phantazm.core.game.scene.RouterStore;
import org.phantazm.core.game.scene.SceneRouter;
import org.phantazm.core.game.scene.event.SceneShutdownEvent;
import org.phantazm.core.player.PlayerView;
import org.phantazm.server.RouterKeys;
import org.phantazm.server.config.server.ShutdownConfig;

import java.util.Objects;

public class OrderlyShutdownCommand extends PermissionLockedCommand {
    public static final Permission PERMISSION = new Permission("admin.orderly_shutdown");

    private final RouterStore routerStore;
    private boolean initialized;
    private long shutdownStart;

    public OrderlyShutdownCommand(@NotNull RouterStore routerStore, @NotNull ShutdownConfig shutdownConfig,
        @NotNull EventNode<Event> globalNode) {
        super("orderly_shutdown", PERMISSION);
        this.routerStore = Objects.requireNonNull(routerStore);

        addSyntax((sender, context) -> {
            if (initialized) {
                sender.sendMessage("Orderly shutdown has already been initialized");
                return;
            }

            globalNode.addListener(SceneShutdownEvent.class, this::onSceneShutdown);
            globalNode.addListener(AsyncPlayerPreLoginEvent.class, event -> {
                event.getPlayer().kick(Component.text("Server is not joinable", NamedTextColor.RED));
            });
            globalNode.addListener(PlayerJoinLobbyEvent.class, event -> {
                event.getPlayer().kick(Component.text("Routing to fresh instance...", NamedTextColor.RED));
            });

            initialized = true;
            shutdownStart = System.currentTimeMillis();

            for (SceneRouter<?, ?> router : routerStore.getRouters()) {
                if (router.isGame()) {
                    router.setJoinable(false);
                }
            }

            MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                long elapsedMs = System.currentTimeMillis() - shutdownStart;
                if (elapsedMs > shutdownConfig.forceShutdownWarningTime()) {
                    Audiences.all().sendMessage(shutdownConfig.forceShutdownMessage());
                } else {
                    Audiences.all().sendMessage(shutdownConfig.shutdownMessage());
                }
            }, TaskSchedule.immediate(), TaskSchedule.millis(shutdownConfig.warningInterval()));

            MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                long elapsedMs = System.currentTimeMillis() - shutdownStart;
                if (elapsedMs > shutdownConfig.forceShutdownTime()) {
                    System.exit(0); //exit even if we've got games
                    return;
                }

                if (noGamesActive()) {
                    System.exit(0);
                }
            }, TaskSchedule.immediate(), TaskSchedule.tick(20));

            if (noGamesActive()) {
                System.exit(0);
            }
        });
    }

    private boolean noGamesActive() {
        for (SceneRouter<?, ?> router : routerStore.getRouters()) {
            if (!router.isGame()) {
                continue;
            }

            if (router.hasActiveScenes()) {
                return false;
            }
        }

        return true;
    }

    private void onSceneShutdown(@NotNull SceneShutdownEvent event) {
        if (noGamesActive()) {
            System.exit(0);
        }
    }
}
