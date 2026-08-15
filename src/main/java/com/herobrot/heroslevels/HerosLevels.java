package com.herobrot.heroslevels;

import com.herobrot.heroslevels.compat.CompatManager;
import com.herobrot.heroslevels.data.RestrictionLoader;
import com.herobrot.heroslevels.data.SkillLoader;
import com.herobrot.heroslevels.init.*;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

@Mod(HerosLevels.MOD_ID)
public class HerosLevels {

    public static final String MOD_ID = "heroslevels";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static boolean isEasyMagicLoaded = false;
    public static boolean isEasyAnvilsLoaded = false;
    public static boolean isJadeLoaded = false;

    public HerosLevels(IEventBus modEventBus) {
        ConfigInit.init();
        verifyingModsInstalled();
        CompatManager.registerEvents();

        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);

        AttachmentInit.ATTACHMENTS.register(modEventBus);
        CriteriaInit.CRITERIA.register(modEventBus);
        EntityInit.ENTITIES.register(modEventBus);

        NetworkInit.init();
    }

    private void onAddReloadListeners(final AddReloadListenerEvent event) {
        event.addListener(new SkillLoader());
        event.addListener(new RestrictionLoader(event.getRegistryAccess()));
    }

    private void verifyingModsInstalled() {
        isJadeLoaded = isModLoaded("jade");
        isEasyMagicLoaded = isModLoaded("easymagic");
        isEasyAnvilsLoaded = isModLoaded("easyanvils");
    }

    public static boolean isModLoaded(String modTarget) { return ModList.get().isLoaded(modTarget); }
}