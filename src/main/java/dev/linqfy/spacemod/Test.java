package dev.linqfy.spacemod;

import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;

public class Test {
    public void test(RegisterDimensionSpecialEffectsEvent event) {
        event.register(ResourceLocation.fromNamespaceAndPath("spacemod", "space"), new DimensionSpecialEffects(Float.NaN, false, DimensionSpecialEffects.SkyType.NONE, false, false) {
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
