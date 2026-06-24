#version 330 core

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec3 localPos;
out vec3 viewPos;

void main() {
    vec4 vPos = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * vPos;
    localPos = Position;
    viewPos = vPos.xyz;
}
