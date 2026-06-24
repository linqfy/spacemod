package dev.linqfy.spacemod;

public class Planet {
    public float x, y, z;
    public float radius;
    public float colorR, colorG, colorB;
    public float atmosphereRadiusMultiplier;
    public float rayleighStrength;
    public float mieStrength;
    public float mieAnisotropy;
    public float atmosphereExposure;
    public float auroraStrength;
    public float lensFlareStrength;

    public Planet(float x, float y, float z, float radius, float colorR, float colorG, float colorB) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.colorR = colorR;
        this.colorG = colorG;
        this.colorB = colorB;
        this.atmosphereRadiusMultiplier = 1.08f;
        this.rayleighStrength = 18.0f;
        this.mieStrength = 1.2f;
        this.mieAnisotropy = 0.76f;
        this.atmosphereExposure = 1.8f;
        this.auroraStrength = 0.65f;
        this.lensFlareStrength = 1.0f;
    }
}
