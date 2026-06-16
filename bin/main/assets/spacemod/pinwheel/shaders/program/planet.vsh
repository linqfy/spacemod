#version 150

in vec3 Position;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord;
out vec3 viewNormal;
out vec3 viewPos;

void main() {
    vec4 vPos = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * vPos;
    texCoord = UV0;
    
    viewPos = vPos.xyz;
    
    // Local position is the normal vector since sphere is generated around (0,0,0)
    vec3 localNormal = normalize(Position);
    // Convert to view space normal
    viewNormal = normalize(mat3(ModelViewMat) * localNormal);
}
