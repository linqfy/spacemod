package dev.linqfy.spacemod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = Spacemod.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER.comment("Whether to log the dirt block on common setup").define("logDirtBlock", true);

    private static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER.comment("A magic number").defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER.comment("What you want the introduction message to be for the magic number").define("magicNumberIntroduction", "The magic number is... ");

    public static final ModConfigSpec.DoubleValue PLANET_X = BUILDER.comment("The X position of the planet").defineInRange("planetX", 0.0, -100000.0, 100000.0);
    public static final ModConfigSpec.DoubleValue PLANET_Y = BUILDER.comment("The Y position of the planet").defineInRange("planetY", 100.0, -100000.0, 100000.0);
    public static final ModConfigSpec.DoubleValue PLANET_Z = BUILDER.comment("The Z position of the planet").defineInRange("planetZ", 0.0, -100000.0, 100000.0);
    public static final ModConfigSpec.DoubleValue PLANET_RADIUS = BUILDER.comment("The radius of the planet").defineInRange("planetRadius", 20.0, 0.1, 10000.0);
    public static final ModConfigSpec.DoubleValue PLANET_COLOR_R = BUILDER.comment("The red component of the planet color").defineInRange("planetColorR", 0.2, 0.0, 1.0);
    public static final ModConfigSpec.DoubleValue PLANET_COLOR_G = BUILDER.comment("The green component of the planet color").defineInRange("planetColorG", 0.6, 0.0, 1.0);
    public static final ModConfigSpec.DoubleValue PLANET_COLOR_B = BUILDER.comment("The blue component of the planet color").defineInRange("planetColorB", 0.8, 0.0, 1.0);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER.comment("A list of items to log on common setup.").defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static Set<Item> items;

    public static double planetX;
    public static double planetY;
    public static double planetZ;
    public static double planetRadius;
    public static double planetColorR;
    public static double planetColorG;
    public static double planetColorB;

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

        planetX = PLANET_X.get();
        planetY = PLANET_Y.get();
        planetZ = PLANET_Z.get();
        planetRadius = PLANET_RADIUS.get();
        planetColorR = PLANET_COLOR_R.get();
        planetColorG = PLANET_COLOR_G.get();
        planetColorB = PLANET_COLOR_B.get();

        items = ITEM_STRINGS.get().stream().map(itemName -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName))).collect(Collectors.toSet());
    }
}
