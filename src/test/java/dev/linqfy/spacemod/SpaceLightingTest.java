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

    @Test
    void skyboxFlareIsHiddenWhenSunIsBehindCamera() throws IOException {
        String shader = Files.readString(Path.of("src/main/resources/assets/spacemod/pinwheel/shaders/program/skybox.fsh"));

        assertTrue(shader.contains("float visibilityGate = step(0.5, visible)"));
        assertTrue(shader.contains("return (ambientColor + lensColor) * LensFlareStrength * visibilityGate"));
    }

    @Test
    void skyboxUsesThreeDimensionalPlanetAtmosphereBloom() throws IOException {
        String shader = Files.readString(Path.of("src/main/resources/assets/spacemod/pinwheel/shaders/program/skybox.fsh"));
        String source = Files.readString(Path.of("src/main/java/dev/linqfy/spacemod/Spacemod.java"));

        assertTrue(shader.contains("uniform vec3 PlanetAtmosphereDirection"));
        assertTrue(shader.contains("uniform vec3 CameraForwardDirection"));
        assertTrue(shader.contains("uniform vec3 PlanetAtmosphereColor"));
        assertTrue(shader.contains("uniform float PlanetSurfaceAngularRadius"));
        assertTrue(shader.contains("uniform float PlanetAtmosphereAngularRadius"));
        assertTrue(shader.contains("uniform float PlanetAtmosphereNotLookingIntensity"));
        assertTrue(shader.contains("uniform float PlanetAtmosphereLookingEdgeIntensity"));
        assertTrue(shader.contains("uniform float PlanetAtmosphereMorphRadius"));
        assertTrue(shader.contains("drawPlanetAtmosphereBloom(dir, sunDir)"));
        assertTrue(shader.contains("float cameraFacingEdge"));
        assertTrue(shader.contains("float surfaceRadius = max(PlanetSurfaceAngularRadius, 0.0001)"));
        assertTrue(shader.contains("float atmosphereRadius = max(PlanetAtmosphereAngularRadius, surfaceRadius + 0.0001)"));
        assertTrue(shader.contains("float shellThickness = max(atmosphereRadius - surfaceRadius"));
        assertTrue(shader.contains("float shellImpact = clamp((angleDist - surfaceRadius) / shellThickness, -1.0, 3.0)"));
        assertTrue(shader.contains("float horizonOpticalDepth"));
        assertTrue(shader.contains("float broadBloom"));
        assertTrue(shader.contains("float tangentLength = length(rawCameraTangent)"));
        assertTrue(shader.contains("float edgeTargetStrength = smoothstep(surfaceRadius * 0.18, surfaceRadius * 0.95, tangentLength)"));
        assertTrue(shader.contains("float lookedEdge = clamp(dot(sampleTangent, cameraTangent) * 0.5 + 0.5, 0.0, 1.0)"));
        assertTrue(shader.contains("float edgeExtension = (PlanetAtmosphereLookingEdgeIntensity * lookedEdge - PlanetAtmosphereNotLookingIntensity * (1.0 - lookedEdge) * 0.35) * edgeTargetStrength"));
        assertTrue(shader.contains("float deformedShellImpact = (angleDist - deformedSurfaceRadius) / shellThickness"));
        assertTrue(shader.contains("float baseShellImpact = clamp((angleDist - surfaceRadius) / shellThickness, -1.0, 4.25)"));
        assertTrue(shader.contains("float edgeOpticalDepth = horizonOpticalDepth + exp(-abs(angleDist - deformedSurfaceRadius) / horizonWidth) * edgeTargetStrength * lookedEdge"));
        assertTrue(shader.contains("float outsideHalo = exp(-max(angleDist - atmosphereRadius, 0.0) / outerWidth)"));
        assertTrue(shader.contains("float edgeOutsideHalo = exp(-max(angleDist - deformedAtmosphereRadius, 0.0) / outerWidth) * edgeTargetStrength * lookedEdge"));
        assertTrue(shader.contains("float broadBloom = exp(-max(angleDist - surfaceRadius, 0.0) / broadWidth) * smoothstep(-0.35, 1.2, baseShellImpact)"));
        assertTrue(shader.contains("float innerOcclusion = smoothstep(surfaceRadius * 0.94, surfaceRadius + shellThickness * 0.55, angleDist)"));
        assertTrue(shader.contains("vec3 atmosphereNormal = normalize(mix(-planetDir, sampleTangent, normalBlend))"));
        assertTrue(shader.contains("float sunlitRim = smoothstep(-0.05, 0.35, dot(atmosphereNormal, sunDir))"));
        assertTrue(shader.contains("float terminatorGlow = exp(-abs(dot(atmosphereNormal, sunDir)) / 0.18) * 0.35"));
        assertTrue(shader.contains("float rayleighPhase = 0.75 * (1.0 + mu * mu)"));
        assertTrue(shader.contains("float miePhase"));
        assertTrue(shader.contains("mix(PlanetAtmosphereNotLookingIntensity, PlanetAtmosphereLookingEdgeIntensity, cameraFacingEdge)"));
        assertTrue(shader.contains("float daylightVisibility = mix(0.015, 1.0, max(sunlitRim, terminatorGlow))"));
        assertTrue(shader.contains("vec3 visibleBloom = rayleigh + mie"));
        assertTrue(shader.contains("return visibleBloom * PlanetAtmosphereIntensity * bloomModeIntensity * daylightVisibility"));
        assertTrue(source.contains("skyPlanetDirUniform.setVector"));
        assertTrue(source.contains("skyCameraForwardUniform.setVector"));
        assertTrue(source.contains("new org.joml.Vector3f(0.0f, 0.0f, -1.0f)"));
        assertTrue(source.contains("planetSurfaceAngularRadius = (float) Math.asin(surfaceAngularInput)"));
        assertTrue(source.contains("skyPlanetSurfaceRadiusUniform.setFloat"));
        assertTrue(source.contains("skyPlanetRadiusUniform.setFloat"));
        assertTrue(source.contains("skyPlanetNotLookingUniform.setFloat"));
        assertTrue(source.contains("skyPlanetLookingEdgeUniform.setFloat"));
        assertTrue(source.contains("skyPlanetMorphRadiusUniform.setFloat"));
        assertTrue(source.contains("\"Not-Looking Bloom\""));
        assertTrue(source.contains("\"Looking Edge Bloom\""));
        assertTrue(source.contains("\"Bloom Morph Radius\""));
    }

    @Test
    void atmosphereShaderUsesPerPlanetGasColorAndCameraOpticalDepth() throws IOException {
        String shader = Files.readString(Path.of("src/main/resources/assets/spacemod/pinwheel/shaders/program/atmosphere.fsh"));

        assertTrue(shader.contains("uniform vec3 AtmosphereColor"));
        assertTrue(shader.contains("uniform vec3 RayleighWavelengths"));
        assertTrue(shader.contains("uniform float GasDensity"));
        assertTrue(shader.contains("rayleighFromWavelengths(RayleighWavelengths)"));
        assertTrue(shader.contains("float cameraBoost = horizonBoost"));
    }

    @Test
    void sunBloomIsGlobalAndUiHasSeparateSunSettings() throws IOException {
        String source = Files.readString(Path.of("src/main/java/dev/linqfy/spacemod/Spacemod.java"));
        String planetShader = Files.readString(Path.of("src/main/resources/assets/spacemod/pinwheel/shaders/program/planet.fsh"));
        String skyboxShader = Files.readString(Path.of("src/main/resources/assets/spacemod/pinwheel/shaders/program/skybox.fsh"));

        assertTrue(source.contains("public static float sunBloomStrength = 1.0f"));
        assertTrue(source.contains("public static float sunGlobalLightingStrength = 1.0f"));
        assertTrue(source.contains("begin\", String.class).invoke(null, \"Sun Settings\")"));
        assertTrue(source.contains("skyLensFlareUniform.setFloat(sunBloomStrength)"));
        assertTrue(source.contains("\"Global Lighting\""));
        assertTrue(source.contains("planetSunLightUniform.setFloat(sunGlobalLightingStrength)"));
        assertTrue(source.contains("skyGlobalLightUniform.setFloat(sunGlobalLightingStrength)"));
        assertTrue(planetShader.contains("uniform float SunLightStrength"));
        assertTrue(skyboxShader.contains("uniform float GlobalLightStrength"));
    }
}
