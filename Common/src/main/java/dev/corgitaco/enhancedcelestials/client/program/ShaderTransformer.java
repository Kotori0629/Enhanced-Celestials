package dev.corgitaco.enhancedcelestials.client.program;

import dev.corgitaco.enhancedcelestials.EnhancedCelestials;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShaderTransformer {
    private static final String VERSION_MACRO = "#version";

    public String transformVersioned(String source) {
        var i = source.indexOf(VERSION_MACRO) + VERSION_MACRO.length() + 1;
        var version = asInt(source.substring(i, source.indexOf(" ", i)));

        return version == 150 ? transform150(source) : version == 330 ? transform330(source) : source;
    }

    private String transform150(String source) {
        EnhancedCelestials.LOGGER.error("------- GLSL version 150 is currently not supported, this is intentional as of writing.");
        return source;
    }

    private String transform330(String source) {
        var outputs = collectOutputs(source);
        if (outputs.size() == 1) {
            var output = outputs.getFirst();
            if (output.type.equals("vec4")) {
                var name = output.name;
                var builder = new StringBuilder();
                for (var line : source.split("\n")) {
                    if (line.contains("%s =".formatted(name))) {
                        line = "%s = %s * vec4(1.0F, 0.25F, 0.25F, 0.0F);".formatted(name, getAssignment(line));
                    }

                    builder.append(line).append("\n");
                }
                var l = attachUniform(builder.toString());
                System.out.println(l);
                return l;
            }
        }
        return source;
    }

    private String attachUniform(String source) {
        var builder = new StringBuilder();
        var split = source.split("\n");
        var length = split.length;
        var j = 0;
        for (var i = 0; i < length; i++) {
            var line = split[0];
            if (line.contains(VERSION_MACRO)) {
                j = i + 1;
                break;
            }
        }
        for (var i = 0; i < length; i++) {
            builder.append(split[i]).append("\n");
            if (i == j) {
                builder.append("uniform vec4 enhancedCelestialsColor;\n");
                builder.append("uniform sampler2D enhancedCelestialsLightmap;\n");
            }
        }
        return builder.toString();
    }

    private static int asInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String getAssignment(String line) {
        return line.substring(line.indexOf("=") + 1, line.lastIndexOf(";")).trim();
    }

    private static List<Output> collectOutputs(String source) {
        var list = new ArrayList<Output>();
        for (var line : source.split("\n")) {
            if (line.contains("out") && !line.contains(",") && !line.contains("void")) {
                var m = 0;
                var k = 0;
                for (var j = line.length() - 1; j > 0; j--) {
                    if (line.charAt(j) == ' ') {
                        if (m == 1) {
                            k = j;
                            break;
                        }
                        m++;
                    }
                }
                var split = line.substring(k + 1, line.indexOf(";")).split(" ", 2);
                list.add(new Output(split[0], split[1]));
            }
        }
        return Collections.unmodifiableList(list);
    }

    private record Output(String type, String name) {
    }
}
