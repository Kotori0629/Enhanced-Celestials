package dev.corgitaco.enhancedcelestials.network;

import dev.corgitaco.enhancedcelestials.EnhancedCelestials;
import dev.corgitaco.enhancedcelestials.EnhancedCelestialsWorldData;
import dev.corgitaco.enhancedcelestials.core.EnhancedCelestialsContext;
import dev.corgitaco.enhancedcelestials.lunarevent.LunarForecast;
import dev.corgitaco.enhancedcelestials.util.ClientUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public record LunarForecastChangedPacket(LunarForecast.Data lunarForecastData) implements ECPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, LunarForecastChangedPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(LunarForecast.Data.CODEC), LunarForecastChangedPacket::lunarForecastData,
            LunarForecastChangedPacket::new
    );

    public static final Type<LunarForecastChangedPacket> TYPE = new Type<>(EnhancedCelestials.createLocation("lunar_forecast_changed"));

    public LunarForecastChangedPacket(LunarForecast forecast) {
        this(forecast.saveData());
    }

    @Override
    public void handle(@Nullable Level level, @Nullable Player player) {
        if (level != null) {
            EnhancedCelestialsContext enhancedCelestialsContext = ((EnhancedCelestialsWorldData) level).getLunarContext();
            if (enhancedCelestialsContext != null) {
                if (level.isClientSide) {
                    ClientUtil.scheduleClientAction(() -> {
                        LunarForecast lunarForecast = enhancedCelestialsContext.getLunarForecast();
                        lunarForecast.loadData(this.lunarForecastData);
                    });
                }
            }
        }
    }

    @Override
    public Type<? extends LunarForecastChangedPacket> type() {
        return TYPE;
    }
}