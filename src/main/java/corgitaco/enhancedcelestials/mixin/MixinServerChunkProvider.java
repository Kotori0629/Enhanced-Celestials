package corgitaco.enhancedcelestials.mixin;

import corgitaco.enhancedcelestials.helper.LevelGetter;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ServerChunkProvider.class)
public class MixinServerChunkProvider {

    @Shadow @Nullable private WorldEntitySpawner.EntityDensityManager lastSpawnState;

    @Shadow @Final public ServerWorld level;

    @Inject(method = "tickChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/IProfiler;pop()V", ordinal = 0))
    private void setDensityManagerLevel(CallbackInfo ci) {
        ((LevelGetter) this.lastSpawnState).setLevel(this.level);
    }
}
