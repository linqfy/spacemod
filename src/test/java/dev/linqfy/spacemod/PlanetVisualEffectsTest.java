package dev.linqfy.spacemod;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlanetVisualEffectsTest {
    @Test
    void newPlanetsStartWithPhysicallyPlausibleAtmosphereDefaults() {
        Planet planet = new Planet(0.0f, 100.0f, 0.0f, 20.0f, 0.2f, 0.6f, 0.8f);

        assertEquals(1.025f, planet.atmosphereRadiusMultiplier, 0.0001f);
        assertEquals(7.5f, planet.rayleighStrength, 0.0001f);
        assertEquals(0.28f, planet.mieStrength, 0.0001f);
        assertEquals(0.82f, planet.mieAnisotropy, 0.0001f);
        assertEquals(1.25f, planet.atmosphereExposure, 0.0001f);
        assertEquals(0.0f, planet.auroraStrength, 0.0001f);
        assertEquals(0.45f, planet.atmosphereColorR, 0.0001f);
        assertEquals(0.63f, planet.atmosphereColorG, 0.0001f);
        assertEquals(1.0f, planet.atmosphereColorB, 0.0001f);
        assertEquals(680.0f, planet.rayleighWavelengthR, 0.0001f);
        assertEquals(550.0f, planet.rayleighWavelengthG, 0.0001f);
        assertEquals(440.0f, planet.rayleighWavelengthB, 0.0001f);
        assertEquals(1.0f, planet.gasDensity, 0.0001f);
        assertEquals(0.55f, planet.notLookingBloomIntensity, 0.0001f);
        assertEquals(2.4f, planet.lookingEdgeBloomIntensity, 0.0001f);
        assertEquals(0.18f, planet.bloomMorphRadius, 0.0001f);
    }

    @Test
    void atmosphereDefaultsRespondToPlanetColor() {
        Planet ocean = new Planet(0.0f, 100.0f, 0.0f, 20.0f, 0.1f, 0.35f, 0.9f);
        Planet dusty = new Planet(0.0f, 100.0f, 0.0f, 20.0f, 0.9f, 0.45f, 0.1f);

        assertTrue(ocean.atmosphereColorB > ocean.atmosphereColorR);
        assertTrue(dusty.atmosphereColorR > dusty.atmosphereColorB);
        assertTrue(dusty.mieStrength > ocean.mieStrength);
        assertTrue(dusty.rayleighWavelengthB > ocean.rayleighWavelengthB);
    }
}
