package corgitaco.enhancedcelestials.lunarevent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.enhancedcelestials.EnhancedCelestials;
import corgitaco.enhancedcelestials.api.EnhancedCelestialsRegistry;
import corgitaco.enhancedcelestials.api.lunarevent.LunarDimensionSettings;
import corgitaco.enhancedcelestials.api.lunarevent.LunarEvent;
import corgitaco.enhancedcelestials.util.CustomTranslationTextComponent;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.*;

public class LunarForecast {

    protected final Level level;
    protected final Holder<LunarDimensionSettings> dimensionSettingsHolder;
    protected final List<LunarEventInstance> forecast = new ArrayList<>();
    protected final List<LunarEventInstance> pastEvents = new ArrayList<>();
    protected final Object2ObjectOpenHashMap<Holder<LunarEvent>, LunarEvent.SpawnRequirements> lunarEventSpawnRequirements = new Object2ObjectOpenHashMap<>();
    protected long lastCheckedDay = -1L;
    private Holder<LunarEvent> lastTickEvent;
    private Holder<LunarEvent> lastStoredEvent;

    protected float blend = 1F;

    public LunarForecast(Level level, Holder<LunarDimensionSettings> lunarDimensionSettingsHolder) {
        this.level = level;
        this.dimensionSettingsHolder = lunarDimensionSettingsHolder;
        Registry<LunarEvent> lunarEvents = level.registryAccess().registry(EnhancedCelestialsRegistry.LUNAR_EVENT_KEY).orElseThrow();

        for (Map.Entry<ResourceKey<LunarEvent>, LunarEvent> resourceKeyLunarEventEntry : lunarEvents.entrySet()) {
            Holder<LunarEvent> lunarEventHolder = lunarEvents.getHolderOrThrow(resourceKeyLunarEventEntry.getKey());
            ResourceKey<Level> levelResourceKey = ResourceKey.create(Registries.DIMENSION, dimensionSettingsHolder.unwrapKey().orElseThrow().location());
            Map<ResourceKey<Level>, LunarEvent.SpawnRequirements> eventChancesByDimension = lunarEventHolder.value().getEventChancesByDimension();
            if (eventChancesByDimension.containsKey(levelResourceKey)) {
                LunarEvent.SpawnRequirements spawnRequirements = eventChancesByDimension.get(levelResourceKey);

                if (spawnRequirements.chance() > 0 && !spawnRequirements.validMoonPhases().isEmpty() && spawnRequirements.minNumberOfNights() >= 0) {
                    lunarEventSpawnRequirements.put(lunarEventHolder, spawnRequirements);
                }
            }
        }
        this.lastTickEvent = currentLunarEvent();
        this.lastStoredEvent = currentLunarEvent();
        String lunarEventNames = Arrays.toString(lunarEventSpawnRequirements.keySet().stream().map(Holder::unwrapKey).map(Optional::orElseThrow).map(ResourceKey::location).map(ResourceLocation::toString).toArray());
        String dimension = level.dimension().location().toString();
        EnhancedCelestials.LOGGER.info("Possible lunar events for dimension \"%s\" are %s.".formatted(dimension, lunarEventNames));

    }

    public void loadData(Data data) {
        this.forecast.clear();
        this.forecast.addAll(data.forecast);
        this.pastEvents.clear();
        this.pastEvents.addAll(data.pastEvents);
        this.lastCheckedDay = data.lastCheckedDay;
    }

    public Data saveData() {
        return new Data(this.forecast, this.pastEvents, lastCheckedDay);
    }

    public void tick() {
        if (blend < 1F) {
            blend += 0.01F;
        }

        if (currentLunarEvent() != lastTickEvent) {
            eventSwitched(lastTickEvent, currentLunarEvent());
        }
        lastTickEvent = getLunarEventForDay(getCurrentDay());
    }

    public boolean switchingEvents() {
        return blend < 1F;
    }

    public void eventSwitched(Holder<LunarEvent> lastEvent, Holder<LunarEvent> nextEvent) {
        blend = 0;
        lastStoredEvent = lastEvent;
    }

    public Holder<LunarEvent> lastLunarEvent() {
        return this.lastStoredEvent;
    }

    public Holder<LunarEvent> currentLunarEvent() {
        if (level.isDay()) {
            return defaultLunarEvent();
        }

        if (this.dimensionSettingsHolder.value().requiresClearSkies()) {
            if (level.isRaining()) {
                return defaultLunarEvent();
            }
        }

        Holder.Reference<LunarEvent> defaultEvent = defaultLunarEvent();
        if (this.forecast.isEmpty()) {
            return defaultEvent;
        }

        LunarEventInstance first = this.forecast.get(0);
        if (first.active(getCurrentDay())) {
            return lunarEventHolder(first.getLunarEventKey());
        }

        return defaultEvent;
    }

    public Holder<LunarEvent> nextScheduledLunarEvent() {
        if (this.forecast.isEmpty()) {
            return defaultLunarEvent();
        }

        LunarEventInstance first = this.forecast.get(0);
        if (first.active(getCurrentDay())) {
            LunarEventInstance second = this.forecast.get(1);
            return lunarEventHolder(second.getLunarEventKey());
        } else {
            return lunarEventHolder(first.getLunarEventKey());
        }
    }

