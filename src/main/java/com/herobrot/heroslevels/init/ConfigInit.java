package com.herobrot.heroslevels.init;

import com.herobrot.heroslevels.config.HerosLevelConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public class ConfigInit {
    public static HerosLevelConfig CONFIG = new HerosLevelConfig();

    public static void init() {
        AutoConfig.register(HerosLevelConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(HerosLevelConfig.class).getConfig();
    }
}