package dev.linqfy.spacemod;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlanetVisualEffectsTest {
    @Test
    void newPlanetsStartWithDramaticAtmosphereDefaults() {
        Planet planet = new Planet(0.0f, 100.0f, 0.0f, 20.0f, 0.2f, 0.6f, 0.8f);

        assertEquals(1.08f, planet.atmosphereRadiusMultiplier, 0.0001f);
        assertEquals(18.0f, planet.rayleighStrength, 0.0001f);
        assertEquals(1.2f, planet.mieStrength, 0.0001f);
        assertEquals(0.76f, planet.mieAnisotropy, 0.0001f);
        assertEquals(1.8f, planet.atmosphereExposure, 0.0001f);
        assertEquals(0.65f, planet.auroraStrength, 0.0001f);
        assertEquals(1.0f, planet.lensFlareStrength, 0.0001f);
    }
}
