package dev.corgitaco.enhancedcelestials.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.corgitaco.enhancedcelestials.EnhancedCelestialsWorldData;
import dev.corgitaco.enhancedcelestials.core.EnhancedCelestialsContext;
import dev.corgitaco.enhancedcelestials.lunarevent.LunarForecast;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {


    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @WrapMethod(method = "giveExperiencePoints")
    private void modifyXPPoints(int xpPoints, Operation<Void> original) {
        EnhancedCelestialsContext lunarContext = ((EnhancedCelestialsWorldData) this.level()).getLunarContext();
        if (lunarContext != null && xpPoints >= 1) {
            LunarForecast lunarForecast = lunarContext.getLunarForecast();
            double xp = lunarForecast.currentLunarEvent().value().xpAmplifier();

            original.call((int) (xp * xpPoints));
        } else {
            original.call(xpPoints);
        }
    }


}
