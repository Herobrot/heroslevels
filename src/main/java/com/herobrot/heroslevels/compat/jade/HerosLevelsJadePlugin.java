package com.herobrot.heroslevels.compat.jade;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class HerosLevelsJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new BlockComponentProvider(), Block.class);
        registration.registerEntityComponent(new EntityComponentProvider(), Entity.class);
    }
}