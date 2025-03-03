package org.phantazm.server;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;
import org.phantazm.server.command.whisper.ReplyCommand;
import org.phantazm.server.command.whisper.WhisperCommand;
import org.phantazm.server.command.whisper.WhisperConfig;
import org.phantazm.server.command.whisper.WhisperManager;

public class WhisperCommandFeature {
    static void initialize(@NotNull WhisperConfig whisperConfig) {
        ConnectionManager connectionManager = MinecraftServer.getConnectionManager();
        CommandManager commandManager = MinecraftServer.getCommandManager();
        WhisperManager whisperManager =
            new WhisperManager(connectionManager, commandManager.getConsoleSender(), whisperConfig,
                MiniMessage.miniMessage());

        commandManager.register(WhisperCommand.whisperCommand(connectionManager, whisperManager));
        commandManager.register(ReplyCommand.replyCommand(whisperManager));
    }

}
