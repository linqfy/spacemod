#version 150

in vec2 texCoord;
in vec3 viewNormal;
in vec3 viewPos;

uniform vec3 PlanetColor;

out vec4 fragColor;

void main() {
    vec3 normal = normalize(viewNormal);
    vec3 viewDir = normalize(-viewPos); // View direction is towards the camera (0,0,0 in view space)
    
    // A directional light in view space
    vec3 lightDir = normalize(vec3(1.0, 1.0, 1.0));
    float diff = max(dot(normal, lightDir), 0.0);
    
    // Base color with lighting (fix later)
    vec3 col = PlanetColor * (diff * 0.8 + 0.2);
    
    // mid ahh fresnel approximation
    float fresnel = pow(1.0 - max(dot(normal, viewDir), 0.0), 3.0);
    col += vec3(0.5, 0.7, 1.0) * fresnel;
    
    fragColor = vec4(col, 1.0);
}
