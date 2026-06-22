#version 330 core

in vec3 Position;

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;

out vec3 vPos;

void main() {
    mat4 mv = ModelViewMat;
    mv[3].xyz = vec3(0.0);
    
    vec4 viewPos = mv * vec4(Position, 1.0);
    vPos = viewPos.xyz;
    
    vec4 pos = ProjMat * viewPos;
    gl_Position = pos.xyww;
}
