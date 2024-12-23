package corgitaco.enhancedcelestials.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Either;
import corgitaco.enhancedcelestials.EnhancedCelestialsWorldData;
import corgitaco.enhancedcelestials.api.EnhancedCelestialsRegistry;
import corgitaco.enhancedcelestials.api.lunarevent.LunarEvent;
import corgitaco.enhancedcelestials.core.EnhancedCelestialsContext;
import corgitaco.enhancedcelestials.lunarevent.LunarForecast;
import corgitaco.enhancedcelestials.lunarevent.ServerLunarForecast;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;

import java.util.Optional;

public class SetLunarEventCommand {

    private static final DynamicCommandExceptionType ERROR_LUNAR_EVENT_INVALID = new DynamicCommandExceptionType(obj -> Component.translatable("enhancedcelestials.commands.setlunarevent.invalid", obj));

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("setLunarEvent")
                .then(Commands.argument("lunarEvent", ResourceOrTagKeyArgument.resourceOrTagKey(EnhancedCelestialsRegistry.LUNAR_EVENT_KEY))
                        .executes(cs -> setLunarEvent(cs.getSource(), ResourceOrTagKeyArgument.getResourceOrTagKey(cs, "lunarEvent", EnhancedCelestialsRegistry.LUNAR_EVENT_KEY, ERROR_LUNAR_EVENT_INVALID))));
    }

    public static int setLunarEvent(CommandSourceStack source, ResourceOrTagKeyArgument.Result<LunarEvent> lunarEventResult) {
        ServerLevel world = source.getLevel();
        EnhancedCelestialsContext enhancedCelestialsContext = ((EnhancedCelestialsWorldData) world).getLunarContext();
        if (enhancedCelestialsContext == null) {
            source.sendFailure(Component.translatable("enhancedcelestials.commands.disabled"));
            return 0;
        }

        LunarForecast forecast = enhancedCelestialsContext.getLunarForecast();

        if (forecast instanceof ServerLunarForecast serverLunarForecast) {
            Either<ResourceKey<LunarEvent>, TagKey<LunarEvent>> unwrap = lunarEventResult.unwrap();
            if (unwrap.left().isPresent()) {
                ResourceKey<LunarEvent> lunarEventResourceKey = unwrap.left().orElseThrow();
                Registry<LunarEvent> lunarEvents = world.registryAccess().registry(EnhancedCelestialsRegistry.LUNAR_EVENT_KEY).orElseThrow();
                if (lunarEvents.containsKey(lunarEventResourceKey) && lunarEvents.getHolderOrThrow(lunarEventResourceKey).isBound()) {
                    serverLunarForecast.setLunarEvent(lunarEventResourceKey);
                    return 1;
                } else {
                    source.sendFailure(Component.literal("Invalid lunar event \"%s\"!".formatted(lunarEventResourceKey.location())));
                    return 0;
                }
            }

            if (unwrap.right().isPresent()) {
                Optional<HolderSet.Named<LunarEvent>> possibleTag = world.registryAccess().registry(EnhancedCelestialsRegistry.LUNAR_EVENT_KEY).orElseThrow().getTag(unwrap.right().orElseThrow());

                if (possibleTag.isPresent()) {
                    HolderSet.Named<LunarEvent> possibleLunarEvents = possibleTag.orElseThrow();

                    Optional<Holder<LunarEvent>> randomLunarEvent = possibleLunarEvents.getRandomElement(world.random);

                    if (randomLunarEvent.isPresent()) {
                        source.getServer().submit(() -> serverLunarForecast.setLunarEvent(randomLunarEvent.orElseThrow().unwrapKey().orElseThrow()));
                        return 1;
                    } else {
                        source.sendFailure(Component.literal("Invalid lunar event tag \"%s\"!".formatted(possibleLunarEvents.key().location())));
                        return 0;
                    }
                } else {
                    source.sendFailure(Component.literal("Invalid lunar event tag."));
                    return 0;
                }
            }
        }
        source.sendFailure(Component.literal("Could not start Lunar Event..."));


        return 0;
    }
}
