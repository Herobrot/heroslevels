package com.herobrot.heroslevels.init;

import com.herobrot.heroslevels.HerosLevels;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = HerosLevels.MOD_ID, value = Dist.CLIENT)
public class KeyInit {

    public static final KeyMapping SCREEN_KEY = new KeyMapping(
            "key.heroslevels.openskillscreen",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "category.heroslevels.keybind"
    );

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(SCREEN_KEY);
    }
}