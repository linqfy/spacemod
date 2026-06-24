#version 330 core

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord;
out vec3 localNormal;
out vec3 viewPos;

void main() {
    vec4 vPos = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * vPos;
    texCoord = UV0;
    localNormal = normalize(Position);
    viewPos = vPos.xyz;
}
