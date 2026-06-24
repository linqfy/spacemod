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
    
    float limb = pow(1.0 - max(dot(normal, viewDir), 0.0), 3.0);
    float terminator = smoothstep(-0.35, 0.18, ndl) * (1.0 - smoothstep(0.08, 0.55, ndl));
    vec3 sunsetScatter = vec3(1.0, 0.34, 0.08) * terminator * limb * 0.75;
    vec3 blueScatter = vec3(0.24, 0.48, 1.0) * limb * smoothstep(-0.1, 0.6, ndl) * 0.32;
    col += sunsetScatter + blueScatter;

    float nightRim = smoothstep(-0.72, -0.1, ndl) * limb;
    col += vec3(0.02, 0.05, 0.12) * nightRim;
    
    fragColor = vec4(col, 1.0);
}
