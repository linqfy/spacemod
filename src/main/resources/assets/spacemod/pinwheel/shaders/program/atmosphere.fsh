#version 330 core

in vec3 localPos;
in vec3 viewPos;

uniform vec3 PlanetCenterView;
uniform vec3 SunViewPos;
uniform vec3 AtmosphereColor;
uniform vec3 RayleighWavelengths;
uniform float PlanetRadius;
uniform float AtmosphereRadius;
uniform float RayleighStrength;
uniform float GasDensity;
uniform float MieStrength;
uniform float MieAnisotropy;
uniform float AtmosphereExposure;
uniform float AuroraStrength;
uniform float Time;

out vec4 fragColor;

const float PI = 3.14159265359;
const int VIEW_STEPS = 16;
const int LIGHT_STEPS = 8;

float hash(vec3 p) {
    p = fract(p * vec3(0.1031, 0.1030, 0.0973));
    p += dot(p, p.yxz + 33.33);
    return fract((p.x + p.y) * p.z);
}

float noise(vec3 x) {
    vec3 i = floor(x);
    vec3 f = fract(x);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(mix(hash(i + vec3(0,0,0)), hash(i + vec3(1,0,0)), f.x),
                   mix(hash(i + vec3(0,1,0)), hash(i + vec3(1,1,0)), f.x), f.y),
               mix(mix(hash(i + vec3(0,0,1)), hash(i + vec3(1,0,1)), f.x),
                   mix(hash(i + vec3(0,1,1)), hash(i + vec3(1,1,1)), f.x), f.y), f.z);
}

float raySphere(vec3 origin, vec3 dir, vec3 center, float radius, out float t0, out float t1) {
    vec3 oc = origin - center;
    float b = dot(oc, dir);
    float c = dot(oc, oc) - radius * radius;
    float h = b * b - c;
    if (h < 0.0) {
        t0 = 0.0;
        t1 = 0.0;
        return 0.0;
    }
    h = sqrt(h);
    t0 = -b - h;
    t1 = -b + h;
    return 1.0;
}

vec2 densitiesAt(vec3 p) {
    float atmosphereThickness = max(AtmosphereRadius - PlanetRadius, 0.001);
    float altitude = max(length(p - PlanetCenterView) - PlanetRadius, 0.0);
    float normalizedAltitude = altitude / atmosphereThickness;

    float rayleighDensity = exp(-normalizedAltitude / 0.28);
    float mieDensity = exp(-normalizedAltitude / 0.095);
    return vec2(rayleighDensity, mieDensity);
}

vec3 rayleighFromWavelengths(vec3 wavelengthsNm) {
    vec3 wavelengths = clamp(wavelengthsNm, vec3(380.0), vec3(780.0));
    vec3 earthReference = vec3(680.0, 550.0, 440.0);
    return pow(earthReference / wavelengths, vec3(4.0));
}

float planetShadow(vec3 p, vec3 sunDir) {
    float hit0;
    float hit1;
    float hit = raySphere(p + sunDir * 0.01, sunDir, PlanetCenterView, PlanetRadius, hit0, hit1);
    return hit > 0.5 && hit1 > 0.0 ? 0.0 : 1.0;
}

vec2 opticalDepthToSun(vec3 p, vec3 sunDir) {
    float atmosphereEnter;
    float atmosphereExit;
    raySphere(p, sunDir, PlanetCenterView, AtmosphereRadius, atmosphereEnter, atmosphereExit);
    float stepSize = max(atmosphereExit, 0.0) / float(LIGHT_STEPS);
    vec2 depth = vec2(0.0);

    for (int i = 0; i < LIGHT_STEPS; i++) {
        float t = (float(i) + 0.5) * stepSize;
        vec3 samplePos = p + sunDir * t;
        depth += densitiesAt(samplePos) * (stepSize / AtmosphereRadius);
    }

    return depth;
}

