package dev.corgitaco.enhancedcelestials.fabric.client;

import dev.corgitaco.enhancedcelestials.client.ECEntityRenderers;
import dev.corgitaco.enhancedcelestials.client.EnhancedCelestialsModelLayers;
import dev.corgitaco.enhancedcelestials.core.ECBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;

public class EnhancedCelestialsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ECEntityRenderers.register(EntityRendererRegistry::register);

        EnhancedCelestialsModelLayers.register((modelLayerLocation, layerDefinitionSupplier) -> EntityModelLayerRegistry.registerModelLayer(modelLayerLocation, layerDefinitionSupplier::get));

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), ECBlocks.SPACE_MOSS_CARPET.get(), ECBlocks.SPACE_MOSS_GRASS.get());
    }
}
