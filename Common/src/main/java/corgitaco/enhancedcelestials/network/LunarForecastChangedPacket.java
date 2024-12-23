package corgitaco.enhancedcelestials.network;

import corgitaco.enhancedcelestials.EnhancedCelestialsWorldData;
import corgitaco.enhancedcelestials.core.EnhancedCelestialsContext;
import corgitaco.enhancedcelestials.lunarevent.LunarForecast;
import corgitaco.enhancedcelestials.util.ClientUtil;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class LunarForecastChangedPacket implements S2CPacket {

    private final LunarForecast.Data lunarForecastData;

    public LunarForecastChangedPacket(LunarForecast lunarForecastData) {
        this(lunarForecastData.saveData());
    }

    public LunarForecastChangedPacket(LunarForecast.Data lunarForecastData) {
        this.lunarForecastData = lunarForecastData;
    }


    public static LunarForecastChangedPacket readFromPacket(FriendlyByteBuf buf) {
        try {
            return new LunarForecastChangedPacket(buf.readWithCodec(NbtOps.INSTANCE, LunarForecast.Data.CODEC));
        } catch (Exception e) {
            throw new IllegalStateException("Lunar Forecast packet could not be read. This is really really bad...\n\n" + e.getMessage());
        }
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        try {
            buf.writeWithCodec(NbtOps.INSTANCE, LunarForecast.Data.CODEC, this.lunarForecastData);
        } catch (Exception e) {
            throw new IllegalStateException("Lunar Forecast packet could not be written to. This is really really bad...\n\n" + e.getMessage());
        }
    }

    @Override
    public void handle(@Nullable Level level) {
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
}