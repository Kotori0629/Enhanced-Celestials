package dev.corgitaco.enhancedcelestials.forge.client;

import dev.corgitaco.enhancedcelestials.EnhancedCelestials;
import dev.corgitaco.enhancedcelestials.client.ECEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BYGForgeClientEventsHandler {

    @SubscribeEvent
    public static void ec_onEntityRenderersEvent$RegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ECEntityRenderers.register(event::registerEntityRenderer);
        EnhancedCelestials.LOGGER.info("Entity renderer register");
    }
}