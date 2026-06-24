#version 330 core

in vec2 texCoord;
in vec3 localNormal;
in vec3 viewPos;

uniform float Time;

out vec4 fragColor;

const float SOLAR_TEMPERATURE_K = 5778.0;
const float SOLAR_EXPOSURE = 18.0;

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

float fbm(vec3 p) {
    float f = 0.0;
    float amp = 0.5;
    for (int i = 0; i < 4; i++) {
        f += amp * noise(p);
        p = p * 2.03 + vec3(7.1, 3.7, 5.3);
        amp *= 0.5;
    }
    return f;
}

vec3 aces(vec3 x) {
    const float a = 2.51;
    const float b = 0.03;
    const float c = 2.43;
    const float d = 0.59;
    const float e = 0.14;
    return clamp((x * (a * x + b)) / (x * (c * x + d) + e), 0.0, 1.0);
}

vec3 blackbodyColor(float temperatureKelvin) {
    float t = clamp(temperatureKelvin / 100.0, 10.0, 400.0);
    float red;
    float green;
    float blue;

    if (t <= 66.0) {
        red = 1.0;
        green = clamp((99.4708025861 * log(t) - 161.1195681661) / 255.0, 0.0, 1.0);
        blue = t <= 19.0 ? 0.0 : clamp((138.5177312231 * log(t - 10.0) - 305.0447927307) / 255.0, 0.0, 1.0);
    } else {
        red = clamp((329.698727446 * pow(t - 60.0, -0.1332047592)) / 255.0, 0.0, 1.0);
        green = clamp((288.1221695283 * pow(t - 60.0, -0.0755148492)) / 255.0, 0.0, 1.0);
        blue = 1.0;
    }

    return normalize(vec3(red, green, blue));
}

void main() {
    vec3 normal = normalize(localNormal);
    vec3 viewDir = normalize(-viewPos);
    float facing = clamp(dot(normal, viewDir), 0.0, 1.0);

    float granules = fbm(normal * 42.0 + vec3(Time * 0.032, Time * 0.017, 0.0));
    float supergranules = fbm(normal * 9.0 - vec3(0.0, Time * 0.011, Time * 0.014));
    float convection = mix(granules, supergranules, 0.28);
    float cellContrast = (convection - 0.5) * 0.24;

    float localTemperature = SOLAR_TEMPERATURE_K + cellContrast * 1450.0;
    vec3 solarWhite = blackbodyColor(SOLAR_TEMPERATURE_K);
    vec3 color = blackbodyColor(localTemperature);

    float limbDarkening = 0.38 + 0.62 * pow(facing, 0.42);
    float photosphereIntensity = SOLAR_EXPOSURE * limbDarkening * (0.94 + convection * 0.22);
    float faculae = smoothstep(0.70, 0.96, convection) * smoothstep(0.15, 0.82, 1.0 - facing);
    float chromosphereRim = pow(1.0 - facing, 3.2);
    float whiteCore = smoothstep(0.74, 1.0, facing);

    color *= photosphereIntensity;
    color += solarWhite * faculae * SOLAR_EXPOSURE * 0.45;
    color += vec3(1.0, 0.30, 0.035) * chromosphereRim * SOLAR_EXPOSURE * 0.18;
    color = mix(color, vec3(SOLAR_EXPOSURE), whiteCore * 0.35);

    fragColor = vec4(aces(color), 1.0);
}
