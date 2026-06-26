#version 330 core

in vec3 vPos;
out vec4 fragColor;

uniform float Time;
uniform mat4 ModelViewMat;
uniform vec3 ScreenSize;
uniform vec3 SunDirection;
uniform vec3 SunScreenPos;
uniform vec3 CameraForwardDirection;
uniform float LensFlareStrength;
uniform float GlobalLightStrength;
uniform vec3 PlanetAtmosphereDirection;
uniform vec3 PlanetAtmosphereColor;
uniform float PlanetSurfaceAngularRadius;
uniform float PlanetAtmosphereAngularRadius;
uniform float PlanetAtmosphereIntensity;
uniform float PlanetAtmosphereNotLookingIntensity;
uniform float PlanetAtmosphereLookingEdgeIntensity;
uniform float PlanetAtmosphereMorphRadius;

float hash(vec3 p) {
    p = fract(p * vec3(0.1031, 0.1030, 0.0973));
    p += dot(p, p.yxz + 33.33);
    return fract((p.x + p.y) * p.z);
}

vec4 hash4(vec3 p) {
    vec4 p4 = fract(p.xyzx * vec4(0.1031, 0.1030, 0.0973, 0.1099));
    p4 += dot(p4, p4.wzxy + 33.33);
    return fract((p4.xxyz + p4.yzzw) * p4.zywx);
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

// standard 5 octave FBM for detailed structure
float fbm(vec3 p) {
    float f = 0.0;
    float amp = 0.5;
    mat3 rot = mat3(
        0.00,  0.80,  0.60,
       -0.80,  0.36, -0.48,
       -0.60, -0.48,  0.64
    );
    for(int i = 0; i < 5; i++) {
        f += amp * noise(p);
        p = rot * p * 2.01;
        amp *= 0.5;
    }
    return f;
}

float fbm3(vec3 p) {
    float f = 0.0;
    float amp = 0.5;
    mat3 rot = mat3(
        0.00,  0.80,  0.60,
       -0.80,  0.36, -0.48,
       -0.60, -0.48,  0.64
    );
    for(int i = 0; i < 3; i++) {
        f += amp * noise(p);
        p = rot * p * 2.01;
        amp *= 0.5;
    }
    return f;
}

// ACES
vec3 aces(vec3 x) {
    const float a = 2.51;
    const float b = 0.03;
    const float c = 2.43;
    const float d = 0.59;
    const float e = 0.14;
    return clamp((x * (a * x + b)) / (x * (c * x + d) + e), 0.0, 1.0);
}

// Stars
vec3 drawStars(vec3 dir, float scale, float twinkleSpeed) {
    vec3 p = dir * scale;
    vec3 ip = floor(p);
    vec3 fp = fract(p);

    vec3 start = step(0.5, fp) - vec3(1.0);
    vec3 color = vec3(0.0);
    
    // this line was like 2hrs of my life </3
    float edge = length(fwidth(dir)) * scale;
    
    for (int x = 0; x <= 1; x++) {
        for (int y = 0; y <= 1; y++) {
            for (int z = 0; z <= 1; z++) {
                vec3 offset = start + vec3(float(x), float(y), float(z));
                vec3 cell = ip + offset;
                vec4 h = hash4(cell);
                
                // only 25% ish of cells contain a star
                float starMask = step(0.75, h.x);
                
                // jitter star position within cell
                vec3 starPos = cell + h.yzw;
                
                // dist from ray to star
                float t = dot(starPos, dir);
                float tMask = step(0.0, t);
                vec3 closestPoint = dir * t;
                float d = length(starPos - closestPoint);
                
                float twinkle = 0.4 + 0.6 * sin(Time * twinkleSpeed + h.x * 62.8);
                
                float size = 0.006 + 0.024 * pow(h.y, 4.0);
                
                // Analytic anti-aliasing to prevent sub-pixel flickering
                float radius = max(size, edge);
                float intensity = size / radius;
                float brightness = (1.0 - smoothstep(0.0, radius, d)) * intensity;
                
                vec3 starColor = mix(vec3(0.7, 0.85, 1.0), vec3(1.0, 0.85, 0.65), h.z);
                
                vec3 rareColor = mix(vec3(0.3, 0.6, 1.0), vec3(1.0, 0.4, 0.4), h.w);
                starColor = mix(starColor, rareColor, step(0.96, h.x));
                
                color += starColor * brightness * twinkle * starMask * tMask * (0.5 + 2.0 * h.w);
            }
        }
    }
    return color;
}

vec3 drawInterplanetaryScatter(vec3 dir, vec3 sunDir) {
    float forward = max(dot(dir, sunDir), 0.0);
    float backscatter = max(dot(dir, -sunDir), 0.0);
    float dustHalo = pow(forward, 12.0) * 0.12 + pow(forward, 3.2) * 0.035;
    float gegenschein = pow(backscatter, 24.0) * 0.018;
    return vec3(1.0, 0.72, 0.42) * dustHalo + vec3(0.55, 0.68, 1.0) * gegenschein;
}

vec3 drawZodiacalLight(vec3 dir, vec3 sunDir) {
    float eclipticBand = exp(-pow(abs(dir.y) * 5.2, 2.0));
    float nearSun = pow(max(dot(dir, sunDir), 0.0), 2.5);
    float oppositeSun = pow(max(dot(dir, -sunDir), 0.0), 18.0);
    vec3 dustColor = vec3(0.9, 0.68, 0.42);
    return dustColor * eclipticBand * (0.035 + nearSun * 0.14 + oppositeSun * 0.05);
}

vec3 drawAuroraSky(vec3 dir, vec3 sunDir) {
    float polarMask = smoothstep(0.62, 0.96, abs(dir.y));
    float nightMask = 1.0 - smoothstep(-0.5, 0.05, dot(dir, sunDir));
    float curtainNoise = fbm3(vec3(atan(dir.z, dir.x) * 2.0, dir.y * 8.0, Time * 0.08));
    float curtains = smoothstep(0.52, 0.82, curtainNoise + sin(atan(dir.z, dir.x) * 16.0 + Time * 0.35) * 0.12);
    vec3 green = vec3(0.04, 0.8, 0.28);
    vec3 violet = vec3(0.32, 0.08, 0.9);
    vec3 red = vec3(0.8, 0.08, 0.05);
    vec3 color = mix(green, violet, smoothstep(0.65, 0.95, curtainNoise));
    color = mix(color, red, smoothstep(0.9, 1.0, abs(dir.y)) * 0.3);
    return color * polarMask * nightMask * curtains * 0.16;
}

vec3 drawLensFlare(vec2 uv, vec2 sunUv, float visible, vec3 dir, vec3 sunDir) {
    float visibilityGate = step(0.5, visible);
    float onscreen = step(-0.25, sunUv.x) * step(sunUv.x, 1.25) * step(-0.25, sunUv.y) * step(sunUv.y, 1.25);
    vec2 center = vec2(0.5);
    vec2 axis = center - sunUv;
    vec3 lensColor = vec3(0.0);
    vec3 ambientColor = vec3(0.0);

    vec2 aspectUv = (uv - sunUv) * vec2(ScreenSize.x / max(ScreenSize.y, 1.0), 1.0);
    
    // Calculate 3D angle distance for stable, omnidirectional massive bloom
    float dotSun = dot(dir, sunDir);
    float angleDist = acos(clamp(dotSun, -1.0, 1.0));

    // Massive Sun Bloom (3D)
    float massiveBloom = exp(-angleDist * 2.5) * 2.0 + exp(-angleDist * 6.0) * 4.0 + exp(-angleDist * 15.0) * 8.0;
    vec3 bloomColor = vec3(1.0, 0.9, 0.7) * massiveBloom;

    // Dramatic Rayleigh & Mie Bloom (3D)
    float rayleighDist = max(angleDist, 0.001);
    vec3 rayleighBloom = vec3(0.1, 0.3, 1.0) * exp(-rayleighDist * 1.8) * 3.5;
    vec3 mieSunsetBloom = vec3(1.0, 0.35, 0.05) * exp(-rayleighDist * 4.5) * 4.0;

    ambientColor += (bloomColor + rayleighBloom + mieSunsetBloom) * (0.15 + 0.02 * sin(Time * 2.0));

    // Dynamic rotating starburst (Screen space)
    float angle = atan(aspectUv.y, aspectUv.x);
    float starburst = sin(angle * 8.0 + Time * 0.5) * sin(angle * 13.0 - Time * 0.3) * 0.5 + 0.5;
    lensColor += vec3(1.0, 0.8, 0.5) * massiveBloom * starburst * 0.05;

    // Ghosts (Screen space)
    for (int i = 0; i < 5; i++) {
        float fi = float(i);
        vec2 ghostOffset = vec2(sin(Time * 0.4 + fi), cos(Time * 0.4 + fi)) * 0.015;
        vec2 ghostPos = sunUv + axis * (0.42 + fi * 0.28) + ghostOffset;
        float ghostDist = length((uv - ghostPos) * vec2(ScreenSize.x / max(ScreenSize.y, 1.0), 1.0));
        float ghost = 1.0 - smoothstep(0.0, 0.11 + fi * 0.012, ghostDist);
        vec3 ghostColor = mix(vec3(0.2, 0.55, 1.0), vec3(1.0, 0.25, 0.08), fract(fi * 0.37));
        float pulse = 1.0 + 0.2 * sin(Time * 1.5 + fi * 2.0);
        lensColor += ghostColor * ghost * (0.08 / (1.0 + fi * 0.28)) * pulse;
    }

    // Dynamic anamorphic streak with Chromatic Aberration (Screen space)
    float streakY = abs(aspectUv.y);
    float streakX = abs(aspectUv.x);
    float streakPulse = 1.0 + 0.15 * sin(Time * 3.0);
    
    float anamorphicR = exp(-(streakY + 0.002) * 180.0 * streakPulse) * (1.0 - smoothstep(0.0, 0.75, streakX));
    float anamorphicG = exp(-streakY * 180.0 * streakPulse) * (1.0 - smoothstep(0.0, 0.75, streakX));
    float anamorphicB = exp(-(streakY - 0.002) * 180.0 * streakPulse) * (1.0 - smoothstep(0.0, 0.75, streakX));
    
    vec3 anamorphicColor = vec3(anamorphicR, anamorphicG, anamorphicB) * vec3(0.6, 0.75, 1.0);
    lensColor += anamorphicColor * (0.25 + 0.05 * sin(Time * 2.5));
    lensColor *= onscreen;

    return (ambientColor + lensColor) * LensFlareStrength * visibilityGate;
}

vec3 drawPlanetAtmosphereBloom(vec3 dir, vec3 sunDir) {
    if (PlanetAtmosphereAngularRadius <= 0.0001 || PlanetAtmosphereIntensity <= 0.0) {
        return vec3(0.0);
    }

    vec3 planetDir = normalize(PlanetAtmosphereDirection);
    vec3 cameraForward = normalize(CameraForwardDirection);
    float planetDot = dot(dir, planetDir);
    float angleDist = acos(clamp(planetDot, -1.0, 1.0));
    float surfaceRadius = max(PlanetSurfaceAngularRadius, 0.0001);
    float atmosphereRadius = max(PlanetAtmosphereAngularRadius, surfaceRadius + 0.0001);
    float shellThickness = max(atmosphereRadius - surfaceRadius, surfaceRadius * 0.015);
    float morphRadius = max(PlanetAtmosphereMorphRadius, 0.01);
    vec3 rawCameraTangent = cameraForward - planetDir * dot(cameraForward, planetDir);
    float tangentLength = length(rawCameraTangent);
    float edgeTargetStrength = smoothstep(surfaceRadius * 0.18, surfaceRadius * 0.95, tangentLength);
    vec3 fallbackAxis = abs(planetDir.y) < 0.95 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0);
    vec3 cameraTangent = tangentLength > 0.0001 ? normalize(rawCameraTangent) : normalize(cross(planetDir, fallbackAxis));
    vec3 rawSampleTangent = dir - planetDir * planetDot;
    vec3 sampleTangent = length(rawSampleTangent) > 0.0001 ? normalize(rawSampleTangent) : cameraTangent;
    float shellImpact = clamp((angleDist - surfaceRadius) / shellThickness, -1.0, 3.0);
    float lookedEdge = clamp(dot(sampleTangent, cameraTangent) * 0.5 + 0.5, 0.0, 1.0);
    float edgeExtension = (PlanetAtmosphereLookingEdgeIntensity * lookedEdge - PlanetAtmosphereNotLookingIntensity * (1.0 - lookedEdge) * 0.35) * edgeTargetStrength;
    float extensionAmount = shellThickness * morphRadius * 0.8;
    float deformedSurfaceRadius = max(surfaceRadius + edgeExtension * extensionAmount, surfaceRadius * 0.82);
    float deformedAtmosphereRadius = max(atmosphereRadius + edgeExtension * extensionAmount, deformedSurfaceRadius + shellThickness);
    float deformedShellImpact = (angleDist - deformedSurfaceRadius) / shellThickness;
    float shapedShellImpact = clamp(deformedShellImpact, -1.0, 4.25);
    float baseShellImpact = clamp((angleDist - surfaceRadius) / shellThickness, -1.0, 4.25);
    float diskPosition = clamp(angleDist / surfaceRadius, 0.0, 1.15);
    float normalBlend = smoothstep(0.08, 1.0, diskPosition);
    vec3 atmosphereNormal = normalize(mix(-planetDir, sampleTangent, normalBlend));
    float horizonWidth = max(shellThickness * (0.75 + morphRadius * 1.15), 0.0015);
    float outerWidth = max(shellThickness * (2.4 + morphRadius * 4.5), 0.003);
    float broadWidth = max(shellThickness * (5.0 + morphRadius * 10.0), 0.006);
    float horizonOpticalDepth = exp(-abs(angleDist - surfaceRadius) / horizonWidth);
    float edgeOpticalDepth = horizonOpticalDepth + exp(-abs(angleDist - deformedSurfaceRadius) / horizonWidth) * edgeTargetStrength * lookedEdge;
    float outsideHalo = exp(-max(angleDist - atmosphereRadius, 0.0) / outerWidth);
    float edgeOutsideHalo = exp(-max(angleDist - deformedAtmosphereRadius, 0.0) / outerWidth) * edgeTargetStrength * lookedEdge;
    float broadBloom = exp(-max(angleDist - surfaceRadius, 0.0) / broadWidth) * smoothstep(-0.35, 1.2, baseShellImpact);
    float innerOcclusion = smoothstep(surfaceRadius * 0.94, surfaceRadius + shellThickness * 0.55, angleDist);
    float shellMask = smoothstep(-0.85, 0.05, baseShellImpact) * (1.0 - smoothstep(3.2 + morphRadius * 1.5, 4.25, baseShellImpact));
    float edgeMask = smoothstep(-0.85, 0.05, shapedShellImpact) * (1.0 - smoothstep(3.2 + morphRadius * 1.5, 4.25, shapedShellImpact));
    float cameraFacingEdge = mix(0.5, smoothstep(0.15, 0.95, lookedEdge), edgeTargetStrength);
    float sunlitRim = smoothstep(-0.05, 0.35, dot(atmosphereNormal, sunDir));
    float terminatorGlow = exp(-abs(dot(atmosphereNormal, sunDir)) / 0.18) * 0.35;
    float daylightVisibility = mix(0.015, 1.0, max(sunlitRim, terminatorGlow));
    float mu = dot(dir, sunDir);
    float rayleighPhase = 0.75 * (1.0 + mu * mu);
    float mieG = 0.76;
    float mieDenom = max(1.0 + mieG * mieG - 2.0 * mieG * mu, 0.05);
    float miePhase = (1.0 - mieG * mieG) / pow(mieDenom, 1.5) * 0.08;
    float bloomModeIntensity = mix(PlanetAtmosphereNotLookingIntensity, PlanetAtmosphereLookingEdgeIntensity, cameraFacingEdge);

    vec3 rayleigh = PlanetAtmosphereColor * (horizonOpticalDepth * 1.05 * shellMask + edgeOpticalDepth * 0.42 * edgeMask + broadBloom * 0.42 + outsideHalo * 0.18 + edgeOutsideHalo * 0.08) * innerOcclusion * rayleighPhase;
    vec3 mie = mix(vec3(1.0, 0.86, 0.65), PlanetAtmosphereColor, 0.25) * miePhase * (outsideHalo * 0.58 + edgeOutsideHalo * 0.18 + broadBloom * 0.32) * innerOcclusion;
    vec3 visibleBloom = rayleigh + mie;
    return visibleBloom * PlanetAtmosphereIntensity * bloomModeIntensity * daylightVisibility * GlobalLightStrength;
}

