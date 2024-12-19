package dev.corgitaco.enhancedcelestials.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.corgitaco.enhancedcelestials.EnhancedCelestialsWorldData;
import dev.corgitaco.enhancedcelestials.core.EnhancedCelestialsContext;
import dev.corgitaco.enhancedcelestials.lunarevent.LunarForecast;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin {


    @ModifyExpressionValue(method = "applyEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;expandTowards(DDD)Lnet/minecraft/world/phys/AABB;"))
    private static AABB scaleBeaconArea(AABB original, Level level) {
        EnhancedCelestialsContext lunarContext = ((EnhancedCelestialsWorldData) level).getLunarContext();
        if (lunarContext != null) {
            LunarForecast lunarForecast = lunarContext.getLunarForecast();
            double beaconRadiusAmplifier = lunarForecast.getCurrentEvent(level.getRainLevel(0) < 1).value().beaconRadiusAmplifier();
            return AABB.ofSize(original.getCenter(), original.getXsize() * beaconRadiusAmplifier, original.getYsize() * beaconRadiusAmplifier, original.getZsize() * beaconRadiusAmplifier);
        }

        return original;
    }
}
