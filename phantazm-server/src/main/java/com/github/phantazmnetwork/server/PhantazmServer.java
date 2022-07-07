package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.api.player.BasicPlayerViewProvider;
import com.github.phantazmnetwork.api.player.MojangIdentitySource;
import com.github.phantazmnetwork.api.player.PlayerViewProvider;
import com.github.phantazmnetwork.mob.trigger.MobTriggers;
import com.github.phantazmnetwork.server.config.lobby.LobbiesConfig;
import com.github.phantazmnetwork.server.config.server.ServerConfig;
import com.github.phantazmnetwork.server.config.server.ServerInfoConfig;
import com.github.steanky.ethylene.codec.yaml.YamlCodec;
import com.github.steanky.ethylene.core.ConfigHandler;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.optifine.OptifineSupport;
import net.minestom.server.extras.velocity.VelocityProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

/**
 * Launches the server, and provides some useful static constants.
 */
public final class PhantazmServer {
    /**
     * The default, global {@link Logger} for PhantazmServer.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(PhantazmServer.class);

    /**
     * The global EventNode for Phantazm events.
     */
    public static final EventNode<Event> PHANTAZM_NODE = EventNode.all("phantazm");

    /**
     * Starting point for the server.
     * @param args Do you even know java?
     */
    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        ServerConfig serverConfig;
        LobbiesConfig lobbiesConfig;
        try {
            LOGGER.info("Loading server configuration data.");
            Configuration.initialize();
            ConfigHandler handler = Configuration.getHandler();
            handler.writeDefaultsAndGet();

            serverConfig = handler.getData(Configuration.SERVER_CONFIG_KEY);
            lobbiesConfig = handler.getData(Configuration.LOBBIES_CONFIG_KEY);
            LOGGER.info("Server configuration loaded successfully.");
        }
        catch (ConfigProcessException e) {
            LOGGER.error("Fatal error when loading configuration data", e);
            return;
        }

        EventNode<Event> node = MinecraftServer.getGlobalEventHandler();
        try {
            LOGGER.info("Initializing features.");
            initializeFeatures(node, PHANTAZM_NODE, serverConfig, lobbiesConfig);
            LOGGER.info("Features initialized successfully.");
        }
        catch (Exception exception) {
            LOGGER.error("Fatal error during initialization", exception);
            return;
        }

        try {
            startServer(node, minecraftServer, serverConfig);
        }
        catch (Exception exception) {
            LOGGER.error("Fatal error during server startup", exception);
        }
    }

    private static void initializeFeatures(EventNode<Event> global, EventNode<Event> phantazm, ServerConfig serverConfig,
                                           LobbiesConfig lobbiesConfig) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(new MojangIdentitySource(ForkJoinPool
                .commonPool()), MinecraftServer.getConnectionManager());

        Lobbies.initialize(global, viewProvider, lobbiesConfig);
        Chat.initialize(global);
        Neuron.initialize(global, serverConfig.pathfinderConfig());
        NeuronTest.initialize(global, Neuron.getSpawner(), phantazm);
        Mob.initialize(global, Neuron.getSpawner(), MobTriggers.TRIGGERS, Path.of("./mobs/"), new YamlCodec());
        EquipmentFeature.initialize(Path.of("./equipment/"), new YamlCodec(() -> new Load(LoadSettings.builder().build()),
                () -> new Dump(DumpSettings.builder()
                        .setDefaultFlowStyle(FlowStyle.BLOCK)
                        .build())));
        GunTest.initialize(global, phantazm, viewProvider);
        ZombiesTest.initialize(global);
    }

    private static void startServer(EventNode<Event> node, MinecraftServer server, ServerConfig serverConfig) {
        ServerInfoConfig infoConfig = serverConfig.serverInfoConfig();

        if (infoConfig.optifineEnabled()) {
            OptifineSupport.enable();
        }

        switch (infoConfig.authType()) {
            case MOJANG -> MojangAuth.init();
            case BUNGEE -> {
                BungeeCordProxy.enable();
                BungeeCordProxy.setBungeeGuardTokens(Set.of(infoConfig.velocitySecret()));
            }
            case VELOCITY -> VelocityProxy.enable(infoConfig.velocitySecret());
        }

        node.addListener(ServerListPingEvent.class, event -> event.getResponseData().setDescription(serverConfig
                .pingListConfig().description()));

        server.start(infoConfig.serverIP(), infoConfig.port());
    }
}