package dev.corgitaco.enhancedcelestials.forge;

import dev.corgitaco.enhancedcelestials.EnhancedCelestials;
import dev.corgitaco.enhancedcelestials.api.EnhancedCelestialsRegistry;
import dev.corgitaco.enhancedcelestials.api.lunarevent.LunarDimensionSettings;
import dev.corgitaco.enhancedcelestials.api.lunarevent.LunarEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DataPackRegistryEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = EnhancedCelestials.MOD_ID)
public class ECModBusEventsHandler {

    @SubscribeEvent
    public static void registerDatapack(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(EnhancedCelestialsRegistry.LUNAR_EVENT_KEY, LunarEvent.DIRECT_CODEC, LunarEvent.DIRECT_CODEC);
        event.dataPackRegistry(EnhancedCelestialsRegistry.LUNAR_DIMENSION_SETTINGS_KEY, LunarDimensionSettings.CODEC, LunarDimensionSettings.CODEC);
    }
}