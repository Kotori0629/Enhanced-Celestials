package dev.corgitaco.enhancedcelestials.client.program;

import static org.lwjgl.opengl.GL20.*;

public final class ProgramUniforms {
    private final int color;
    private final int lightmap;

    private ProgramUniforms(int program) {
        color = glGetUniformLocation(program, "enhancedCelestialsColor");
        lightmap = glGetUniformLocation(program, "enhancedCelestialsLightmap");
    }

    /**
     * Program is required to be bound before uploading.
     * Normalized (0.0F - 1.0F) color values. Alpha is always set to 1.0F
     * @param r Red channel.
     * @param g Green channel.
     * @param b Blue channel.
     */
    public void uploadColor(float r, float g, float b) {
        glUniform4f(color, r, g, b, 1.0F);
    }

    /**
     * Program is required to be bound before uploading.
     * @param unit Texture unit of a lightmap. Either create your own texture and set the unit, or use an existing one.
     */
    public void uploadLightmap(int unit) {
        glUniform1i(lightmap, unit);
    }

    public static ProgramUniforms of(int program) {
        if (!glIsProgram(program)) {
            throw new UnsupportedOperationException("Attempted to use a non GL program.");
        }
        return new ProgramUniforms(program);
    }
}
