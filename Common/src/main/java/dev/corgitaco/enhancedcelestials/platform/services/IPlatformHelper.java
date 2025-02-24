package dev.corgitaco.enhancedcelestials.platform.services;

import dev.corgitaco.enhancedcelestials.network.ECPacket;
import dev.corgitaco.enhancedcelestials.platform.Services;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.List;

public interface IPlatformHelper {

     IPlatformHelper PLATFORM = Services.load(IPlatformHelper.class);

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    <P extends ECPacket> void sendToClient(ServerPlayer player, P packet);

    default <P extends ECPacket> void sendToAllClients(List<ServerPlayer> players, P packet) {
        for (ServerPlayer player : players) {
            sendToClient(player, packet);
        }
    }

    Path configDir();
}
