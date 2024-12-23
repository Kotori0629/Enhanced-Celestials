package dev.corgitaco.enhancedcelestials.lunarevent;

import dev.corgitaco.enhancedcelestials.api.lunarevent.LunarDimensionSettings;
import dev.corgitaco.enhancedcelestials.api.lunarevent.LunarEvent;
import dev.corgitaco.enhancedcelestials.api.lunarevent.LunarTextComponents;
import dev.corgitaco.enhancedcelestials.network.LunarForecastChangedPacket;
import dev.corgitaco.enhancedcelestials.platform.services.IPlatformHelper;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ServerLunarForecast extends LunarForecast {

    public ServerLunarForecast(ServerLevel level, Holder<LunarDimensionSettings> lunarDimensionSettingsHolder) {
        super(level, lunarDimensionSettingsHolder);
    }

    @Override
    public void tick() {
        boolean[] sendPacket = new boolean[1];
        this.forecast.removeIf(lunarEventInstance -> {
            if (lunarEventInstance.passed(getCurrentDay())) {
                this.pastEvents.addFirst(lunarEventInstance);
                sendPacket[0] = true;
                return true;
            }

            if (lunarEventInstance.scheduledDay() > getCurrentDay() + this.dimensionSettingsHolder.value().yearLengthInDays()) {
                sendPacket[0] = true;
                return true;
            }

            return false;
        });

        this.pastEvents.removeIf(lunarEventInstance -> {
            if (lunarEventInstance.scheduledDay() > getCurrentDay()) {
                sendPacket[0] = true;
                return true;
            }

            if (lunarEventInstance.scheduledDay() < getCurrentDay() - this.dimensionSettingsHolder.value().yearLengthInDays()) {
                sendPacket[0] = true;
                return true;
            }
            return false;
        });
        super.tick();

        createOrUpdateForecast(this.lastCheckedDay, sendPacket);

        checkEmptyForecastOrThrow();

        if (sendPacket[0]) {
            IPlatformHelper.PLATFORM.sendToAllClients(((ServerLevel) level).players(), new LunarForecastChangedPacket(this));
        }
    }


    @Override
    public void eventSwitched(Holder<LunarEvent> lastEvent, Holder<LunarEvent> nextEvent) {
        super.eventSwitched(lastEvent, nextEvent);

        for (Player player : level.players()) {
            lastEvent.value().getTextComponents().setNotification().ifPresent(notification -> {
                if (notification.notificationType() != LunarTextComponents.NotificationType.NONE) {
                    player.displayClientMessage(notification.customTranslationTextComponent().getComponent(), notification.notificationType() == LunarTextComponents.NotificationType.HOT_BAR);
                }
            });

            nextEvent.value().getTextComponents().riseNotification().ifPresent(notification -> {
                if (notification.notificationType() != LunarTextComponents.NotificationType.NONE) {
                    player.displayClientMessage(notification.customTranslationTextComponent().getComponent(), notification.notificationType() == LunarTextComponents.NotificationType.HOT_BAR);
                }
            });
        }
    }

    private void checkEmptyForecastOrThrow() {
        if (this.forecast.isEmpty() && this.dimensionSettingsHolder.value().yearLengthInDays() > this.dimensionSettingsHolder.value().maxDaysBetweenEvents()) {
            throw new IllegalStateException("Forecast cannot be empty.... this should be impossible.... crashing game..... Report this to the Enhanced Celestials Github immediately, please provide your current world instance + other mods.");
        }
    }

    public void recomputeForecast() {
        this.forecast.clear();
        createOrUpdateForecast(getCurrentDay(), new boolean[1]);
        IPlatformHelper.PLATFORM.sendToAllClients(((ServerLevel) level).players(), new LunarForecastChangedPacket(this));
    }

    public void setLunarEvent(ResourceKey<LunarEvent> lunarEvent) {
        if (!this.level.isNight()) {
            ((ServerLevel) this.level).setDayTime((getCurrentDay() * this.dimensionSettingsHolder.value().dayLength()) + 13000L);
        }

        LunarEventInstance first = this.forecast.getFirst();
        if (first.active(getCurrentDay())) {
            this.forecast.removeFirst();
        }
        if (lunarEvent != this.dimensionSettingsHolder.value().defaultEvent()) {
            this.forecast.addFirst(new LunarEventInstance(lunarEvent, getCurrentDay(), true));
        }
        IPlatformHelper.PLATFORM.sendToAllClients(((ServerLevel) level).players(), new LunarForecastChangedPacket(this));
    }

    private void createOrUpdateForecast(long lastCheckedDay, boolean[] sendPacket) {
        long yearLengthInDays = this.dimensionSettingsHolder.value().yearLengthInDays();

        if (getCurrentDay() < lastCheckedDay - yearLengthInDays) {
            lastCheckedDay = getCurrentDay();
            this.lastCheckedDay = lastCheckedDay;
            sendPacket[0] = true;
        }

        long dayDifference = Mth.clamp(lastCheckedDay - getCurrentDay(), 0, yearLengthInDays);

        if (dayDifference < yearLengthInDays) {
            Object2LongArrayMap<ResourceKey<LunarEvent>> eventsByDay = eventsByDay();
            long lastScheduledEventDay = lastScheduledEventDay();
            long yearDayDifference = yearLengthInDays - dayDifference;
            for (int dayOffset = 0; dayOffset <= yearDayDifference; dayOffset++) {
                long day = getCurrentDay() + dayDifference + dayOffset;

                long seed = day + ((ServerLevel) level).getSeed() + level.dimension().hashCode();
                Random random = new Random(seed);

                List<Holder<LunarEvent>> scrambledLunarEvents = new ArrayList<>(this.lunarEventSpawnRequirements.keySet());
                Collections.shuffle(scrambledLunarEvents, random);

                for (Holder<LunarEvent> scrambledLunarEvent : scrambledLunarEvents) {
                    LunarEvent.SpawnRequirements spawnRequirements = this.lunarEventSpawnRequirements.get(scrambledLunarEvent);
                    boolean pastMinNumberOfNightsBetweenThisTypeOfEvent = (day - eventsByDay.getOrDefault(scrambledLunarEvent.unwrapKey().orElseThrow(), getCurrentDay())) > spawnRequirements.minNumberOfNights();
                    boolean pastMinNumberOfNightsBetweenAllEvents = (day - lastScheduledEventDay) > this.dimensionSettingsHolder.value().minDaysBetweenEvents();
                    boolean isValidMoonPhase = spawnRequirements.validMoonPhases().contains(this.level.dimensionType().moonPhase(getDayTimeFromDay(day)));
                    boolean chance = spawnRequirements.chance() >= random.nextDouble();
                    boolean checksPass = pastMinNumberOfNightsBetweenThisTypeOfEvent && pastMinNumberOfNightsBetweenAllEvents && isValidMoonPhase && chance;

                    boolean override = !checksPass && lastScheduledEventDay != -1 && day - lastScheduledEventDay >= this.dimensionSettingsHolder.value().maxDaysBetweenEvents();
                    if (checksPass || override) {
                        lastScheduledEventDay = day;
                        LunarEventInstance newLunarEventInstance = new LunarEventInstance(scrambledLunarEvent.unwrapKey().orElseThrow(), day);
                        eventsByDay.put(newLunarEventInstance.getLunarEventKey(), day);
                        forecast.add(newLunarEventInstance);
                    }
                }
            }
            this.lastCheckedDay = getCurrentDay() + yearLengthInDays;
            sendPacket[0] = true;
        }
    }
}
