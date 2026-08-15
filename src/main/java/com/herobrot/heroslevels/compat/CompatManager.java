package com.herobrot.heroslevels.compat;

import com.herobrot.heroslevels.compat.accessories.AccessoriesCompatEvents;
import com.herobrot.heroslevels.compat.curios.CuriosCompatEvents;
import io.wispforest.accessories.api.events.CanEquipCallback;
import net.neoforged.neoforge.common.NeoForge;

import static com.herobrot.heroslevels.HerosLevels.isModLoaded;

public class CompatManager {

    public static void registerEvents() {
        if (isModLoaded("curios"))
            NeoForge.EVENT_BUS.register(CuriosCompatEvents.class);
        if (isModLoaded("accessories"))
            CanEquipCallback.EVENT.register(new AccessoriesCompatEvents());
    }
}