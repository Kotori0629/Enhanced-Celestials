package dev.corgitaco.enhancedcelestials.forge.platform;

import com.google.auto.service.AutoService;
import dev.corgitaco.enhancedcelestials.network.ECPacket;
import dev.corgitaco.enhancedcelestials.platform.services.IPlatformHelper;
import dev.corgitaco.enhancedcelestials.forge.network.ForgeNetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

@AutoService(IPlatformHelper.class)
public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public <P extends ECPacket> void sendToClient(ServerPlayer player, P packet) {
        ForgeNetworkHandler.sendToPlayer(player, packet);
    }

    @Override
    public Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