    public Holder<LunarEvent> lastScheduledLunarEvent() {
        Holder.Reference<LunarEvent> defaultEvent = defaultLunarEvent();
        if (this.pastEvents.isEmpty()) {
            return defaultEvent;
        }

        LunarEventInstance first = this.pastEvents.get(0);
        if (first.active(getCurrentDay())) {
            return lunarEventHolder(first.getLunarEventKey());
        }

        return defaultEvent;
    }

    public Holder<LunarEvent> getLunarEventForDay(long day) {
        for (LunarEventInstance lunarEventInstance : this.forecast) {
            if (lunarEventInstance.active(day)) {
                return lunarEventHolder(lunarEventInstance.getLunarEventKey());
            }
        }
        for (LunarEventInstance lunarEventInstance : this.pastEvents) {
            if (lunarEventInstance.active(day)) {
                return lunarEventHolder(lunarEventInstance.getLunarEventKey());
            }
        }

        return defaultLunarEvent();
    }

    /**
     * @return A forecast text component to display in the chat showing up to the next 100 events.
     */
    public Component getForecastComponent() {
        MutableComponent textComponent = null;

        for (int i = Math.min(100, this.forecast.size() - 1); i >= 0; i--) {
            LunarEventInstance lunarEventInstance = this.forecast.get(i);
            Holder<LunarEvent> event = lunarEventHolder(lunarEventInstance.getLunarEventKey());
            CustomTranslationTextComponent name = event.value().getTextComponents().name();
            TextColor color = name.getStyle().getColor();
            if (textComponent == null) {
                textComponent = Component.translatable(name.getKey()).withStyle(Style.EMPTY.withColor(color));
            } else {
                textComponent.append(Component.literal(", ").withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))).append(Component.translatable(name.getKey()).withStyle(Style.EMPTY.withColor(color)));
            }
            textComponent.append(Component.translatable("enhancedcelestials.lunarforecast.days_left", lunarEventInstance.getDaysUntil(getCurrentDay())).withStyle(Style.EMPTY.withColor(color)));
        }

        if (textComponent != null) {
            return Component.translatable("enhancedcelestials.lunarforecast.header", textComponent.append(Component.literal(".").withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))));
        } else {
            return Component.translatable("enhancedcelestials.lunarforecast.empty", textComponent).withStyle(ChatFormatting.YELLOW);
        }

    }

    private Holder.Reference<LunarEvent> defaultLunarEvent() {
        return lunarEventHolder(this.dimensionSettingsHolder.value().defaultEvent());
    }

    private Holder.Reference<LunarEvent> lunarEventHolder(ResourceKey<LunarEvent> lunarEventKey) {
        return level.registryAccess().registry(EnhancedCelestialsRegistry.LUNAR_EVENT_KEY).orElseThrow().getHolderOrThrow(lunarEventKey);
    }


    public Object2LongArrayMap<ResourceKey<LunarEvent>> eventsByDay() {
        Object2LongArrayMap<ResourceKey<LunarEvent>> eventByLastTime = new Object2LongArrayMap<>();

        for (LunarEventInstance lunarEventInstance : this.pastEvents) {
            eventByLastTime.put(lunarEventInstance.getLunarEventKey(), lunarEventInstance.scheduledDay());
        }

        for (LunarEventInstance lunarEventInstance : this.forecast) {
            eventByLastTime.put(lunarEventInstance.getLunarEventKey(), lunarEventInstance.scheduledDay());
        }

        return eventByLastTime;
    }

    public long lastScheduledEventDay() {
        long lastScheduledEventDay = -1L;

        for (LunarEventInstance lunarEventInstance : this.forecast) {
            lastScheduledEventDay = Math.max(lunarEventInstance.scheduledDay(), lastScheduledEventDay);
        }

        for (LunarEventInstance lunarEventInstance : this.pastEvents) {
            lastScheduledEventDay = Math.max(lunarEventInstance.scheduledDay(), lastScheduledEventDay);
        }

        return lastScheduledEventDay;
    }


    public long getCurrentDay() {
        return getDayFromDayTime(this.level.getDayTime());
    }

    public long getDayFromDayTime(long dayTime) {
        return dayTime / this.dimensionSettingsHolder.value().dayLength();
    }

    public long getDayTimeFromDay(long day) {
        return day * this.dimensionSettingsHolder.value().dayLength();
    }

    public float getBlend() {
        return blend;
    }

    /**
     * The save / packet data for the lunar forecast.
     */
    public record Data(List<LunarEventInstance> forecast,
                       List<LunarEventInstance> pastEvents,
                       long lastCheckedDay) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(builder ->
                builder.group(
                        LunarEventInstance.CODEC.listOf().fieldOf("forecast").forGetter(Data::forecast),
                        LunarEventInstance.CODEC.listOf().fieldOf("past_events").forGetter(Data::pastEvents),
                        Codec.LONG.fieldOf("last_checked_day").forGetter(Data::lastCheckedDay)
                ).apply(builder, Data::new)
        );
    }
}