void main() {
    mat4 mv = ModelViewMat;
    mv[3].xyz = vec3(0.0);
    vec3 viewDir = normalize(vPos);
    vec3 dir = transpose(mat3(mv)) * viewDir;
    vec3 sunDir = normalize(SunDirection);
    
    vec3 color = vec3(0.005, 0.005, 0.01);
    
    color += drawStars(dir, 120.0, 1.2);      
    color += drawStars(dir, 50.0, 0.6) * 1.5; 
    
    vec3 nebP = dir * 2.0;
    nebP += fbm3(dir * 2.5 + Time * 0.005) * 1.5;
    
    float n1 = fbm(nebP + vec3(0.0, Time * 0.002, 0.0));
    float n2 = fbm(nebP * 2.0 - vec3(Time * 0.003, 0.0, 0.0));

    float density = smoothstep(0.4, 0.9, n1);
    float structure = smoothstep(0.2, 0.8, n2);

    vec3 nebColor1 = vec3(0.02, 0.008, 0.05); // purple
    vec3 nebColor2 = vec3(0.0, 0.03, 0.04);   // teal
    vec3 nebColor3 = vec3(0.03, 0.008, 0.02); // burgundy
    
    vec3 finalNebColor = mix(nebColor1, nebColor2, structure);
    finalNebColor = mix(finalNebColor, nebColor3, density * structure);
    
    finalNebColor += vec3(0.04, 0.02, 0.06) * pow(density, 3.0);
    
    color += finalNebColor * density * 0.25;
    
    float dust = smoothstep(0.3, 0.7, fbm3(dir * 3.0 - Time * 0.001));
    color *= mix(1.0, 0.7, dust * density);

    color += drawZodiacalLight(dir, sunDir) * GlobalLightStrength;
    color += drawAuroraSky(dir, sunDir);
    color += drawInterplanetaryScatter(dir, sunDir) * GlobalLightStrength;
    color += drawPlanetAtmosphereBloom(dir, sunDir);
    color += drawLensFlare(gl_FragCoord.xy / max(ScreenSize.xy, vec2(1.0)), SunScreenPos.xy, SunScreenPos.z, dir, sunDir);

    color = aces(color);
    
    fragColor = vec4(color, 1.0);
}
