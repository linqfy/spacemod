package dev.linqfy.spacemod;

public class Planet {
    public float x, y, z;
    public float radius;
    public float colorR, colorG, colorB;
    public float atmosphereRadiusMultiplier;
    public float atmosphereColorR, atmosphereColorG, atmosphereColorB;
    public float rayleighWavelengthR, rayleighWavelengthG, rayleighWavelengthB;
    public float rayleighStrength;
    public float gasDensity;
    public float mieStrength;
    public float mieAnisotropy;
    public float atmosphereExposure;
    public float auroraStrength;
    public float notLookingBloomIntensity;
    public float lookingEdgeBloomIntensity;
    public float bloomMorphRadius;

    public Planet(float x, float y, float z, float radius, float colorR, float colorG, float colorB) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.colorR = colorR;
        this.colorG = colorG;
        this.colorB = colorB;
        deriveAtmosphereFromColor();
    }

    private void deriveAtmosphereFromColor() {
        this.atmosphereRadiusMultiplier = 1.025f;
        this.rayleighStrength = 7.5f;
        this.mieAnisotropy = 0.82f;
        this.atmosphereExposure = 1.25f;
        this.auroraStrength = 0.0f;
        this.gasDensity = 1.0f;
        this.notLookingBloomIntensity = 0.55f;
        this.lookingEdgeBloomIntensity = 2.4f;
        this.bloomMorphRadius = 0.18f;

        boolean blueDominant = colorB >= colorR && colorB >= colorG;
        if (blueDominant) {
            this.atmosphereColorR = 0.45f;
            this.atmosphereColorG = 0.63f;
            this.atmosphereColorB = 1.0f;
            this.rayleighWavelengthR = 680.0f;
            this.rayleighWavelengthG = 550.0f;
            this.rayleighWavelengthB = 440.0f;
            this.mieStrength = 0.28f;
            return;
        }

        float warmBias = clamp01(colorR * 0.75f + colorG * 0.35f - colorB * 0.45f);
        this.atmosphereColorR = clamp01(0.55f + colorR * 0.42f + warmBias * 0.10f);
        this.atmosphereColorG = clamp01(0.48f + colorG * 0.26f);
        this.atmosphereColorB = clamp01(0.32f + colorB * 0.34f - warmBias * 0.08f);
        this.rayleighWavelengthR = 690.0f;
        this.rayleighWavelengthG = 560.0f;
        this.rayleighWavelengthB = 455.0f + warmBias * 45.0f;
        this.mieStrength = 0.34f + warmBias * 0.34f;
        this.gasDensity = 0.95f + warmBias * 0.20f;
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
