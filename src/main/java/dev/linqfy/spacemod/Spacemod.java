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
                            
                            float[] pos = {(float) Config.planetX, (float) Config.planetY, (float) Config.planetZ};
                            if ((Boolean) imGuiClass.getMethod("dragFloat3", String.class, float[].class).invoke(null, "Position", pos)) {
                                Config.planetX = pos[0];
                                Config.planetY = pos[1];
                                Config.planetZ = pos[2];
                            }
                            
                            float[] radius = {(float) Config.planetRadius};
                            if ((Boolean) imGuiClass.getMethod("dragFloat", String.class, float[].class).invoke(null, "Radius", radius)) {
                                Config.planetRadius = radius[0];
                            }
                            
                            float[] color = {(float) Config.planetColorR, (float) Config.planetColorG, (float) Config.planetColorB};
                            if ((Boolean) imGuiClass.getMethod("colorEdit3", String.class, float[].class).invoke(null, "Color", color)) {
                                Config.planetColorR = color[0];
                                Config.planetColorG = color[1];
                                Config.planetColorB = color[2];
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
            if (event.getStage() == net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
                var ms = event.getPoseStack();
                var cam = event.getCamera();

                foundry.veil.api.client.render.shader.program.ShaderProgram shader = foundry.veil.api.client.render.VeilRenderSystem.setShader(Spacemod.PLANET_SHADER);
                if (shader == null) {
                    return;
                }

                foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess colorUniform = shader.getUniform("PlanetColor");
                if (colorUniform != null) colorUniform.setVector((float) Config.planetColorR, (float) Config.planetColorG, (float) Config.planetColorB);

                ms.pushPose();
                ms.translate(Config.planetX - cam.getPosition().x, Config.planetY - cam.getPosition().y, Config.planetZ - cam.getPosition().z);

                float r = (float) Config.planetRadius;

                com.mojang.blaze3d.vertex.Tesselator tesselator = com.mojang.blaze3d.vertex.Tesselator.getInstance();
                com.mojang.blaze3d.vertex.BufferBuilder buffer = tesselator.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX);

                org.joml.Matrix4f matrix = ms.last().pose();

                // Build a UV sphere mesh
                int rings = 32;
                int sectors = 32;
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
                        
                        buffer.addVertex(matrix, x00, y0, z00).setUv(u0, v0);
                        buffer.addVertex(matrix, x10, y1, z10).setUv(u0, v1);
                        buffer.addVertex(matrix, x11, y1, z11).setUv(u1, v1);
                        buffer.addVertex(matrix, x01, y0, z01).setUv(u1, v0);
                    }
                }

                // Enable depth mask so it properly intersects with the 3D world
                com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
                com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
                
                com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();

                com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(buffer.buildOrThrow());

                com.mojang.blaze3d.systems.RenderSystem.disableBlend();

                ms.popPose();
            }
        }

    }
}