vec3 auroraColor(vec3 samplePos, vec3 sunDir) {
    vec3 local = normalize(samplePos - PlanetCenterView);
    float polarMask = smoothstep(0.58, 0.92, abs(local.y));
    float nightMask = 1.0 - smoothstep(-0.45, 0.15, dot(local, sunDir));
    float curtain = sin(22.0 * atan(local.z, local.x) + noise(local * 9.0 + vec3(0.0, Time * 0.2, 0.0)) * 8.0);
    float bands = smoothstep(0.18, 0.95, curtain * 0.5 + 0.5);
    vec3 green = vec3(0.12, 1.0, 0.42);
    vec3 violet = vec3(0.35, 0.08, 1.0);
    vec3 red = vec3(1.0, 0.18, 0.10);
    vec3 color = mix(green, violet, noise(local * 5.0 + Time * 0.03));
    color = mix(color, red, smoothstep(0.84, 1.0, abs(local.y)) * 0.35);
    return color * polarMask * nightMask * bands * AuroraStrength;
}

void main() {
    vec3 rayOrigin = vec3(0.0);
    vec3 rayDir = normalize(viewPos);

    float atmosphereEnter;
    float atmosphereExit;
    if (raySphere(rayOrigin, rayDir, PlanetCenterView, AtmosphereRadius, atmosphereEnter, atmosphereExit) < 0.5) {
        discard;
    }

    float planetEnter;
    float planetExit;
    if (raySphere(rayOrigin, rayDir, PlanetCenterView, PlanetRadius, planetEnter, planetExit) > 0.5 && planetEnter > 0.0) {
        atmosphereExit = min(atmosphereExit, planetEnter);
    }

    atmosphereEnter = max(atmosphereEnter, 0.0);
    if (atmosphereExit <= atmosphereEnter) {
        discard;
    }

    vec3 sunDir = normalize(SunViewPos - PlanetCenterView);
    vec3 betaRayleigh = vec3(5.802, 13.558, 33.100) * rayleighFromWavelengths(RayleighWavelengths) * 0.00055 * RayleighStrength * GasDensity;
    vec3 betaMie = vec3(0.012) * MieStrength * GasDensity;

    float mu = dot(rayDir, sunDir);
    float rayleighPhase = (3.0 / (16.0 * PI)) * (1.0 + mu * mu);
    float g = clamp(MieAnisotropy, 0.0, 0.98);
    float miePhase = (3.0 / (8.0 * PI)) * ((1.0 - g * g) * (1.0 + mu * mu)) / ((2.0 + g * g) * pow(max(1.0 + g * g - 2.0 * g * mu, 0.001), 1.5));

    float stepSize = (atmosphereExit - atmosphereEnter) / float(VIEW_STEPS);
    float normalizedPathLength = (atmosphereExit - atmosphereEnter) / max(AtmosphereRadius, 0.001);
    float horizonBoost = smoothstep(0.035, 0.28, normalizedPathLength);
    float cameraBoost = horizonBoost;
    vec2 viewDepth = vec2(0.0);
    vec3 scattered = vec3(0.0);
    float alphaDepth = 0.0;

    for (int i = 0; i < VIEW_STEPS; i++) {
        float t = atmosphereEnter + (float(i) + 0.5) * stepSize;
        vec3 samplePos = rayOrigin + rayDir * t;
        vec2 density = densitiesAt(samplePos);
        float segment = (stepSize / AtmosphereRadius) * mix(0.72, 1.55, cameraBoost);
        viewDepth += density * segment;

        float sunlight = planetShadow(samplePos, sunDir);
        vec2 lightDepth = opticalDepthToSun(samplePos, sunDir);
        vec3 extinction = exp(-(betaRayleigh * (viewDepth.x + lightDepth.x) + betaMie * (viewDepth.y + lightDepth.y)));

        vec3 rayleigh = betaRayleigh * density.x * rayleighPhase * AtmosphereColor;
        vec3 mie = betaMie * density.y * miePhase * mix(vec3(1.0), AtmosphereColor, 0.35);
        scattered += (rayleigh + mie) * extinction * sunlight * segment;
        scattered += auroraColor(samplePos, sunDir) * density.x * segment * 0.08;
        alphaDepth += (density.x * 0.045 + density.y * 0.08) * segment;
    }

    vec3 color = vec3(1.0) - exp(-scattered * AtmosphereExposure);
    float alpha = clamp(alphaDepth * AtmosphereExposure * 1.75 + max(max(color.r, color.g), color.b) * 0.58, 0.0, 0.68);
    fragColor = vec4(color, alpha);
}
