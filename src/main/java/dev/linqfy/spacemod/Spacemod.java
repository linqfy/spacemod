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

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }

    public static final net.minecraft.resources.ResourceLocation PLANET_SHADER = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MODID, "planet");

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

                com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
                com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
                com.mojang.blaze3d.systems.RenderSystem.disableCull();
                com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();

                java.util.List<Planet> planets = PlanetManager.getPlanets();
                for (Planet p : planets) {
                    ms.pushPose();
                    
                    // Minecraft's event.getPoseStack() typically contains the camera rotation.
                    // If it does, ms.translate() applies in view space, which is wrong for world objects!
                    // To fix this, we build the ModelView matrix from scratch:
                    // 1. Start with identity.
                    // 2. Apply camera rotation inverse (World to View).
                    // 3. Translate by world position (Local to World).
                    // 4. Scale.
                    org.joml.Matrix4f mv = new org.joml.Matrix4f();
                    
                    // The camera's left/right/up vectors are defined by its rotation.
                    // To go from World to View, we rotate by the inverse of the camera's orientation.
                    org.joml.Quaternionf camRot = new org.joml.Quaternionf(cam.rotation());
                    camRot.conjugate(); // Inverse of a normalized quaternion is its conjugate
                    mv.rotation(camRot);
                    
                    // Now translate in world space relative to the camera position
                    float dx = p.x - (float) cam.getPosition().x;
                    float dy = p.y - (float) cam.getPosition().y;
                    float dz = p.z - (float) cam.getPosition().z;
                    mv.translate(dx, dy, dz);
                    mv.scale(p.radius, p.radius, p.radius);

                    foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess mvUniform = shader.getUniform("ModelViewMat");
                    if (mvUniform != null) mvUniform.setMatrix(mv);

                    foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess colorUniform = shader.getUniform("PlanetColor");
                    if (colorUniform != null) colorUniform.setVector(p.colorR, p.colorG, p.colorB);

                    foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess sunPosUniform = shader.getUniform("SunViewPos");
                    if (sunPosUniform != null) {
                        org.joml.Matrix4f viewMat = new org.joml.Matrix4f().rotation(camRot);
                        org.joml.Vector4f sunPos = new org.joml.Vector4f((float) -cam.getPosition().x, (float) -cam.getPosition().y, (float) -cam.getPosition().z, 1.0f);
                        viewMat.transform(sunPos);
                        sunPosUniform.setVector(sunPos.x, sunPos.y, sunPos.z);
                    }

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
