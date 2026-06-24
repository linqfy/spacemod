package dev.linqfy.spacemod;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpaceLightingTest {
    @Test
    void sunUsesRequestedTwoHundredBlockRadius() {
        assertEquals(200.0f, Spacemod.SUN_RADIUS_BLOCKS, 0.0001f);
    }

    @Test
    void sunShaderUsesSolarBlackbodyAndHighExposure() throws IOException {
        String shader = Files.readString(Path.of("src/main/resources/assets/spacemod/pinwheel/shaders/program/sun.fsh"));

        assertTrue(shader.contains("SOLAR_TEMPERATURE_K = 5778.0"));
        assertTrue(shader.contains("SOLAR_EXPOSURE = 18.0"));
        assertTrue(shader.contains("blackbodyColor"));
        assertTrue(shader.contains("limbDarkening"));
    }
}
