package com.herobrot.heroslevels;

import com.herobrot.heroslevels.config.HerosLevelConfig;
import com.herobrot.heroslevels.init.RenderInit;
import me.shedaniel.autoconfig.AutoConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = HerosLevels.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = HerosLevels.MOD_ID, value = Dist.CLIENT)
public class HerosLevelsClient {

    public HerosLevelsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, parentScreen) ->
                AutoConfig.getConfigScreen(HerosLevelConfig.class, parentScreen).get());
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            RenderInit.registerTabs();
            RenderInit.registerConfigSync();
        });
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        RenderInit.registerRenderers(event);
    }
}