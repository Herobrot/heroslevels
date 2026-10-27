package com.herobrot.heroslevels.compat;

import com.herobrot.heroslevels.compat.accessories.AccessoriesCompatEvents;
import io.wispforest.accessories.api.events.CanEquipCallback;

import static com.herobrot.heroslevels.HerosLevels.isModLoaded;

public class CompatManager {

    public static void registerEvents() {
        if (isModLoaded("accessories")) CanEquipCallback.EVENT.register(new AccessoriesCompatEvents());
    }
}