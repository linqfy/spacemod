#version 330 core

in vec2 texCoord;
in vec3 viewNormal;
in vec3 viewPos;

uniform vec3 PlanetColor;
uniform vec3 SunViewPos;
uniform float SunLightStrength;

out vec4 fragColor;

void main() {
    vec3 normal = normalize(viewNormal);
    vec3 viewDir = normalize(-viewPos); // View direction is towards the camera (0,0,0 in view space)
    
    // Light is located at world space 0,0,0 (SunViewPos in view space)
    vec3 lightDir = normalize(SunViewPos - viewPos);
    float ndl = dot(normal, lightDir);
    float direct = max(ndl, 0.0);
    float terminatorSoftness = smoothstep(-0.035, 0.045, ndl);
    
    vec3 col = PlanetColor * (direct * terminatorSoftness * 0.92 * SunLightStrength + 0.012);
    
    float limb = pow(1.0 - max(dot(normal, viewDir), 0.0), 3.0);
    float litLimb = limb * smoothstep(-0.08, 0.3, ndl);
    float nightLimb = limb * (1.0 - smoothstep(-0.22, 0.08, ndl));
    col += vec3(0.018, 0.03, 0.045) * litLimb * SunLightStrength;
    col += vec3(0.006, 0.012, 0.025) * nightLimb;
    
    fragColor = vec4(col, 1.0);
}
