package dev.linqfy.spacemod;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.api.client.render.vertex.VertexArray;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class PlanetManager {
    private static final List<Planet> planets = new ArrayList<>();
    
    // LOD VertexArrays
    private static VertexArray lodHigh;
    private static VertexArray lodMedium;
    private static VertexArray lodLow;
    private static VertexArray lodLowest;
    
    private static boolean initialized = false;

    public static List<Planet> getPlanets() {
        return planets;
    }

    public static void initialize() {
        if (initialized) return;

        // Initialize with default planet if empty
        if (planets.isEmpty()) {
            planets.add(new Planet((float) Config.planetX, (float) Config.planetY, (float) Config.planetZ, 
                    (float) Config.planetRadius, (float) Config.planetColorR, (float) Config.planetColorG, (float) Config.planetColorB));
        }

        lodHigh = generateSphereVBO(64, 64);
        lodMedium = generateSphereVBO(32, 32);
        lodLow = generateSphereVBO(16, 16);
        lodLowest = generateSphereVBO(8, 8);

        initialized = true;
    }

    private static VertexArray generateSphereVBO(int rings, int sectors) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float r = 1.0f; // Unit sphere

        for (int ring = 0; ring < rings; ring++) {
            float phi0 = (float) Math.PI * ring / rings;
            float phi1 = (float) Math.PI * (ring + 1) / rings;

            float y0 = r * (float) Math.cos(phi0);
            float y1 = r * (float) Math.cos(phi1);
            float r0 = r * (float) Math.sin(phi0);
            float r1 = r * (float) Math.sin(phi1);

            for (int sector = 0; sector < sectors; sector++) {
                float theta0 = (float) (2 * Math.PI * sector / sectors);
                float theta1 = (float) (2 * Math.PI * (sector + 1) / sectors);

                float x00 = r0 * (float) Math.cos(theta0);
                float z00 = r0 * (float) Math.sin(theta0);
                float x01 = r0 * (float) Math.cos(theta1);
                float z01 = r0 * (float) Math.sin(theta1);

                float x10 = r1 * (float) Math.cos(theta0);
                float z10 = r1 * (float) Math.sin(theta0);
                float x11 = r1 * (float) Math.cos(theta1);
                float z11 = r1 * (float) Math.sin(theta1);

                float u0 = (float) sector / sectors;
                float u1 = (float) (sector + 1) / sectors;
                float v0 = (float) ring / rings;
                float v1 = (float) (ring + 1) / rings;

                // POSITION_TEX format
                buffer.addVertex(x00, y0, z00).setUv(u0, v0);
                buffer.addVertex(x10, y1, z10).setUv(u0, v1);
                buffer.addVertex(x11, y1, z11).setUv(u1, v1);
                buffer.addVertex(x01, y0, z01).setUv(u1, v0);
            }
        }

        VertexArray vertexArray = VertexArray.create();
        vertexArray.upload(buffer.buildOrThrow(), VertexArray.DrawUsage.STATIC);
        return vertexArray;
    }

    public static VertexArray getLODForDistance(float distance, float radius) {
        float ratio = distance / radius;
        if (ratio < 5.0f) {
            return lodHigh;
        } else if (ratio < 15.0f) {
            return lodMedium;
        } else if (ratio < 50.0f) {
            return lodLow;
        } else {
            return lodLowest;
        }
    }

    public static void cleanup() {
        if (!initialized) return;
        lodHigh.free();
        lodMedium.free();
        lodLow.free();
        lodLowest.free();
        initialized = false;
    }
}
