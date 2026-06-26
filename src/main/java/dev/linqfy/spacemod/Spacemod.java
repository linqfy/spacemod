package dev.linqfy.spacemod;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(Spacemod.MODID)
public class Spacemod {
    public static final String MODID = "spacemod";
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);

    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder().alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.spacemod")).withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> EXAMPLE_ITEM.get().getDefaultInstance()).displayItems((parameters, output) -> {
        output.accept(EXAMPLE_ITEM.get());
    }).build());

    public Spacemod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) event.accept(EXAMPLE_BLOCK_ITEM);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }

        @SubscribeEvent
        public static void registerDimensionEffects(net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent event) {
            event.register(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MODID, "space"),
                new net.minecraft.client.renderer.DimensionSpecialEffects(Float.NaN, false, net.minecraft.client.renderer.DimensionSpecialEffects.SkyType.NONE, false, false) {
                    @Override
                    public net.minecraft.world.phys.Vec3 getBrightnessDependentFogColor(net.minecraft.world.phys.Vec3 color, float sunHeight) {
                        return color;
                    }
                    @Override
                    public boolean isFoggyAt(int x, int y) {
                        return false;
                    }
                });
        }
    }

    public static final net.minecraft.resources.ResourceLocation PLANET_SHADER = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MODID, "planet");
    public static final net.minecraft.resources.ResourceLocation SKYBOX_SHADER = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MODID, "skybox");
    public static final net.minecraft.resources.ResourceLocation SUN_SHADER = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MODID, "sun");
    public static final float SUN_RADIUS_BLOCKS = 200.0f;
    public static float sunBloomStrength = 1.0f;
    public static float sunGlobalLightingStrength = 1.0f;

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class GameClientEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
            
            try {
                Class eventClass = Class.forName("foundry.imgui.neoforge.api.event.RenderImGuiEventsNeoforge$Post");
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                    net.neoforged.bus.api.EventPriority.NORMAL,
                    false,
                    eventClass,
                    (java.util.function.Consumer<net.neoforged.bus.api.Event>) (e) -> {
                        try {
                            Class<?> imGuiClass = Class.forName("imgui.ImGui");
                            imGuiClass.getMethod("begin", String.class).invoke(null, "Sun Settings");
                            float[] sunBloom = {sunBloomStrength};
                            if ((Boolean) imGuiClass.getMethod("dragFloat", String.class, float[].class).invoke(null, "Uniform Bloom", sunBloom)) {
                                sunBloomStrength = Math.max(0.0f, sunBloom[0]);
                            }
                            float[] globalLighting = {sunGlobalLightingStrength};
                            if ((Boolean) imGuiClass.getMethod("dragFloat", String.class, float[].class).invoke(null, "Global Lighting", globalLighting)) {
                                sunGlobalLightingStrength = Math.max(0.0f, globalLighting[0]);
                            }
                            imGuiClass.getMethod("end").invoke(null);

                            imGuiClass.getMethod("begin", String.class).invoke(null, "Planet Settings");

                            java.util.List<Planet> planets = PlanetManager.getPlanets();
                            for (int i = 0; i < planets.size(); i++) {
                                Planet p = planets.get(i);
                                imGuiClass.getMethod("pushID", int.class).invoke(null, i);
                                
                                if ((Boolean) imGuiClass.getMethod("collapsingHeader", String.class).invoke(null, "Planet " + (i + 1))) {
                                    float[] pos = {p.x, p.y, p.z};
                                    if ((Boolean) imGuiClass.getMethod("dragFloat3", String.class, float[].class).invoke(null, "Position", pos)) {
                                        p.x = pos[0]; p.y = pos[1]; p.z = pos[2];
                                    }
                                    
                                    float[] radius = {p.radius};
                                    if ((Boolean) imGuiClass.getMethod("dragFloat", String.class, float[].class).invoke(null, "Radius", radius)) {
                                        p.radius = Math.max(0.1f, radius[0]);
                                    }
                                    
                                    float[] color = {p.colorR, p.colorG, p.colorB};
                                    if ((Boolean) imGuiClass.getMethod("colorEdit3", String.class, float[].class).invoke(null, "Color", color)) {
                                        p.colorR = color[0]; p.colorG = color[1]; p.colorB = color[2];
                                    }

                                    float[] atmosphereColor = {p.atmosphereColorR, p.atmosphereColorG, p.atmosphereColorB};
                                    if ((Boolean) imGuiClass.getMethod("colorEdit3", String.class, float[].class).invoke(null, "Atmosphere Color", atmosphereColor)) {
                                        p.atmosphereColorR = atmosphereColor[0]; p.atmosphereColorG = atmosphereColor[1]; p.atmosphereColorB = atmosphereColor[2];
                                    }

                                    float[] atmosphereRadius = {p.atmosphereRadiusMultiplier};
                                    if ((Boolean) imGuiClass.getMethod("dragFloat", String.class, float[].class).invoke(null, "Atmosphere Radius", atmosphereRadius)) {
                                        p.atmosphereRadiusMultiplier = Math.max(1.001f, atmosphereRadius[0]);
                                    }

                                    float[] rayleigh = {p.rayleighStrength};
                                    if ((Boolean) imGuiClass.getMethod("dragFloat", String.class, float[].class).invoke(null, "Rayleigh Strength", rayleigh)) {
                                        p.rayleighStrength = Math.max(0.0f, rayleigh[0]);
                                    }

                                    float[] gasDensity = {p.gasDensity};
                                    if ((Boolean) imGuiClass.getMethod("dragFloat", String.class, float[].class).invoke(null, "Gas Density", gasDensity)) {
                                        p.gasDensity = Math.max(0.0f, gasDensity[0]);
                                    }

                                    float[] wavelengths = {p.rayleighWavelengthR, p.rayleighWavelengthG, p.rayleighWavelengthB};
                                    if ((Boolean) imGuiClass.getMethod("dragFloat3", String.class, float[].class).invoke(null, "Rayleigh Wavelengths", wavelengths)) {
                                        p.rayleighWavelengthR = Math.max(380.0f, Math.min(780.0f, wavelengths[0]));
                                        p.rayleighWavelengthG = Math.max(380.0f, Math.min(780.0f, wavelengths[1]));
                                        p.rayleighWavelengthB = Math.max(380.0f, Math.min(780.0f, wavelengths[2]));
                                    }

                                    float[] mie = {p.mieStrength};
                                    if ((Boolean) imGuiClass.getMethod("dragFloat", String.class, float[].class).invoke(null, "Mie Haze", mie)) {
                                        p.mieStrength = Math.max(0.0f, mie[0]);
                                    }

                                    float[] mieAnisotropy = {p.mieAnisotropy};
                                    if ((Boolean) imGuiClass.getMethod("dragFloat", String.class, float[].class).invoke(null, "Mie Directionality", mieAnisotropy)) {
                                        p.mieAnisotropy = Math.max(0.0f, Math.min(0.98f, mieAnisotropy[0]));
                                    }

                                    float[] atmosphereExposure = {p.atmosphereExposure};
                                    if ((Boolean) imGuiClass.getMethod("dragFloat", String.class, float[].class).invoke(null, "Atmosphere Exposure", atmosphereExposure)) {
                                        p.atmosphereExposure = Math.max(0.0f, atmosphereExposure[0]);
                                    }

                                    float[] notLookingBloom = {p.notLookingBloomIntensity};
                                    if ((Boolean) imGuiClass.getMethod("dragFloat", String.class, float[].class).invoke(null, "Not-Looking Bloom", notLookingBloom)) {
                                        p.notLookingBloomIntensity = Math.max(0.0f, notLookingBloom[0]);
                                    }

                                    float[] lookingEdgeBloom = {p.lookingEdgeBloomIntensity};
                                    if ((Boolean) imGuiClass.getMethod("dragFloat", String.class, float[].class).invoke(null, "Looking Edge Bloom", lookingEdgeBloom)) {
                                        p.lookingEdgeBloomIntensity = Math.max(0.0f, lookingEdgeBloom[0]);
                                    }

                                    float[] bloomMorphRadius = {p.bloomMorphRadius};
                                    if ((Boolean) imGuiClass.getMethod("dragFloat", String.class, float[].class).invoke(null, "Bloom Morph Radius", bloomMorphRadius)) {
                                        p.bloomMorphRadius = Math.max(0.01f, bloomMorphRadius[0]);
                                    }

                                    float[] aurora = {p.auroraStrength};
                                    if ((Boolean) imGuiClass.getMethod("dragFloat", String.class, float[].class).invoke(null, "Aurora Strength", aurora)) {
                                        p.auroraStrength = Math.max(0.0f, aurora[0]);
                                    }
                                    
                                    if ((Boolean) imGuiClass.getMethod("button", String.class).invoke(null, "Remove Planet")) {
                                        planets.remove(i);
                                        imGuiClass.getMethod("popID").invoke(null);
                                        break; // avoid concurrent modification
                                    }
                                }
                                
                                imGuiClass.getMethod("popID").invoke(null);
                            }
                            
                            if ((Boolean) imGuiClass.getMethod("button", String.class).invoke(null, "Add Planet")) {
                                planets.add(new Planet(0, 100, 0, 20, 1.0f, 1.0f, 1.0f));
                            }
                            
                            imGuiClass.getMethod("end").invoke(null);
                        } catch (Exception ex) {
                        }
                    }
                );
            } catch (Exception e) {
                LOGGER.error("Failed to register ImGui event", e);
            }
        }

        @SubscribeEvent
        public static void onRenderLevelStage(net.neoforged.neoforge.client.event.RenderLevelStageEvent event) {
            if (event.getStage() == net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_SKY) {
                var ms = event.getPoseStack();
                var cam = event.getCamera();

                PlanetManager.initialize();

                foundry.veil.api.client.render.shader.program.ShaderProgram shader = foundry.veil.api.client.render.VeilRenderSystem.setShader(Spacemod.PLANET_SHADER);
                if (shader == null) {
                    return;
                }

                shader.bind();

                foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess projUniform = shader.getUniform("ProjMat");
                if (projUniform != null) projUniform.setMatrix(event.getProjectionMatrix());

                org.joml.Quaternionf camRot = new org.joml.Quaternionf(cam.rotation());
                camRot.conjugate();
                org.joml.Matrix4f viewMat = new org.joml.Matrix4f().rotation(camRot);
                org.joml.Vector3f cameraForwardDirection = new org.joml.Vector3f(0.0f, 0.0f, -1.0f);
                new org.joml.Quaternionf(cam.rotation()).transform(cameraForwardDirection);
                cameraForwardDirection.normalize();
                org.joml.Vector4f sunView = new org.joml.Vector4f((float) -cam.getPosition().x, (float) -cam.getPosition().y, (float) -cam.getPosition().z, 1.0f);
                viewMat.transform(sunView);

                org.joml.Vector4f sunClip = new org.joml.Vector4f(sunView.x, sunView.y, sunView.z, 1.0f);
                event.getProjectionMatrix().transform(sunClip);
                float sunScreenX = 0.5f;
                float sunScreenY = 0.5f;
                float sunVisible = 0.0f;
                if (Math.abs(sunClip.w) > 0.0001f) {
                    float invW = 1.0f / sunClip.w;
                    sunScreenX = sunClip.x * invW * 0.5f + 0.5f;
                    sunScreenY = sunClip.y * invW * 0.5f + 0.5f;
                    sunVisible = sunClip.w > 0.0f ? 1.0f : 0.0f;
                }

                org.joml.Vector3f sunWorldDirection = new org.joml.Vector3f((float) -cam.getPosition().x, (float) -cam.getPosition().y, (float) -cam.getPosition().z);
                if (sunWorldDirection.lengthSquared() < 0.0001f) {
                    sunWorldDirection.set(0.0f, 1.0f, 0.0f);
                } else {
                    sunWorldDirection.normalize();
                }
                java.util.List<Planet> planets = PlanetManager.getPlanets();
                org.joml.Vector3f planetAtmosphereDirection = new org.joml.Vector3f(0.0f, 1.0f, 0.0f);
                float planetAtmosphereR = 0.45f;
                float planetAtmosphereG = 0.63f;
                float planetAtmosphereB = 1.0f;
                float planetSurfaceAngularRadius = 0.0f;
                float planetAtmosphereAngularRadius = 0.0f;
                float planetAtmosphereIntensity = 0.0f;
                float planetAtmosphereNotLookingIntensity = 0.0f;
                float planetAtmosphereLookingEdgeIntensity = 0.0f;
                float planetAtmosphereMorphRadius = 0.18f;
                float nearestPlanetDistance = Float.MAX_VALUE;
                for (Planet planet : planets) {
                    float px = planet.x - (float) cam.getPosition().x;
                    float py = planet.y - (float) cam.getPosition().y;
                    float pz = planet.z - (float) cam.getPosition().z;
                    float planetDistance = (float) Math.sqrt(px * px + py * py + pz * pz);
                    if (planetDistance > 0.001f && planetDistance < nearestPlanetDistance) {
                        nearestPlanetDistance = planetDistance;
                        planetAtmosphereDirection.set(px / planetDistance, py / planetDistance, pz / planetDistance);
                        planetAtmosphereR = planet.atmosphereColorR;
                        planetAtmosphereG = planet.atmosphereColorG;
                        planetAtmosphereB = planet.atmosphereColorB;
                        float surfaceAngularInput = Math.max(0.0f, Math.min(0.95f, planet.radius / planetDistance));
                        planetSurfaceAngularRadius = (float) Math.asin(surfaceAngularInput);
                        float atmosphereRadius = planet.radius * Math.max(1.001f, planet.atmosphereRadiusMultiplier);
                        float angularInput = Math.max(0.0f, Math.min(0.95f, atmosphereRadius / planetDistance));
                        planetAtmosphereAngularRadius = (float) Math.asin(angularInput);
                        planetAtmosphereIntensity = Math.min(2.5f, planet.atmosphereExposure * planet.gasDensity * (0.45f + planet.rayleighStrength * 0.035f));
                        planetAtmosphereNotLookingIntensity = planet.notLookingBloomIntensity;
                        planetAtmosphereLookingEdgeIntensity = planet.lookingEdgeBloomIntensity;
                        planetAtmosphereMorphRadius = planet.bloomMorphRadius;
                    }
                }
                com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
                com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
                com.mojang.blaze3d.systems.RenderSystem.disableCull();
                com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();

                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.level != null && mc.level.dimension().location().equals(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MODID, "space"))) {
                    foundry.veil.api.client.render.shader.program.ShaderProgram skyboxShader = foundry.veil.api.client.render.VeilRenderSystem.setShader(Spacemod.SKYBOX_SHADER);
                    if (skyboxShader != null) {
                        skyboxShader.bind();
                        foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess skyProjUniform = skyboxShader.getUniform("ProjMat");
                        if (skyProjUniform != null) skyProjUniform.setMatrix(event.getProjectionMatrix());
                        
                        org.joml.Matrix4f skyMv = new org.joml.Matrix4f();
                        org.joml.Quaternionf skyCamRot = new org.joml.Quaternionf(cam.rotation());
                        skyCamRot.conjugate();
                        skyMv.rotation(skyCamRot);
                        
                        foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess skyMvUniform = skyboxShader.getUniform("ModelViewMat");
                        if (skyMvUniform != null) skyMvUniform.setMatrix(skyMv);
                        
                        foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess skyTimeUniform = skyboxShader.getUniform("Time");
                        if (skyTimeUniform != null) skyTimeUniform.setFloat((System.currentTimeMillis() % 100000L) / 1000.0f);
                        foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess skySunScreenUniform = skyboxShader.getUniform("SunScreenPos");
                        if (skySunScreenUniform != null) skySunScreenUniform.setVector(sunScreenX, sunScreenY, sunVisible);
                        foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess skyCameraForwardUniform = skyboxShader.getUniform("CameraForwardDirection");
                        if (skyCameraForwardUniform != null) skyCameraForwardUniform.setVector(cameraForwardDirection.x, cameraForwardDirection.y, cameraForwardDirection.z);
                        foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess skyScreenUniform = skyboxShader.getUniform("ScreenSize");
                        if (skyScreenUniform != null) skyScreenUniform.setVector((float) mc.getWindow().getWidth(), (float) mc.getWindow().getHeight(), 1.0f);
                        foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess skySunDirUniform = skyboxShader.getUniform("SunDirection");
                        if (skySunDirUniform != null) skySunDirUniform.setVector(sunWorldDirection.x, sunWorldDirection.y, sunWorldDirection.z);
                        foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess skyLensFlareUniform = skyboxShader.getUniform("LensFlareStrength");
                        if (skyLensFlareUniform != null) skyLensFlareUniform.setFloat(sunBloomStrength);
                        foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess skyGlobalLightUniform = skyboxShader.getUniform("GlobalLightStrength");
                        if (skyGlobalLightUniform != null) skyGlobalLightUniform.setFloat(sunGlobalLightingStrength);
                        foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess skyPlanetDirUniform = skyboxShader.getUniform("PlanetAtmosphereDirection");
                        if (skyPlanetDirUniform != null) skyPlanetDirUniform.setVector(planetAtmosphereDirection.x, planetAtmosphereDirection.y, planetAtmosphereDirection.z);
                        foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess skyPlanetColorUniform = skyboxShader.getUniform("PlanetAtmosphereColor");
                        if (skyPlanetColorUniform != null) skyPlanetColorUniform.setVector(planetAtmosphereR, planetAtmosphereG, planetAtmosphereB);
                        foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess skyPlanetSurfaceRadiusUniform = skyboxShader.getUniform("PlanetSurfaceAngularRadius");
                        if (skyPlanetSurfaceRadiusUniform != null) skyPlanetSurfaceRadiusUniform.setFloat(planetSurfaceAngularRadius);
                        foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess skyPlanetRadiusUniform = skyboxShader.getUniform("PlanetAtmosphereAngularRadius");
                        if (skyPlanetRadiusUniform != null) skyPlanetRadiusUniform.setFloat(planetAtmosphereAngularRadius);
                        foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess skyPlanetIntensityUniform = skyboxShader.getUniform("PlanetAtmosphereIntensity");
                        if (skyPlanetIntensityUniform != null) skyPlanetIntensityUniform.setFloat(planetAtmosphereIntensity);
                        foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess skyPlanetNotLookingUniform = skyboxShader.getUniform("PlanetAtmosphereNotLookingIntensity");
                        if (skyPlanetNotLookingUniform != null) skyPlanetNotLookingUniform.setFloat(planetAtmosphereNotLookingIntensity);
                        foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess skyPlanetLookingEdgeUniform = skyboxShader.getUniform("PlanetAtmosphereLookingEdgeIntensity");
                        if (skyPlanetLookingEdgeUniform != null) skyPlanetLookingEdgeUniform.setFloat(planetAtmosphereLookingEdgeIntensity);
                        foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess skyPlanetMorphRadiusUniform = skyboxShader.getUniform("PlanetAtmosphereMorphRadius");
                        if (skyPlanetMorphRadiusUniform != null) skyPlanetMorphRadiusUniform.setFloat(planetAtmosphereMorphRadius);

                        com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
                        com.mojang.blaze3d.systems.RenderSystem.depthMask(false);

                        ms.pushPose();
                        // Render a large sphere to cover everything
                        foundry.veil.api.client.render.vertex.VertexArray skyboxVbo = PlanetManager.getLODForDistance(0, 100);
                        skyboxVbo.bind();
                        skyboxVbo.draw();
                        foundry.veil.api.client.render.vertex.VertexArray.unbind();
                        ms.popPose();

                        com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
                        com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
                        
                        foundry.veil.api.client.render.shader.program.ShaderProgram.unbind();
                        shader.bind(); // Rebind planet shader
                    }

                    foundry.veil.api.client.render.shader.program.ShaderProgram sunShader = foundry.veil.api.client.render.VeilRenderSystem.setShader(Spacemod.SUN_SHADER);
                    if (sunShader != null) {
                        sunShader.bind();
                        foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess sunProjUniform = sunShader.getUniform("ProjMat");
                        if (sunProjUniform != null) sunProjUniform.setMatrix(event.getProjectionMatrix());

                        org.joml.Matrix4f sunMv = new org.joml.Matrix4f();
                        sunMv.rotation(camRot);
                        sunMv.translate((float) -cam.getPosition().x, (float) -cam.getPosition().y, (float) -cam.getPosition().z);
                        sunMv.scale(SUN_RADIUS_BLOCKS, SUN_RADIUS_BLOCKS, SUN_RADIUS_BLOCKS);

                        foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess sunMvUniform = sunShader.getUniform("ModelViewMat");
                        if (sunMvUniform != null) sunMvUniform.setMatrix(sunMv);
                        foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess sunTimeUniform = sunShader.getUniform("Time");
                        if (sunTimeUniform != null) sunTimeUniform.setFloat((System.currentTimeMillis() % 100000L) / 1000.0f);

                        float sunDistance = (float) Math.sqrt(
                            cam.getPosition().x * cam.getPosition().x +
                            cam.getPosition().y * cam.getPosition().y +
                            cam.getPosition().z * cam.getPosition().z
                        );
                        foundry.veil.api.client.render.vertex.VertexArray sunVertexArray = PlanetManager.getLODForDistance(sunDistance, SUN_RADIUS_BLOCKS);
                        sunVertexArray.bind();
                        sunVertexArray.draw();
                        foundry.veil.api.client.render.vertex.VertexArray.unbind();

                        foundry.veil.api.client.render.shader.program.ShaderProgram.unbind();
                        shader.bind();
                    }
                }

                for (Planet p : planets) {
                    ms.pushPose();
                    
                    org.joml.Matrix4f mv = new org.joml.Matrix4f();
                    mv.rotation(camRot);
                    
                    float dx = p.x - (float) cam.getPosition().x;
                    float dy = p.y - (float) cam.getPosition().y;
                    float dz = p.z - (float) cam.getPosition().z;
                    mv.translate(dx, dy, dz);
                    mv.scale(p.radius, p.radius, p.radius);

                    shader.bind();

                    foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess mvUniform = shader.getUniform("ModelViewMat");
                    if (mvUniform != null) mvUniform.setMatrix(mv);

                    foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess colorUniform = shader.getUniform("PlanetColor");
                    if (colorUniform != null) colorUniform.setVector(p.colorR, p.colorG, p.colorB);

                    foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess sunPosUniform = shader.getUniform("SunViewPos");
                    if (sunPosUniform != null) sunPosUniform.setVector(sunView.x, sunView.y, sunView.z);
                    foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess planetSunLightUniform = shader.getUniform("SunLightStrength");
                    if (planetSunLightUniform != null) planetSunLightUniform.setFloat(sunGlobalLightingStrength);

                    float distance = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
                    foundry.veil.api.client.render.vertex.VertexArray vertexArray = PlanetManager.getLODForDistance(distance, p.radius);
                    
                    vertexArray.bind();
                    vertexArray.draw();
                    foundry.veil.api.client.render.vertex.VertexArray.unbind();

                    ms.popPose();
                }

                com.mojang.blaze3d.systems.RenderSystem.enableCull();
                com.mojang.blaze3d.systems.RenderSystem.disableBlend();
                foundry.veil.api.client.render.shader.program.ShaderProgram.unbind();
            }
        }
    }
}
