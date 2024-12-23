package corgitaco.enhancedcelestials.core;

import corgitaco.enhancedcelestials.EnhancedCelestials;
import corgitaco.enhancedcelestials.api.EnhancedCelestialsRegistry;
import corgitaco.enhancedcelestials.api.lunarevent.LunarDimensionSettings;
import corgitaco.enhancedcelestials.api.lunarevent.LunarEvent;
import corgitaco.enhancedcelestials.lunarevent.LunarForecast;
import corgitaco.enhancedcelestials.lunarevent.ServerLunarForecast;
import corgitaco.enhancedcelestials.meteor.MeteorContext;
import corgitaco.enhancedcelestials.save.LunarForecastSavedData;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;

import org.jetbrains.annotations.Nullable;
import java.util.Optional;

public class EnhancedCelestialsContext {

    private final LunarForecast lunarForecast;

    @Nullable
    private final MeteorContext meteorContext;

    private EnhancedCelestialsContext(LunarForecast forecast) {
        this.lunarForecast = forecast;
        this.meteorContext = !EnhancedCelestials.NEW_CONTENT ? null : new MeteorContext();
    }

    @Nullable
    public static EnhancedCelestialsContext forLevel(Level level, Optional<LunarForecast.Data> saveData) {
        Registry<LunarDimensionSettings> lunarDimensionSettingsRegistry = level.registryAccess().registryOrThrow(EnhancedCelestialsRegistry.LUNAR_DIMENSION_SETTINGS_KEY);
        ResourceLocation location = level.dimension().location();
        Optional<Holder.Reference<LunarDimensionSettings>> possibleLunarDimensionSettings = lunarDimensionSettingsRegistry.getHolder(ResourceKey.create(EnhancedCelestialsRegistry.LUNAR_DIMENSION_SETTINGS_KEY, location));

        if (possibleLunarDimensionSettings.isPresent()) {
            Holder<LunarDimensionSettings> lunarDimensionSettings = possibleLunarDimensionSettings.get();

            LunarForecast forecast;

            if (!level.isClientSide) {
                forecast = new ServerLunarForecast((ServerLevel) level, lunarDimensionSettings);
            } else {
                forecast = new LunarForecast(level, lunarDimensionSettings);
            }
            LunarForecast.Data forecastData = saveData.orElseGet(() -> LunarForecastSavedData.get(level).getForecastSaveData());

            if (forecastData != null) {
                forecast.loadData(forecastData);
            }

            return new EnhancedCelestialsContext(forecast);
        }
        return null;
    }

    public void tick(Level world) {
        this.lunarForecast.tick();
        if (world.getGameTime() % 2400 == 0) {
            save(world);
        }
    }

    public void chunkTick(Level level, ChunkAccess chunkAccess) {
        if (meteorContext != null) {
            meteorContext.chunkTick(level, chunkAccess);
        }
    }

    public void save(Level world) {
        LunarForecastSavedData.get(world).setForecastSaveData(this.lunarForecast.saveData());
    }

    public LunarForecast getLunarForecast() {
        return lunarForecast;
    }

}
