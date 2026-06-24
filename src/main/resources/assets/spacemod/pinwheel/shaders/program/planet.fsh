#version 330 core

in vec2 texCoord;
in vec3 viewNormal;
in vec3 viewPos;

uniform vec3 PlanetColor;
uniform vec3 SunViewPos;

out vec4 fragColor;

void main() {
    vec3 normal = normalize(viewNormal);
    vec3 viewDir = normalize(-viewPos); // View direction is towards the camera (0,0,0 in view space)
    
    // Light is located at world space 0,0,0 (SunViewPos in view space)
    vec3 lightDir = normalize(SunViewPos - viewPos);
    float ndl = dot(normal, lightDir);
    float diff = smoothstep(-0.08, 0.88, ndl);
    
    // Base color with lighting
    vec3 col = PlanetColor * (diff * 0.88 + 0.025);
    
    // Dynamic Rayleigh phase function and atmospheric depth
    float limb = pow(1.0 - max(dot(normal, viewDir), 0.0), 3.0);
    float rayleighPhase = 0.75 * (1.0 + pow(max(dot(viewDir, lightDir), 0.0), 2.0));
    
    // Terminator sunset scattering (Boosted for Bloom)
    float terminator = smoothstep(-0.4, 0.15, ndl) * (1.0 - smoothstep(0.05, 0.55, ndl));
    vec3 sunsetScatter = vec3(1.0, 0.35, 0.1) * terminator * limb * rayleighPhase * 15.0;
    
    // Dynamic blue sky scattering (Boosted for Bloom)
    vec3 blueScatter = vec3(0.2, 0.45, 1.0) * limb * smoothstep(-0.15, 0.6, ndl) * rayleighPhase * 8.0;
    
    // Light Refraction (Boosted for Bloom)
    float refractionMask = pow(max(0.0, dot(normal, viewDir)), 2.0);
    float backLight = pow(max(0.0, dot(-viewDir, lightDir)), 4.0);
    float nightMask = smoothstep(-0.8, -0.1, ndl);
    vec3 refractionColor = vec3(0.8, 0.25, 0.05) * refractionMask * nightMask * backLight * 25.0;

    col += sunsetScatter + blueScatter + refractionColor;

    float nightRim = smoothstep(-0.72, -0.1, ndl) * limb;
    col += vec3(0.02, 0.05, 0.12) * nightRim;
    
    fragColor = vec4(col, 1.0);
}
