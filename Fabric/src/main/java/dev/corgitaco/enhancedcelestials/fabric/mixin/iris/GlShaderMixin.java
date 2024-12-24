package dev.corgitaco.enhancedcelestials.fabric.mixin.iris;

import dev.corgitaco.enhancedcelestials.client.program.ShaderTransformer;
import net.irisshaders.iris.gl.shader.GlShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = GlShader.class, remap = false)
public final class GlShaderMixin {

    @ModifyArg(method = "createShader", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/gl/shader/ShaderWorkarounds;safeShaderSource(ILjava/lang/CharSequence;)V"), index = 1, remap = false)
    private static CharSequence ec$createShader(CharSequence source) {
        var transformer = new ShaderTransformer();
        var src = (String) source;

        return transformer.transformVersioned(src);
    }
}
