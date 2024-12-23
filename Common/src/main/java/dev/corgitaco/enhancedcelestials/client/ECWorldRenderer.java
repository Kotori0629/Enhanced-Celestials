package dev.corgitaco.enhancedcelestials.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.corgitaco.enhancedcelestials.EnhancedCelestialsWorldData;
import dev.corgitaco.enhancedcelestials.api.client.ColorSettings;
import dev.corgitaco.enhancedcelestials.api.lunarevent.LunarEvent;
import dev.corgitaco.enhancedcelestials.core.EnhancedCelestialsContext;
import dev.corgitaco.enhancedcelestials.lunarevent.LunarForecast;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class ECWorldRenderer {

    public static void changeMoonColor(float partialTicks) {
        ClientLevel level = Minecraft.getInstance().level;
        EnhancedCelestialsContext enhancedCelestialsContext = ((EnhancedCelestialsWorldData) level).getLunarContext();
        if (enhancedCelestialsContext != null) {
            LunarForecast lunarForecast = enhancedCelestialsContext.getLunarForecast();

            ColorSettings lastColorSettings = lunarForecast.lastLunarEvent().value().getClientSettings().colorSettings();
            ColorSettings currentColorSettings = lunarForecast.currentLunarEvent().value().getClientSettings().colorSettings();

            Vector3f lastGLColor = lastColorSettings.getGLMoonColor();
            Vector3f currentGLColor = currentColorSettings.getGLMoonColor();

            float blend = lunarForecast.getBlend();

            float r = Mth.clampedLerp(lastGLColor.x(), currentGLColor.x(), blend);
            float g = Mth.clampedLerp(lastGLColor.y(), currentGLColor.y(), blend);
            float b = Mth.clampedLerp(lastGLColor.z(), currentGLColor.z(), blend);
            RenderSystem.setShaderColor(r, g, b, 1.0F - level.getRainLevel(partialTicks));
        }
    }

    public static void bindMoonTexture(int moonTextureId, ResourceLocation moonLocation) {
        ClientLevel level = Minecraft.getInstance().level;
        EnhancedCelestialsContext enhancedCelestialsContext = ((EnhancedCelestialsWorldData) level).getLunarContext();
        if (enhancedCelestialsContext != null) {
            LunarForecast lunarForecast = enhancedCelestialsContext.getLunarForecast();
            RenderSystem.setShaderTexture(moonTextureId, lunarForecast.currentLunarEvent().value().getClientSettings().moonTextureLocation());
        } else {
            RenderSystem.setShaderTexture(moonTextureId, moonLocation);
        }
    }

    public static float getMoonSize(float arg0) {
        ClientLevel level = Minecraft.getInstance().level;
        EnhancedCelestialsContext enhancedCelestialsContext = ((EnhancedCelestialsWorldData) level).getLunarContext();
        if (enhancedCelestialsContext != null) {
            LunarForecast lunarForecast = enhancedCelestialsContext.getLunarForecast();
            return Mth.clampedLerp(lunarForecast.lastLunarEvent().value().getClientSettings().moonSize(), lunarForecast.currentLunarEvent().value().getClientSettings().moonSize(), lunarForecast.getBlend());
        }
        return arg0;
    }

    public static void eventLightMap(Vector3f skyVector, float partialTicks) {
        ClientLevel level = Minecraft.getInstance().level;
        EnhancedCelestialsWorldData enhancedCelestialsWorldData = (EnhancedCelestialsWorldData) level;
        if (enhancedCelestialsWorldData != null) {
            EnhancedCelestialsContext enhancedCelestialsContext = enhancedCelestialsWorldData.getLunarContext();
            if (enhancedCelestialsContext != null) {
                LunarForecast lunarForecast = enhancedCelestialsContext.getLunarForecast();
                LunarEvent lastEvent = lunarForecast.lastLunarEvent().value();
                LunarEvent currentEvent = lunarForecast.currentLunarEvent().value();

                ColorSettings colorSettings = currentEvent.getClientSettings().colorSettings();
                ColorSettings lastColorSettings = lastEvent.getClientSettings().colorSettings();

                Vector3f glSkyLightColor = lastColorSettings.getGLSkyLightColor();
                Vector3f targetColor = new Vector3f(glSkyLightColor.x(), glSkyLightColor.y(), glSkyLightColor.z());

                float skyDarken = (level.getSkyDarken(1.0F) - 0.2F) / 0.8F;
                float eventBlend = lunarForecast.getBlend() - skyDarken;
                targetColor.lerp(colorSettings.getGLSkyLightColor(), eventBlend);

                float skyBlend = (1 - skyDarken) - level.getRainLevel(partialTicks);
                skyVector.lerp(targetColor, skyBlend);
            }
        }
    }
}
