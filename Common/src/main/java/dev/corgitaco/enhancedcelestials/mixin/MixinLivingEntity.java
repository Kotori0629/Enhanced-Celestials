package dev.corgitaco.enhancedcelestials.mixin;

import dev.corgitaco.enhancedcelestials.EnhancedCelestialsWorldData;
import dev.corgitaco.enhancedcelestials.core.EnhancedCelestialsContext;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("ConstantConditions")
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {


    public MixinLivingEntity(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void lunarEntityTick(CallbackInfo ci) {
        EnhancedCelestialsContext enhancedCelestialsContext = ((EnhancedCelestialsWorldData) this.level()).getLunarContext();
        if (enhancedCelestialsContext != null) {
            enhancedCelestialsContext.getLunarForecast().currentLunarEvent().value().livingEntityTick((LivingEntity) (Object) this);
        }
    }

    @Inject(method = "checkBedExists", at = @At("HEAD"), cancellable = true)
    private void blockSleeping(CallbackInfoReturnable<Boolean> cir) {
        EnhancedCelestialsContext enhancedCelestialsContext = ((EnhancedCelestialsWorldData) this.level()).getLunarContext();
        if (enhancedCelestialsContext != null) {
            if (enhancedCelestialsContext.getLunarForecast().currentLunarEvent().value().blockSleeping((LivingEntity) (Object) this)) {
                if (((LivingEntity) (Object) this) instanceof ServerPlayer) {
                    ((ServerPlayer) (Object) this).displayClientMessage(Component.translatable("enhancedcelestials.sleep.fail").withStyle(ChatFormatting.RED), true);
                }
                cir.setReturnValue(false);
            }
        }
    }
}
