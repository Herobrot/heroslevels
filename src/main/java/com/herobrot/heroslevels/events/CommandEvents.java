package com.herobrot.heroslevels.events;

import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.command.LevelCommand;
import com.herobrot.heroslevels.command.RestrictCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = HerosLevels.MOD_ID)
public class CommandEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        RestrictCommand.register(event.getDispatcher());
        LevelCommand.register(event.getDispatcher());
    }
}