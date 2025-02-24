package dev.corgitaco.enhancedcelestials.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import dev.corgitaco.enhancedcelestials.EnhancedCelestialsWorldData;
import dev.corgitaco.enhancedcelestials.core.EnhancedCelestialsContext;
import dev.corgitaco.enhancedcelestials.lunarevent.LunarForecast;
import dev.corgitaco.enhancedcelestials.lunarevent.ServerLunarForecast;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class LunarForecastCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("lunarForecast").executes(cs -> displayLunarForecast(cs.getSource())).then(Commands.literal("recompute").executes(cs -> recompute(cs.getSource())));
    }


    public static int recompute(CommandSourceStack source) {
        ServerLevel world = source.getLevel();

        EnhancedCelestialsContext enhancedCelestialsContext = ((EnhancedCelestialsWorldData) world).getLunarContext();

        if (enhancedCelestialsContext == null) {
            source.sendFailure(Component.translatable("enhancedcelestials.commands.disabled"));
            return 0;
        }
        LunarForecast lunarForecast = enhancedCelestialsContext.getLunarForecast();
        if (lunarForecast instanceof ServerLunarForecast serverLunarForecast) {
            serverLunarForecast.recomputeForecast();
        }
        source.sendSuccess(() -> Component.translatable("enhancedcelestials.lunarforecast.recompute"), true);
        return 1;
    }


    public static int displayLunarForecast(CommandSourceStack source) {
        ServerLevel world = source.getLevel();
        EnhancedCelestialsContext enhancedCelestialsContext = ((EnhancedCelestialsWorldData) world).getLunarContext();

        if (enhancedCelestialsContext == null) {
            source.sendFailure(Component.translatable("enhancedcelestials.commands.disabled"));
            return 0;
        }

        source.sendSuccess(() -> enhancedCelestialsContext.getLunarForecast().getForecastComponent(), true);
        return 1;
    }
}
